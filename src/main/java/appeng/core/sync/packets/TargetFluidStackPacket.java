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

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;

import appeng.container.implementations.FluidTerminalContainer;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.fluid.AEFluidStack;

/**
 * @author BrockWS
 * @version rv6 - 23/05/2018
 * @since rv6 23/05/2018
 */
public class TargetFluidStackPacket extends BasePacket {
    private AEFluidStack stack;

    public TargetFluidStackPacket(final PacketBuffer stream) {
        try {
            if (stream.readableBytes() > 0) {
                this.stack = (AEFluidStack) AEFluidStack.fromPacket(stream);
            } else {
                this.stack = null;
            }
        } catch (Exception ex) {
            AELog.debug(ex);
            this.stack = null;
        }
    }

    // api
    public TargetFluidStackPacket(AEFluidStack stack) {

        this.stack = stack;

        final PacketBuffer data = new PacketBuffer(Unpooled.buffer());
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
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        if (player.openContainer instanceof FluidTerminalContainer) {
            ((FluidTerminalContainer) player.openContainer).setTargetStack(this.stack);
        }
    }
}
