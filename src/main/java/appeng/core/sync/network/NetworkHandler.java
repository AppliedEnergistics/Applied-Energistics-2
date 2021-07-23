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

import net.minecraft.client.Minecraft;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import appeng.core.sync.BasePacket;

public class NetworkHandler {
    private static NetworkHandler instance;

    private final net.minecraft.resources.ResourceLocation myChannelName;

    private final IPacketHandler clientHandler;
    private final IPacketHandler serverHandler;

    public NetworkHandler(final net.minecraft.resources.ResourceLocation channelName) {
        EventNetworkChannel ec = NetworkRegistry.ChannelBuilder.named(myChannelName = channelName)
                .networkProtocolVersion(() -> "1").clientAcceptedVersions(s -> true).serverAcceptedVersions(s -> true)
                .eventNetworkChannel();
        ec.registerObject(this);

        this.clientHandler = DistExecutor.unsafeRunForDist(() -> ClientPacketHandler::new, () -> () -> null);
        this.serverHandler = this.createServerSide();
    }

    public static void init(final ResourceLocation channelName) {
        instance = new NetworkHandler(channelName);
    }

    public static NetworkHandler instance() {
        return instance;
    }

    private IPacketHandler createServerSide() {
        try {
            return new ServerPacketHandler();
        } catch (final Throwable t) {
            return null;
        }
    }

    @SubscribeEvent
    public void serverPacket(final NetworkEvent.ClientCustomPayloadEvent ev) {
        if (this.serverHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                ServerGamePacketListenerImpl netHandler = (ServerGamePacketListenerImpl) ctx.getNetworkManager().getPacketListener();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(
                        () -> this.serverHandler.onPacketData(null, netHandler, ev.getPayload(), netHandler.player));

            } catch (final RunningOnDifferentThreadException ignored) {

            }
        }
    }

    @SubscribeEvent
    public void clientPacket(final NetworkEvent.ServerCustomPayloadEvent ev) {
        if (ev instanceof NetworkEvent.ServerCustomPayloadLoginEvent) {
            return;
        }
        if (this.clientHandler != null) {
            try {
                NetworkEvent.Context ctx = ev.getSource().get();
                PacketListener netHandler = ctx.getNetworkManager().getPacketListener();
                ctx.setPacketHandled(true);
                ctx.enqueueWork(() -> this.clientHandler.onPacketData(null, netHandler, ev.getPayload(), null));
            } catch (final RunningOnDifferentThreadException ignored) {

            }
        }
    }

    public net.minecraft.resources.ResourceLocation getChannel() {
        return this.myChannelName;
    }

    public void sendToAll(final BasePacket message) {
        getServer().getPlayerList().broadcastAll(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendTo(final BasePacket message, final ServerPlayer player) {
        player.connection.send(message.toPacket(NetworkDirection.PLAY_TO_CLIENT));
    }

    public void sendToAllAround(final BasePacket message, final TargetPoint point) {
        Packet<?> pkt = message.toPacket(NetworkDirection.PLAY_TO_CLIENT);
        getServer().getPlayerList().broadcast(point.excluded, point.x, point.y, point.z, point.r2,
                point.world.dimension(), pkt);
    }

    public void sendToServer(final BasePacket message) {
        Minecraft.getInstance().getConnection().send(message.toPacket(NetworkDirection.PLAY_TO_SERVER));
    }

    private MinecraftServer getServer() {
        return LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
    }
}
