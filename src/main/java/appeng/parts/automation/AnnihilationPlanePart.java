/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.automation;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEItemKey;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.core.sync.packets.ItemTransitionEffectPacket;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.util.Platform;

public class AnnihilationPlanePart extends BasicStatePart implements IGridTickable {

    public static final ResourceLocation TAG_BLACKLIST = new ResourceLocation(AppEng.MOD_ID,
            "blacklisted/item_annihilation_plane");

    private static final Tag<Block> BLOCK_BLACKLIST = TagRegistry.block(TAG_BLACKLIST);

    private static final Tag<Item> ITEM_BLACKLIST = TagRegistry.item(TAG_BLACKLIST);

    private static final PlaneModels MODELS = new PlaneModels("part/item_annihilation_plane",
            "part/item_annihilation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final IActionSource mySrc = new MachineSource(this);
    private boolean isAccepting = true;
    private boolean breaking = false;

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    public AnnihilationPlanePart(final ItemStack is) {
        super(is);
        getMainNode()
                .addService(IGridTickable.class, this);
    }

    private void finishBreakBlock() {
        this.breaking = false;
        this.breakBlock(true);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {

        // For collision, we're using a simplified bounding box
        if (bch.isBBCollision()) {
            // The smaller collision hitbox here is needed to allow for the entity collision event
            bch.addBox(0, 0, 14, 16, 16, 15.5);
            return;
        }

        connectionHelper.getBoxes(bch);

    }

    /**
     * @return An object describing which adjacent planes this plane connects to visually.
     */
    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            this.refresh();
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public void onEntityCollision(final Entity entity) {
        if (this.isAccepting && entity instanceof ItemEntity itemEntity && entity.isAlive() && !isClientSide()
                && this.getMainNode().isActive()) {

            if (isItemBlacklisted(itemEntity.getItem().getItem())) {
                return;
            }

            var pos = this.getBlockEntity().getBlockPos();
            var planePosX = pos.getX();
            var planePosY = pos.getY();
            var planePosZ = pos.getZ();

            // This is the middle point of the entities BB, which is better suited for comparisons
            // that don't rely on it "touching" the plane
            var posYMiddle = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;
            var entityPosX = entity.getX();
            var entityPosY = entity.getY();
            var entityPosZ = entity.getZ();

            var captureX = entityPosX > planePosX && entityPosX < planePosX + 1;
            var captureY = posYMiddle > planePosY && posYMiddle < planePosY + 1;
            var captureZ = entityPosZ > planePosZ && entityPosZ < planePosZ + 1;

            var capture = false;

            switch (this.getSide()) {
                case DOWN:
                    capture = captureX && captureZ && entityPosY < planePosY + 0.1;
                    break;
                case UP:
                    capture = captureX && captureZ && entityPosY > planePosY + 0.9;
                    break;
                case SOUTH:
                    capture = captureX && captureY && entityPosZ > planePosZ + 0.9;
                    break;
                case NORTH:
                    capture = captureX && captureY && entityPosZ < planePosZ + 0.1;
                    break;
                case EAST:
                    capture = captureZ && captureY && entityPosX > planePosX + 0.9;
                    break;
                case WEST:
                    capture = captureZ && captureY && entityPosX < planePosX + 0.1;
                    break;
                default:
                    // umm?
                    break;
            }

            if (capture) {
                var changed = this.storeEntityItem(itemEntity);

                if (changed) {
                    AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64,
                            this.getBlockEntity().getLevel(), new ItemTransitionEffectPacket(entity.getX(),
                                    entity.getY(), entity.getZ(), this.getSide().getOpposite()));
                }
            }
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    /**
     * Stores an {@link ItemEntity} inside the network and either marks it as dead or sets it to the leftover stackSize.
     *
     * @param entityItem {@link ItemEntity} to store
     */
    private boolean storeEntityItem(final ItemEntity entityItem) {
        if (entityItem.isAlive()) {
            var inserted = this.storeItemStack(entityItem.getItem());

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
    private int storeItemStack(ItemStack item) {
        var grid = getMainNode().getGrid();
        if (grid == null || item.isEmpty()) {
            return 0;
        }

        var what = AEItemKey.of(item);
        var amount = item.getCount();
        var storage = grid.getStorageService();
        var energy = grid.getEnergyService();
        var inserted = (int) StorageHelper.poweredInsert(energy,
                storage.getInventory(),
                what, amount, this.mySrc);

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
    private boolean handleOverflow(final ItemEntity entityItem, int inserted) {
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

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.refresh();
    }

    private TickRateModulation breakBlock(final boolean modulate) {
        if (this.isAccepting && this.getMainNode().isActive()) {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                var te = this.getBlockEntity();
                var level = (ServerLevel) te.getLevel();

                var pos = te.getBlockPos().relative(this.getSide());
                var energy = grid.getEnergyService();

                var blockState = level.getBlockState(pos);
                if (this.canHandleBlock(level, pos, blockState)) {
                    // Query the loot-table and get a potential outcome of the loot-table evaluation
                    var items = this.obtainBlockDrops(level, pos);
                    var requiredPower = this.calculateEnergyUsage(level, pos, items);

                    var hasPower = energy.extractAEPower(requiredPower, Actionable.SIMULATE,
                            PowerMultiplier.CONFIG) > requiredPower - 0.1;
                    var canStore = this.canStoreItemStacks(items);

                    if (hasPower && canStore) {
                        if (modulate) {
                            performBreakBlock(level, pos, blockState, energy, requiredPower, items);
                        } else {
                            this.breaking = true;
                            TickHandler.instance().addCallable(this.getBlockEntity().getLevel(),
                                    this::finishBreakBlock);
                        }
                        return TickRateModulation.URGENT;
                    }
                }
            }
        }

        // nothing to do here :)
        return TickRateModulation.IDLE;
    }

    private void performBreakBlock(ServerLevel level, BlockPos pos, BlockState blockState, IEnergyService energy,
            float requiredPower, List<ItemStack> items) {

        if (!this.breakBlockAndStoreExtraItems(level, pos)) {
            // We failed to actually replace the block with air or it already was the case
            return;
        }

        for (var item : items) {
            var inserted = storeItemStack(item);
            // If inserting the item fully was not possible, drop it as an item entity instead if the storage clears up,
            // we'll pick it up that way
            if (inserted < item.getCount()) {
                item.shrink(inserted);
                Platform.spawnDrops(level, pos, Collections.singletonList(item));
            }
        }

        energy.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);

        AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, level,
                new BlockTransitionEffectPacket(pos, blockState, this.getSide().getOpposite(),
                        BlockTransitionEffectPacket.SoundMode.NONE));
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.breaking) {
            return TickRateModulation.URGENT;
        }

        this.isAccepting = true;
        return this.breakBlock(false);
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
    protected float calculateEnergyUsage(final ServerLevel level, final BlockPos pos, final List<ItemStack> items) {
        var state = level.getBlockState(pos);
        var hardness = state.getDestroySpeed(level, pos);

        var requiredEnergy = 1 + hardness;
        for (var is : items) {
            requiredEnergy += is.getCount();
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
    private boolean canStoreItemStacks(final List<ItemStack> itemStacks) {
        var canStore = itemStacks.isEmpty();

        var grid = getMainNode().getGrid();
        if (grid != null) {
            var storage = grid.getStorageService();

            for (var itemStack : itemStacks) {
                var itemToTest = AEItemKey.of(itemStack);
                var inserted = storage
                        .insert(itemToTest, itemStack.getCount(), Actionable.SIMULATE, this.mySrc);
                if (inserted > 0) {
                    canStore = true;
                }
            }
        }

        this.isAccepting = canStore;
        return canStore;
    }

    private boolean breakBlockAndStoreExtraItems(final ServerLevel level, final BlockPos pos) {
        // Kill the block, but signal no drops
        if (!level.destroyBlock(pos, false)) {
            // The block was no longer there
            return false;
        }

        // This handles items that do not spawn via loot-tables but rather normal block breaking i.e. our cable-buses do
        // this (bad practice, really)
        var box = new AABB(pos).inflate(0.2);
        for (final Object ei : level.getEntitiesOfClass(ItemEntity.class, box)) {
            if (ei instanceof ItemEntity entityItem) {
                this.storeEntityItem(entityItem);
            }
        }
        return true;
    }

    private void refresh() {
        this.isAccepting = true;

        getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice(n));
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public Object getRenderAttachmentData() {
        return getConnections();
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
    protected HarvestTool createHarvestTool(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return new HarvestTool(new ItemStack(Items.DIAMOND_PICKAXE, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return new HarvestTool(new ItemStack(Items.DIAMOND_AXE, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return new HarvestTool(new ItemStack(Items.DIAMOND_SHOVEL, 1), false);
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return new HarvestTool(new ItemStack(Items.DIAMOND_HOE, 1), false);
        } else {
            // Use a pickaxe for everything else. Mostly to allow silk touch enchants
            return new HarvestTool(new ItemStack(Items.DIAMOND_PICKAXE, 1), true);
        }
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
