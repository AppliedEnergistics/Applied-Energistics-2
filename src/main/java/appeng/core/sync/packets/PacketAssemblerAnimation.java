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


import appeng.api.storage.data.IAEItemStack;
import appeng.client.EffectType;
import appeng.core.AppEng;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;


public class PacketAssemblerAnimation extends AppEngPacket {

    private final int x;
    private final int y;
    private final int z;
    public final byte rate;
    public final IAEItemStack is;

    // automatic.
    public PacketAssemblerAnimation(final ByteBuf stream) throws IOException {
        this.x = stream.readInt();
        this.y = stream.readInt();
        this.z = stream.readInt();
        this.rate = stream.readByte();
        this.is = AEItemStack.fromPacket(stream);
    }

    // api
    public PacketAssemblerAnimation(final BlockPos pos, final byte rate, final IAEItemStack is) throws IOException {

        final ByteBuf data = Unpooled.buffer();

        data.writeInt(this.getPacketID());
        data.writeInt(this.x = pos.getX());
        data.writeInt(this.y = pos.getY());
        data.writeInt(this.z = pos.getZ());
        data.writeByte(this.rate = rate);
        is.writeToPacket(data);
        this.is = is;

        this.configureWrite(data);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void clientPacketData(final INetworkInfo network, final AppEngPacket packet, final EntityPlayer player) {
        final double d0 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
        final double d1 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);
        final double d2 = 0.5d;// + ((double) (Platform.getRandomFloat() - 0.5F) * 0.26D);

        AppEng.proxy.spawnEffect(EffectType.Assembler, player.getEntityWorld(), this.x + d0, this.y + d1, this.z + d2, this);
    }
}
