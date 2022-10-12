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

package appeng.container.implementations;


import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketCompressedNBT;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.parts.misc.PartInterface;
import appeng.parts.reporting.PartInterfaceConfigurationTerminal;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import appeng.util.helpers.ItemHandlerUtil;
import appeng.util.inv.WrapperRangeItemHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


public final class ContainerInterfaceConfigurationTerminal extends AEBaseContainer {

    /**
     * this stuff is all server side..
     */

    private static long autoBase = Long.MIN_VALUE;
    private final Map<IInterfaceHost, ConfigTracker> diList = new HashMap<>();
    private final Map<Long, ConfigTracker> byId = new HashMap<>();
    private IGrid grid;
    private NBTTagCompound data = new NBTTagCompound();

    public ContainerInterfaceConfigurationTerminal(final InventoryPlayer ip, final PartInterfaceConfigurationTerminal anchor) {
        super(ip, anchor);

        if (Platform.isServer()) {
            this.grid = anchor.getActionableNode().getGrid();
        }

        this.bindPlayerInventory(ip, 14, 235 - /* height of player inventory */82);
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }

        super.detectAndSendChanges();

        if (this.grid == null) {
            return;
        }

        int total = 0;
        boolean missing = false;

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileInterface.class)) {
                    if (gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if (ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final ConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartInterface.class)) {
                    if (gn.isActive()) {
                        final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                        if (ih.getInterfaceDuality().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final ConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityInterface dual = ih.getInterfaceDuality();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }
            }
        }

        if (total != this.diList.size() || missing) {
            this.regenList(this.data);
        } else {
            for (final Entry<IInterfaceHost, ConfigTracker> en : this.diList.entrySet()) {
                final ConfigTracker inv = en.getValue();
                for (int x = 0; x < inv.server.getSlots(); x++) {
                    if (this.isDifferent(inv.server.getStackInSlot(x), inv.client.getStackInSlot(x))) {
                        this.addItems(this.data, inv, x, 1);
                    }
                }
            }
        }

        if (!this.data.hasNoTags()) {
            try {
                NetworkHandler.instance().sendTo(new PacketCompressedNBT(this.data), (EntityPlayerMP) this.getPlayerInv().player);
            } catch (final IOException e) {
                // :P
            }

            this.data = new NBTTagCompound();
        }
    }

    public ConfigTracker getSlotByID(long id) {
        return this.byId.get(id);
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        final ConfigTracker inv = this.byId.get(id);
        if (inv != null) {
            final boolean hasItemInHand = !player.inventory.getItemStack().isEmpty();
            final IItemHandler theSlot = new WrapperRangeItemHandler(inv.server, slot, slot + 1);

            ItemStack inSlot = theSlot.getStackInSlot(0);

            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    if (hasItemInHand) {
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, player.inventory.getItemStack().copy());
                    } else {
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
                    }
                    break;
                case PLACE_SINGLE:
                    if (inSlot.getCount() < inSlot.getMaxStackSize() * 8) {
                        inSlot.grow(1);
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
                    }
                    break;
                case PICKUP_SINGLE:
                    if (theSlot.getStackInSlot(0).getCount() > 1) {
                        inSlot.shrink(1);
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot);
                    }
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    if (hasItemInHand) {
                        if (ItemStack.areItemsEqual(inSlot, player.inventory.getItemStack()) && ItemStack.areItemStackTagsEqual(inSlot, player.inventory.getItemStack())) {
                            inSlot.grow(1);
                            ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot.copy());
                        } else {
                            ItemStack configuredStack = player.inventory.getItemStack().copy();
                            configuredStack.setCount(1);
                            ItemHandlerUtil.setStackInSlot(theSlot, 0, configuredStack);
                        }

                    } else if (!inSlot.isEmpty()) {
                        inSlot.shrink(1);
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, inSlot.copy());
                    }

                    break;
                case SHIFT_CLICK:
                    ItemHandlerUtil.setStackInSlot(theSlot, 0, ItemStack.EMPTY);
                    break;

                case CREATIVE_DUPLICATE:

                    if (player.capabilities.isCreativeMode && hasItemInHand) {
                        ItemHandlerUtil.setStackInSlot(theSlot, 0, player.inventory.getItemStack().copy());
                    }

                    break;
                default:
                    return;
            }

            this.updateHeld(player);
        }
    }

    private void regenList(final NBTTagCompound data) {
        this.byId.clear();
        this.diList.clear();

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileInterface.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new ConfigTracker(dual, dual.getConfig(), dual.getTermName()));
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartInterface.class)) {
                    final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
                    final DualityInterface dual = ih.getInterfaceDuality();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new ConfigTracker(dual, dual.getConfig(), dual.getTermName()));
                    }
                }
            }
        }

        data.setBoolean("clear", true);

        for (final Entry<IInterfaceHost, ConfigTracker> en : this.diList.entrySet()) {
            final ConfigTracker inv = en.getValue();
            this.byId.put(inv.which, inv);
            this.addItems(data, inv, 0, inv.server.getSlots());
        }
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return false;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }

        return !ItemStack.areItemStacksEqual(a, b);
    }

    private void addItems(final NBTTagCompound data, final ConfigTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final NBTTagCompound tag = data.getCompoundTag(name);

        if (tag.hasNoTags()) {
            tag.setLong("sortBy", inv.sortBy);
            tag.setString("un", inv.unlocalizedName);
            tag.setTag("pos", NBTUtil.createPosTag(inv.pos));
            tag.setInteger("dim", inv.dim);
        }

        for (int x = 0; x < length; x++) {
            final NBTTagCompound itemNBT = new NBTTagCompound();

            final ItemStack is = inv.server.getStackInSlot(x + offset);

            // "update" client side.
            ItemHandlerUtil.setStackInSlot(inv.client, x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

            if (!is.isEmpty()) {
                is.writeToNBT(itemNBT);
                if (is.getCount() > is.getMaxStackSize()) {
                    itemNBT.setInteger("stackSize", is.getCount());
                }
            }

            tag.setTag(Integer.toString(x + offset), itemNBT);
        }

        data.setTag(name, tag);
    }

    public static class ConfigTracker {

        private final long sortBy;
        private final long which = autoBase++;
        private final String unlocalizedName;
        private final IItemHandler client;
        private final IItemHandler server;
        private final BlockPos pos;
        private final int dim;

        public ConfigTracker(final DualityInterface dual, final IItemHandler configSlots, final String unlocalizedName) {
            this.server = configSlots;
            this.client = new AppEngInternalInventory(null, this.server.getSlots());
            this.unlocalizedName = unlocalizedName;
            this.sortBy = dual.getSortValue();
            this.pos = dual.getLocation().getPos();
            this.dim = dual.getLocation().getWorld().provider.getDimension();
        }

        public IItemHandler getServer() {
            return server;
        }
    }
}
