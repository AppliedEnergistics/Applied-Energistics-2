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

package appeng.container.me.networktool;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.core.sync.packets.NetworkStatusPacket;

/**
 * @see NetworkStatusScreen
 */
public class NetworkStatusContainer extends AEBaseContainer {

    public static final MenuType<NetworkStatusContainer> TYPE = ContainerTypeBuilder
            .create(NetworkStatusContainer::new, INetworkTool.class)
            .build("networkstatus");

    private IGrid grid;
    private int delay = 40;

    public NetworkStatusContainer(int id, Inventory ip, final INetworkTool te) {
        super(TYPE, id, ip, null);
        var host = te.getGridHost();

        if (host != null) {
            for (var d : Direction.values()) {
                this.findNode(host, d);
            }
        }

        if (this.grid == null && isServer()) {
            this.setValidContainer(false);
        }
    }

    private void findNode(final IInWorldGridNodeHost host, final Direction d) {
        if (this.grid == null) {
            final IGridNode node = host.getGridNode(d);
            if (node != null) {
                this.grid = node.getGrid();
            }
        }
    }

    @Override
    public void broadcastChanges() {
        this.delay++;
        if (isServer() && this.delay > 15 && this.grid != null) {
            this.delay = 0;

            NetworkStatus status = NetworkStatus.fromGrid(this.grid);

            sendPacketToClient(new NetworkStatusPacket(status));
        }
        super.broadcastChanges();
    }

}
