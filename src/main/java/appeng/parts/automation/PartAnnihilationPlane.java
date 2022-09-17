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


import appeng.api.AEApi;
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
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.PacketTransitionEffect;
import appeng.hooks.TickHandler;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartBasicState;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.google.common.collect.Lists;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;


public class PartAnnihilationPlane extends PartBasicState implements IGridTickable, IWorldCallable<TickRateModulation> {

    private static final PlaneModels MODELS = new PlaneModels("part/annihilation_plane_", "part/annihilation_plane_on_");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final IActionSource mySrc = new MachineSource(this);
    private boolean isAccepting = true;
    private boolean breaking = false;

    public PartAnnihilationPlane(final ItemStack is) {
        super(is);
    }

    @Override
    public TickRateModulation call(final World world) throws Exception {
        this.breaking = false;
        return this.breakBlock(true);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        int minX = 1;
        int minY = 1;
        int maxX = 15;
        int maxY = 15;

        final IPartHost host = this.getHost();
        if (host != null) {
            final TileEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            final EnumFacing e = bch.getWorldX();
            final EnumFacing u = bch.getWorldY();

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e.getOpposite())), this.getSide())) {
                minX = 0;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e)), this.getSide())) {
                maxX = 16;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(u.getOpposite())), this.getSide())) {
                minY = 0;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(e)), this.getSide())) {
                maxY = 16;
            }
        }

        bch.addBox(5, 5, 14, 11, 11, 15);
        // The smaller collision hitbox here is needed to allow for the entity collision event
        bch.addBox(minX, minY, 15, maxX, maxY, bch.isBBCollision() ? 15 : 16);
    }

    /**
     * @return An object describing which adjacent planes this plane connects to visually.
     */
    public PlaneConnections getConnections() {

        final EnumFacing facingRight, facingUp;
        AEPartLocation location = this.getSide();
        switch (location) {
            case UP:
                facingRight = EnumFacing.EAST;
                facingUp = EnumFacing.NORTH;
                break;
            case DOWN:
                facingRight = EnumFacing.WEST;
                facingUp = EnumFacing.NORTH;
                break;
            case NORTH:
                facingRight = EnumFacing.WEST;
                facingUp = EnumFacing.UP;
                break;
            case SOUTH:
                facingRight = EnumFacing.EAST;
                facingUp = EnumFacing.UP;
                break;
            case WEST:
                facingRight = EnumFacing.SOUTH;
                facingUp = EnumFacing.UP;
                break;
            case EAST:
                facingRight = EnumFacing.NORTH;
                facingUp = EnumFacing.UP;
                break;
            default:
            case INTERNAL:
                return PlaneConnections.of(false, false, false, false);
        }

        boolean left = false, right = false, down = false, up = false;

        final IPartHost host = this.getHost();
        if (host != null) {
            final TileEntity te = host.getTile();

            final BlockPos pos = te.getPos();

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingRight.getOpposite())), this.getSide())) {
                left = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingRight)), this.getSide())) {
                right = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingUp.getOpposite())), this.getSide())) {
                down = true;
            }

            if (this.isAnnihilationPlane(te.getWorld().getTileEntity(pos.offset(facingUp)), this.getSide())) {
                up = true;
            }
        }

        return PlaneConnections.of(up, right, down, left);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {
            this.refresh();
        }
    }

    @Override
    public void onEntityCollision(final Entity entity) {
        if (this.isAccepting && entity instanceof EntityItem && !entity.isDead && Platform.isServer() && this.getProxy().isActive()) {
            boolean capture = false;
            final BlockPos pos = this.getTile().getPos();

            // This is the middle point of the entities BB, which is better suited for comparisons that don't rely on it
            // "touching" the plane
            double posYMiddle = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0D;

            switch (this.getSide()) {
                case DOWN:
                case UP:
                    if (entity.posX > pos.getX() && entity.posX < pos.getX() + 1) {
                        if (entity.posZ > pos.getZ() && entity.posZ < pos.getZ() + 1) {
                            if ((entity.posY > pos.getY() + 0.9 && this.getSide() == AEPartLocation.UP) || (entity.posY < pos.getY() + 0.1 && this
                                    .getSide() == AEPartLocation.DOWN)) {
                                capture = true;
                            }
                        }
                    }
                    break;
                case SOUTH:
                case NORTH:
                    if (entity.posX > pos.getX() && entity.posX < pos.getX() + 1) {
                        if (posYMiddle > pos.getY() && posYMiddle < pos.getY() + 1) {
                            if ((entity.posZ > pos.getZ() + 0.9 && this.getSide() == AEPartLocation.SOUTH) || (entity.posZ < pos.getZ() + 0.1 && this
                                    .getSide() == AEPartLocation.NORTH)) {
                                capture = true;
                            }
                        }
                    }
                    break;
                case EAST:
                case WEST:
                    if (entity.posZ > pos.getZ() && entity.posZ < pos.getZ() + 1) {
                        if (posYMiddle > pos.getY() && posYMiddle < pos.getY() + 1) {
                            if ((entity.posX > pos.getX() + 0.9 && this.getSide() == AEPartLocation.EAST) || (entity.posX < pos.getX() + 0.1 && this
                                    .getSide() == AEPartLocation.WEST)) {
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
                final boolean changed = this.storeEntityItem((EntityItem) entity);

                if (changed) {
                    AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, this.getTile().getWorld(),
                            new PacketTransitionEffect(entity.posX, entity.posY, entity.posZ, this.getSide(), false));
                }
            }
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    /**
     * Stores an {@link EntityItem} inside the network and either marks it as dead or sets it to the leftover stackSize.
     *
     * @param entityItem {@link EntityItem} to store
     */
    private boolean storeEntityItem(final EntityItem entityItem) {
        if (!entityItem.isDead) {
            final IAEItemStack overflow = this.storeItemStack(entityItem.getItem());

            return this.handleOverflow(entityItem, overflow);
        }

        return false;
    }

    /**
     * Stores an {@link ItemStack} inside the network.
     *
     * @param item {@link ItemStack} to store
     * @return the leftover items, which could not be stored inside the network
     */
    private IAEItemStack storeItemStack(final ItemStack item) {
        final IAEItemStack itemToStore = AEItemStack.fromItemStack(item);
        try {
            final IStorageGrid storage = this.getProxy().getStorage();
            final IEnergyGrid energy = this.getProxy().getEnergy();
            final IAEItemStack overflow = Platform.poweredInsert(energy,
                    storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)), itemToStore, this.mySrc);

            this.isAccepting = overflow == null;

            return overflow;
        } catch (final GridAccessException e1) {
            // :P
        }

        return null;
    }

    /**
     * Handles a possible overflow or none at all.
     * It will update the entity to match the leftover stack size as well as mark it as dead without any leftover
     * amount.
     *
     * @param entityItem the entity to update or destroy
     * @param overflow   the leftover {@link IAEItemStack}
     * @return true, if the entity was changed otherwise false.
     */
    private boolean handleOverflow(final EntityItem entityItem, final IAEItemStack overflow) {
        if (overflow == null || overflow.getStackSize() == 0) {
            entityItem.setDead();
            return true;
        }

        final int oldStackSize = entityItem.getItem().getCount();
        final int newStackSize = (int) overflow.getStackSize();
        final boolean changed = oldStackSize != newStackSize;

        entityItem.getItem().setCount(newStackSize);

        return changed;
    }

    protected boolean isAnnihilationPlane(final TileEntity blockTileEntity, final AEPartLocation side) {
        if (blockTileEntity instanceof IPartHost) {
            final IPart p = ((IPartHost) blockTileEntity).getPart(side);
            return p != null && p.getClass() == this.getClass();
        }
        return false;
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
                final WorldServer w = (WorldServer) te.getWorld();

                final BlockPos pos = te.getPos().offset(this.getSide().getFacing());
                final IEnergyGrid energy = this.getProxy().getEnergy();

                if (this.canHandleBlock(w, pos)) {
                    final List<ItemStack> items = this.obtainBlockDrops(w, pos);
                    final float requiredPower = this.calculateEnergyUsage(w, pos, items);

                    final boolean hasPower = energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) > requiredPower - 0.1;
                    final boolean canStore = this.canStoreItemStacks(items);

                    if (hasPower && canStore) {
                        if (modulate) {
                            energy.extractAEPower(requiredPower, Actionable.MODULATE, PowerMultiplier.CONFIG);
                            this.breakBlockAndStoreItems(w, pos);
                            AppEng.proxy.sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, w,
                                    new PacketTransitionEffect(pos.getX(), pos.getY(), pos.getZ(), this.getSide(), true));
                        } else {
                            this.breaking = true;
                            TickHandler.INSTANCE.addCallable(this.getTile().getWorld(), this);
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

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false, true);
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
    private boolean canHandleBlock(final WorldServer w, final BlockPos pos) {
        final IBlockState state = w.getBlockState(pos);
        final Material material = state.getMaterial();
        final float hardness = state.getBlockHardness(w, pos);
        final boolean ignoreMaterials = material == Material.AIR || material == Material.LAVA || material == Material.WATER || material.isLiquid();
        final boolean ignoreBlocks = state.getBlock() == Blocks.BEDROCK || state.getBlock() == Blocks.END_PORTAL || state
                .getBlock() == Blocks.END_PORTAL_FRAME || state.getBlock() == Blocks.COMMAND_BLOCK;

        return !ignoreMaterials && !ignoreBlocks && hardness >= 0f && !w.isAirBlock(pos) && w.isBlockLoaded(pos) && w.canMineBlockBody(
                Platform.getPlayer(w),
                pos);
    }

    protected List<ItemStack> obtainBlockDrops(final WorldServer w, final BlockPos pos) {
        final ItemStack[] out = Platform.getBlockDrops(w, pos);
        return Lists.newArrayList(out);
    }

    /**
     * Checks if this plane can handle the block at the specific coordinates.
     */
    protected float calculateEnergyUsage(final WorldServer w, final BlockPos pos, final List<ItemStack> items) {
        final IBlockState state = w.getBlockState(pos);
        final float hardness = state.getBlockHardness(w, pos);

        float requiredEnergy = 1 + hardness;
        for (final ItemStack is : items) {
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
        boolean canStore = itemStacks.isEmpty();

        try {
            final IStorageGrid storage = this.getProxy().getStorage();

            for (final ItemStack itemStack : itemStacks) {
                final IAEItemStack itemToTest = AEItemStack.fromItemStack(itemStack);
                final IAEItemStack overflow = storage.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class))
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

    private void breakBlockAndStoreItems(final WorldServer w, final BlockPos pos) {
        w.destroyBlock(pos, true);

        final AxisAlignedBB box = new AxisAlignedBB(pos).grow(0.2);
        for (final Object ei : w.getEntitiesWithinAABB(EntityItem.class, box)) {
            if (ei instanceof EntityItem) {
                final EntityItem entityItem = (EntityItem) ei;
                this.storeEntityItem(entityItem);
            }
        }
    }

    private void refresh() {
        this.isAccepting = true;

        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.getConnections(), this.isPowered(), this.isActive());
    }

}
