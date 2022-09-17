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


import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.GuiCraftConfirm;
import appeng.client.gui.implementations.GuiCraftingCPU;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.implementations.GuiNetworkStatus;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class PacketMEInventoryUpdate extends AppEngPacket {
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
    private final ByteBuf data;
    @Nullable
    private final GZIPOutputStream compressFrame;

    private int writtenBytes = 0;
    private boolean empty = true;

    // automatic.
    public PacketMEInventoryUpdate(final ByteBuf stream) throws IOException {
        this.data = null;
        this.compressFrame = null;
        this.list = new ArrayList<>();
        this.ref = stream.readByte();

        // int originalBytes = stream.readableBytes();

        try (GZIPInputStream gzReader = new GZIPInputStream(new InputStream() {
            @Override
            public int read() throws IOException {
                if (stream.readableBytes() <= 0) {
                    return -1;
                }

                return stream.readByte() & STREAM_MASK;
            }
        })) {
            final ByteBuf uncompressed = Unpooled.buffer(stream.readableBytes());
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
        }

        this.empty = this.list.isEmpty();

    }

    // api
    public PacketMEInventoryUpdate() throws IOException {
        this((byte) 0);
    }

    // api
    public PacketMEInventoryUpdate(final byte ref) throws IOException {
        this.ref = ref;
        this.data = Unpooled.buffer(OPERATION_BYTE_LIMIT);
        this.data.writeInt(this.getPacketID());
        this.data.writeByte(this.ref);

        this.compressFrame = new GZIPOutputStream(new OutputStream() {
            @Override
            public void write(final int value) throws IOException {
                PacketMEInventoryUpdate.this.data.writeByte(value);
            }
        });

        this.list = null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        final GuiScreen gs = Minecraft.getMinecraft().currentScreen;

        if (gs instanceof GuiCraftConfirm) {
            ((GuiCraftConfirm) gs).postUpdate(this.list, this.ref);
        }

        if (gs instanceof GuiCraftingCPU) {
            ((GuiCraftingCPU) gs).postUpdate(this.list, this.ref);
        }

        if (gs instanceof GuiMEMonitorable) {
            ((GuiMEMonitorable) gs).postUpdate(this.list);
        }

        if (gs instanceof GuiNetworkStatus) {
            ((GuiNetworkStatus) gs).postUpdate(this.list);
        }
    }

    @Nullable
    @Override
    public FMLProxyPacket getProxy() {
        try {
            this.compressFrame.close();

            this.configureWrite(this.data);
            return super.getProxy();
        } catch (final IOException e) {
            AELog.debug(e);
        }

        return null;
    }

    public void appendItem(final IAEItemStack is) throws IOException, BufferOverflowException {
        final ByteBuf tmp = Unpooled.buffer(OPERATION_BYTE_LIMIT);
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
