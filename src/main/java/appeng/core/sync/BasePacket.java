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

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import appeng.api.features.AEFeature;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.network.INetworkInfo;

public abstract class BasePacket {

    /**
     * Sadly {@link PacketBuffer#readString()} gets inlined by Proguard which means
     * it's not available on the Server. This field has the default string length
     * that is used for writeString, which then also should be used for readString
     * when it has no special length requirements.
     */
    public static final int MAX_STRING_LENGTH = 32767;

    // KEEP THIS SHORT. It's serialized as a string!
    public static final Identifier CHANNEL = new Identifier("ae2:m");

    private PacketByteBuf p;

    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a server side handler.");
    }

    public final int getPacketID() {
        return BasePacketHandler.PacketTypes.getID(this.getClass()).ordinal();
    }

    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        throw new UnsupportedOperationException(
                "This packet ( " + this.getPacketID() + " does not implement a client side handler.");
    }

    protected void configureWrite(final PacketByteBuf data) {
        data.capacity(data.readableBytes());
        this.p = data;
    }

    public Packet<?> toPacket(NetworkSide direction) {
        if (this.p.array().length > 2 * 1024 * 1024) // 2k walking room :)
        {
            throw new IllegalArgumentException(
                    "Sorry AE2 made a " + this.p.array().length + " byte packet by accident!");
        }

        if (AEConfig.instance().isFeatureEnabled(AEFeature.PACKET_LOGGING)) {
            AELog.info(this.getClass().getName() + " : " + p.readableBytes());
        }

        if (direction == NetworkSide.SERVERBOUND) {
            return ClientSidePacketRegistry.INSTANCE.toPacket(CHANNEL, this.p);
        } else {
            return ServerSidePacketRegistry.INSTANCE.toPacket(CHANNEL, this.p);
        }
    }
}
