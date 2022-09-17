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
import appeng.core.AEConfig;
import appeng.core.sync.GuiBridge;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.storage.MEInventoryHandler;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PartFormationPlane extends PartAbstractFormationPlane<IAEItemStack> {

    private static final PlaneModels MODELS = new PlaneModels("part/formation_plane_", "part/formation_plane_on_");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private final MEInventoryHandler<IAEItemStack> myHandler = new MEInventoryHandler<>(this, AEApi.instance()
            .storage()
            .getStorageChannel(IItemStorageChannel.class));
    private final AppEngInternalAEInventory Config = new AppEngInternalAEInventory(this, 63);

    public PartFormationPlane(final ItemStack is) {
        super(is);

        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.PLACE_BLOCK, YesNo.YES);
        this.updateHandler();
    }

    @Override
    protected void updateHandler() {
        this.myHandler.setBaseAccess(AccessRestriction.WRITE);
        this.myHandler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
        this.myHandler.setPriority(this.getPriority());

        final IItemList<IAEItemStack> priorityList = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createList();

        final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
        for (int x = 0; x < this.Config.getSlots() && x < slotsToUse; x++) {
            final IAEItemStack is = this.Config.getAEStackInSlot(x);
            if (is != null) {
                priorityList.add(is);
            }
        }

        if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.myHandler.setPartitionList(
                    new FuzzyPriorityList<IAEItemStack>(priorityList, (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
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
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack) {
        super.onChangeInventory(inv, slot, mc, removedStack, newStack);

        if (inv == this.Config) {
            this.updateHandler();
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.Config.readFromNBT(data, "config");
        this.updateHandler();
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
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

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged changedChannels) {
        this.stateChanged();
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            Platform.openGUI(player, this.getHost().getTile(), this.getSide(), GuiBridge.GUI_FORMATION_PLANE);
        }
        return true;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)) {
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

        final BlockPos tePos = te.getPos().offset(side.getFacing());

        if (w.getBlockState(tePos).getBlock().isReplaceable(w, tePos)) {
            if (placeBlock == YesNo.YES && (i instanceof ItemBlock || i instanceof ItemBlockSpecial || i instanceof IPlantable || i instanceof ItemSkull || i instanceof ItemFirework || i instanceof IPartItem || i == Item
                    .getItemFromBlock(Blocks.REEDS))) {
                final EntityPlayer player = Platform.getPlayer((WorldServer) w);
                Platform.configurePlayer(player, side, this.getTile());
                EnumHand hand = player.getActiveHand();
                player.setHeldItem(hand, is);

                maxStorage = is.getCount();
                worked = true;
                if (type == Actionable.MODULATE) {
                    if (i instanceof IPlantable || i instanceof ItemSkull || i == Item.getItemFromBlock(Blocks.REEDS)) {
                        boolean Worked = false;

                        if (side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i.onItemUse(player, w, tePos.offset(side.getFacing()), hand, side.getFacing().getOpposite(), side.xOffset,
                                    side.yOffset, side.zOffset) == EnumActionResult.SUCCESS;
                        }

                        if (!Worked && side.xOffset == 0 && side.zOffset == 0) {
                            Worked = i.onItemUse(player, w, tePos.offset(side.getFacing().getOpposite()), hand, side.getFacing(), side.xOffset,
                                    side.yOffset, side.zOffset) == EnumActionResult.SUCCESS;
                        }

                        if (!Worked && side.yOffset == 0) {
                            Worked = i.onItemUse(player, w, tePos.offset(EnumFacing.DOWN), hand, EnumFacing.UP, side.xOffset, side.yOffset,
                                    side.zOffset) == EnumActionResult.SUCCESS;
                        }

                        if (!Worked) {
                            i.onItemUse(player, w, tePos, hand, side.getFacing().getOpposite(), side.xOffset, side.yOffset, side.zOffset);
                        }

                        maxStorage -= is.getCount();
                    } else {
                        i.onItemUse(player, w, tePos, hand, side.getFacing().getOpposite(), side.xOffset, side.yOffset, side.zOffset);
                        maxStorage -= is.getCount();
                    }
                } else {
                    maxStorage = 1;
                }

                // Safe keeping
                player.setHeldItem(hand, ItemStack.EMPTY);
            } else {
                worked = true;

                final int sum = this.countEntitesAround(w, tePos);

                if (sum < AEConfig.instance().getFormationPlaneEntityLimit()) {
                    if (type == Actionable.MODULATE) {
                        is.setCount((int) maxStorage);
                        final double x = (side.xOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.xOffset + .5 + te.getPos().getX();
                        final double y = (side.yOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.yOffset + .5 + te.getPos().getY();
                        final double z = (side.zOffset != 0 ? 0 : .7 * (Platform.getRandomFloat() - .5)) + side.zOffset + .5 + te.getPos().getZ();

                        final EntityItem ei = new EntityItem(w, x, y, z, is.copy());

                        Entity result = ei;

                        ei.motionX = side.xOffset * 0.2;
                        ei.motionY = side.yOffset * 0.2;
                        ei.motionZ = side.zOffset * 0.2;

                        if (is.getItem().hasCustomEntity(is)) {
                            result = is.getItem().createEntity(w, ei, is);
                            if (result != null) {
                                ei.setDead();
                            } else {
                                result = ei;
                            }
                        }

                        if (!w.spawnEntity(result)) {
                            result.setDead();
                            worked = false;
                        }
                    }
                } else {
                    worked = false;
                }
            }
        }

        this.blocked = !w.getBlockState(tePos).getBlock().isReplaceable(w, tePos);

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
        return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.getConnections(), this.isPowered(), this.isActive());
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return AEApi.instance().definitions().parts().formationPlane().maybeStack(1).orElse(ItemStack.EMPTY);
    }

    @Override
    public GuiBridge getGuiBridge() {
        return GuiBridge.GUI_FORMATION_PLANE;
    }

    private int countEntitesAround(World world, BlockPos pos) {
        final AxisAlignedBB t = new AxisAlignedBB(pos).grow(8);
        final List<Entity> list = world.getEntitiesWithinAABB(Entity.class, t);

        return list.size();
    }
}
