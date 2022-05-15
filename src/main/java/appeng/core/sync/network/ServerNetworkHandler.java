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

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public class ServerNetworkHandler implements NetworkHandler {

    public ServerNetworkHandler() {
        NetworkHandlerHolder.INSTANCE = this;
        ServerPlayNetworking.registerGlobalReceiver(BasePacket.CHANNEL, this::handlePacketFromClient);
    }

    public void sendToAll(BasePacket message) {
        MinecraftServer server = AppEng.instance().getCurrentServer();
        if (server != null) {
            var packet = message.toPacket(PacketFlow.CLIENTBOUND);

            PlayerLookup.all(server).forEach(player -> ServerPlayNetworking.getSender(player).sendPacket(packet));
        }
    }

    public void sendTo(BasePacket message, ServerPlayer player) {
        var packet = message.toPacket(PacketFlow.CLIENTBOUND);
        ServerPlayNetworking.getSender(player).sendPacket(packet);
    }

    public void sendToAllAround(BasePacket message, TargetPoint point) {
        var packet = message.toPacket(PacketFlow.CLIENTBOUND);
        PlayerLookup.around((ServerLevel) point.level, new Vec3(point.x, point.y, point.z), point.radius)
                .forEach(player -> {
                    if (player != point.excluded) {
                        ServerPlayNetworking.getSender(player).sendPacket(packet);
                    }
                });
    }

    public void sendToDimension(BasePacket message, Level world) {
        var packet = message.toPacket(PacketFlow.CLIENTBOUND);
        PlayerLookup.world((ServerLevel) world)
                .forEach(player -> ServerPlayNetworking.getSender(player).sendPacket(packet));
    }

    @Override
    public void sendToServer(BasePacket message) {
        throw new IllegalStateException("Cannot send packets to the server when we're the server!");
    }

    private void handlePacketFromClient(MinecraftServer server, ServerPlayer player,
            ServerGamePacketListenerImpl handler, FriendlyByteBuf payload, PacketSender responseSender) {
        final int packetType = payload.readInt();
        final BasePacket pack = BasePacketHandler.PacketTypes.getPacket(packetType).parsePacket(payload);

        server.execute(() -> {
            pack.serverPacketData(player);
        });
    }

}
