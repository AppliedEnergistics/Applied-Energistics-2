/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEFluidInventoryUpdatePacket;
import appeng.core.sync.packets.TargetFluidStackPacket;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidStack;

/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class FluidTerminalContainer extends AEBaseContainer
        implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEFluidStack> {

    public static ContainerType<FluidTerminalContainer> TYPE;

    private static final ContainerHelper<FluidTerminalContainer, ITerminalHost> helper = new ContainerHelper<>(
            FluidTerminalContainer::new, ITerminalHost.class, SecurityPermissions.BUILD);

    public static FluidTerminalContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final IConfigManager clientCM;
    private final IMEMonitor<IAEFluidStack> monitor;
    private final IItemList<IAEFluidStack> fluids = Api.instance().storage()
            .getStorageChannel(IFluidStorageChannel.class).createList();
    @GuiSync(99)
    public boolean hasPower = false;
    private ITerminalHost terminal;
    private IConfigManager serverCM;
    private IConfigManagerHost gui;
    private IGridNode networkNode;
    // Holds the fluid the client wishes to extract, or null for insert
    private IAEFluidStack clientRequestedTargetFluid = null;

    public FluidTerminalContainer(int id, PlayerInventory ip, ITerminalHost terminal) {
        super(TYPE, id, ip, terminal);
        this.terminal = terminal;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        if (Platform.isServer()) {
            this.serverCM = terminal.getConfigManager();
            this.monitor = terminal
                    .getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));

            if (this.monitor != null) {
                this.monitor.addListener(this, null);

                if (terminal instanceof IEnergySource) {
                    this.setPowerSource((IEnergySource) terminal);
                } else if (terminal instanceof IGridHost || terminal instanceof IActionHost) {
                    final IGridNode node;
                    if (terminal instanceof IGridHost) {
                        node = ((IGridHost) terminal).getGridNode(AEPartLocation.INTERNAL);
                    } else if (terminal instanceof IActionHost) {
                        node = ((IActionHost) terminal).getActionableNode();
                    } else {
                        node = null;
                    }

                    if (node != null) {
                        this.networkNode = node;
                        final IGrid g = node.getGrid();
                        if (g != null) {
                            this.setPowerSource(new ChannelPowerSrc(this.networkNode,
                                    (IEnergySource) g.getCache(IEnergyGrid.class)));
                        }
                    }
                }
            }
        } else {
            this.monitor = null;
        }
        this.bindPlayerInventory(ip, 0, 222 - 82);
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change,
            IActionSource actionSource) {
        for (final IAEFluidStack is : change) {
            this.fluids.add(is);
        }
    }

    @Override
    public void onListUpdate() {
        for (final IContainerListener c : this.listeners) {
            this.queueInventory(c);
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);

        this.queueInventory(listener);
    }

    @Override
    public void onContainerClosed(final PlayerEntity player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    private void queueInventory(final IContainerListener c) {
        if (Platform.isServer() && c instanceof PlayerEntity && this.monitor != null) {
            try {
                MEFluidInventoryUpdatePacket piu = new MEFluidInventoryUpdatePacket();
                final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

                for (final IAEFluidStack send : monitorCache) {
                    try {
                        piu.appendFluid(send);
                    } catch (final BufferOverflowException boe) {
                        NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);

                        piu = new MEFluidInventoryUpdatePacket();
                        piu.appendFluid(send);
                    }
                }

                NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);
            } catch (final IOException e) {
                AELog.debug(e);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public void setTargetStack(final IAEFluidStack stack) {
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetFluid == null) {
                return;
            }
            if (stack != null && this.clientRequestedTargetFluid != null
                    && stack.getFluidStack().isFluidEqual(this.clientRequestedTargetFluid.getFluidStack())) {
                return;
            }
            NetworkHandler.instance().sendToServer(new TargetFluidStackPacket((AEFluidStack) stack));
        }

        this.clientRequestedTargetFluid = stack == null ? null : stack.copy();
    }

    @Override
    public void updateSetting(IConfigManager manager, Settings settingName, Enum<?> newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (this.monitor != this.terminal
                    .getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class))) {
                this.setValidContainer(false);
            }

            for (final Settings set : this.serverCM.getSettings()) {
                final Enum<?> sideLocal = this.serverCM.getSetting(set);
                final Enum<?> sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    this.clientCM.putSetting(set, sideLocal);
                    for (final IContainerListener crafter : this.listeners) {
                        if (crafter instanceof ServerPlayerEntity) {
                            NetworkHandler.instance().sendTo(new ConfigValuePacket(set.name(), sideLocal.name()),
                                    (ServerPlayerEntity) crafter);
                        }
                    }
                }
            }

            if (!this.fluids.isEmpty()) {
                try {
                    final IItemList<IAEFluidStack> monitorCache = this.monitor.getStorageList();

                    final MEFluidInventoryUpdatePacket piu = new MEFluidInventoryUpdatePacket();

                    for (final IAEFluidStack is : this.fluids) {
                        final IAEFluidStack send = monitorCache.findPrecise(is);
                        if (send == null) {
                            is.setStackSize(0);
                            piu.appendFluid(is);
                        } else {
                            piu.appendFluid(send);
                        }
                    }

                    if (!piu.isEmpty()) {
                        this.fluids.resetStatus();

                        for (final Object c : this.listeners) {
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

            super.detectAndSendChanges();
        }
    }

    @Override
    public void doAction(ServerPlayerEntity player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        if (held.getCount() != 1) {
            // only support stacksize 1 for now
            return;
        }

        final LazyOptional<IFluidHandlerItem> fhOpt = FluidUtil.getFluidHandler(held);
        if (!fhOpt.isPresent()) {
            // only fluid handlers items
            return;
        }
        IFluidHandlerItem fh = fhOpt.orElse(null);

        if (action == InventoryAction.FILL_ITEM && this.clientRequestedTargetFluid != null) {
            final IAEFluidStack stack = this.clientRequestedTargetFluid.copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = fh.fill(stack.getFluidStack(), FluidAction.SIMULATE);
            stack.setStackSize(amountAllowed);

            // Check if we can pull out of the system
            final IAEFluidStack canPull = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack,
                    this.getActionSource(), Actionable.SIMULATE);
            if (canPull == null || canPull.getStackSize() < 1) {
                return;
            }

            // How much could fit into the container
            final int canFill = fh.fill(canPull.getFluidStack(), FluidAction.SIMULATE);
            if (canFill == 0) {
                return;
            }

            // Now actually pull out of the system
            stack.setStackSize(canFill);
            final IAEFluidStack pulled = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack,
                    this.getActionSource());
            if (pulled == null || pulled.getStackSize() < 1) {
                // Something went wrong
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                return;
            }

            // Actually fill
            final int used = fh.fill(pulled.getFluidStack(), FluidAction.EXECUTE);

            if (used != canFill) {
                AELog.error("Fluid item [%s] reported a different possible amount than it actually accepted.",
                        held.getDisplayName());
            }

            player.inventory.setItemStack(fh.getContainer());
            this.updateHeld(player);
        } else if (action == InventoryAction.EMPTY_ITEM) {
            // See how much we can drain from the item
            final FluidStack extract = fh.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            if (extract.isEmpty() || extract.getAmount() < 1) {
                return;
            }

            // Check if we can push into the system
            final IAEFluidStack notStorable = Platform.poweredInsert(this.getPowerSource(), this.monitor,
                    AEFluidStack.fromFluidStack(extract), this.getActionSource(), Actionable.SIMULATE);

            if (notStorable != null && notStorable.getStackSize() > 0) {
                final int toStore = (int) (extract.getAmount() - notStorable.getStackSize());
                final FluidStack storable = fh.drain(toStore, FluidAction.SIMULATE);

                if (storable.isEmpty() || storable.getAmount() == 0) {
                    return;
                } else {
                    extract.setAmount(storable.getAmount());
                }
            }

            // Actually drain
            final FluidStack drained = fh.drain(extract, FluidAction.EXECUTE);
            extract.setAmount(drained.getAmount());

            final IAEFluidStack notInserted = Platform.poweredInsert(this.getPowerSource(), this.monitor,
                    AEFluidStack.fromFluidStack(extract), this.getActionSource());

            if (notInserted != null && notInserted.getStackSize() > 0) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                        held.getDisplayName());
            }

            player.inventory.setItemStack(fh.getContainer());
            this.updateHeld(player);
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
        } catch (final Exception ignore) {
            // :P
        }
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }
}
