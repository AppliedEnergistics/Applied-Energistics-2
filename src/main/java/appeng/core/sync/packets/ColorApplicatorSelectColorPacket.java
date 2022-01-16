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

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.items.tools.powered.ColorApplicatorItem;

/**
 * Switches the color of any held color applicator to the desired color.
 */
public class ColorApplicatorSelectColorPacket extends BasePacket {
    @Nullable
    private AEColor color;

    public ColorApplicatorSelectColorPacket(FriendlyByteBuf stream) {
        if (stream.readBoolean()) {
            this.color = stream.readEnum(AEColor.class);
        } else {
            this.color = null;
        }
    }

    // api
    public ColorApplicatorSelectColorPacket(@Nullable AEColor color) {
        var data = new FriendlyByteBuf(Unpooled.buffer());
        data.writeInt(this.getPacketID());
        if (color != null) {
            data.writeBoolean(true);
            data.writeEnum(color);
        } else {
            data.writeBoolean(false);
        }
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(INetworkInfo manager, ServerPlayer player) {
        switchColor(player.getMainHandItem(), color);
        switchColor(player.getOffhandItem(), color);
    }

    private static void switchColor(ItemStack stack, AEColor color) {
        if (!stack.isEmpty() && stack.getItem() instanceof ColorApplicatorItem colorApplicator) {
            colorApplicator.setActiveColor(stack, color);
        }
    }
}
