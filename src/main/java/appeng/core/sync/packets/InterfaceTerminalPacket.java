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

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.me.interfaceterminal.InterfaceTerminalScreen;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

/**
 * Sends the content for the interface terminal GUI to the client.
 */
public class InterfaceTerminalPacket extends BasePacket {

    // input.
    private boolean clearExistingData;
    private long inventoryId;
    private CompoundTag in;

    public InterfaceTerminalPacket(final FriendlyByteBuf stream) {
        this.clearExistingData = stream.readBoolean();
        this.inventoryId = stream.readLong();
        this.in = stream.readNbt();
    }

    // api
    private InterfaceTerminalPacket(boolean clearExistingData, long inventoryId, CompoundTag din) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer(2048));
        data.writeInt(this.getPacketID());
        data.writeBoolean(clearExistingData);
        data.writeLong(inventoryId);
        data.writeNbt(din);
        this.configureWrite(data);
    }

    public static InterfaceTerminalPacket clearExistingData() {
        return new InterfaceTerminalPacket(true, -1, new CompoundTag());
    }

    public static InterfaceTerminalPacket inventory(long id, CompoundTag data) {
        return new InterfaceTerminalPacket(false, id, data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(INetworkInfo network, Player player) {
        if (Minecraft.getInstance().screen instanceof InterfaceTerminalScreen interfaceTerminal) {
            interfaceTerminal.postInventoryUpdate(this.clearExistingData, this.inventoryId, this.in);
        }
    }
}
