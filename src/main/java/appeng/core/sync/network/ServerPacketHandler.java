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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.PacketBuffer;

import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

import appeng.core.sync.BasePacketHandler.PacketTypes;

public final class ServerPacketHandler extends BasePacketHandler implements IPacketHandler {

    @Override
    public void onPacketData(final INetworkInfo manager, final INetHandler handler, final PacketBuffer packet,
            final PlayerEntity player) {
        try {
            final int packetType = packet.readInt();
            final BasePacket pack = PacketTypes.getPacket(packetType).parsePacket(packet);
            pack.serverPacketData(manager, player);
        } catch (final IllegalArgumentException e) {
            AELog.warn(e);
        }
    }
}
