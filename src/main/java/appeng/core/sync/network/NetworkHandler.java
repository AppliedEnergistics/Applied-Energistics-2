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

import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.DistExecutor;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PlayNetworkDirection;
import net.neoforged.neoforge.network.event.EventNetworkChannel;

import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.BasePacketHandler;

public class NetworkHandler {
    private static NetworkHandler instance;

    private final ResourceLocation myChannelName;

    private final Consumer<BasePacket> clientHandler;

    public NetworkHandler(final ResourceLocation channelName) {
        EventNetworkChannel ec = NetworkRegistry.ChannelBuilder.named(myChannelName = channelName)
                .networkProtocolVersion(() -> "1").clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true)
                .eventNetworkChannel();
        ec.registerObject(this);

        this.clientHandler = DistExecutor.unsafeRunForDist(() -> () -> ClientPacketHandler::onPacketData,
                () -> () -> pkt -> {
                });
    }

    public static void init(final ResourceLocation channelName) {
        instance = new NetworkHandler(channelName);
    }

    public static NetworkHandler instance() {
        return instance;
    }

    @SubscribeEvent
    public void serverPacket(final NetworkEvent.ClientCustomPayloadEvent ev) {
        try {
            var ctx = ev.getSource();
            ctx.setPacketHandled(true);
            var packet = deserializePacket(ev.getPayload());
            var player = ctx.getSender();
            ctx.enqueueWork(
                    () -> {
                        try {
                            packet.serverPacketData(player);
                        } catch (final IllegalArgumentException e) {
                            AELog.warn(e);
                        }
                    });
        } catch (final RunningOnDifferentThreadException ignored) {

        }
    }

    @SubscribeEvent
    public void clientPacket(NetworkEvent.ServerCustomPayloadEvent ev) {
        if (ev instanceof NetworkEvent.ServerCustomPayloadLoginEvent) {
            return;
        }
        if (this.clientHandler != null) {
            try {
                var ctx = ev.getSource();
                ctx.setPacketHandled(true);

                var packet = deserializePacket(ev.getPayload());

                ctx.enqueueWork(() -> this.clientHandler.accept(packet));
            } catch (RunningOnDifferentThreadException ignored) {

            }
        }
    }

    private static BasePacket deserializePacket(FriendlyByteBuf payload) {
        var packetId = payload.readInt();
        return BasePacketHandler.PacketTypes.getPacket(packetId).parsePacket(payload);
    }

    public ResourceLocation getChannel() {
        return this.myChannelName;
    }

    public void sendToAll(BasePacket message) {
        var server = AppEng.instance().getCurrentServer();
        if (server != null) {
            server.getPlayerList().broadcastAll(message.toPacket(PlayNetworkDirection.PLAY_TO_CLIENT));
        }
    }

    public void sendTo(BasePacket message, ServerPlayer player) {
        player.connection.send(message.toPacket(PlayNetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendToAllAround(BasePacket message, TargetPoint point) {
        var server = AppEng.instance().getCurrentServer();
        if (server != null) {
            Packet<?> pkt = message.toPacket(PlayNetworkDirection.PLAY_TO_CLIENT);
            server.getPlayerList().broadcast(point.excluded, point.x, point.y, point.z, point.r2,
                    point.level.dimension(), pkt);
        }
    }

    public void sendToServer(BasePacket message) {
        Minecraft.getInstance().getConnection().send(message.toPacket(PlayNetworkDirection.PLAY_TO_SERVER));
    }
}
