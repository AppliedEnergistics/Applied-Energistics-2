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
import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;

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

import appeng.api.storage.data.IAEItemStack;
import appeng.client.gui.implementations.CraftConfirmScreen;
import appeng.client.gui.implementations.CraftingCPUScreen;
import appeng.client.gui.me.NetworkStatusScreen;
import appeng.client.gui.me.items.MEMonitorableScreen;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;

public class MEInventoryUpdatePacket extends BasePacket {
    private static final int UNCOMPRESSED_PACKET_BYTE_LIMIT = 16 * 1024 * 1024;
    private static final int OPERATION_BYTE_LIMIT = 2 * 1024;

    // input.
    @Nullable
    private final List<IAEItemStack> list;
    // output...
    private final byte ref;

    @Nullable
    private final PacketBuffer data;

    private int writtenBytes = 0;
    private boolean empty = true;

    public MEInventoryUpdatePacket(final PacketBuffer stream) {
        this.data = null;
        this.list = new ArrayList<>();
        this.ref = stream.readByte();

        while (stream.readableBytes() > 0) {
            this.list.add(AEItemStack.fromPacket(stream));
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
        this.data = new PacketBuffer(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        this.data.writeInt(this.getPacketID());
        this.data.writeByte(this.ref);
        this.list = null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof CraftConfirmScreen) {
            ((CraftConfirmScreen) gs).postUpdate(this.list, this.ref);
        }

        if (gs instanceof CraftingCPUScreen) {
            ((CraftingCPUScreen<?>) gs).postUpdate(this.list, this.ref);
        }

        if (gs instanceof MEMonitorableScreen) {
            ((MEMonitorableScreen<?>) gs).postUpdate(this.list);
        }
    }

    @Nullable
    @Override
    public IPacket<?> toPacket(NetworkDirection direction) {
        this.configureWrite(this.data);
        return super.toPacket(direction);
    }

    public void appendItem(final IAEItemStack is) throws IOException, BufferOverflowException {
        final PacketBuffer tmp = new PacketBuffer(Unpooled.buffer(OPERATION_BYTE_LIMIT));
        is.writeToPacket(tmp);

        if (this.writtenBytes + tmp.readableBytes() > UNCOMPRESSED_PACKET_BYTE_LIMIT) {
            throw new BufferOverflowException();
        } else {
            this.writtenBytes += tmp.readableBytes();
            this.data.writeBytes(tmp.array(), 0, tmp.readableBytes());
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
