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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayerFactory;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.core.sync.packets.ItemTransitionEffectPacket;
import appeng.hooks.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class AnnihilationPlanePart extends BasicStatePart implements IGridTickable, IWorldCallable<TickRateModulation> {

    public static final ResourceLocation TAG_BLACKLIST = new ResourceLocation(AppEng.MOD_ID,
            "blacklisted/annihilation_plane");

    private static final ITag.INamedTag<Block> BLOCK_BLACKLIST = BlockTags.makeWrapperTag(TAG_BLACKLIST.toString());

    private static final ITag.INamedTag<Item> ITEM_BLACKLIST = ItemTags.makeWrapperTag(TAG_BLACKLIST.toString());

    private static final PlaneModels MODELS = new PlaneModels("part/annihilation_plane", "part/annihilation_plane_on");

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
    }

    @Override
    public TickRateModulation call(final World world) {
        this.breaking = false;
        return this.breakBlock(true);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {

        // For collision, we're using a simplified bounding box
        if (bch.isBBCollision()) {
            // The smaller collision hitbox here is needed to allow for the entity collision
            // event
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
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            this.refresh();
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public void onEntityCollision(final Entity entity) {
        if (this.isAccepting && entity instanceof ItemEntity && entity.isAlive() && Platform.isServer()
                && this.getProxy().isActive()) {

            ItemEntity itemEntity = (ItemEntity) entity;
            if (isItemBlacklisted(itemEntity.getItem().getItem())) {
                return;
            }

            boolean capture = false;
            final BlockPos pos = this.getTile().getPos();

            // This is the middle point of the entities BB, which is better suited for
            // comparisons that don't rely on it
            // "touching" the plane
            double posYMiddle = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D;

            switch (this.getSide()) {
                case DOWN:
                case UP:
                    if (entity.getPosX() > pos.getX() && entity.getPosX() < pos.getX() + 1) {
                        if (entity.getPosZ() > pos.getZ() && entity.getPosZ() < pos.getZ() + 1) {
                            if ((entity.getPosY() > pos.getY() + 0.9 && this.getSide() == AEPartLocation.UP)
                                    || (entity.getPosY() < pos.getY() + 0.1 && this.getSide() == AEPartLocation.DOWN)) {
                                capture = true;
                            }
                        }
                    }
                    break;
                case SOUTH:
                case NORTH:
                    if (entity.getPosX() > pos.getX() && entity.getPosX() < pos.getX() + 1) {
                        if (posYMiddle > pos.getY() && posYMiddle < pos.getY() + 1) {
                            if ((entity.getPosZ() > pos.getZ() + 0.9 && this.getSide() == AEPartLocation.SOUTH)
                                    || (entity.getPosZ() < pos.getZ() + 0.1
                                            && this.getSide() == AEPartLocation.NORTH)) {
                                capture = true;
                            }
                        }
                    }
                    break;
                case EAST:
                case WEST:
                    if (entity.getPosZ() > pos.getZ() && entity.getPosZ() < pos.getZ() + 1) {
                        if (posYMiddle > pos.getY() && posYMiddle < pos.getY() + 1) {
                            if ((entity.getPosX() > pos.getX() + 0.9 && this.getSide() == AEPartLocation.EAST)
                                    || (entity.getPosX() < pos.getX() + 0.1 && this.getSide() == AEPartLocation.WEST)) {
                                capture = true;
                            }
                        }
                    }
                    break;
                default:
                    // umm?
                    break;
            }

            if (capture) {
                final boolean changed = this.storeEntityItem(itemEntity);

                if (changed) {
                    AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64,
                            this.getTile().getWorld(), new ItemTransitionEffectPacket(entity.getPosX(),
                                    entity.getPosY(), entity.getPosZ(), this.getSide().getOpposite()));
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
            final IAEItemStack overflow = this.storeItemStack(entityItem.getItem());

            return this.handleOverflow(entityItem, overflow);
        }

        return false;
    }

    /**
     * Stores an {@link ItemStack} inside the network.
     *
     * @param item {@link ItemStack} to store
     *
     * @return the leftover items, which could not be stored inside the network
     */
    private IAEItemStack storeItemStack(final ItemStack item) {
        final IAEItemStack itemToStore = AEItemStack.fromItemStack(item);
        try {
            final IStorageGrid storage = this.getProxy().getStorage();
            final IEnergyGrid energy = this.getProxy().getEnergy();
            final IAEItemStack overflow = Platform.poweredInsert(energy,
                    storage.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class)),
                    itemToStore, this.mySrc);

            this.isAccepting = overflow == null;

            return overflow;
        } catch (final GridAccessException e1) {
            // :P
        }

        return null;
    }

    /**
     * Handles a possible overflow or none at all. It will update the entity to match the leftover stack size as well as
     * mark it as dead without any leftover amount.
     *
     * @param entityItem the entity to update or destroy
     * @param overflow   the leftover {@link IAEItemStack}
     *
     * @return true, if the entity was changed otherwise false.
     */
    private boolean handleOverflow(final ItemEntity entityItem, final IAEItemStack overflow) {
        if (overflow == null || overflow.getStackSize() == 0) {
            entityItem.remove();
            return true;
        }

        final int oldStackSize = entityItem.getItem().getCount();
        final int newStackSize = (int) overflow.getStackSize();
        final boolean changed = oldStackSize != newStackSize;

        entityItem.getItem().setCount(newStackSize);

        return changed;
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged c) {
        this.refresh();
        this.getHost().markForUpdate();
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.refresh();
        this.getHost().markForUpdate();
    }

    private TickRateModulation breakBlock(final boolean modulate) {
        if (this.isAccepting && this.getProxy().isActive()) {
            try {
                final TileEntity te = this.getTile();
                final ServerWorld w = (ServerWorld) te.getWorld();

                final BlockPos pos = te.getPos().offset(this.getSide().getFacing());
                final IEnergyGrid energy = this.getProxy().getEnergy();

                final BlockState blockState = w.getBlockState(pos);
                if (this.canHandleBlock(w, pos, blockState)) {
                    // Query the loot-table and get a potential outcome of the loot-table evaluation
                    final List<ItemStack> items = this.obtainBlockDrops(w, pos);
                    final float requiredPower = this.calculateEnergyUsage(w, pos, items);

                    final boolean hasPower = energy.extractAEPower(requiredPower, Actionable.SIMULATE,
                            PowerMultiplier.CONFIG) > requiredPower - 0.1;
                    final boolean canStore = this.canStoreItemStacks(items);

                    if (hasPower && canStore) {
                        if (modulate) {
                            performBreakBlock(w, pos, blockState, energy, requiredPower, items);
                        } else {
                            this.breaking = true;
                            TickHandler.instance().addCallable(this.getTile().getWorld(), this);
                        }
                        return TickRateModulation.URGENT;
                    }
                }
            } catch (final GridAccessException e1) {
                // :P
            }
        }

        // nothing to do here :)
        return TickRateModulation.IDLE;
    }

    private void performBreakBlock(ServerWorld w, BlockPos pos, BlockState blockState, IEnergyGrid energy,
            float requiredPower, List<ItemStack> items) {

        if (!this.breakBlockAndStoreExtraItems(w, pos)) {
            // We failed to actually replace the block with air or it already was the case
            return;
        }

        for (ItemStack item : items) {
            IAEItemStack overflow = storeItemStack(item);
            // If inserting the item fully was not possible, drop it as an item entity
            // instead
            // if the storage clears up, we'll pick it up that way
            if (overflow != null) {
                Platform.spawnDrops(w, pos, Collections.singletonList(overflow.createItemStack()));
            }
        }

        energy.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);

        AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, w,
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
    private boolean canHandleBlock(final ServerWorld w, final BlockPos pos, final BlockState state) {
        if (state.isAir(w, pos)) {
            return false;
        }

        if (isBlockBlacklisted(state.getBlock())) {
            return false;
        }

        final Material material = state.getMaterial();
        final float hardness = state.getBlockHardness(w, pos);
        final boolean ignoreMaterials = material == Material.AIR || material == Material.LAVA
                || material == Material.WATER || material.isLiquid();

        return !ignoreMaterials && hardness >= 0f && w.isBlockLoaded(pos)
                && w.isBlockModifiable(Platform.getPlayer(w), pos);
    }

    protected List<ItemStack> obtainBlockDrops(final ServerWorld w, final BlockPos pos) {

        Entity fakePlayer = FakePlayerFactory.getMinecraft(w);

        final BlockState state = w.getBlockState(pos);

        ItemStack harvestTool = createHarvestTool(state);

        if (harvestTool == null) {
            if (!state.getRequiresTool()) {
                harvestTool = ItemStack.EMPTY;
            } else {
                // In case the block does NOT allow us to harvest it without a tool, or the
                // proper tool, do not return anything.
                return Collections.emptyList();
            }
        }

        TileEntity te = w.getTileEntity(pos);
        return Block.getDrops(state, w, pos, te, fakePlayer, harvestTool);
    }

    /**
     * Checks if this plane can handle the block at the specific coordinates.
     */
    protected float calculateEnergyUsage(final ServerWorld w, final BlockPos pos, final List<ItemStack> items) {
        final BlockState state = w.getBlockState(pos);
        final float hardness = state.getBlockHardness(w, pos);

        float requiredEnergy = 1 + hardness;
        for (final ItemStack is : items) {
            requiredEnergy += is.getCount();
        }

        return requiredEnergy;
    }

    /**
     * Checks if the network can store the possible drops.
     *
     * It also sets isAccepting to false, if the item can not be stored.
     *
     * @param itemStacks an array of {@link ItemStack} to test
     *
     * @return true, if the network can store at least a single item of all drops or no drops are reported
     */
    private boolean canStoreItemStacks(final List<ItemStack> itemStacks) {
        boolean canStore = itemStacks.isEmpty();

        try {
            final IStorageGrid storage = this.getProxy().getStorage();

            for (final ItemStack itemStack : itemStacks) {
                final IAEItemStack itemToTest = AEItemStack.fromItemStack(itemStack);
                final IAEItemStack overflow = storage
                        .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class))
                        .injectItems(itemToTest, Actionable.SIMULATE, this.mySrc);
                if (overflow == null || itemToTest.getStackSize() > overflow.getStackSize()) {
                    canStore = true;
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        this.isAccepting = canStore;
        return canStore;
    }

    private boolean breakBlockAndStoreExtraItems(final ServerWorld w, final BlockPos pos) {
        // Kill the block, but signal no drops
        if (!w.destroyBlock(pos, false)) {
            // The block was no longer there
            return false;
        }

        // This handles items that do not spawn via loot-tables but rather normal block
        // breaking
        // i.e. our cable-buses do this (bad practice, really)
        final AxisAlignedBB box = new AxisAlignedBB(pos).grow(0.2);
        for (final Object ei : w.getEntitiesWithinAABB(ItemEntity.class, box)) {
            if (ei instanceof ItemEntity) {
                final ItemEntity entityItem = (ItemEntity) ei;
                this.storeEntityItem(entityItem);
            }
        }
        return true;
    }

    private void refresh() {
        this.isAccepting = true;

        getTile().requestModelDataUpdate();

        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new PlaneModelData(getConnections());
    }

    /**
     * Creates the fake (and temporary) tool that will be used to calculate the loot tables of a block this plane wants
     * to break.
     *
     * @param state The state of the block about to be broken.
     */
    protected ItemStack createHarvestTool(BlockState state) {
        // Try to use the right tool...
        ToolType harvestToolType = state.getBlock().getHarvestTool(state);
        if (harvestToolType == ToolType.AXE) {
            return new ItemStack(Items.DIAMOND_AXE, 1);
        } else if (harvestToolType == ToolType.SHOVEL) {
            return new ItemStack(Items.DIAMOND_SHOVEL, 1);
        } else if (harvestToolType == ToolType.PICKAXE) {
            return new ItemStack(Items.DIAMOND_PICKAXE, 1);
        } else {
            return null;
        }
    }

    public static boolean isBlockBlacklisted(Block b) {
        return BLOCK_BLACKLIST.contains(b);
    }

    public static boolean isItemBlacklisted(Item i) {
        return ITEM_BLACKLIST.contains(i);
    }

}
