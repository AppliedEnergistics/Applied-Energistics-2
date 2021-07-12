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

import appeng.core.Api;
import appeng.me.helpers.IGridConnectedTileEntity;
import com.google.common.collect.Iterables;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.hooks.ticking.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.service.TickManagerService;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.tile.networking.ControllerTileEntity;
import appeng.util.InteractionUtil;

public class DebugCardItem extends AEBaseItem {

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

        if (InteractionUtil.isInAlternateUseMode(player)) {
            int grids = 0;
            int totalNodes = 0;

            for (final Grid g : TickHandler.instance().getGridList()) {
                grids++;
                totalNodes += g.size();
            }

            this.outputMsg(player, "Grids: " + grids);
            this.outputMsg(player, "Total Nodes: " + totalNodes);
        } else {
            var gh = Api.instance().grid().getNodeHost(world, pos);
            if (gh != null) {
                this.outputMsg(player, "---------------------------------------------------");
                var node = (GridNode) gh.getGridNode(side);
                // If we couldn't get a world-accessible node, fall back to getting it via internal APIs
                if (node == null) {
                    if (gh instanceof IGridConnectedTileEntity gridConnectedTileEntity) {
                        node = (GridNode) gridConnectedTileEntity.getMainNode().getNode();
                        this.outputMsg(player, "Main node of IGridConnectedTileEntity");
                    }
                } else {
                    this.outputMsg(player, "Node exposed on side " + side);
                }
                if (node != null) {
                    final Grid g = node.getInternalGrid();
                    final IGridNode center = g.getPivot();
                    this.outputMsg(player, "Grid Powered:", String.valueOf(g.getService(IEnergyGrid.class).isNetworkPowered()));
                    this.outputMsg(player, "Grid Booted:", String.valueOf(!g.getService(IPathingGrid.class).isNetworkBooting()));
                    this.outputMsg(player, "Nodes in grid:", String.valueOf(Iterables.size(g.getNodes())));
                    this.outputMsg(player, "Grid Pivot Node:", String.valueOf(center));

                    this.outputMsg(player, "This Node:", String.valueOf(node));
                    this.outputMsg(player, "This Node Active:", String.valueOf(node.isActive()));

                    var pg = g.getService(IPathingGrid.class);
                    if (pg.getControllerState() == ControllerState.CONTROLLER_ONLINE) {

                        Set<IGridNode> next = new HashSet<>();
                        next.add(node);

                        final int maxLength = 10000;

                        int length = 0;
                        outer: while (!next.isEmpty()) {
                            final Iterable<IGridNode> current = next;
                            next = new HashSet<>();

                            for (final IGridNode n : current) {
                                if (n.getNodeOwner() instanceof ControllerTileEntity) {
                                    break outer;
                                }

                                for (var c : n.getConnections()) {
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

                    if (center.getNodeOwner() instanceof P2PTunnelPart<?> tunnelPart) {
                        this.outputMsg(player, "Freq: " + tunnelPart.getFrequency());
                    }

                    var tmc = (TickManagerService) g.getService(ITickManager.class);
                    for (var c : g.getMachineClasses()) {
                        int o = 0;
                        long nanos = 0;
                        for (var oj : g.getMachineNodes(c)) {
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

            var te = world.getTileEntity(pos);
            if (te instanceof IPartHost partHost) {
                final IPart center = partHost.getPart(AEPartLocation.INTERNAL);
                partHost.markForUpdate();
                if (center != null) {
                    final GridNode n = (GridNode) center.getGridNode();
                    this.outputMsg(player, "Node Channels: " + n.usedChannels());
                    for (var entry : n.getInWorldConnections().entrySet()) {
                        this.outputMsg(player, entry.getKey() + ": " + entry.getValue().getUsedChannels());
                    }
                }
            }

            if (te instanceof IAEPowerStorage ps) {
                this.outputMsg(player, "Energy: " + ps.getAECurrentPower() + " / " + ps.getAEMaxPower());

                if (gh != null) {
                    final IGridNode node = gh.getGridNode(side);
                    if (node != null && node.getGrid() != null) {
                        final IEnergyGrid eg = node.getGrid().getService(IEnergyGrid.class);
                        this.outputMsg(player,
                                "GridEnergy: " + eg.getStoredPower() + " : " + eg.getEnergyDemand(Double.MAX_VALUE));
                    }
                }
            }
        }
        return ActionResultType.func_233537_a_(world.isRemote());
    }

    private void outputMsg(final Entity player, final String string) {
        player.sendMessage(new StringTextComponent(string), Util.DUMMY_UUID);
    }

    private void outputMsg(final Entity player, String label, String value) {
        player.sendMessage(new StringTextComponent("")
                .appendSibling(new StringTextComponent(label).mergeStyle(TextFormatting.BOLD, TextFormatting.LIGHT_PURPLE))
                .appendString(value), Util.DUMMY_UUID);
    }

    private String timeMeasurement(final long nanos) {
        final long ms = nanos / 100000;
        if (nanos <= 100000) {
            return nanos + "ns";
        }
        return (ms / 10.0f) + "ms";
    }
}
