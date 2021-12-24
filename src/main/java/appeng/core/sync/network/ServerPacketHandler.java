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

package appeng.core.sync.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public final class ServerPacketHandler extends BasePacketHandler implements IPacketHandler {

    @Override
    public void onPacketData(final INetworkInfo manager, final PacketListener handler, final FriendlyByteBuf packet,
            final Player player) {
        try {
            final int packetType = packet.readInt();
            final BasePacket pack = PacketTypes.getPacket(packetType).parsePacket(packet);
            pack.serverPacketData(manager, (ServerPlayer) player);
        } catch (final IllegalArgumentException e) {
            AELog.warn(e);
        }
    }
}
