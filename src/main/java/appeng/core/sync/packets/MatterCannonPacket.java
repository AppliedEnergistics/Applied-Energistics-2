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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.client.render.effects.ParticleTypes;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class MatterCannonPacket extends BasePacket {

    private final double x;
    private final double y;
    private final double z;
    private final double dx;
    private final double dy;
    private final double dz;
    private final byte len;

    public MatterCannonPacket(FriendlyByteBuf stream) {
        this.x = stream.readFloat();
        this.y = stream.readFloat();
        this.z = stream.readFloat();
        this.dx = stream.readFloat();
        this.dy = stream.readFloat();
        this.dz = stream.readFloat();
        this.len = stream.readByte();
    }

    // api
    public MatterCannonPacket(double x, double y, double z, float dx, float dy,
            float dz, byte len) {
        final float dl = dx * dx + dy * dy + dz * dz;
        final float dlz = (float) Math.sqrt(dl);

        this.x = x;
        this.y = y;
        this.z = z;
        this.dx = dx / dlz;
        this.dy = dy / dlz;
        this.dz = dz / dlz;
        this.len = len;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeFloat((float) x);
        data.writeFloat((float) y);
        data.writeFloat((float) z);
        data.writeFloat((float) this.dx);
        data.writeFloat((float) this.dy);
        data.writeFloat((float) this.dz);
        data.writeByte(len);

        this.configureWrite(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(INetworkInfo network, Player player) {
        try {
            for (int a = 1; a < this.len; a++) {
                Minecraft.getInstance().particleEngine.createParticle(ParticleTypes.MATTER_CANNON, this.x + this.dx * a,
                        this.y + this.dy * a, this.z + this.dz * a, 0, 0, 0);
            }
        } catch (Exception ignored) {
        }
    }
}
