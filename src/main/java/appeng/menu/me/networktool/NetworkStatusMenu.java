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

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.client.gui.me.networktool.NetworkStatusScreen;
import appeng.core.sync.packets.NetworkStatusPacket;
import appeng.items.contents.NetworkToolMenuHost;
import appeng.menu.AEBaseMenu;
import appeng.menu.implementations.MenuTypeBuilder;

/**
 * @see NetworkStatusScreen
 */
public class NetworkStatusMenu extends AEBaseMenu {

    public static final MenuType<NetworkStatusMenu> NETWORK_TOOL_TYPE = MenuTypeBuilder
            .create(NetworkStatusMenu::new, NetworkToolMenuHost.class)
            .build("networkstatus");

    public static final MenuType<NetworkStatusMenu> CONTROLLER_TYPE = MenuTypeBuilder
            .create(NetworkStatusMenu::new, ControllerBlockEntity.class)
            .build("controller_networkstatus");

    private IGrid grid;
    private int delay = 40;

    public NetworkStatusMenu(int id, Inventory ip, NetworkToolMenuHost host) {
        super(NETWORK_TOOL_TYPE, id, ip, host);

        buildForGridHost(host.getGridHost());
    }

    public NetworkStatusMenu(int id, Inventory ip, ControllerBlockEntity host) {
        super(CONTROLLER_TYPE, id, ip, host);

        buildForGridHost(host);
    }

    private void buildForGridHost(IInWorldGridNodeHost gridHost) {
        if (gridHost != null) {
            for (var d : Direction.values()) {
                this.findNode(gridHost, d);
            }
        }

        if (this.grid == null && isServerSide()) {
            this.setValidMenu(false);
        }
    }

    private void findNode(IInWorldGridNodeHost host, Direction d) {
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
        if (isServerSide() && this.delay > 15 && this.grid != null) {
            this.delay = 0;

            NetworkStatus status = NetworkStatus.fromGrid(this.grid);

            sendPacketToClient(new NetworkStatusPacket(status));
        }
        super.broadcastChanges();
    }

}
