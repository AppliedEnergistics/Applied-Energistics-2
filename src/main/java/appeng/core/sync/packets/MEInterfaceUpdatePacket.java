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

import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.gui.implementations.InterfaceTerminalScreen;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class MEInterfaceUpdatePacket extends BasePacket {

    // input.
    private final CompoundNBT in;
    // output...
    private final PacketBuffer data;

    public MEInterfaceUpdatePacket(final PacketBuffer stream) {
        this.data = null;
        this.in = stream.readNbt();
    }

    // api
    public MEInterfaceUpdatePacket(final CompoundNBT din) throws IOException {
        this.in = null;
        this.data = new PacketBuffer(Unpooled.buffer(2048));
        this.data.writeInt(this.getPacketID());
        this.data.writeNbt(din);
        this.configureWrite(this.data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        final Screen gs = Minecraft.getInstance().screen;

        if (gs instanceof InterfaceTerminalScreen) {
            ((InterfaceTerminalScreen) gs).postUpdate(this.in);
        }
    }
}
