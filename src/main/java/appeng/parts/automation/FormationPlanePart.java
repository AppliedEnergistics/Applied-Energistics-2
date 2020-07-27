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

import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.config.*;
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
import appeng.util.FakePlayer;
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
        for (int x = 0; x < this.Config.getSlotCount() && x < slotsToUse; x++) {
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
    public void onChangeInventory(final FixedItemInv inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.Config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.Config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        this.Config.writeToNBT(data, "config");
    }

    @Override
    public FixedItemInv getInventoryByName(final String name) {
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
    public boolean onPartActivate(final PlayerEntity player, final Hand hand, final Vec3d pos) {
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

        long maxStorage = Math.min(input.getStackSize(), is.getMaxCount());
        boolean worked = false;

        final BlockEntity te = this.getHost().getTile();
        final World w = te.getWorld();
        final AEPartLocation side = this.getSide();

        final BlockPos placePos = te.getPos().offset(side.getFacing());

        if (w.getBlockState(placePos).getMaterial().isReplaceable()) {
            if (placeBlock == YesNo.YES && (i instanceof BlockItem || i instanceof FireworkChargeItem
                    || i instanceof FireworkItem || i instanceof IPartItem)) {
                final PlayerEntity player = FakePlayer.getOrCreate((ServerWorld) w);
                Platform.configurePlayer(player, side, this.getTile());
                Hand hand = player.getActiveHand();
                player.setStackInHand(hand, is);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    // The side the plane is attached to will be considered the look direction
                    // in terms of placing an item
                    Direction lookDirection = side.getFacing();

                    // FIXME No idea what any of this is _supposed_ to do, comment badly needed
                    if (i instanceof WallStandingBlockItem) {
                        boolean Worked = false;

                        // Up or Down, Attempt 1??
                        if (side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i
                                    .useOnBlock(new AutomaticItemPlacementContext(w, placePos.offset(side.getFacing()),
                                            lookDirection, is, side.getFacing())) == ActionResult.SUCCESS;
                        }

                        // Up or Down, Attempt 2??
                        if (!Worked && side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i.useOnBlock(new AutomaticItemPlacementContext(w,
                                    placePos.offset(side.getFacing().getOpposite()), lookDirection, is,
                                    side.getFacing().getOpposite())) == ActionResult.SUCCESS;
                        }

                        // Horizontal, attempt 1??
                        if (!Worked && side.yOffset == 0) {
                            Worked = i.useOnBlock(new AutomaticItemPlacementContext(w, placePos.offset(Direction.DOWN),
                                    lookDirection, is, Direction.DOWN)) == ActionResult.SUCCESS;
                        }

                        if (!Worked) {
                            i.useOnBlock(new AutomaticItemPlacementContext(w, placePos, lookDirection, is,
                                    lookDirection.getOpposite()));
                        }

                        maxStorage -= is.getCount();
                    } else {
                        i.useOnBlock(new AutomaticItemPlacementContext(w, placePos, lookDirection, is,
                                lookDirection.getOpposite()));
                        maxStorage -= is.getCount();
                    }
                } else {
                    maxStorage = 1;
                }

                // Safe keeping
                player.setStackInHand(hand, ItemStack.EMPTY);
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

                        ei.setVelocity(side.xOffset * 0.2, side.yOffset * 0.2, side.zOffset * 0.2);

                        // FIXME FABRIC No custom item entity support
                        // FIXME FABRIC if (is.getItem().hasCustomEntity(is)) {
                        // FIXME FABRIC result = is.getItem().createEntity(w, ei, is);
                        // FIXME FABRIC if (result != null) {
                        // FIXME FABRIC ei.remove();
                        // FIXME FABRIC } else {
                        // FIXME FABRIC result = ei;
                        // FIXME FABRIC }
                        // FIXME FABRIC }

                        if (!w.spawnEntity(result)) {
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

    @Override
    public Object getModelData() {
        return getConnections();
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return Api.instance().definitions().parts().formationPlane().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public ScreenHandlerType<?> getContainerType() {
        return FormationPlaneContainer.TYPE;
    }

    private int countEntitesAround(World world, BlockPos pos) {
        final Box t = new Box(pos).expand(8);
        final List<Entity> list = world.getEntities(Entity.class, t, null);

        return list.size();
    }

    private class ForcedItemUseContext extends ItemUsageContext {
        protected ForcedItemUseContext(World worldIn, @Nullable PlayerEntity player, Hand handIn, ItemStack heldItem,
                BlockHitResult rayTraceResultIn) {
            super(worldIn, player, handIn, heldItem, rayTraceResultIn);
        }
    }

}
