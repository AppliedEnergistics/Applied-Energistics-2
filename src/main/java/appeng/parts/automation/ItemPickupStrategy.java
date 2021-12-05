package appeng.parts.automation;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEItemKey;
import appeng.core.AppEng;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.core.sync.packets.ItemTransitionEffectPacket;
import appeng.util.Platform;

public class ItemPickupStrategy implements PickupStrategy {

    private static final float SILK_TOUCH_FACTOR = 16;

    public static final ResourceLocation TAG_BLACKLIST = new ResourceLocation(AppEng.MOD_ID,
            "blacklisted/annihilation_plane");

    private static final Tag<Block> BLOCK_BLACKLIST = BlockTags.createOptional(TAG_BLACKLIST);

    private static final Tag<Item> ITEM_BLACKLIST = ItemTags.createOptional(TAG_BLACKLIST);

    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    private final BlockEntity host;
    private final boolean allowSilkTouch;

    private boolean isAccepting = true;

    public ItemPickupStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host,
            boolean allowSilkTouch) {
        this.level = level;
        this.pos = pos;
        this.side = side;
        this.host = host;
        this.allowSilkTouch = allowSilkTouch;
    }

    @Override
    public void reset() {
        this.isAccepting = true;
    }

    public boolean canPickUpEntity(Entity entity) {
        return entity instanceof ItemEntity;
    }

    public boolean pickUpEntity(IEnergySource energySource, PickupSink sink, Entity entity) {
        if (!this.isAccepting || !(entity instanceof ItemEntity itemEntity)) {
            return false;
        }

        if (isItemBlacklisted(itemEntity.getItem().getItem())) {
            return false;
        }

        var changed = this.storeEntityItem(sink, itemEntity);

        if (changed) {
            AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64,
                    level, new ItemTransitionEffectPacket(entity.getX(),
                            entity.getY(), entity.getZ(), side));
        }

        return true;
    }

    @Override
    public Result tryStartPickup(IEnergySource energySource, PickupSink sink) {
        if (this.isAccepting) {
            var blockState = level.getBlockState(pos);
            if (this.canHandleBlock(level, pos, blockState)) {
                // Query the loot-table and get a potential outcome of the loot-table evaluation
                var items = this.obtainBlockDrops(level, pos);
                var requiredPower = this.calculateEnergyUsage(level, pos, items);

                var hasPower = energySource.extractAEPower(requiredPower, Actionable.SIMULATE,
                        PowerMultiplier.CONFIG) > requiredPower - 0.1;
                var canStore = this.canStoreItemStacks(sink, items);

                if (hasPower && canStore) {
                    return Result.PICKED_UP;
                } else {
                    return Result.CANT_STORE;
                }
            }
        }

        return Result.CANT_PICKUP;
    }

    @Override
    public void completePickup(IEnergySource energySource, PickupSink sink) {

        var blockState = level.getBlockState(pos);
        if (!this.canHandleBlock(level, pos, blockState)) {
            return;
        }

        var items = this.obtainBlockDrops(level, pos);
        var requiredPower = this.calculateEnergyUsage(level, pos, items);

        if (!this.breakBlockAndStoreExtraItems(sink, level, pos)) {
            // We failed to actually replace the block with air, or it already was the case
            return;
        }

        for (var item : items) {
            var inserted = storeItemStack(sink, item);
            // If inserting the item fully was not possible, drop it as an item entity instead if the storage clears up,
            // we'll pick it up that way
            if (inserted < item.getCount()) {
                item.shrink(inserted);
                Platform.spawnDrops(level, pos, Collections.singletonList(item));
            }
        }

        energySource.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);

        AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, level,
                new BlockTransitionEffectPacket(pos, blockState, side, BlockTransitionEffectPacket.SoundMode.NONE));
    }

    /**
     * Stores an {@link ItemEntity} inside the network and either marks it as dead or sets it to the leftover stackSize.
     *
     * @param entityItem {@link ItemEntity} to store
     */
    private boolean storeEntityItem(PickupSink sink, ItemEntity entityItem) {
        if (entityItem.isAlive()) {
            var inserted = this.storeItemStack(sink, entityItem.getItem());

            return this.handleOverflow(entityItem, inserted);
        }

        return false;
    }

    /**
     * Stores an {@link ItemStack} inside the network.
     *
     * @param item {@link ItemStack} to store
     * @return count inserted
     */
    private int storeItemStack(PickupSink sink, ItemStack item) {
        if (item.isEmpty()) {
            return 0;
        }

        var what = AEItemKey.of(item);
        var amount = item.getCount();
        var inserted = (int) sink.insert(what, amount, Actionable.MODULATE);

        this.isAccepting = inserted >= amount;

        return inserted;
    }

    /**
     * Handles a possible overflow or none at all. It will update the entity to match the leftover stack size as well as
     * mark it as dead without any leftover amount.
     *
     * @param entityItem the entity to update or destroy
     * @param inserted   amount inserted
     * @return true, if the entity was changed otherwise false.
     */
    private boolean handleOverflow(ItemEntity entityItem, int inserted) {
        int entityItemCount = entityItem.getItem().getCount();
        if (inserted >= entityItemCount) {
            entityItem.discard();
            return true;
        }

        var newStackSize = entityItemCount - inserted;
        var changed = entityItemCount != newStackSize;

        entityItem.getItem().setCount(newStackSize);

        return changed;
    }

    /**
     * Checks if this plane can handle the block at the specific coordinates.
     */
    private boolean canHandleBlock(final ServerLevel level, final BlockPos pos, final BlockState state) {
        if (state.isAir()) {
            return false;
        }

        if (isBlockBlacklisted(state.getBlock())) {
            return false;
        }

        var material = state.getMaterial();
        // Note: bedrock, portals, and other unbreakable blocks have a hardness < 0, hence the >= 0 check below.
        var hardness = state.getDestroySpeed(level, pos);
        var ignoreAirAndFluids = material == Material.AIR || material.isLiquid();

        return !ignoreAirAndFluids && hardness >= 0f && level.hasChunkAt(pos)
                && level.mayInteract(Platform.getPlayer(level), pos);
    }

    protected List<ItemStack> obtainBlockDrops(final ServerLevel level, final BlockPos pos) {
        var fakePlayer = Platform.getPlayer(level);
        var state = level.getBlockState(pos);
        var blockEntity = level.getBlockEntity(pos);

        var harvestTool = createHarvestTool(state);
        var harvestToolItem = harvestTool.item();

        if (!state.requiresCorrectToolForDrops() && harvestTool.fallback()) {
            // Do not use a tool when not required, no hints about it and not enchanted in cases like silk touch.
            harvestToolItem = ItemStack.EMPTY;
        }

        return Block.getDrops(state, level, pos, blockEntity, fakePlayer, harvestToolItem);
    }

    /**
     * Checks if this plane can handle the block at the specific coordinates.
     */
    protected float calculateEnergyUsage(ServerLevel level, BlockPos pos, List<ItemStack> items) {
        var state = level.getBlockState(pos);
        var hardness = state.getDestroySpeed(level, pos);

        var requiredEnergy = 1 + hardness;
        for (var is : items) {
            requiredEnergy += is.getCount();
        }

        if (allowSilkTouch) {
            requiredEnergy *= SILK_TOUCH_FACTOR;
        }

        return requiredEnergy;
    }

    /**
     * Checks if the network can store the possible drops.
     * <p>
     * It also sets isAccepting to false, if the item can not be stored.
     *
     * @param itemStacks an array of {@link ItemStack} to test
     * @return true, if the network can store at least a single item of all drops or no drops are reported
     */
    private boolean canStoreItemStacks(PickupSink sink, List<ItemStack> itemStacks) {
        var canStore = itemStacks.isEmpty();

        for (var itemStack : itemStacks) {
            var itemToTest = AEItemKey.of(itemStack);
            var inserted = sink.insert(itemToTest, itemStack.getCount(), Actionable.SIMULATE);
            if (inserted > 0) {
                canStore = true;
            }
        }

        this.isAccepting = canStore;
        return canStore;
    }

    private boolean breakBlockAndStoreExtraItems(PickupSink sink, ServerLevel level, BlockPos pos) {
        // Kill the block, but signal no drops
        if (!level.destroyBlock(pos, false)) {
            // The block was no longer there
            return false;
        }

        // This handles items that do not spawn via loot-tables but rather normal block breaking i.e. our cable-buses do
        // this (bad practice, really)
        var box = new AABB(pos).inflate(0.2);
        for (var itemEntity : level.getEntitiesOfClass(ItemEntity.class, box)) {
            this.storeEntityItem(sink, itemEntity);
        }
        return true;
    }

    /**
     * Creates the fake (and temporary) tool based on the provided hints in case a loot table relies on it.
     * <p>
     * Generally could use a stick as tool or anything else which can be enchanted. {@link ItemStack#EMPTY} is not an
     * option as at least anything having a fortune effect need something enchantable even without the enchantment,
     * otherwise it will not drop anything.
     *
     * @param state The block state of the block about to be broken.
     */
    private HarvestTool createHarvestTool(BlockState state) {
        HarvestTool tool;

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            tool = new HarvestTool(new ItemStack(Items.DIAMOND_PICKAXE, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            tool = new HarvestTool(new ItemStack(Items.DIAMOND_AXE, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            tool = new HarvestTool(new ItemStack(Items.DIAMOND_SHOVEL, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            tool = new HarvestTool(new ItemStack(Items.DIAMOND_HOE, 1), false);
        } else {
            // Use a pickaxe for everything else. Mostly to allow silk touch enchants
            tool = new HarvestTool(new ItemStack(Items.DIAMOND_PICKAXE, 1), true);
        }

        if (allowSilkTouch) {
            // For silk touch purposes, enchant the fake tool
            var item = tool.item().copy();
            EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SILK_TOUCH, 1), item);

            // Set fallback to false, to ensure it'll be used even if not strictly required
            tool = new HarvestTool(item, false);
        }

        return tool;
    }

    public static boolean isBlockBlacklisted(Block b) {
        return BLOCK_BLACKLIST.contains(b);
    }

    public static boolean isItemBlacklisted(Item i) {
        return ITEM_BLACKLIST.contains(i);
    }

    record HarvestTool(ItemStack item, boolean fallback) {
    }

}
