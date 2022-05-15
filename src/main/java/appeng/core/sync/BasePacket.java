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

package appeng.core.sync;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import appeng.core.AEConfig;
import appeng.core.AELog;

public abstract class BasePacket {

    // KEEP THIS SHORT. It's serialized as a string!
    public static final ResourceLocation CHANNEL = new ResourceLocation("ae2:m");

    private FriendlyByteBuf p;

    public void serverPacketData(ServerPlayer player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a server side handler.");
    }

    public final int getPacketID() {
        return BasePacketHandler.PacketTypes.getID(this.getClass()).ordinal();
    }

    public void clientPacketData(Player player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a client side handler.");
    }

    protected void configureWrite(FriendlyByteBuf data) {
        data.capacity(data.readableBytes());
        this.p = data;
    }

    public Packet<?> toPacket(PacketFlow direction) {
        if (this.p.array().length > 2 * 1024 * 1024) // 2k walking room :)
        {
            throw new IllegalArgumentException(
                    "Sorry AE2 made a " + this.p.array().length + " byte packet by accident!");
        }

        if (AEConfig.instance().isPacketLogEnabled()) {
            AELog.info(this.getClass().getName() + " : " + p.readableBytes());
        }

        if (direction == PacketFlow.SERVERBOUND) {
            return ClientPlayNetworking.createC2SPacket(CHANNEL, this.p);
        } else {
            return ServerPlayNetworking.createS2CPacket(CHANNEL, this.p);
        }
    }
}
