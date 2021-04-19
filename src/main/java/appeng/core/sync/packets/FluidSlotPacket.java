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
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.client.gui.widgets.FluidSlotWidget;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.util.AEFluidStack;

/**
 * Similar to {@link net.minecraft.network.play.server.SSetSlotPacket}, but for fluids, and used in both directions
 * (server->client and client->server).
 * <p/>
 * The key used in for synchronization is {@link FluidSlotWidget#getId()}.
 * <p/>
 * The container on both sides of the synchronization must implement {@link IFluidSyncContainer}.
 */
public class FluidSlotPacket extends BasePacket {
    private final Map<Integer, IAEFluidStack> list;

    public FluidSlotPacket(final PacketBuffer stream) {
        this.list = new HashMap<>();
        CompoundNBT tag = stream.readCompoundTag();

        for (final String key : tag.keySet()) {
            this.list.put(Integer.parseInt(key), AEFluidStack.fromNBT(tag.getCompound(key)));
        }
    }

    // api
    public FluidSlotPacket(final Map<Integer, IAEFluidStack> list) {
        this.list = list;
        final CompoundNBT sendTag = new CompoundNBT();
        for (Map.Entry<Integer, IAEFluidStack> fs : list.entrySet()) {
            final CompoundNBT tag = new CompoundNBT();
            if (fs.getValue() != null) {
                fs.getValue().writeToNBT(tag);
            }
            sendTag.put(fs.getKey().toString(), tag);
        }

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeCompoundTag(sendTag);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final PlayerEntity player) {
        final Container c = player.openContainer;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, PlayerEntity player) {
        final Container c = player.openContainer;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }
}
