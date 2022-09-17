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

package appeng.core.sync;


import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.network.NetworkHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public abstract class AppEngPacket implements Packet {
    private PacketBuffer p;
    private PacketCallState caller;

    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a server side handler.");
    }

    public final int getPacketID() {
        return AppEngPacketHandlerBase.PacketTypes.getID(this.getClass()).ordinal();
    }

    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        throw new UnsupportedOperationException("This packet ( " + this.getPacketID() + " does not implement a client side handler.");
    }

    protected void configureWrite(final ByteBuf data) {
        data.capacity(data.readableBytes());
        this.p = new PacketBuffer(data);
    }

    public FMLProxyPacket getProxy() {
        if (this.p.array().length > 2 * 1024 * 1024) // 2k walking room :)
        {
            throw new IllegalArgumentException("Sorry AE2 made a " + this.p.array().length + " byte packet by accident!");
        }

        final FMLProxyPacket pp = new FMLProxyPacket(this.p, NetworkHandler.instance().getChannel());

        if (AEConfig.instance().isFeatureEnabled(AEFeature.PACKET_LOGGING)) {
            AELog.info(this.getClass().getName() + " : " + pp.payload().readableBytes());
        }

        return pp;
    }

    @Override
    public void readPacketData(final PacketBuffer buf) throws IOException {
        throw new RuntimeException("Not Implemented");
    }

    @Override
    public void writePacketData(final PacketBuffer buf) throws IOException {
        throw new RuntimeException("Not Implemented");
    }

    // TODO: Figure out why Forge/Minecraft on the server sets the stream data buffer to PooledUnsafeDirectByteBuf

    public ByteArrayInputStream getPacketByteArray(ByteBuf stream, int readerIndex, int readableBytes) {
        final ByteArrayInputStream bytes;
        if (stream.hasArray()) {
            bytes = new ByteArrayInputStream(stream.array(), readerIndex, readableBytes);
        } else {
            byte[] data = new byte[stream.capacity()];
            stream.getBytes(readerIndex, data, 0, readableBytes);
            bytes = new ByteArrayInputStream(data);
        }
        return bytes;
    }

    public ByteArrayInputStream getPacketByteArray(ByteBuf stream) {
        return this.getPacketByteArray(stream, 0, stream.readableBytes());
    }

    public void setCallParam(final PacketCallState call) {
        this.caller = call;
    }

    @Override
    public void processPacket(final INetHandler handler) {
        this.caller.call(this);
    }

}
