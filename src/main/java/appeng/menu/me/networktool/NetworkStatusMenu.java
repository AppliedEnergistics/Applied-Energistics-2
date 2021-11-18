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

package appeng.menu.me.networktool;

import appeng.items.contents.NetworkToolMenuHost;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.core.sync.packets.NetworkStatusPacket;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;

/**
 * @see NetworkStatusScreen
 */
public class NetworkStatusMenu extends AEBaseMenu {

    public static final MenuType<NetworkStatusMenu> TYPE = MenuTypeBuilder
            .create(NetworkStatusMenu::new, NetworkToolMenuHost.class)
            .build("networkstatus");

    private IGrid grid;
    private int delay = 40;

    public NetworkStatusMenu(int id, Inventory ip, NetworkToolMenuHost host) {
        super(TYPE, id, ip, host);

        var gridHost = host.getGridHost();
        if (gridHost != null) {
            for (var d : Direction.values()) {
                this.findNode(gridHost, d);
            }
        }

        if (this.grid == null && isServer()) {
            this.setValidMenu(false);
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
