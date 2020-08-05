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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.core.AEConfig;
import appeng.core.Api;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;

public class FormationPlanePart extends AbstractFormationPlanePart<IAEItemStack> {

    private static final PlaneModels MODELS = new PlaneModels("part/formation_plane", "part/formation_plane_on");
    private static final Random RANDOM_OFFSET = new Random();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEItemStack> myHandler = new MEInventoryHandler<>(this,
            Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
    private final AppEngInternalAEInventory Config = new AppEngInternalAEInventory(this, 63);

    public FormationPlanePart(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(
                this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IItemList<IAEItemStack> priorityList = Api.instance().storage()
                .getStorageChannel(IItemStorageChannel.class).createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.Config.getSlots() && x < slotsToUse; x++) {
            final IAEItemStack is = this.Config.getAEStackInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }

        if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.myHandler.setPartitionList(new FuzzyPriorityList<IAEItemStack>(priorityList,
                    (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
        } else {
            this.myHandler.setPartitionList(new PrecisePriorityList<IAEItemStack>(priorityList));
        }

        try {
            this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.Config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final CompoundNBT data) {
        super.readFromNBT(data);
        this.Config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundNBT data) {
        super.writeToNBT(data);
        this.Config.writeToNBT(data, "config");
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.Config;
        }

        return super.getInventoryByName(name);
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.stateChanged();
    }

    @MENetworkEventSubscribe
    public void updateChannels(final MENetworkChannelsChanged changedChannels) {
        this.stateChanged();
    }

    @Override
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vector3d pos) {
        if (Platform.isServer()) {
            ContainerOpener.openContainer(FormationPlaneContainer.TYPE, player, ContainerLocator.forPart(this));
        }
        return true;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (this.getProxy().isActive()
                && channel == Api.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
            final List<IMEInventoryHandler> handler = new ArrayList<>(1);
            handler.add(this.myHandler);
            return handler;
        }
        return Collections.emptyList();
    }

    @Override
    public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
        if (this.blocked || input == null || input.getStackSize() <= 0) {
            return input;
        }

        final YesNo placeBlock = (YesNo) this.getConfigManager().getSetting(Settings.PLACE_BLOCK);

        final ItemStack is = input.createItemStack();
        final Item i = is.getItem();

        long maxStorage = Math.min(input.getStackSize(), is.getMaxStackSize());
        boolean worked = false;

        final TileEntity te = this.getHost().getTile();
        final World w = te.getWorld();
        final AEPartLocation side = this.getSide();

        final BlockPos placePos = te.getPos().offset(side.getFacing());

        if (w.getBlockState(placePos).getMaterial().isReplaceable()) {
            if (placeBlock == YesNo.YES) {
                final PlayerEntity player = Platform.getPlayer((ServerWorld) w);
                Platform.configurePlayer(player, side, this.getTile());
                // Seems to work without...
                // Hand hand = player.getActiveHand();
                // player.setHeldItem(hand, is);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    // The side the plane is attached to will be considered the look direction
                    // in terms of placing an item
                    Direction lookDirection = side.getFacing();
                    PlaneDirectionalPlaceContext context = new PlaneDirectionalPlaceContext(w, player, placePos,
                            lookDirection, is, lookDirection.getOpposite());

                    i.onItemUse(context);
                    maxStorage -= is.getCount();

                } else {
                    maxStorage = 1;
                }

                // Seems to work without... Safe keeping
                // player.setHeldItem(hand, ItemStack.EMPTY);
            } else {
                final int sum = this.countEntitesAround(w, placePos);

                // Disable spawning once there is a certain amount of entities in an area.
                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    worked = true;

                    if (type == Actionable.MODULATE) {
                        is.setCount((int) maxStorage);
                        if (!spawnItemEntity(w, te, side, is)) {
                            // revert in case something prevents spawning.
                            worked = false;
                        }

                    }
                }
            }
        }

        this.blocked = !w.getBlockState(placePos).getMaterial().isReplaceable();

        if (worked) {
            final IAEItemStack out = input.copy();
            out.decStackSize(maxStorage);
            if (out.getStackSize() == 0) {
                return null;
            }
            return out;
        }

        return input;
    }

    private static boolean spawnItemEntity(World w, TileEntity te, AEPartLocation side, ItemStack is) {
        // the item offset based on the entity height plus some offset
        final double itemOffset = .55 + EntityType.ITEM.getHeight();

        // The center of the block the plane is located in
        final double centerX = te.getPos().getX() + .5;
        final double centerY = te.getPos().getY() + .5;
        final double centerZ = te.getPos().getZ() + .5;

        // When spawning downwards, we have to take the item height of 0.25 into account
        // Otherwise it will get stuck and be spit out in a random direction as
        // minecraft spawns it at its feet position and not center
        final double additionalYOffset = side.yOffset == -1 ? -.3 : 0;

        // Calculate the offsets to spawn it into the adjacent block, taking the sign
        // into account.
        // Spawn it 0.8 blocks away from the center pos when facing in this direction
        // Every other direction will select a position in a .5 block area around the
        // block center.
        final double offsetX = (side.xOffset == 0) ? ((RANDOM_OFFSET.nextFloat() / 2) - .25)
                : (side.xOffset * itemOffset);
        final double offsetY = (side.yOffset == 0) ? ((RANDOM_OFFSET.nextFloat() / 2) - .25)
                : ((side.yOffset * itemOffset) + additionalYOffset);
        final double offsetZ = (side.zOffset == 0) ? ((RANDOM_OFFSET.nextFloat() / 2) - .25)
                : (side.zOffset * itemOffset);

        final double absoluteX = centerX + offsetX;
        final double absoluteY = centerY + offsetY;
        final double absoluteZ = centerZ + offsetZ;

        final ItemEntity ei = new ItemEntity(w, absoluteX, absoluteY, absoluteZ, is.copy());
        Entity result = ei;

        ei.setMotion(side.xOffset * .1, side.yOffset * 0.1, side.zOffset * 0.1);

        if (is.getItem().hasCustomEntity(is)) {
            result = is.getItem().createEntity(w, ei, is);
            if (result != null) {
                ei.remove();
            } else {
                result = ei;
            }
        }

        if (!w.addEntity(result)) {
            result.remove();
            return false;
        }
        return true;
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IItemStorageChannel.class);
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

    @Override
    public ItemStack getItemStackRepresentation() {
        return Api.instance().definitions().parts().formationPlane().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ContainerType<?> getContainerType() {
        return FormationPlaneContainer.TYPE;
    }

    private int countEntitesAround(World world, BlockPos pos) {
        final AxisAlignedBB t = new AxisAlignedBB(pos).grow(8);
        final List<Entity> list = world.getEntitiesWithinAABB(Entity.class, t);

        return list.size();
    }

    /**
     * A custom {@link DirectionalPlaceContext} which also accepts a player needed
     * various blocks like seeds.
     * <p>
     * Also removed {@link DirectionalPlaceContext#replacingClickedOnBlock} as this
     * can cause a {@link StackOverflowError} for certain replaceable blocks.
     */
    private static class PlaneDirectionalPlaceContext extends BlockItemUseContext {
        private final Direction lookDirection;

        public PlaneDirectionalPlaceContext(World world, PlayerEntity player, BlockPos pos, Direction lookDirection,
                ItemStack itemStack, Direction facing) {
            super(world, player, Hand.MAIN_HAND, itemStack,
                    new BlockRayTraceResult(Vector3d.copyCenteredHorizontally(pos), facing, pos, false));
            this.lookDirection = lookDirection;
        }

        @Override
        public BlockPos getPos() {
            return this.rayTraceResult.getPos();
        }

        @Override
        public boolean canPlace() {
            return this.world.getBlockState(this.rayTraceResult.getPos()).isReplaceable(this);
        }

        @Override
        public Direction getNearestLookingDirection() {
            return Direction.DOWN;
        }

        @Override
        public Direction[] getNearestLookingDirections() {
            switch (this.lookDirection) {
                case DOWN:
                default:
                    return new Direction[] { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH,
                            Direction.WEST, Direction.UP };
                case UP:
                    return new Direction[] { Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST,
                            Direction.SOUTH, Direction.WEST };
                case NORTH:
                    return new Direction[] { Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.WEST,
                            Direction.UP, Direction.SOUTH };
                case SOUTH:
                    return new Direction[] { Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.WEST,
                            Direction.UP, Direction.NORTH };
                case WEST:
                    return new Direction[] { Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.UP,
                            Direction.NORTH, Direction.EAST };
                case EAST:
                    return new Direction[] { Direction.DOWN, Direction.EAST, Direction.SOUTH, Direction.UP,
                            Direction.NORTH, Direction.WEST };
            }
        }

        @Override
        public Direction getPlacementHorizontalFacing() {
            return this.lookDirection.getAxis() == Axis.Y ? Direction.NORTH : this.lookDirection;
        }

        @Override
        public boolean func_225518_g_() {
            return false;
        }

        @Override
        public float getPlacementYaw() {
            return (float) (this.lookDirection.getHorizontalIndex() * 90);
        }
    }
}
