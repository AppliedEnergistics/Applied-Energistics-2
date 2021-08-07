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
import net.minecraft.world.entity.player.Player;

import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class ConfirmAutoCraftPacket extends BasePacket {

    private final int amount;
    private final boolean autoStart;

    public ConfirmAutoCraftPacket(final FriendlyByteBuf stream) {
        this.autoStart = stream.readBoolean();
        this.amount = stream.readInt();
    }

    public ConfirmAutoCraftPacket(final int craftAmt, final boolean autoStart) {
        this.amount = craftAmt;
        this.autoStart = autoStart;

        final FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBoolean(autoStart);
        data.writeInt(this.amount);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final Player player) {
        if (player.containerMenu instanceof CraftAmountMenu menu) {
            menu.confirm(amount, autoStart);
        }
    }
}
