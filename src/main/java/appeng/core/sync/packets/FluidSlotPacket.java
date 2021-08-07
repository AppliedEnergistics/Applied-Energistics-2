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

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.FluidSlotWidget;
import appeng.container.implementations.IFluidSyncContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.fluid.AEFluidStack;

/**
 * Similar to {@link ClientboundContainerSetSlotPacket}, but for fluids, and used in both directions (server->client and
 * client->server).
 * <p/>
 * The key used in for synchronization is {@link FluidSlotWidget#getId()}.
 * <p/>
 * The container on both sides of the synchronization must implement {@link IFluidSyncContainer}.
 */
public class FluidSlotPacket extends BasePacket {
    private final Map<Integer, IAEFluidStack> list;

    public FluidSlotPacket(final FriendlyByteBuf stream) {
        this.list = new HashMap<>();
        CompoundTag tag = stream.readNbt();

        for (final String key : tag.getAllKeys()) {
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

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeNbt(sendTag);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final Player player) {
        final AbstractContainerMenu c = player.containerMenu;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, Player player) {
        final AbstractContainerMenu c = player.containerMenu;
        if (c instanceof IFluidSyncContainer) {
            ((IFluidSyncContainer) c).receiveFluidSlots(this.list);
        }
    }
}
