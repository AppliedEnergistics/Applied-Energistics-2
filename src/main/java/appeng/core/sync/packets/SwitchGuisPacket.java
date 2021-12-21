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

package appeng.core.sync.packets;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;

public class SwitchGuisPacket extends BasePacket {

    @Nullable
    private final MenuType<?> newGui;

    public SwitchGuisPacket(FriendlyByteBuf stream) {
        if (stream.readBoolean()) {
            this.newGui = Registry.MENU.get(stream.readResourceLocation());
        } else {
            this.newGui = null;
        }
    }

    // api
    private SwitchGuisPacket(@Nullable MenuType<? extends ISubMenu> newGui) {
        this.newGui = null;

        var data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        if (newGui != null) {
            data.writeBoolean(true);
            data.writeResourceLocation(Registry.MENU.getKey(newGui));
        } else {
            data.writeBoolean(false);
        }

        this.configureWrite(data);
    }

    /**
     * Opens a sub-menu for the current menu, which will allow the player to return to the previous menu.
     */
    public static SwitchGuisPacket openSubMenu(MenuType<? extends ISubMenu> menuType) {
        return new SwitchGuisPacket(menuType);
    }

    /**
     * Creates a packet that instructs the server to return to the parent menu for the currently opened sub-menu.
     */
    public static SwitchGuisPacket returnToParentMenu() {
        return new SwitchGuisPacket((MenuType<? extends ISubMenu>) null);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        if (this.newGui != null) {
            doOpenSubMenu(player);
        } else {
            doReturnToParentMenu(player);
        }
    }

    private void doOpenSubMenu(ServerPlayer player) {
        if (player.containerMenu instanceof AEBaseMenu bc) {
            var locator = bc.getLocator();
            if (locator != null) {
                MenuOpener.open(newGui, player, locator);
            }
        }
    }

    private void doReturnToParentMenu(ServerPlayer player) {
        if (player.containerMenu instanceof ISubMenu subMenu) {
            subMenu.getHost().returnToMainMenu(player, subMenu);
        }
    }
}
