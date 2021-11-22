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

package appeng.menu.me.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.*;
import appeng.api.implementations.blockentities.IMEChest;
import appeng.api.implementations.blockentities.IViewCellStorage;
import appeng.api.implementations.menuobjects.IPortableCell;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.me.common.MEMonitorableScreen;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInteractionPacket;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import appeng.menu.guisync.GuiSync;
import appeng.menu.slot.AppEngSlot;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerListener;

/**
 * @see MEMonitorableScreen
 */
public abstract class MEMonitorableMenu extends AEBaseMenu
        implements IConfigManagerListener, IConfigurableObject, IMEMonitorListener, IMEInteractionHandler {

    private final List<RestrictedInputSlot> viewCellSlots;
    private final IConfigManager clientCM;
    private final ITerminalHost host;
    @GuiSync(99)
    public boolean canEditViewCells;
    @GuiSync(98)
    public boolean hasPower = false;
    /**
     * The number of active crafting jobs in the network. -1 means unknown and will hide the label on the screen.
     */
    @GuiSync(100)
    public int activeCraftingJobs = -1;

    private IConfigManagerListener gui;
    private IConfigManager serverCM;
    private IGridNode networkNode;

    protected final IEnergySource powerSource;
    protected final IMEMonitor monitor;

    private final IncrementalUpdateHelper updateHelper = new IncrementalUpdateHelper();

    /**
     * The repository of entries currently known on the client-side. This is maintained by the screen associated with
     * this menu and will only be non-null on the client-side.
     */
    @Nullable
    private IClientRepo clientRepo;

    /**
     * The last set of craftables sent to the client.
     */
    private Set<AEKey> previousCraftables = Collections.emptySet();

    public MEMonitorableMenu(MenuType<?> menuType, int id, Inventory ip,
            final ITerminalHost host, final boolean bindInventory,
            IStorageChannel<?> storageChannel) {
        super(menuType, id, ip, host);

        this.host = host;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        IEnergySource powerSource = null;
        if (isServer()) {
            this.serverCM = host.getConfigManager();

            this.monitor = host.getInventory();
            if (this.monitor != null) {
                this.monitor.addListener(this, null);

                if (host instanceof IPortableCell || host instanceof IMEChest) {
                    powerSource = (IEnergySource) host;
                } else if (host instanceof IActionHost actionHost) {
                    var node = actionHost.getActionableNode();
                    if (node != null) {
                        this.networkNode = node;
                        var g = node.getGrid();
                        if (g != null) {
                            powerSource = new ChannelPowerSrc(this.networkNode, g.getEnergyService());
                        }
                    }
                }
            } else {
                this.setValidMenu(false);
            }
        } else {
            this.monitor = null;
        }
        this.powerSource = powerSource;

        // Create slots for the view cells, in case the terminal host supports those
        if (!hideViewCells() && host instanceof IViewCellStorage) {
            var viewCellStorage = ((IViewCellStorage) host).getViewCellStorage();
            this.viewCellSlots = new ArrayList<>(viewCellStorage.size());
            for (int i = 0; i < viewCellStorage.size(); i++) {
                var slot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL,
                        viewCellStorage, i);
                this.addSlot(slot, SlotSemantic.VIEW_CELL);
                this.viewCellSlots.add(slot);
            }
        } else {
            this.viewCellSlots = Collections.emptyList();
        }
        updateViewCellPermission();

        if (bindInventory) {
            this.createPlayerInventorySlots(ip);
        }
    }

    protected boolean hideViewCells() {
        return false;
    }

    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    protected boolean isKeyVisible(AEKey key) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        if (isServer()) {
            // Close the screen if the backing network inventory has changed
            if (this.monitor != this.host.getInventory()) {
                this.setValidMenu(false);
                return;
            }

            this.updateActiveCraftingJobs();

            for (var set : this.serverCM.getSettings()) {
                var sideLocal = this.serverCM.getSetting(set);
                var sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    set.copy(serverCM, clientCM);
                    sendPacketToClient(new ConfigValuePacket(set, serverCM));
                }
            }

            var craftables = getCraftablesFromGrid();
            // This is currently not supported/backed by any network service
            var requestables = new KeyCounter();

            if (this.updateHelper.hasChanges() || !previousCraftables.equals(craftables)) {
                try {
                    var builder = MEInventoryUpdatePacket
                            .builder(containerId, updateHelper.isFullUpdate());
                    builder.setFilter(this::isKeyVisible);

                    var storageList = monitor.getCachedAvailableStacks();
                    if (this.updateHelper.isFullUpdate()) {
                        builder.addFull(updateHelper, storageList, craftables, requestables);
                    } else {
                        builder.addChanges(updateHelper, storageList, craftables, requestables);
                    }

                    builder.buildAndSend(this::sendPacketToClient);

                } catch (Exception e) {
                    AELog.warn(e, "Failed to send incremental inventory update to client");
                }

                updateHelper.commitChanges();
                previousCraftables = ImmutableSet.copyOf(craftables);
            }

            this.updatePowerStatus();

            updateViewCellPermission();

            super.broadcastChanges();
        }

    }

    private Set<AEKey> getCraftablesFromGrid() {
        if (networkNode != null && networkNode.isActive()) {
            return networkNode.getGrid().getCraftingService().getCraftables()
                    .stream()
                    .filter(this::isKeyVisible)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    /**
     * The player's permission w.r.t. editing the terminal can change while it is open. Update the view cell permissions
     * accordingly.
     */
    private void updateViewCellPermission() {
        final boolean oldAccessible = this.canEditViewCells;
        this.canEditViewCells = this.hasAccess(SecurityPermissions.BUILD, false);
        if (this.canEditViewCells != oldAccessible) {
            for (RestrictedInputSlot slot : viewCellSlots) {
                slot.setAllowEdit(this.canEditViewCells);
            }
        }
    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.powerSource instanceof IEnergyService) {
                this.setPowered(((IEnergyService) this.powerSource).isNetworkPowered());
            } else {
                this.setPowered(
                        this.powerSource.extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable t) {
            // :P
        }
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();

        // When the canEditViewCells field changes on the client-side after a data sync from the server,
        // update the associated slots accordingly
        for (RestrictedInputSlot slot : viewCellSlots) {
            slot.setAllowEdit(this.canEditViewCells);
        }
    }

    private void updateActiveCraftingJobs() {
        IGridNode hostNode = networkNode;
        // Wireless terminals do not directly expose the target grid (even though they
        // have one)
        if (hostNode == null && host instanceof IActionHost) {
            hostNode = ((IActionHost) host).getActionableNode();
        }
        IGrid grid = null;
        if (hostNode != null) {
            grid = hostNode.getGrid();
        }

        if (grid == null) {
            // No grid to query crafting jobs from
            this.activeCraftingJobs = -1;
            return;
        }

        int activeJobs = 0;
        ICraftingService craftingGrid = grid.getCraftingService();
        for (ICraftingCPU cpus : craftingGrid.getCpus()) {
            if (cpus.isBusy()) {
                activeJobs++;
            }
        }
        this.activeCraftingJobs = activeJobs;
    }

    @Override
    public void removed(final Player player) {
        super.removed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(IMEMonitor monitor, Iterable<AEKey> change, IActionSource source) {
        for (AEKey key : change) {
            this.updateHelper.addChange(key);
        }
    }

    @Override
    public void onListUpdate() {
        if (isServer()) {
            // This resets it back to the initial state of requiring a full update,
            // which will be carried out in the next update tick
            this.updateHelper.clear();
        }
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        if (this.getGui() != null) {
            this.getGui().onSettingChanged(manager, setting);
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public List<ItemStack> getViewCells() {
        return this.viewCellSlots.stream()
                .map(AppEngSlot::getItem)
                .collect(Collectors.toList());
    }

    /**
     * Checks that the inventory monitor is connected, a power source exists and that it is powered.
     */
    protected final boolean canInteractWithGrid() {
        return this.monitor != null && this.powerSource != null && this.isPowered();
    }

    @Override
    public final void handleInteraction(long serial, InventoryAction action) {
        if (isClient()) {
            NetworkHandler.instance().sendToServer(new MEInteractionPacket(containerId, serial, action));
            return;
        }

        // Do not allow interactions if there's no monitor or no power
        if (!canInteractWithGrid()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) this.getPlayerInventory().player;

        // Serial -1 is used to target empty virtual slots, which only allows the player to put
        // items under their cursor into the network inventory
        if (serial == -1) {
            handleNetworkInteraction(player, null, action);
            return;
        }

        AEKey stack = getStackBySerial(serial);
        if (stack == null) {
            // This can happen if the client sent the request after we removed the item, but before
            // the client knows about it (-> network delay).
            return;
        }

        handleNetworkInteraction(player, stack, action);
    }

    protected abstract void handleNetworkInteraction(ServerPlayer player, @Nullable AEKey clickedKey,
            InventoryAction action);

    @Nullable
    protected final AEKey getStackBySerial(long serial) {
        return updateHelper.getBySerial(serial);
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    private IConfigManagerListener getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerListener gui) {
        this.gui = gui;
    }

    @Nullable
    public IClientRepo getClientRepo() {
        return clientRepo;
    }

    public void setClientRepo(@Nullable IClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

}
