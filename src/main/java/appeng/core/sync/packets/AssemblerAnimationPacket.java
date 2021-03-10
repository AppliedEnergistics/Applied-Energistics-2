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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.tile.crafting.AssemblerAnimationStatus;
import appeng.tile.crafting.MolecularAssemblerTileEntity;
import appeng.util.item.AEItemStack;

public class AssemblerAnimationPacket extends BasePacket {

    private final BlockPos pos;
    public final byte rate;
    public final IAEItemStack is;

    public AssemblerAnimationPacket(final PacketBuffer stream) {
        this.pos = stream.readBlockPos();
        this.rate = stream.readByte();
        this.is = AEItemStack.fromPacket(stream);
    }

    // api
    public AssemblerAnimationPacket(final BlockPos pos, final byte rate, final IAEItemStack is) {

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBlockPos(this.pos = pos);
        data.writeByte(this.rate = rate);
        is.writeToPacket(data);
        this.is = is;

        this.configureWrite(data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void clientPacketData(final INetworkInfo network, final PlayerEntity player) {
        TileEntity te = player.getCommandSenderWorld().getBlockEntity(pos);
        if (te instanceof MolecularAssemblerTileEntity) {
            MolecularAssemblerTileEntity ma = (MolecularAssemblerTileEntity) te;
            ma.setAnimationStatus(new AssemblerAnimationStatus(rate, is.asItemStackRepresentation()));
        }
    }
}
