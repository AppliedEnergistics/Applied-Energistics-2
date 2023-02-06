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


import appeng.container.implementations.ContainerFluidInterfaceConfigurationTerminal;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.fluids.container.ContainerFluidInterface;
import appeng.fluids.container.ContainerFluidTerminal;
import appeng.fluids.container.ContainerWirelessFluidTerminal;
import appeng.fluids.util.AEFluidStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;


/**
 * @author BrockWS
 * @version rv6 - 23/05/2018
 * @since rv6 23/05/2018
 */
public class PacketTargetFluidStack extends AppEngPacket {
    private AEFluidStack stack;

    // automatic.
    public PacketTargetFluidStack(final ByteBuf stream) {
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
    public PacketTargetFluidStack(AEFluidStack stack) {

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
        if (player.openContainer instanceof ContainerFluidTerminal) {
            ((ContainerFluidTerminal) player.openContainer).setTargetStack(this.stack);
        } else if (player.openContainer instanceof ContainerWirelessFluidTerminal) {
            ((ContainerWirelessFluidTerminal) player.openContainer).setTargetStack(this.stack);
        } else if (player.openContainer instanceof ContainerFluidInterface) {
            ((ContainerFluidInterface) player.openContainer).setTargetStack(this.stack);
        } else if (player.openContainer instanceof ContainerFluidInterfaceConfigurationTerminal) {
            ((ContainerFluidInterfaceConfigurationTerminal) player.openContainer).setTargetStack(this.stack);
        }
    }
}
