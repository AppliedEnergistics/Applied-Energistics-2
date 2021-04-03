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

package appeng.container.me;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.client.gui.me.NetworkStatusScreen;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.implementations.ContainerHelper;
import appeng.core.sync.packets.NetworkStatusPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.PacketBuffer;

/**
 * @see NetworkStatusScreen
 */
public class NetworkStatusContainer extends AEBaseContainer {

    public static ContainerType<NetworkStatusContainer> TYPE;

    private static final ContainerHelper<NetworkStatusContainer, INetworkTool> helper = new ContainerHelper<>(
            NetworkStatusContainer::new, INetworkTool.class);

    public static NetworkStatusContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private IGrid grid;
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

        if (this.grid == null && isServer()) {
            this.setValidContainer(false);
        }
    }

    private void findNode(final IGridHost host, final AEPartLocation d) {
        if (this.grid == null) {
            final IGridNode node = host.getGridNode(d);
            if (node != null) {
                this.grid = node.getGrid();
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        this.delay++;
        if (isServer() && this.delay > 15 && this.grid != null) {
            this.delay = 0;

            NetworkStatus status = NetworkStatus.fromGrid(this.grid);

            sendPacketToClient(new NetworkStatusPacket(status));
        }
        super.detectAndSendChanges();
    }

}
