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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class NetworkStatusContainer extends AEBaseContainer {

    public static ScreenHandlerType<NetworkStatusContainer> TYPE;

    private static final ContainerHelper<NetworkStatusContainer, INetworkTool> helper = new ContainerHelper<>(
            NetworkStatusContainer::new, INetworkTool.class);

    public static NetworkStatusContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    @GuiSync(0)
    public long avgAddition;
    @GuiSync(1)
    public long powerUsage;
    @GuiSync(2)
    public long currentPower;
    @GuiSync(3)
    public long maxPower;
    private IGrid network;
    private int delay = 40;

    public NetworkStatusContainer(int id, PlayerInventory ip, final INetworkTool te) {
        super(TYPE, id, ip, null, null);
        final IGridHost host = te.getGridHost();

        if (host != null) {
            this.findNode(host, AEPartLocation.INTERNAL);
            for (final AEPartLocation d : AEPartLocation.SIDE_LOCATIONS) {
                this.findNode(host, d);
            }
        }

        if (this.network == null && Platform.isServer()) {
            this.setValidContainer(false);
        }
    }

    private void findNode(final IGridHost host, final AEPartLocation d) {
        if (this.network == null) {
            final IGridNode node = host.getGridNode(d);
            if (node != null) {
                this.network = node.getGrid();
            }
        }
    }

    @Override
    public void sendContentUpdates() {
        this.delay++;
        if (Platform.isServer() && this.delay > 15 && this.network != null) {
            this.delay = 0;

            final IEnergyGrid eg = this.network.getCache(IEnergyGrid.class);
            if (eg != null) {
                this.setAverageAddition((long) (100.0 * eg.getAvgPowerInjection()));
                this.setPowerUsage((long) (100.0 * eg.getAvgPowerUsage()));
                this.setCurrentPower((long) (100.0 * eg.getStoredPower()));
                this.setMaxPower((long) (100.0 * eg.getMaxStoredPower()));
            }

            try {
                final MEInventoryUpdatePacket piu = new MEInventoryUpdatePacket();

                for (final Class<? extends IGridHost> machineClass : this.network.getMachinesClasses()) {
                    final IItemList<IAEItemStack> list = AEApi.instance().storage()
                            .getStorageChannel(IItemStorageChannel.class).createList();
                    for (final IGridNode machine : this.network.getMachines(machineClass)) {
                        final IGridBlock blk = machine.getGridBlock();
                        final ItemStack is = blk.getMachineRepresentation();
                        if (!is.isEmpty()) {
                            final IAEItemStack ais = AEItemStack.fromItemStack(is);
                            ais.setStackSize(1);
                            ais.setCountRequestable((long) (blk.getIdlePowerUsage() * 100.0));
                            list.add(ais);
                        }
                    }

                    for (final IAEItemStack ais : list) {
                        piu.appendItem(ais);
                    }
                }

                for (final Object c : this.getListeners()) {
                    if (c instanceof PlayerEntity) {
                        NetworkHandler.instance().sendTo(piu, (ServerPlayerEntity) c);
                    }
                }
            } catch (final IOException e) {
                // :P
            }
        }
        super.sendContentUpdates();
    }

    public long getCurrentPower() {
        return this.currentPower;
    }

    private void setCurrentPower(final long currentPower) {
        this.currentPower = currentPower;
    }

    public long getMaxPower() {
        return this.maxPower;
    }

    private void setMaxPower(final long maxPower) {
        this.maxPower = maxPower;
    }

    public long getAverageAddition() {
        return this.avgAddition;
    }

    private void setAverageAddition(final long avgAddition) {
        this.avgAddition = avgAddition;
    }

    public long getPowerUsage() {
        return this.powerUsage;
    }

    private void setPowerUsage(final long powerUsage) {
        this.powerUsage = powerUsage;
    }
}
