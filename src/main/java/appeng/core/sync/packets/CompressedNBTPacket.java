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

package appeng.core.sync.packets;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.Unpooled;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.PacketByteBuf;

import appeng.client.gui.implementations.InterfaceTerminalScreen;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

//TODO, this is pointless, NBT is already compressed when written to a PacketBuffer.
public class CompressedNBTPacket extends BasePacket {

    // input.
    private final CompoundTag in;
    // output...
    private final PacketByteBuf data;
    private final GZIPOutputStream compressFrame;

    public CompressedNBTPacket(final PacketByteBuf stream) {
        this.data = null;
        this.compressFrame = null;

        try (DataInputStream inStream = new DataInputStream(new GZIPInputStream(new InputStream() {

            @Override
            public int read() {
                if (stream.readableBytes() <= 0) {
                    return -1;
                }

                return stream.readByte() & 0xff;
            }
        }))) {
            this.in = NbtIo.read(inStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress packet.", e);
        }
    }

    // api
    public CompressedNBTPacket(final CompoundTag din) throws IOException {

        this.data = new PacketByteBuf(Unpooled.buffer(2048));
        this.data.writeInt(this.getPacketID());

        this.in = din;

        this.compressFrame = new GZIPOutputStream(new OutputStream() {

            @Override
            public void write(final int value) {
                CompressedNBTPacket.this.data.writeByte(value);
            }
        });

        NbtIo.write(din, new DataOutputStream(this.compressFrame));
        this.compressFrame.close();

        this.configureWrite(this.data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = MinecraftClient.getInstance().currentScreen;

        if (gs instanceof InterfaceTerminalScreen) {
            ((InterfaceTerminalScreen) gs).postUpdate(this.in);
        }
    }
}
