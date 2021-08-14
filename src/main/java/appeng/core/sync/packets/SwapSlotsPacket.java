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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.menu.AEBaseMenu;

public class SwapSlotsPacket extends BasePacket {

    private final int slotA;
    private final int slotB;

    public SwapSlotsPacket(final FriendlyByteBuf stream) {
        this.slotA = stream.readInt();
        this.slotB = stream.readInt();
    }

    // api
    public SwapSlotsPacket(final int slotA, final int slotB) {
        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeInt(this.slotA = slotA);
        data.writeInt(this.slotB = slotB);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final ServerPlayer player) {
        if (player != null && player.containerMenu instanceof AEBaseMenu) {
            ((AEBaseMenu) player.containerMenu).swapSlotContents(this.slotA, this.slotB);
        }
    }
}
