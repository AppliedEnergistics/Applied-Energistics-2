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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkStarItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.WallOrFloorItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.IPlantable;
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
import appeng.api.parts.IPartItem;
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
            if (placeBlock == YesNo.YES && (i instanceof BlockItem || i instanceof IPlantable
                    || i instanceof FireworkStarItem || i instanceof FireworkRocketItem || i instanceof IPartItem)) {
                final PlayerEntity player = Platform.getPlayer((ServerWorld) w);
                Platform.configurePlayer(player, side, this.getTile());
                Hand hand = player.getActiveHand();
                player.setHeldItem(hand, is);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    // The side the plane is attached to will be considered the look direction
                    // in terms of placing an item
                    Direction lookDirection = side.getFacing();

                    // FIXME No idea what any of this is _supposed_ to do, comment badly needed
                    if (i instanceof IPlantable || i instanceof WallOrFloorItem) {
                        boolean Worked = false;

                        // Up or Down, Attempt 1??
                        if (side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i.onItemUse(new DirectionalPlaceContext(w, placePos.offset(side.getFacing()),
                                    lookDirection, is, side.getFacing())) == ActionResultType.SUCCESS;
                        }

                        // Up or Down, Attempt 2??
                        if (!Worked && side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i.onItemUse(new DirectionalPlaceContext(w,
                                    placePos.offset(side.getFacing().getOpposite()), lookDirection, is,
                                    side.getFacing().getOpposite())) == ActionResultType.SUCCESS;
                        }

                        // Horizontal, attempt 1??
                        if (!Worked && side.yOffset == 0) {
                            Worked = i.onItemUse(new DirectionalPlaceContext(w, placePos.offset(Direction.DOWN),
                                    lookDirection, is, Direction.DOWN)) == ActionResultType.SUCCESS;
                        }

                        if (!Worked) {
                            i.onItemUse(new DirectionalPlaceContext(w, placePos, lookDirection, is,
                                    lookDirection.getOpposite()));
                        }

                        maxStorage -= is.getCount();
                    } else {
                        i.onItemUse(new DirectionalPlaceContext(w, placePos, lookDirection, is,
                                lookDirection.getOpposite()));
                        maxStorage -= is.getCount();
                    }
                } else {
                    maxStorage = 1;
                }

                // Safe keeping
                player.setHeldItem(hand, ItemStack.EMPTY);
            } else {
                worked = true;

                final int sum = this.countEntitesAround(w, placePos);

                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    if (type == Actionable.MODULATE) {
                        is.setCount((int) maxStorage);
                        final double x = (side.xOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.xOffset
                                + .5 + te.getPos().getX();
                        final double y = (side.yOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.yOffset
                                + .5 + te.getPos().getY();
                        final double z = (side.zOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.zOffset
                                + .5 + te.getPos().getZ();

                        final ItemEntity ei = new ItemEntity(w, x, y, z, is.copy());

                        Entity result = ei;

                        ei.setMotion(side.xOffset * 0.2, side.yOffset * 0.2, side.zOffset * 0.2);

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
                            worked = false;
                        }
                    }
                } else {
                    worked = false;
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

    private class ForcedItemUseContext extends ItemUseContext {
        protected ForcedItemUseContext(World worldIn, @Nullable PlayerEntity player, Hand handIn, ItemStack heldItem,
                BlockRayTraceResult rayTraceResultIn) {
            super(worldIn, player, handIn, heldItem, rayTraceResultIn);
        }
    }

}
