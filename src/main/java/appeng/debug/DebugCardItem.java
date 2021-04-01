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

package appeng.debug;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.hooks.AEToolItem;
import appeng.hooks.ticking.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.cache.TickManagerCache;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.tile.networking.ControllerTileEntity;

public class DebugCardItem extends AEBaseItem implements AEToolItem {

    public DebugCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getWorld().isRemote()) {
            return ActionResultType.PASS;
        }

        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction side = context.getFace();

        if (player == null) {
            return ActionResultType.PASS;
        }

        if (player.isCrouching()) {
            int grids = 0;
            int totalNodes = 0;

            for (final Grid g : TickHandler.instance().getGridList()) {
                grids++;
                totalNodes += g.getNodes().size();
            }

            this.outputMsg(player, "Grids: " + grids);
            this.outputMsg(player, "Total Nodes: " + totalNodes);
        } else {
            final TileEntity te = world.getTileEntity(pos);

            if (te instanceof IGridHost) {
                final GridNode node = (GridNode) ((IGridHost) te).getGridNode(AEPartLocation.fromFacing(side));
                if (node != null) {
                    final Grid g = node.getInternalGrid();
                    final IGridNode center = g.getPivot();
                    this.outputMsg(player, "This Node: " + node.toString());
                    this.outputMsg(player, "Center Node: " + center.toString());

                    final IPathingGrid pg = g.getCache(IPathingGrid.class);
                    if (pg.getControllerState() == ControllerState.CONTROLLER_ONLINE) {

                        Set<IGridNode> next = new HashSet<>();
                        next.add(node);

                        final int maxLength = 10000;

                        int length = 0;
                        outer: while (!next.isEmpty()) {
                            final Iterable<IGridNode> current = next;
                            next = new HashSet<>();

                            for (final IGridNode n : current) {
                                if (n.getMachine() instanceof ControllerTileEntity) {
                                    break outer;
                                }

                                for (final IGridConnection c : n.getConnections()) {
                                    next.add(c.getOtherSide(n));
                                }
                            }

                            length++;

                            if (length > maxLength) {
                                break;
                            }
                        }

                        this.outputMsg(player, "Cable Distance: " + length);
                    }

                    if (center.getMachine() instanceof P2PTunnelPart) {
                        this.outputMsg(player, "Freq: " + ((P2PTunnelPart<?>) center.getMachine()).getFrequency());
                    }

                    final TickManagerCache tmc = g.getCache(ITickManager.class);
                    for (final Class<? extends IGridHost> c : g.getMachineClasses()) {
                        int o = 0;
                        long nanos = 0;
                        for (final IGridNode oj : g.getMachines(c)) {
                            o++;
                            nanos += tmc.getAvgNanoTime(oj);
                        }

                        if (nanos < 0) {
                            this.outputMsg(player, c.getSimpleName() + " - " + o);
                        } else {
                            this.outputMsg(player, c.getSimpleName() + " - " + o + "; " + this.timeMeasurement(nanos));
                        }
                    }
                } else {
                    this.outputMsg(player, "No Node Available.");
                }
            } else {
                this.outputMsg(player, "Not Networked Block");
            }

            if (te instanceof IPartHost) {
                final IPart center = ((IPartHost) te).getPart(AEPartLocation.INTERNAL);
                ((IPartHost) te).markForUpdate();
                if (center != null) {
                    final GridNode n = (GridNode) center.getGridNode();
                    this.outputMsg(player, "Node Channels: " + n.usedChannels());
                    for (final IGridConnection gc : n.getConnections()) {
                        final AEPartLocation fd = gc.getDirection(n);
                        if (fd != AEPartLocation.INTERNAL) {
                            this.outputMsg(player, fd.toString() + ": " + gc.getUsedChannels());
                        }
                    }
                }
            }

            if (te instanceof IAEPowerStorage) {
                final IAEPowerStorage ps = (IAEPowerStorage) te;
                this.outputMsg(player, "Energy: " + ps.getAECurrentPower() + " / " + ps.getAEMaxPower());

                if (te instanceof IGridHost) {
                    final IGridNode node = ((IGridHost) te).getGridNode(AEPartLocation.fromFacing(side));
                    if (node != null && node.getGrid() != null) {
                        final IEnergyGrid eg = node.getGrid().getCache(IEnergyGrid.class);
                        this.outputMsg(player,
                                "GridEnergy: " + eg.getStoredPower() + " : " + eg.getEnergyDemand(Double.MAX_VALUE));
                    }
                }
            }
        }
        return ActionResultType.SUCCESS;
    }

    private void outputMsg(final Entity player, final String string) {
        player.sendMessage(new StringTextComponent(string), Util.DUMMY_UUID);
    }

    private String timeMeasurement(final long nanos) {
        final long ms = nanos / 100000;
        if (nanos <= 100000) {
            return nanos + "ns";
        }
        return (ms / 10.0f) + "ms";
    }
}
