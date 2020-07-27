/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.util.AEFluidStack;

public class FluidSlotPacket extends BasePacket {
    private final Map<Integer, IAEFluidStack> list;

    public FluidSlotPacket(final PacketByteBuf stream) {
        this.list = new HashMap<>();
        CompoundTag tag = stream.readCompoundTag();

        for (final String key : tag.getKeys()) {
            this.list.put(Integer.parseInt(key), AEFluidStack.fromNBT(tag.getCompound(key)));
        }
    }

    // api
    public FluidSlotPacket(final Map<Integer, IAEFluidStack> list) {
        this.list = list;
        final CompoundTag sendTag = new CompoundTag();
        for (Map.Entry<Integer, IAEFluidStack> fs : list.entrySet()) {
            final CompoundTag tag = new CompoundTag();
            if (fs.getValue() != null) {
                fs.getValue().writeToNBT(tag);
            }
            sendTag.put(fs.getKey().toString(), tag);
        }

        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeCompoundTag(sendTag);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final ScreenHandler c = player.currentScreenHandler;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        final ScreenHandler c = player.currentScreenHandler;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }
}
