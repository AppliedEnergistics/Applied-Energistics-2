/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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
import net.minecraft.world.InteractionHand;

import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IMouseWheelItem;

public class MouseWheelPacket extends BasePacket {
    private boolean wheelUp;

    public MouseWheelPacket(FriendlyByteBuf byteBuf) {
        wheelUp = byteBuf.readBoolean();
    }

    public MouseWheelPacket(boolean wheelUp) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        data.writeBoolean(wheelUp);
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        var mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        var offHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if (mainHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(mainHand, wheelUp);
        } else if (offHand.getItem() instanceof IMouseWheelItem mouseWheelItem) {
            mouseWheelItem.onWheel(offHand, wheelUp);
        }
    }
}
