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

import appeng.container.me.crafting.CraftAmountContainer;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class ConfirmAutoCraftPacket extends BasePacket {

    private final int amount;
    private final boolean autoStart;

    public ConfirmAutoCraftPacket(final PacketBuffer stream) {
        this.autoStart = stream.readBoolean();
        this.amount = stream.readInt();
    }

    public ConfirmAutoCraftPacket(final int craftAmt, final boolean autoStart) {
        this.amount = craftAmt;
        this.autoStart = autoStart;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBoolean(autoStart);
        data.writeInt(this.amount);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        if (player.containerMenu instanceof CraftAmountContainer) {
            final CraftAmountContainer cca = (CraftAmountContainer) player.containerMenu;
            cca.confirm(amount, autoStart);
        }
    }
}
