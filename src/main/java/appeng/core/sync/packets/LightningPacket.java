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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.AEConfig;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class LightningPacket extends BasePacket {

    private final double x;
    private final double y;
    private final double z;

    public LightningPacket(final PacketBuffer stream) {
        this.x = stream.readFloat();
        this.y = stream.readFloat();
        this.z = stream.readFloat();
    }

    // api
    public LightningPacket(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);

        this.configureWrite(data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        try {
            if (AEConfig.instance().isEnableEffects()) {
                player.getEntityWorld().addParticle(ParticleTypes.LIGHTNING, this.x, this.y, this.z, 0.0f, 0.0f,
                        0.0f);
            }
        } catch (final Exception ignored) {
        }
    }
}
