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
import appeng.api.config.ShowPatternProviders;
import appeng.api.config.YesNo;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.core.AELog;
import appeng.core.sync.packets.InterfaceTerminalPacket;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.InventoryAction;
import appeng.helpers.iface.PatternProviderLogic;
import appeng.helpers.iface.PatternProviderLogicHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.reporting.PatternAccessTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

/**
 * @see appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen
 */
public class InterfaceTerminalMenu extends AEBaseMenu {

    private PatternAccessTerminalPart host;
    @GuiSync(1)
    public ShowPatternProviders showPatternProviders = ShowPatternProviders.UNHIDDEN;

    public ShowPatternProviders getShownProviders() {
        return showPatternProviders;
    }

    public static final MenuType<InterfaceTerminalMenu> TYPE = MenuTypeBuilder
            .create(InterfaceTerminalMenu::new, PatternAccessTerminalPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("interfaceterminal");

    /**
     * this stuff is all server side.
     */

    // We use this serial number to uniquely identify all inventories we send to the client
    // It is used in packets sent by the client to interact with these inventories
    private static long inventorySerial = Long.MIN_VALUE;
    private final Map<PatternProviderLogicHost, InvTracker> diList = new IdentityHashMap<>();
    private final Long2ObjectOpenHashMap<InvTracker> byId = new Long2ObjectOpenHashMap<>();

    public InterfaceTerminalMenu(int id, Inventory ip, PatternAccessTerminalPart anchor) {
        this(TYPE, id, ip, anchor, true);
        this.host = anchor;
    }

    public InterfaceTerminalMenu(MenuType<?> menuType, int id, Inventory ip, Object host,
            boolean bindInventory) {
        super(menuType, id, ip, host);
        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isClientSide()) {
            return;
        }

        showPatternProviders = this.host.getConfigManager().getSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS);

        super.broadcastChanges();

        IGrid grid = getGrid();

        VisitorState state = new VisitorState();
        if (grid != null) {
            visitInterfaceHosts(grid, PatternProviderBlockEntity.class, state);
            visitInterfaceHosts(grid, PatternProviderPart.class, state);
        }

        if (state.total != this.diList.size() || state.forceFullUpdate) {
            sendFullUpdate(grid);
        } else {
            sendIncrementalUpdate();
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

    private <T extends PatternProviderLogicHost> void visitInterfaceHosts(IGrid grid, Class<T> machineClass,
            VisitorState state) {
        for (var ih : grid.getActiveMachines(machineClass)) {
            var dual = ih.getLogic();
            boolean isVisible = dual.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
            if (!isVisible && (showPatternProviders == ShowPatternProviders.UNHIDDEN
                    || showPatternProviders == ShowPatternProviders.UNFILLED
                            && dual.getAvailablePatterns().size() == 9)) {
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
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
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

    private void sendFullUpdate(@Nullable IGrid grid) {
        this.byId.clear();
        this.diList.clear();

        sendPacketToClient(InterfaceTerminalPacket.clearExistingData());

        if (grid == null) {
            return;
        }

        for (var ih : grid.getActiveMachines(PatternProviderBlockEntity.class)) {
            var dual = ih.getLogic();
            boolean isVisible = dual.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
            if (showPatternProviders == ShowPatternProviders.ALL
                    || showPatternProviders == ShowPatternProviders.UNHIDDEN && isVisible
                    || showPatternProviders == ShowPatternProviders.UNFILLED && isVisible
                            && dual.getAvailablePatterns().size() != 9) {
                this.diList.put(ih, new InvTracker(dual, dual.getPatternInv(), dual.getTermName()));
            }
        }

        for (var ih : grid.getActiveMachines(PatternProviderPart.class)) {
            var dual = ih.getLogic();
            boolean isVisible = dual.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL) == YesNo.YES;
            if (showPatternProviders == ShowPatternProviders.ALL
                    || showPatternProviders == ShowPatternProviders.UNHIDDEN && isVisible
                    || showPatternProviders == ShowPatternProviders.UNFILLED && isVisible
                            && dual.getAvailablePatterns().size() != 9) {
                this.diList.put(ih, new InvTracker(dual, dual.getPatternInv(), dual.getTermName()));
            }
        }

        for (var inv : this.diList.values()) {
            this.byId.put(inv.serverId, inv);
            CompoundTag data = new CompoundTag();
            this.addItems(data, inv, 0, inv.server.size());
            sendPacketToClient(InterfaceTerminalPacket.inventory(inv.serverId, data));
        }
    }

    private void sendIncrementalUpdate() {
        for (var inv : this.diList.values()) {
            CompoundTag data = null;
            for (int x = 0; x < inv.server.size(); x++) {
                if (this.isDifferent(inv.server.getStackInSlot(x), inv.client.getStackInSlot(x))) {
                    if (data == null) {
                        data = new CompoundTag();
                    }
                    this.addItems(data, inv, x, 1);
                }
            }
            if (data != null) {
                sendPacketToClient(InterfaceTerminalPacket.inventory(inv.serverId, data));
            }
        }
    }

    private boolean isDifferent(ItemStack a, ItemStack b) {
        if (a.isEmpty() && b.isEmpty()) {
            return false;
        }

        if (a.isEmpty() || b.isEmpty()) {
            return true;
        }

        return !ItemStack.matches(a, b);
    }

    private void addItems(CompoundTag tag, InvTracker inv, int offset, int length) {
        if (tag.isEmpty()) {
            tag.putLong("sortBy", inv.sortBy);
            tag.putString("un", Serializer.toJson(inv.name));
        }

        for (int x = 0; x < length; x++) {
            var itemNBT = new CompoundTag();

            var is = inv.server.getStackInSlot(x + offset);

            // "update" client side.
            inv.client.setItemDirect(x + offset, is.isEmpty() ? ItemStack.EMPTY : is.copy());

            if (!is.isEmpty()) {
                is.save(itemNBT);
            }

            tag.put(Integer.toString(x + offset), itemNBT);
        }
    }

    private static class InvTracker {

        private final long sortBy;
        private final long serverId = inventorySerial++;
        private final Component name;
        // This is used to track the inventory contents we sent to the client for change detection
        private final InternalInventory client;
        // This is a reference to the real inventory used by this machine
        private final InternalInventory server;

        public InvTracker(PatternProviderLogic dual, InternalInventory patterns, Component name) {
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
