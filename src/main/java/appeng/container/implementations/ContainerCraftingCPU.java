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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerHelper;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.ICustomNameObject;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;

public class ContainerCraftingCPU extends AEBaseContainer
        implements IMEMonitorHandlerReceiver<IAEItemStack>, ICustomNameObject {

    public static ContainerType<ContainerCraftingCPU> TYPE;

    private static final ContainerHelper<ContainerCraftingCPU, TileCraftingTile> helper = new ContainerHelper<>(
            ContainerCraftingCPU::new, TileCraftingTile.class, SecurityPermissions.CRAFT);

    private final IItemList<IAEItemStack> list = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private IGrid network;
    private CraftingCPUCluster monitor = null;
    private ITextComponent cpuName = null;

    @GuiSync(0)
    public long eta = -1;

    private ContainerCraftingCPU(int id, final PlayerInventory ip, final TileCraftingTile te) {
        this(TYPE, id, ip, te);
    }

    public ContainerCraftingCPU(ContainerType<?> containerType, int id, final PlayerInventory ip, final Object te) {
        super(containerType, id, ip, te);
        final IActionHost host = (IActionHost) (te instanceof IActionHost ? te : null);

        if (host != null && host.getActionableNode() != null) {
            this.setNetwork(host.getActionableNode().getGrid());
        }

        if (te instanceof TileCraftingTile) {
            this.setCPU((ICraftingCPU) ((IAEMultiBlock) te).getCluster());
        }

        if (this.getNetwork() == null && Platform.isServer()) {
            this.setValidContainer(false);
        }
    }

    public static ContainerCraftingCPU fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    protected void setCPU(final ICraftingCPU c) {
        if (c == this.getMonitor()) {
            return;
        }

        if (this.getMonitor() != null) {
            this.getMonitor().removeListener(this);
        }

        for (final Object g : this.listeners) {
            if (g instanceof PlayerEntity) {
                NetworkHandler.instance().sendTo(new PacketValueConfig("CraftingStatus", "Clear"),
                        (ServerPlayerEntity) g);
            }
        }

        if (c instanceof CraftingCPUCluster) {
            this.cpuName = c.getName();
            this.setMonitor((CraftingCPUCluster) c);
            this.list.resetStatus();
            this.getMonitor().getListOfItem(this.list, CraftingItemList.ALL);
            this.getMonitor().addListener(this, null);
            this.setEstimatedTime(0);
        } else {
            this.setMonitor(null);
            this.cpuName = null;
            this.setEstimatedTime(-1);
        }
    }

    public void cancelCrafting() {
        if (this.getMonitor() != null) {
            this.getMonitor().cancel();
        }
        this.setEstimatedTime(-1);
    }

    @Override
    public void removeListener(final IContainerListener c) {
        super.removeListener(c);

        if (this.listeners.isEmpty() && this.getMonitor() != null) {
            this.getMonitor().removeListener(this);
        }
    }

    @Override
    public void onContainerClosed(final PlayerEntity player) {
        super.onContainerClosed(player);
        if (this.getMonitor() != null) {
            this.getMonitor().removeListener(this);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer() && this.getMonitor() != null && !this.list.isEmpty()) {
            try {
                if (this.getEstimatedTime() >= 0) {
                    final long elapsedTime = this.getMonitor().getElapsedTime();
                    final double remainingItems = this.getMonitor().getRemainingItemCount();
                    final double startItems = this.getMonitor().getStartItemCount();
                    final long eta = (long) (elapsedTime / Math.max(1d, (startItems - remainingItems))
                            * remainingItems);
                    this.setEstimatedTime(eta);
                }

                final PacketMEInventoryUpdate a = new PacketMEInventoryUpdate((byte) 0);
                final PacketMEInventoryUpdate b = new PacketMEInventoryUpdate((byte) 1);
                final PacketMEInventoryUpdate c = new PacketMEInventoryUpdate((byte) 2);

                for (final IAEItemStack out : this.list) {
                    a.appendItem(this.getMonitor().getItemStack(out, CraftingItemList.STORAGE));
                    b.appendItem(this.getMonitor().getItemStack(out, CraftingItemList.ACTIVE));
                    c.appendItem(this.getMonitor().getItemStack(out, CraftingItemList.PENDING));
                }

                this.list.resetStatus();

                for (final Object g : this.listeners) {
                    if (g instanceof PlayerEntity) {
                        if (!a.isEmpty()) {
                            NetworkHandler.instance().sendTo(a, (ServerPlayerEntity) g);
                        }

                        if (!b.isEmpty()) {
                            NetworkHandler.instance().sendTo(b, (ServerPlayerEntity) g);
                        }

                        if (!c.isEmpty()) {
                            NetworkHandler.instance().sendTo(c, (ServerPlayerEntity) g);
                        }
                    }
                }
            } catch (final IOException e) {
                // :P
            }
        }
        super.detectAndSendChanges();
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final IActionSource actionSource) {
        for (IAEItemStack is : change) {
            is = is.copy();
            is.setStackSize(1);
            this.list.add(is);
        }
    }

    @Override
    public void onListUpdate() {

    }

    @Override
    public ITextComponent getCustomInventoryName() {
        return this.cpuName;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return this.cpuName != null;
    }

    public long getEstimatedTime() {
        return this.eta;
    }

    private void setEstimatedTime(final long eta) {
        this.eta = eta;
    }

    CraftingCPUCluster getMonitor() {
        return this.monitor;
    }

    private void setMonitor(final CraftingCPUCluster monitor) {
        this.monitor = monitor;
    }

    IGrid getNetwork() {
        return this.network;
    }

    private void setNetwork(final IGrid network) {
        this.network = network;
    }
}
