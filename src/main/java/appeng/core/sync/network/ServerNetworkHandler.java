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

import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public class ServerNetworkHandler implements NetworkHandler {

    private final ServerSidePacketRegistry registry = ServerSidePacketRegistry.INSTANCE;

    public ServerNetworkHandler() {
        NetworkHandlerHolder.INSTANCE = this;
        registry.register(BasePacket.CHANNEL, this::handlePacketFromClient);
    }

    public void sendToAll(final BasePacket message) {
        MinecraftServer server = AppEng.instance().getServer();
        IPacket<?> packet = message.toPacket(PacketDirection.CLIENTBOUND);

        PlayerStream.all(server).forEach(player -> registry.sendToPlayer(player, packet));
    }

    public void sendTo(final BasePacket message, final ServerPlayerEntity player) {
        IPacket<?> packet = message.toPacket(PacketDirection.CLIENTBOUND);
        registry.sendToPlayer(player, packet);
    }

    public void sendToAllAround(final BasePacket message, final TargetPoint point) {
        IPacket<?> packet = message.toPacket(PacketDirection.CLIENTBOUND);
        PlayerStream.around(point.world, new Vector3d(point.x, point.y, point.z), point.radius).forEach(player -> {
            if (player != point.excluded) {
                ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet);
            }
        });
    }

    public void sendToDimension(final BasePacket message, final World world) {
        IPacket<?> packet = message.toPacket(PacketDirection.CLIENTBOUND);
        PlayerStream.world(world).forEach(player -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet));
    }

    @Override
    public void sendToServer(BasePacket message) {
        throw new IllegalStateException("Cannot send packets to the server when we're the server!");
    }

    private void handlePacketFromClient(PacketContext packetContext, PacketBuffer payload) {

        // Deserialize the packet on the netwhrok th
        PlayerEntity player = packetContext.getPlayer();
        if (!(player instanceof ServerPlayerEntity)) {
            AELog.warn("Received a packet for a non-server player entity!", player);
            return;
        }

        final int packetType = payload.readInt();
        final BasePacket pack = BasePacketHandler.PacketTypes.getPacket(packetType).parsePacket(payload);

        packetContext.getTaskQueue().execute(() -> {
            try {
                pack.serverPacketData(null, player);
            } catch (final IllegalArgumentException e) {
                AELog.debug(e);
            }
        });
    }

}
