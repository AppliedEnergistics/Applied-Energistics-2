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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import appeng.client.gui.me.patternaccess.PatternAccessTermScreen;
import appeng.core.sync.BasePacket;

/**
 * Sends the content for the pattern access terminal GUI to the client.
 */
public class PatternAccessTerminalPacket extends BasePacket {

    // input.
    private boolean clearExistingData;
    private long inventoryId;
    private CompoundTag in;

    public PatternAccessTerminalPacket(FriendlyByteBuf stream) {
        this.clearExistingData = stream.readBoolean();
        this.inventoryId = stream.readLong();
        this.in = stream.readNbt();
    }

    // api
    private PatternAccessTerminalPacket(boolean clearExistingData, long inventoryId, CompoundTag din) {
        FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer(2048));
        data.writeInt(this.getPacketID());
        data.writeBoolean(clearExistingData);
        data.writeLong(inventoryId);
        data.writeNbt(din);
        this.configureWrite(data);
    }

    public static PatternAccessTerminalPacket clearExistingData() {
        return new PatternAccessTerminalPacket(true, -1, new CompoundTag());
    }

    public static PatternAccessTerminalPacket inventory(long id, CompoundTag data) {
        return new PatternAccessTerminalPacket(false, id, data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(Player player) {
        if (Minecraft.getInstance().screen instanceof PatternAccessTermScreen patternAccessTerminal) {
            patternAccessTerminal.postInventoryUpdate(this.clearExistingData, this.inventoryId, this.in);
        }
    }
}
