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

import java.util.concurrent.Future;

import io.netty.buffer.Unpooled;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.CraftAmountContainer;
import appeng.container.implementations.CraftConfirmContainer;
import appeng.core.AELog;
import appeng.core.sync.BasePacket;
import appeng.core.sync.network.INetworkInfo;

public class CraftRequestPacket extends BasePacket {

    private final long amount;
    private final boolean heldShift;

    public CraftRequestPacket(final PacketByteBuf stream) {
        this.heldShift = stream.readBoolean();
        this.amount = stream.readLong();
    }

    public CraftRequestPacket(final int craftAmt, final boolean shift) {
        this.amount = craftAmt;
        this.heldShift = shift;

        final PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());

        data.writeInt(this.getPacketID());
        data.writeBoolean(shift);
        data.writeLong(this.amount);

        this.configureWrite(data);
    }

    @Override
    public void serverPacketData(final INetworkInfo manager, final PlayerEntity player) {
        if (player.currentScreenHandler instanceof CraftAmountContainer) {
            final CraftAmountContainer cca = (CraftAmountContainer) player.currentScreenHandler;
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
                    futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(),
                            cca.getItemToCraft(), null);

                    final ContainerLocator locator = cca.getLocator();
                    if (locator != null) {
                        ContainerOpener.openContainer(CraftConfirmContainer.TYPE, player, locator);

                        if (player.currentScreenHandler instanceof CraftConfirmContainer) {
                            final CraftConfirmContainer ccc = (CraftConfirmContainer) player.currentScreenHandler;
                            ccc.setAutoStart(this.heldShift);
                            ccc.setJob(futureJob);
                            cca.sendContentUpdates();
                        }
                    }
                } catch (final Throwable e) {
                    if (futureJob != null) {
                        futureJob.cancel(true);
                    }
                    AELog.info(e);
                }
            }
        }
    }
}
