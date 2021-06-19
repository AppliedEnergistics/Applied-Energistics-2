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

import appeng.core.AppEngClient;
import io.netty.buffer.Unpooled;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.util.AEPartLocation;
import appeng.client.render.effects.EnergyParticleData;
import appeng.core.AppEng;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;

/**
 * Plays a transition particle effect into the supplied direction. Used primarily by annihilation planes.
 */
public class ItemTransitionEffectPacket extends BasePacket {

    private final double x;
    private final double y;
    private final double z;
    private final AEPartLocation d;

    public ItemTransitionEffectPacket(double x, double y, double z, AEPartLocation direction) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.d = direction;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeByte(this.d.ordinal());

        this.configureWrite(data);
    }

    public ItemTransitionEffectPacket(final PacketBuffer stream) {
        this.x = stream.readFloat();
        this.y = stream.readFloat();
        this.z = stream.readFloat();
        this.d = AEPartLocation.fromOrdinal(stream.readByte());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        EnergyParticleData data = new EnergyParticleData(true, this.d);
        for (int zz = 0; zz < 8; zz++) {
            if (AppEngClient.instance().shouldAddParticles(Platform.getRandom())) {
                // Distribute the spawn point around the item's position
                double x = this.x + Platform.getRandomFloat() * 0.5 - 0.25;
                double y = this.y + Platform.getRandomFloat() * 0.5 - 0.25;
                double z = this.z + Platform.getRandomFloat() * 0.5 - 0.25;
                double speedX = 0.1f * this.d.xOffset;
                double speedY = 0.1f * this.d.yOffset;
                double speedZ = 0.1f * this.d.zOffset;
                Minecraft.getInstance().particles.addParticle(data, x, y, z, speedX, speedY, speedZ);
            }
        }
    }

}
