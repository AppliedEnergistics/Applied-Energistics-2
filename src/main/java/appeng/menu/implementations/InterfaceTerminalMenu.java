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

package appeng.menu.implementations;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.iface.DualityPatternProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InterfaceTerminalPacket;
import appeng.helpers.InventoryAction;
import appeng.helpers.iface.IPatternProviderHost;
import appeng.items.misc.EncodedPatternItem;
import appeng.menu.AEBaseMenu;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.reporting.InterfaceTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

/**
 * @see appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen
 */
public final class InterfaceTerminalMenu extends AEBaseMenu {

    public static final MenuType<InterfaceTerminalMenu> TYPE = MenuTypeBuilder
            .create(InterfaceTerminalMenu::new, InterfaceTerminalPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("interfaceterminal");

    /**
     * this stuff is all server side..
     */

    // We use this serial number to uniquely identify all inventories we send to the client
    // It is used in packets sent by the client to interact with these inventories
    private static long inventorySerial = Long.MIN_VALUE;
    private final Map<IPatternProviderHost, InvTracker> diList = new IdentityHashMap<>();
    private final Long2ObjectOpenHashMap<InvTracker> byId = new Long2ObjectOpenHashMap<>();

    public InterfaceTerminalMenu(int id, final Inventory ip, final InterfaceTerminalPart anchor) {
        super(TYPE, id, ip, anchor);
        this.createPlayerInventorySlots(ip);
    }

    @Override
    public void broadcastChanges() {
        if (isClient()) {
            return;
        }

        super.broadcastChanges();

        IGrid grid = getGrid();

        VisitorState state = new VisitorState();
        if (grid != null) {
            visitInterfaceHosts(grid, PatternProviderBlockEntity.class, state);
            visitInterfaceHosts(grid, PatternProviderPart.class, state);
        }

        InterfaceTerminalPacket packet;
        if (state.total != this.diList.size() || state.forceFullUpdate) {
            packet = this.createFullUpdate(grid);
        } else {
            packet = createIncrementalUpdate();
        }

        if (packet != null) {
            NetworkHandler.instance().sendTo(packet, (ServerPlayer) this.getPlayerInventory().player);
        }
    }

    @Nullable
    private IGrid getGrid() {
        IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                return agn.getGrid();
            }
        }
        return null;
    }

    private static class VisitorState {
        // Total number of interface hosts founds
        int total;
        // Set to true if any visited machines were missing from diList, or had a different name
        boolean forceFullUpdate;
    }

    private <T extends IPatternProviderHost> void visitInterfaceHosts(IGrid grid, Class<T> machineClass,
                                                                      VisitorState state) {
        for (var ih : grid.getActiveMachines(machineClass)) {
            var dual = ih.getDuality();
            if (dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                continue;
            }

            final InvTracker t = this.diList.get(ih);
            if (t == null || !t.name.equals(dual.getTermName())) {
                state.forceFullUpdate = true;
            }

            state.total++;
        }
    }

    @Override
    public void doAction(final ServerPlayer player, final InventoryAction action, final int slot, final long id) {
        final InvTracker inv = this.byId.get(id);
        if (inv == null) {
            // Can occur if the client sent an interaction packet right before an inventory got removed
            return;
        }
        if (slot < 0 || slot >= inv.server.size()) {
            // Client refers to an invalid slot. This should NOT happen
            AELog.warn("Client refers to invalid slot %d of inventory %s", slot, inv.name.getString());
            return;
        }

        final ItemStack is = inv.server.getStackInSlot(slot);

        var interfaceSlot = new FilteredInternalInventory(inv.server.getSlotInv(slot), new PatternSlotFilter());

        var carried = getCarried();
        switch (action) {
            case PICKUP_OR_SET_DOWN:

                if (!carried.isEmpty()) {
                    ItemStack inSlot = interfaceSlot.getStackInSlot(0);
                    if (inSlot.isEmpty()) {
                        setCarried(interfaceSlot.addItems(carried));
                    } else {
                        inSlot = inSlot.copy();
                        final ItemStack inHand = carried.copy();

                        interfaceSlot.setItemDirect(0, ItemStack.EMPTY);
                        setCarried(ItemStack.EMPTY);

                        setCarried(interfaceSlot.addItems(inHand.copy()));

                        if (carried.isEmpty()) {
                            setCarried(inSlot);
                        } else {
                            setCarried(inHand);
                            interfaceSlot.setItemDirect(0, inSlot);
                        }
                    }
                } else {
                    setCarried(interfaceSlot.getStackInSlot(0));
                    interfaceSlot.setItemDirect(0, ItemStack.EMPTY);
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:

                if (!carried.isEmpty()) {
                    ItemStack extra = carried.split(1);
                    if (!extra.isEmpty()) {
                        extra = interfaceSlot.addItems(extra);
                    }
                    if (!extra.isEmpty()) {
                        carried.grow(extra.getCount());
                    }
                } else if (!is.isEmpty()) {
                    setCarried(interfaceSlot.extractItem(0, (is.getCount() + 1) / 2, false));
                }

                break;
            case SHIFT_CLICK: {
                var stack = interfaceSlot.getStackInSlot(0).copy();
                if (!player.getInventory().add(stack)) {
                    interfaceSlot.setItemDirect(0, stack);
                } else {
                    interfaceSlot.setItemDirect(0, ItemStack.EMPTY);
                }
            }
                break;
            case MOVE_REGION:
                for (int x = 0; x < inv.server.size(); x++) {
                    var stack = inv.server.getStackInSlot(x);
                    if (!player.getInventory().add(stack)) {
                        interfaceSlot.setItemDirect(0, stack);
                    } else {
                        interfaceSlot.setItemDirect(0, ItemStack.EMPTY);
                    }
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.getAbilities().instabuild && carried.isEmpty()) {
                    setCarried(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                }
                break;
        }
    }

    private InterfaceTerminalPacket createFullUpdate(@Nullable IGrid grid) {
        this.byId.clear();
        this.diList.clear();

        if (grid == null) {
            return new InterfaceTerminalPacket(true, new CompoundTag());
        }

        for (var ih : grid.getActiveMachines(PatternProviderBlockEntity.class)) {
            var dual = ih.getDuality();
            if (dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                this.diList.put(ih, new InvTracker(dual, dual.getPatternInv(), dual.getTermName()));
            }
        }

        for (var ih : grid.getActiveMachines(PatternProviderPart.class)) {
            var dual = ih.getDuality();
            if (dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                this.diList.put(ih, new InvTracker(dual, dual.getPatternInv(), dual.getTermName()));
            }
        }

        CompoundTag data = new CompoundTag();
        for (var inv : this.diList.values()) {
            this.byId.put(inv.serverId, inv);
            this.addItems(data, inv, 0, inv.server.size());
        }
        return new InterfaceTerminalPacket(true, data);
    }

    private InterfaceTerminalPacket createIncrementalUpdate() {
        CompoundTag data = null;
        for (var inv : this.diList.values()) {
            for (int x = 0; x < inv.server.size(); x++) {
                if (this.isDifferent(inv.server.getStackInSlot(x), inv.client.getStackInSlot(x))) {
                    if (data == null) {
                        data = new CompoundTag();
                    }
                    this.addItems(data, inv, x, 1);
                }
            }
        }
        if (data != null) {
            return new InterfaceTerminalPacket(false, data);
        }
        return null;
    }

    private boolean isDifferent(final ItemStack a, final ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return false;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }

        return !ItemStack.matches(a, b);
    }

    private void addItems(final CompoundTag data, final InvTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.serverId, Character.MAX_RADIX);
        final CompoundTag tag = data.getCompound(name);

        if (tag.isEmpty()) {
            tag.putLong("sortBy", inv.sortBy);
            tag.putString("un", Serializer.toJson(inv.name));
        }

        for (int x = 0; x < length; x++) {
            final CompoundTag itemNBT = new CompoundTag();

            final ItemStack is = inv.server.getStackInSlot(x + offset);

            // "update" client side.
            inv.client.setItemDirect(x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

            if (!is.isEmpty()) {
                is.save(itemNBT);
            }

            tag.put(Integer.toString(x + offset), itemNBT);
        }

        data.put(name, tag);
    }

    private static class InvTracker {

        private final long sortBy;
        private final long serverId = inventorySerial++;
        private final Component name;
        // This is used to track the inventory contents we sent to the client for change detection
        private final InternalInventory client;
        // This is a reference to the real inventory used by this machine
        private final InternalInventory server;

        public InvTracker(final DualityPatternProvider dual, final InternalInventory patterns, final Component name) {
            this.server = patterns;
            this.client = new AppEngInternalInventory(this.server.size());
            this.name = name;
            this.sortBy = dual.getSortValue();
        }
    }

    private static class PatternSlotFilter implements IAEItemFilter {
        @Override
        public boolean allowExtract(InternalInventory inv, int slot, int amount) {
            return true;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem;
        }
    }
}
