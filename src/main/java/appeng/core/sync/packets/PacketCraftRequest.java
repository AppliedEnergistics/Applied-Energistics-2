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


import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.core.AELog;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.INetworkInfo;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.concurrent.Future;


public class PacketCraftRequest extends AppEngPacket {

    private final long amount;
    private final boolean heldShift;

    // automatic.
    public PacketCraftRequest(final ByteBuf stream) {
        this.heldShift = stream.readBoolean();
        this.amount = stream.readLong();
    }

    public PacketCraftRequest(final int craftAmt, final boolean shift) {
        this.amount = craftAmt;
        this.heldShift = shift;

        final ByteBuf data = Unpooled.buffer();

        data.writeInt(this.getPacketID());
        data.writeBoolean(shift);
        data.writeLong(this.amount);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        if (player.openContainer instanceof ContainerCraftAmount) {
            final ContainerCraftAmount cca = (ContainerCraftAmount) player.openContainer;
            final Object target = cca.getTarget();
            if (target instanceof IActionHost) {
                final IActionHost ah = (IActionHost) target;
                final IGridNode gn = ah.getActionableNode();
                if (gn == null) {
                    return;
                }

                final IGrid g = gn.getGrid();
                if (g == null || cca.getItemToCraft() == null) {
                    return;
                }

                cca.getItemToCraft().setStackSize(this.amount);

                Future<ICraftingJob> futureJob = null;
                try {
                    final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                    futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);

                    final ContainerOpenContext context = cca.getOpenContext();
                    if (context != null) {
                        final TileEntity te = context.getTile();
                        Platform.openGUI(player, te, cca.getOpenContext().getSide(), GuiBridge.GUI_CRAFTING_CONFIRM);

                        if (player.openContainer instanceof ContainerCraftConfirm) {
                            final ContainerCraftConfirm ccc = (ContainerCraftConfirm) player.openContainer;
                            ccc.setAutoStart(this.heldShift);
                            ccc.setJob(futureJob);
                            cca.detectAndSendChanges();
                        }
                    }
                } catch (final Throwable e) {
                    if (futureJob != null) {
                        futureJob.cancel(true);
                    }
                    AELog.debug(e);
                }
            }
        }
    }
}
