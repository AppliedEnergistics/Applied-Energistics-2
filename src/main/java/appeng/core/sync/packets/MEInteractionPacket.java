/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.menu.me.common.IMEInteractionHandler;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.InventoryAction;

/**
 * Packet sent by clients to interact with an ME inventory such as an item terminal.
 */
public class MEInteractionPacket extends BasePacket {

    private final int windowId;
    private final long serial;
    private final InventoryAction action;

    public MEInteractionPacket(FriendlyByteBuf buffer) {
        this.windowId = buffer.readInt();
        this.serial = buffer.readVarLong();
        this.action = buffer.readEnum(InventoryAction.class);
    }

    public MEInteractionPacket(int windowId, long serial, InventoryAction action) {
        this.windowId = windowId;
        this.serial = serial;
        this.action = action;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeInt(windowId);
        data.writeVarLong(serial);
        data.writeEnum(action);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, Player player) {
        if (player.containerMenu instanceof IMEInteractionHandler) {
            // The open screen has changed since the client sent the packet
            if (player.containerMenu.containerId != windowId) {
                return;
            }

            IMEInteractionHandler handler = (IMEInteractionHandler) player.containerMenu;
            handler.handleInteraction(serial, action);
        }
    }

}
