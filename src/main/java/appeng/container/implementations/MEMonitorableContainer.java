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

import java.io.IOException;
import java.nio.BufferOverflowException;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.implementations.tiles.IViewCellStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.RestrictedInputSlot;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class MEMonitorableContainer extends AEBaseContainer
        implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEItemStack> {

    public static ContainerType<MEMonitorableContainer> TYPE;

    private static final ContainerHelper<MEMonitorableContainer, ITerminalHost> helper = new ContainerHelper<>(
            MEMonitorableContainer::new, ITerminalHost.class);

    public static MEMonitorableContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final RestrictedInputSlot[] cellView = new RestrictedInputSlot[5];
    private final IMEMonitor<IAEItemStack> monitor;
    private final IItemList<IAEItemStack> items = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private final IConfigManager clientCM;
    private final ITerminalHost host;
    @GuiSync(99)
    public boolean canAccessViewCells = false;
    @GuiSync(98)
    public boolean hasPower = false;
    /**
     * The number of active crafting jobs in the network. -1 means unknown and will hide the label on the screen.
     */
    @GuiSync(100)
    public int activeCraftingJobs = -1;

    private IConfigManagerHost gui;
    private IConfigManager serverCM;
    private IGridNode networkNode;

    public MEMonitorableContainer(int id, final PlayerInventory ip, final ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public MEMonitorableContainer(ContainerType<?> containerType, int id, PlayerInventory ip,
            final ITerminalHost monitorable, final boolean bindInventory) {
        super(containerType, id, ip, monitorable instanceof TileEntity ? (TileEntity) monitorable : null,
                monitorable instanceof IPart ? (IPart) monitorable : null,
                monitorable instanceof IGuiItemObject ? (IGuiItemObject) monitorable : null);

        this.host = monitorable;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        if (isServer()) {
            this.serverCM = monitorable.getConfigManager();

            this.monitor = monitorable
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            if (this.monitor != null) {
                this.monitor.addListener(this, null);

                this.setCellInventory(this.monitor);

                if (monitorable instanceof IPortableCell) {
                    this.setPowerSource((IEnergySource) monitorable);
                } else if (monitorable instanceof IMEChest) {
                    this.setPowerSource((IEnergySource) monitorable);
                } else if (monitorable instanceof IGridHost || monitorable instanceof IActionHost) {
                    final IGridNode node;
                    if (monitorable instanceof IGridHost) {
                        node = ((IGridHost) monitorable).getGridNode(AEPartLocation.INTERNAL);
                    } else if (monitorable instanceof IActionHost) {
                        node = ((IActionHost) monitorable).getActionableNode();
                    } else {
                        node = null;
                    }

                    if (node != null) {
                        this.networkNode = node;
                        final IGrid g = node.getGrid();
                        if (g != null) {
                            this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                        }
                    }
                }
            } else {
                this.setValidContainer(false);
            }
        } else {
            this.monitor = null;
        }

        this.canAccessViewCells = false;
        if (monitorable instanceof IViewCellStorage) {
            for (int y = 0; y < 5; y++) {
                this.cellView[y] = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.VIEW_CELL,
                        ((IViewCellStorage) monitorable).getViewCellStorage(), y, 206, y * 18 + 8,
                        this.getPlayerInventory());
                this.cellView[y].setAllowEdit(this.canAccessViewCells);
                this.addSlot(this.cellView[y]);
            }
        }

        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 0);
        }
    }

    public IGridNode getNetworkNode() {
        return this.networkNode;
    }

    @Override
    public void detectAndSendChanges() {
        if (isServer()) {
            if (this.monitor != this.host
                    .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class))) {
                this.setValidContainer(false);
            }

            this.updateActiveCraftingJobs();

            for (final Settings set : this.serverCM.getSettings()) {
                final Enum<?> sideLocal = this.serverCM.getSetting(set);
                final Enum<?> sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    this.clientCM.putSetting(set, sideLocal);
                    for (final IContainerListener crafter : this.getListeners()) {
                        if (crafter instanceof ServerPlayerEntity) {
                            NetworkHandler.instance().sendTo(new ConfigValuePacket(set.name(), sideLocal.name()),
                                    (ServerPlayerEntity) crafter);
                        }
                    }
                }
            }

            if (!this.items.isEmpty()) {
                try {
                    final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

                    final MEInventoryUpdatePacket piu = new MEInventoryUpdatePacket();

                    for (final IAEItemStack is : this.items) {
                        final IAEItemStack send = monitorCache.findPrecise(is);
                        if (send == null) {
                            is.setStackSize(0);
                            piu.appendItem(is);
                        } else {
                            piu.appendItem(send);
                        }
                    }

                    if (!piu.isEmpty()) {
                        this.items.resetStatus();

                        for (final Object c : this.getListeners()) {
                            if (c instanceof PlayerEntity) {
                                NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);
                            }
                        }
                    }
                } catch (final IOException e) {
                    AELog.debug(e);
                }
            }

            this.updatePowerStatus();

            final boolean oldAccessible = this.canAccessViewCells;
            this.canAccessViewCells = this.hasAccess(SecurityPermissions.BUILD, false);
            if (this.canAccessViewCells != oldAccessible) {
                for (int y = 0; y < 5; y++) {
                    if (this.cellView[y] != null) {
                        this.cellView[y].setAllowEdit(this.canAccessViewCells);
                    }
                }
            }

            super.detectAndSendChanges();
        }

    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(
                        this.getPowerSource().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Throwable t) {
            // :P
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("canAccessViewCells")) {
            for (int y = 0; y < 5; y++) {
                if (this.cellView[y] != null) {
                    this.cellView[y].setAllowEdit(this.canAccessViewCells);
                }
            }
        }

        super.onUpdate(field, oldValue, newValue);
    }

    @Override
    public void addListener(final IContainerListener c) {
        super.addListener(c);

        this.queueInventory(c);
    }

    private void updateActiveCraftingJobs() {
        IGridNode hostNode = networkNode;
        if (hostNode == null) {
            // Wireless terminals do not directly expose the target grid (even though they
            // have one)
            if (host instanceof IActionHost) {
                hostNode = ((IActionHost) host).getActionableNode();
            }
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
        ICraftingGrid craftingGrid = grid.getCache(ICraftingGrid.class);
        for (ICraftingCPU cpus : craftingGrid.getCpus()) {
            if (cpus.isBusy()) {
                activeJobs++;
            }
        }
        this.activeCraftingJobs = activeJobs;
    }

    private void queueInventory(final IContainerListener c) {
        if (isServer() && c instanceof PlayerEntity && this.monitor != null) {
            try {
                MEInventoryUpdatePacket piu = new MEInventoryUpdatePacket();
                final IItemList<IAEItemStack> monitorCache = this.monitor.getStorageList();

                for (final IAEItemStack send : monitorCache) {
                    try {
                        piu.appendItem(send);
                    } catch (final BufferOverflowException boe) {
                        NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);

                        piu = new MEInventoryUpdatePacket();
                        piu.appendItem(send);
                    }
                }

                NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);
            } catch (final IOException e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public void removeListener(final IContainerListener c) {
        super.removeListener(c);

        if (this.getListeners().isEmpty() && this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public void onContainerClosed(final PlayerEntity player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final IActionSource source) {
        for (final IAEItemStack is : change) {
            this.items.add(is);
        }
    }

    @Override
    public void onListUpdate() {
        for (final IContainerListener c : this.getListeners()) {
            this.queueInventory(c);
        }
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings settingName, final Enum<?> newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public ItemStack[] getViewCells() {
        final ItemStack[] list = new ItemStack[this.cellView.length];

        for (int x = 0; x < this.cellView.length; x++) {
            list[x] = this.cellView[x].getStack();
        }

        return list;
    }

    public RestrictedInputSlot getCellViewSlot(final int index) {
        return this.cellView[index];
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }
}
