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
    private final boolean fullUpdate;
    private final CompoundNBT in;

    public InterfaceTerminalPacket(final PacketBuffer stream) {
        this.fullUpdate = stream.readBoolean();
        this.in = stream.readNbt();
    }

    // api
    public InterfaceTerminalPacket(boolean fullUpdate, CompoundNBT din) {
        this.fullUpdate = false;
        this.in = null;
        PacketBuffer data = new PacketBuffer(Unpooled.buffer(2048));
        data.writeInt(this.getPacketID());
        data.writeBoolean(fullUpdate);
        data.writeNbt(din);
        this.configureWrite(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().screen;

        if (gs instanceof InterfaceTerminalScreen) {
            ((InterfaceTerminalScreen) gs).postUpdate(fullUpdate, this.in);
        }
    }
}
