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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.implementations.GuiInterfaceTerminal;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;

//TODO, this is pointless, NBT is already compressed when written to a PacketBuffer.
public class PacketCompressedNBT extends AppEngPacket {

    // input.
    private final CompoundNBT in;
    // output...
    private final PacketBuffer data;
    private final GZIPOutputStream compressFrame;

    public PacketCompressedNBT(final PacketBuffer stream) {
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
            this.in = CompressedStreamTools.read(inStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress packet.", e);
        }
    }

    // FIXME: this is pointless, PacketBuffer.writeNBT will already compress
    // api
    public PacketCompressedNBT(final CompoundNBT din) throws IOException {

        this.data = new PacketBuffer(Unpooled.buffer(2048));
        this.data.writeInt(this.getPacketID());

        this.in = din;

        this.compressFrame = new GZIPOutputStream(new OutputStream() {

            @Override
            public void write(final int value) {
                PacketCompressedNBT.this.data.writeByte(value);
            }
        });

        CompressedStreamTools.write(din, new DataOutputStream(this.compressFrame));
        this.compressFrame.close();

        this.configureWrite(this.data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof GuiInterfaceTerminal) {
            ((GuiInterfaceTerminal) gs).postUpdate(this.in);
        }
    }
}
