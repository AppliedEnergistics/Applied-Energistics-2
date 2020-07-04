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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;

public class MEInventoryUpdatePacket extends BasePacket {
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
    private static final int OPERATION_BYTE_LIMIT = 2 * 1024;
    private static final int TEMP_BUFFER_SIZE = 1024;
    private static final int STREAM_MASK = 0xff;

    // input.
    @Nullable
    private final List<IAEItemStack> list;
    // output...
    private final byte ref;

    @Nullable
    private final PacketByteBuf data;
    @Nullable
    private final GZIPOutputStream compressFrame;

    private int writtenBytes = 0;
    private boolean empty = true;

    public MEInventoryUpdatePacket(final PacketByteBuf stream) {
        this.data = null;
        this.compressFrame = null;
        this.list = new ArrayList<>();
        this.ref = stream.readByte();

        // int originalBytes = stream.readableBytes();

        try (GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {
            @Override
            public int read() {
                if (stream.readableBytes() <= 0) {
                    return -1;
                }

                return stream.readByte() & STREAM_MASK;
            }
        })) {
            final PacketByteBuf uncompressed = new PacketByteBuf(Unpooled.buffer(stream.readableBytes()));
            final byte[] tmp = new byte[TEMP_BUFFER_SIZE];

            while (gzReader.available() != 0) {
                final int bytes = gzReader.read(tmp);

                if (bytes > 0) {
                    uncompressed.writeBytes(tmp, 0, bytes);
                }
            }

            while (uncompressed.readableBytes() > 0) {
                this.list.add(AEItemStack.fromPacket(uncompressed));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress packet.", e);
        }

        this.empty = this.list.isEmpty();
    }

    // api
    public MEInventoryUpdatePacket() throws IOException {
        this((byte) 0);
    }

    // api
    public MEInventoryUpdatePacket(final byte ref) throws IOException {
        this.ref = ref;
        this.data = new PacketByteBuf(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        this.data.writeInt(this.getPacketID());
        this.data.writeByte(this.ref);

        this.compressFrame = new GZIPOutputStream(new OutputStream() {
            @Override
            public void write(final int value) {
                MEInventoryUpdatePacket.this.data.writeByte(value);
            }
        });

        this.list = null;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = MinecraftClient.getInstance().currentScreen;

        throw new IllegalStateException();
// FIXME FABRIC        if (gs instanceof CraftConfirmScreen) {
// FIXME FABRIC            ((CraftConfirmScreen) gs).postUpdate(this.list, this.ref);
// FIXME FABRIC        }
// FIXME FABRIC
// FIXME FABRIC        if (gs instanceof CraftingCPUScreen) {
// FIXME FABRIC            ((CraftingCPUScreen<?>) gs).postUpdate(this.list, this.ref);
// FIXME FABRIC        }
// FIXME FABRIC
// FIXME FABRIC        if (gs instanceof MEMonitorableScreen) {
// FIXME FABRIC            ((MEMonitorableScreen<?>) gs).postUpdate(this.list);
// FIXME FABRIC        }
// FIXME FABRIC
// FIXME FABRIC        if (gs instanceof NetworkStatusScreen) {
// FIXME FABRIC            ((NetworkStatusScreen) gs).postUpdate(this.list);
// FIXME FABRIC        }
    }

    @Nullable
    @Override
    public Packet<?> toPacket(NetworkSide direction) {
        try {
            this.compressFrame.close();

            this.configureWrite(this.data);
            return super.toPacket(direction);
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return null;
    }

    public void appendItem(final IAEItemStack is) throws IOException, BufferOverflowException {
        final PacketByteBuf tmp = new PacketByteBuf(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        is.writeToPacket(tmp);

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
