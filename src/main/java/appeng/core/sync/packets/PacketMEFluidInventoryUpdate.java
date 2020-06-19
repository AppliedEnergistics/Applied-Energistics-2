/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkDirection;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.client.gui.GuiFluidTerminal;
import appeng.fluids.util.AEFluidStack;

/**
 * @author BrockWS
 * @version rv6 - 22/05/2018
 * @since rv6 22/05/2018
 */
public class PacketMEFluidInventoryUpdate extends AppEngPacket {
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
    private static final int OPERATION_BYTE_LIMIT = 2 * 1024;
    private static final int TEMP_BUFFER_SIZE = 1024;
    private static final int STREAM_MASK = 0xff;

    // input.
    @Nullable
    private final List<IAEFluidStack> list;
    // output...
    private final byte ref;

    @Nullable
    private final PacketBuffer data;
    @Nullable
    private final GZIPOutputStream compressFrame;

    private int writtenBytes = 0;
    private boolean empty = true;

    public PacketMEFluidInventoryUpdate(final PacketBuffer stream) {
        this.data = null;
        this.compressFrame = null;
        this.list = new LinkedList<>();
        this.ref = stream.readByte();

        try (final GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {
            @Override
            public int read() {
                if (stream.readableBytes() <= 0) {
                    return -1;
                }

                return stream.readByte() & STREAM_MASK;
            }
        })) {

            final PacketBuffer uncompressed = new PacketBuffer(Unpooled.buffer(stream.readableBytes()));
            final byte[] tmp = new byte[TEMP_BUFFER_SIZE];

            while (gzReader.available() != 0) {
                final int bytes = gzReader.read(tmp);

                if (bytes > 0) {
                    uncompressed.writeBytes(tmp, 0, bytes);
                }
            }

            while (uncompressed.readableBytes() > 0) {
                this.list.add(AEFluidStack.fromPacket(uncompressed));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress packet.", e);
        }

        this.empty = this.list.isEmpty();
    }

    // api
    public PacketMEFluidInventoryUpdate() throws IOException {
        this((byte) 0);
    }

    // api
    public PacketMEFluidInventoryUpdate(final byte ref) throws IOException {
        this.ref = ref;
        this.data = new PacketBuffer(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        this.data.writeInt(this.getPacketID());
        this.data.writeByte(this.ref);

        this.compressFrame = new GZIPOutputStream(new OutputStream() {
            @Override
            public void write(final int value) {
                PacketMEFluidInventoryUpdate.this.data.writeByte(value);
            }
        });

        this.list = null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof GuiFluidTerminal) {
            ((GuiFluidTerminal) gs).postUpdate(this.list);
        }
    }

    @Nullable
    @Override
    public IPacket<?> toPacket(NetworkDirection direction) {
        try {
            this.compressFrame.close();

            this.configureWrite(this.data);
            return super.toPacket(direction);
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return null;
    }

    public void appendFluid(final IAEFluidStack fs) throws IOException, BufferOverflowException {
        final PacketBuffer tmp = new PacketBuffer(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        fs.writeToPacket(tmp);

        this.compressFrame.flush();
        if (this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT) {
            throw new BufferOverflowException();
        } else {
            this.writtenBytes += tmp.readableBytes();
            this.compressFrame.write(tmp.array(), 0, tmp.readableBytes());
            this.empty = false;
        }
    }

    public int getLength() {
        return this.data.readableBytes();
    }

    public boolean isEmpty() {
        return this.empty;
    }
}
