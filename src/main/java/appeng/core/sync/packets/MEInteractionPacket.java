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
import net.minecraft.server.level.ServerPlayer;

import appeng.core.sync.BasePacket;
import appeng.helpers.InventoryAction;
import appeng.menu.me.common.IMEInteractionHandler;

/**
 * Packet sent by clients to interact with an ME inventory such as an item terminal.
 */
public class MEInteractionPacket extends BasePacket {

    private final int containerId;
    private final long serial;
    private final InventoryAction action;

    public MEInteractionPacket(FriendlyByteBuf buffer) {
        this.containerId = buffer.readInt();
        this.serial = buffer.readVarLong();
        this.action = buffer.readEnum(InventoryAction.class);
    }

    public MEInteractionPacket(int containerId, long serial, InventoryAction action) {
        this.containerId = containerId;
        this.serial = serial;
        this.action = action;

        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeInt(containerId);
        data.writeVarLong(serial);
        data.writeEnum(action);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(ServerPlayer player) {
        if (player.containerMenu instanceof IMEInteractionHandler handler) {
            // The open screen has changed since the client sent the packet
            if (player.containerMenu.containerId != containerId) {
                return;
            }

            handler.handleInteraction(serial, action);
        }
    }

}
