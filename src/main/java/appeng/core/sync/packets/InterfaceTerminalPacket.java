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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
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
    private CompoundNBT in;

    public InterfaceTerminalPacket(final PacketBuffer stream) {
        this.clearExistingData = stream.readBoolean();
        this.inventoryId = stream.readLong();
        this.in = stream.readCompoundTag();
    }

    // api
    private InterfaceTerminalPacket(boolean clearExistingData, long inventoryId, CompoundNBT din) {
        PacketBuffer data = new PacketBuffer(Unpooled.buffer(2048));
        data.writeInt(this.getPacketID());
        data.writeBoolean(clearExistingData);
        data.writeLong(inventoryId);
        data.writeCompoundTag(din);
        this.configureWrite(data);
    }

    public static InterfaceTerminalPacket clearExistingData() {
        return new InterfaceTerminalPacket(true, -1, new CompoundNBT());
    }

    public static InterfaceTerminalPacket inventory(long id, CompoundNBT data) {
        return new InterfaceTerminalPacket(false, id, data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(INetworkInfo network, PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().currentScreen;

        if (gs instanceof InterfaceTerminalScreen) {
            ((InterfaceTerminalScreen) gs).postInventoryUpdate(this.clearExistingData, this.inventoryId, this.in);
        }
    }
}
