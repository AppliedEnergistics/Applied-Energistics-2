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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.ShowPatternProviders;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.AELog;
import appeng.core.sync.packets.ClearPatternAccessTerminalPacket;
import appeng.core.sync.packets.PatternAccessTerminalPacket;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.helpers.InventoryAction;
import appeng.helpers.patternprovider.PatternContainer;
import appeng.menu.AEBaseMenu;
import appeng.menu.guisync.GuiSync;
import appeng.parts.reporting.PatternAccessTerminalPart;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.FilteredInternalInventory;
import appeng.util.inv.filter.IAEItemFilter;

/**
 * @see PatternAccessTermScreen
 */
public class PatternAccessTermMenu extends AEBaseMenu {

    private final IConfigurableObject host;
    @GuiSync(1)
    public ShowPatternProviders showPatternProviders = ShowPatternProviders.VISIBLE;

    public ShowPatternProviders getShownProviders() {
        return showPatternProviders;
    }

    public static final MenuType<PatternAccessTermMenu> TYPE = MenuTypeBuilder
            .create(PatternAccessTermMenu::new, PatternAccessTerminalPart.class)
            .requirePermission(SecurityPermissions.BUILD)
            .build("patternaccessterminal");

    /**
     * this stuff is all server side.
     */

    // We use this serial number to uniquely identify all inventories we send to the client
    // It is used in packets sent by the client to interact with these inventories
    private static long inventorySerial = Long.MIN_VALUE;
    private final Map<PatternContainer, ContainerTracker> diList = new IdentityHashMap<>();
    private final Long2ObjectOpenHashMap<ContainerTracker> byId = new Long2ObjectOpenHashMap<>();
    /**
     * Tracks hosts that were visible before, even if they no longer match the filter. For
     * {@link ShowPatternProviders#NOT_FULL}.
     */
    private final Set<PatternContainer> pinnedHosts = Collections.newSetFromMap(new IdentityHashMap<>());

    public PatternAccessTermMenu(int id, Inventory ip, PatternAccessTerminalPart anchor) {
        this(TYPE, id, ip, anchor, true);
    }

    public PatternAccessTermMenu(MenuType<?> menuType, int id, Inventory ip, IConfigurableObject host,
            boolean bindInventory) {
        super(menuType, id, ip, host);
        this.host = host;
        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void broadcastChanges() {
        if (isClientSide()) {
            return;
        }

        showPatternProviders = this.host.getConfigManager().getSetting(Settings.TERMINAL_SHOW_PATTERN_PROVIDERS);

        super.broadcastChanges();

        if (showPatternProviders != ShowPatternProviders.NOT_FULL) {
            this.pinnedHosts.clear();
        }

        IGrid grid = getGrid();

        var state = new VisitorState();
        if (grid != null) {
            for (var machineClass : grid.getMachineClasses()) {
                if (PatternContainer.class.isAssignableFrom(machineClass)) {
                    visitPatternProviderHosts(grid, (Class<? extends PatternContainer>) machineClass, state);
                }
            }

            // Ensure we don't keep references to removed hosts
            pinnedHosts.removeIf(host -> host.getGrid() != grid);
        } else {
            pinnedHosts.clear();
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
        // Total number of pattern provider hosts found
        int total;
        // Set to true if any visited machines were missing from diList, or had a different name
        boolean forceFullUpdate;
    }

    private boolean isFull(PatternContainer logic) {
        for (int i = 0; i < logic.getTerminalPatternInventory().size(); i++) {
            if (logic.getTerminalPatternInventory().getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isVisible(PatternContainer container) {
        boolean isVisible = container.isVisibleInTerminal();

        return switch (getShownProviders()) {
            case VISIBLE -> isVisible;
            case NOT_FULL -> isVisible && (pinnedHosts.contains(container) || !isFull(container));
            case ALL -> true;
        };
    }

    private <T extends PatternContainer> void visitPatternProviderHosts(IGrid grid, Class<T> machineClass,
            VisitorState state) {
        for (var container : grid.getActiveMachines(machineClass)) {
            if (!isVisible(container)) {
                continue;
            }

            if (getShownProviders() == ShowPatternProviders.NOT_FULL) {
                pinnedHosts.add(container);
            }

            var t = this.diList.get(container);
            if (t == null || !t.group.equals(container.getTerminalGroup())) {
                state.forceFullUpdate = true;
            }

            state.total++;
        }
    }

    @Override
    public void doAction(ServerPlayer player, InventoryAction action, int slot, long id) {
        final ContainerTracker inv = this.byId.get(id);
        if (inv == null) {
            // Can occur if the client sent an interaction packet right before an inventory got removed
            return;
        }
        if (slot < 0 || slot >= inv.server.size()) {
            // Client refers to an invalid slot. This should NOT happen
            AELog.warn("Client refers to invalid slot %d of inventory %s", slot, inv.container);
            return;
        }

        final ItemStack is = inv.server.getStackInSlot(slot);

        var patternSlot = new FilteredInternalInventory(inv.server.getSlotInv(slot), new PatternSlotFilter());

        var carried = getCarried();
        switch (action) {
            case PICKUP_OR_SET_DOWN -> {
                if (!carried.isEmpty()) {
                    ItemStack inSlot = patternSlot.getStackInSlot(0);
                    if (inSlot.isEmpty()) {
                        setCarried(patternSlot.addItems(carried));
                    } else {
                        inSlot = inSlot.copy();
                        final ItemStack inHand = carried.copy();

                        patternSlot.setItemDirect(0, ItemStack.EMPTY);
                        setCarried(ItemStack.EMPTY);

                        setCarried(patternSlot.addItems(inHand.copy()));

                        if (getCarried().isEmpty()) {
                            setCarried(inSlot);
                        } else {
                            setCarried(inHand);
                            patternSlot.setItemDirect(0, inSlot);
                        }
                    }
                } else {
                    setCarried(patternSlot.getStackInSlot(0));
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                }
            }
            case SPLIT_OR_PLACE_SINGLE -> {
                if (!carried.isEmpty()) {
                    ItemStack extra = carried.split(1);
                    if (!extra.isEmpty()) {
                        extra = patternSlot.addItems(extra);
                    }
                    if (!extra.isEmpty()) {
                        carried.grow(extra.getCount());
                    }
                } else if (!is.isEmpty()) {
                    setCarried(patternSlot.extractItem(0, (is.getCount() + 1) / 2, false));
                }
            }
            case SHIFT_CLICK -> {
                var stack = patternSlot.getStackInSlot(0).copy();
                if (!player.getInventory().add(stack)) {
                    patternSlot.setItemDirect(0, stack);
                } else {
                    patternSlot.setItemDirect(0, ItemStack.EMPTY);
                }
            }
            case MOVE_REGION -> {
                for (int x = 0; x < inv.server.size(); x++) {
                    var stack = inv.server.getStackInSlot(x);
                    if (!player.getInventory().add(stack)) {
                        patternSlot.setItemDirect(0, stack);
                    } else {
                        patternSlot.setItemDirect(0, ItemStack.EMPTY);
                    }
                }
            }
            case CREATIVE_DUPLICATE -> {
                if (player.getAbilities().instabuild && carried.isEmpty()) {
                    setCarried(is.isEmpty() ? ItemStack.EMPTY : is.copy());
                }
            }
        }
    }

    private void sendFullUpdate(@Nullable IGrid grid) {
        this.byId.clear();
        this.diList.clear();

        sendPacketToClient(new ClearPatternAccessTerminalPacket());

        if (grid == null) {
            return;
        }

        for (var machineClass : grid.getMachineClasses()) {
            var containerClass = tryCastMachineToContainer(machineClass);
            if (containerClass == null) {
                continue;
            }

            for (var container : grid.getActiveMachines(containerClass)) {
                if (isVisible(container)) {
                    this.diList.put(container, new ContainerTracker(container,
                            container.getTerminalPatternInventory(),
                            container.getTerminalGroup()));
                }
            }
        }

        for (var inv : this.diList.values()) {
            this.byId.put(inv.serverId, inv);
            sendPacketToClient(inv.createFullPacket());
        }
    }

    private void sendIncrementalUpdate() {
        for (var inv : this.diList.values()) {
            var packet = inv.createUpdatePacket();
            if (packet != null) {
                sendPacketToClient(packet);
            }
        }
    }

    private static class ContainerTracker {

        private final PatternContainer container;
        private final long sortBy;
        private final long serverId = inventorySerial++;
        private final PatternContainerGroup group;
        // This is used to track the inventory contents we sent to the client for change detection
        private final InternalInventory client;
        // This is a reference to the real inventory used by this machine
        private final InternalInventory server;

        public ContainerTracker(PatternContainer container, InternalInventory patterns, PatternContainerGroup group) {
            this.container = container;
            this.server = patterns;
            this.client = new AppEngInternalInventory(this.server.size());
            this.group = group;
            this.sortBy = container.getTerminalSortOrder();
        }

        public PatternAccessTerminalPacket createFullPacket() {
            var slots = new Int2ObjectArrayMap<ItemStack>(server.size());
            for (int i = 0; i < server.size(); i++) {
                var stack = server.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    slots.put(i, stack);
                }
            }

            return PatternAccessTerminalPacket.fullUpdate(
                    serverId,
                    server.size(),
                    sortBy,
                    group,
                    slots);
        }

        @Nullable
        public PatternAccessTerminalPacket createUpdatePacket() {
            var changedSlots = detectChangedSlots();
            if (changedSlots == null) {
                return null;
            }

            var slots = new Int2ObjectArrayMap<ItemStack>(changedSlots.size());
            for (int i = 0; i < changedSlots.size(); i++) {
                var slot = changedSlots.getInt(i);
                var stack = server.getStackInSlot(slot);
                // "update" client side.
                client.setItemDirect(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
                slots.put(slot, stack);
            }

            return PatternAccessTerminalPacket.incrementalUpdate(
                    serverId,
                    slots);
        }

        @Nullable
        private IntList detectChangedSlots() {
            IntList changedSlots = null;
            for (int x = 0; x < server.size(); x++) {
                if (isDifferent(server.getStackInSlot(x), client.getStackInSlot(x))) {
                    if (changedSlots == null) {
                        changedSlots = new IntArrayList();
                    }
                    changedSlots.add(x);
                }
            }
            return changedSlots;
        }

        private static boolean isDifferent(ItemStack a, ItemStack b) {
            if (a.isEmpty() && b.isEmpty()) {
                return false;
            }

            if (a.isEmpty() || b.isEmpty()) {
                return true;
            }

            return !ItemStack.matches(a, b);
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

    private static Class<? extends PatternContainer> tryCastMachineToContainer(Class<?> machineClass) {
        if (PatternContainer.class.isAssignableFrom(machineClass)) {
            return machineClass.asSubclass(PatternContainer.class);
        }
        return null;
    }
}
