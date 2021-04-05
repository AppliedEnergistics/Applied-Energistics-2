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

package appeng.core.sync.network;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;

import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public class ClientNetworkHandler extends ServerNetworkHandler {

    private final ClientSidePacketRegistry registry = ClientSidePacketRegistry.INSTANCE;

    public ClientNetworkHandler() {
        registry.register(BasePacket.CHANNEL, this::handlePacketFromServer);
    }

    @Override
    public void sendToServer(BasePacket message) {
        registry.sendToServer(message.toPacket(PacketDirection.SERVERBOUND));
    }

    private void handlePacketFromServer(PacketContext packetContext, PacketBuffer payload) {
        final int packetType = payload.readInt();
        final BasePacket packet = BasePacketHandler.PacketTypes.getPacket(packetType).parsePacket(payload);

        packetContext.getTaskQueue().execute(() -> {
            try {
                packet.clientPacketData(null, packetContext.getPlayer());
            } catch (final IllegalArgumentException e) {
                AELog.debug(e);
            }
        });
    }

}
