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

package appeng.fluids.container;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.BufferOverflowException;

import javax.annotation.Nonnull;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ExactFluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.misc.Ref;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

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
import appeng.container.implementations.ContainerHelper;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.ConfigValuePacket;
import appeng.core.sync.packets.MEFluidInventoryUpdatePacket;
import appeng.core.sync.packets.TargetFluidStackPacket;
import appeng.fluids.util.AEFluidStack;
import appeng.helpers.InventoryAction;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;

/**
 * @author BrockWS
 * @version rv6 - 12/05/2018
 * @since rv6 12/05/2018
 */
public class FluidTerminalContainer extends AEBaseContainer
        implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEFluidStack> {

    public static ScreenHandlerType<FluidTerminalContainer> TYPE;

    private static final ContainerHelper<FluidTerminalContainer, ITerminalHost> helper = new ContainerHelper<>(
            FluidTerminalContainer::new, ITerminalHost.class, SecurityPermissions.BUILD);

    public static FluidTerminalContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
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
        for (final ScreenHandlerListener c : this.getListeners()) {
            this.queueInventory(c);
        }
    }

    @Override
    public void addListener(ScreenHandlerListener listener) {
        super.addListener(listener);

        this.queueInventory(listener);
    }

    @Override
    public void close(final PlayerEntity player) {
        super.close(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    private void queueInventory(final ScreenHandlerListener c) {
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
                    && FluidVolume.areEqualExceptAmounts(stack.getFluidStack(), this.clientRequestedTargetFluid.getFluidStack())) {
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
    public void sendContentUpdates() {
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
                    for (final ScreenHandlerListener crafter : this.getListeners()) {
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

            super.sendContentUpdates();
        }
    }

    @Override
    public void doAction(ServerPlayerEntity player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getCursorStack();
        if (held.getCount() != 1) {
            // only support stacksize 1 for now
            return;
        }

        if (action == InventoryAction.FILL_ITEM && this.clientRequestedTargetFluid != null) {
            Ref<ItemStack> container = new Ref<>(held);
            FluidInsertable insertable = FluidAttributes.INSERTABLE.getFirstOrNull(container);
            if (insertable == null) {
                return; // Not a fillable item.
            }

            // Check how much we can store in the item
            FluidVolume volumeAllowed = insertable.attemptInsertion(this.clientRequestedTargetFluid.getFluidStack().withAmount(FluidAmount.MAX_VALUE), Simulation.SIMULATE);

            final IAEFluidStack stack = AEFluidStack.fromFluidVolume(this.clientRequestedTargetFluid.getFluidStack().withAmount(volumeAllowed.amount()), RoundingMode.DOWN);
            if (stack == null) {
                return; // Might be nothing allowed...
            }

            // Check if we can pull out of the system
            final IAEFluidStack canPull = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack,
                    this.getActionSource(), Actionable.SIMULATE);
            if (canPull == null || canPull.getStackSize() < 1) {
                return;
            }

            // How much could fit into the container
            volumeAllowed = insertable.attemptInsertion(canPull.getFluidStack(), Simulation.SIMULATE);
            if (volumeAllowed.isEmpty()) {
                return;
            }

            // Now actually pull out of the system
            stack.setStackSize(volumeAllowed.amount().asLong(1000, RoundingMode.DOWN));
            final IAEFluidStack pulled = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack,
                    this.getActionSource());
            if (pulled == null || pulled.getStackSize() < 1) {
                // Something went wrong
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                return;
            }

            // Actually fill
            final FluidVolume reallyFilled = insertable.attemptInsertion(pulled.getFluidStack(), Simulation.ACTION);

            if (!reallyFilled.amount().equals(volumeAllowed.amount())) {
                AELog.error("Fluid item [%s] reported a different possible amount than it actually accepted.",
                        held.getName());
            }

            player.inventory.setCursorStack(container.get());
            this.updateHeld(player);
        } else if (action == InventoryAction.EMPTY_ITEM) {
            Ref<ItemStack> container = new Ref<>(held);
            FluidExtractable extractable = FluidAttributes.EXTRACTABLE.getFirstOrNull(container);
            if (extractable == null) {
                return; // Not a drainable item.
            }

            // See how much we can drain from the item
            FluidVolume extract = extractable.attemptAnyExtraction(FluidAmount.MAX_VALUE, Simulation.SIMULATE);
            AEFluidStack aeStack = AEFluidStack.fromFluidVolume(extract, RoundingMode.DOWN);
            if (aeStack == null || aeStack.getStackSize() == 0) {
                return; // Not enough liquid in the container
            }

            // Check if we can push into the system
            final IAEFluidStack notStorable = Platform.poweredInsert(this.getPowerSource(), this.monitor,
                    aeStack, this.getActionSource(), Actionable.SIMULATE);

            // Adjust the amount if needed
            if (notStorable != null && notStorable.getStackSize() > 0) {
                final FluidAmount toStore = aeStack.getAmount().sub(notStorable.getAmount());
                extract = extractable.attemptExtraction(new ExactFluidFilter(aeStack.getFluid()), toStore, Simulation.SIMULATE);

                if (extract.isEmpty()) {
                    return;
                }
            }

            // Actually drain
            FluidVolume drained = extractable.extract(extract.getFluidKey(), extract.amount());
            aeStack = AEFluidStack.fromFluidVolume(drained, RoundingMode.DOWN);
            if (aeStack == null) {
                // TODO WARN
                return; // I guess the extractable decided to return a different amount upon execution
            }

            final IAEFluidStack notInserted = Platform.poweredInsert(this.getPowerSource(), this.monitor,
                    aeStack, this.getActionSource());

            if (notInserted != null && notInserted.getStackSize() > 0) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.",
                        held.getName());
            }

            player.inventory.setCursorStack(container.get());
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
