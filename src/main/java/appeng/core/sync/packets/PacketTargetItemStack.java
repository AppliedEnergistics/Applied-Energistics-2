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


import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;


public class PacketTargetItemStack extends AppEngPacket {
    private AEItemStack stack;

    // automatic.
    public PacketTargetItemStack(final ByteBuf stream) {
        try {
            if (stream.readableBytes() > 0) {
                this.stack = AEItemStack.fromPacket(stream);
            } else {
                this.stack = null;
            }
        } catch (Exception ex) {
            AELog.debug(ex);
            this.stack = null;
        }
    }

    // api
    public PacketTargetItemStack(AEItemStack stack) {

        this.stack = stack;

        final ByteBuf data = Unpooled.buffer();
        data.writeInt(this.getPacketID());
        if (stack != null) {
            try {
                stack.writeToPacket(data);
            } catch (Exception ex) {
                AELog.debug(ex);
            }
        }
        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        if (player.openContainer instanceof AEBaseContainer) {
            ((AEBaseContainer) player.openContainer).setTargetStack(this.stack);
        }
    }

}
