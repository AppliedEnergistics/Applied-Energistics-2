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

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class MockExplosionPacket extends BasePacket {

    private final double x;
    private final double y;
    private final double z;

    public MockExplosionPacket(FriendlyByteBuf stream) {
        this.x = stream.readDouble();
        this.y = stream.readDouble();
        this.z = stream.readDouble();
    }

    // api
    public MockExplosionPacket(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeDouble(x);
        data.writeDouble(y);
        data.writeDouble(z);

        this.configureWrite(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(INetworkInfo network, Player player) {
        final Level level = player.getCommandSenderWorld();
        level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
    }
}
