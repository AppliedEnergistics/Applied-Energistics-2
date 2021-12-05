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

import com.google.common.collect.Iterables;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class DebugCardItem extends AEBaseItem {

    public DebugCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (InteractionUtil.isInAlternateUseMode(player)) {
            int grids = 0;
            int totalNodes = 0;

            for (Grid g : TickHandler.instance().getGridList()) {
                grids++;
                totalNodes += g.size();
            }

            this.outputSecondaryMessage(player, "Grids", Integer.toString(grids));
            this.outputSecondaryMessage(player, "Total Nodes", Integer.toString(totalNodes));
        } else {
            var gh = GridHelper.getNodeHost(level, pos);
            if (gh != null) {
                this.outputMessage(player, "---------------------------------------------", ChatFormatting.BOLD,
                        ChatFormatting.DARK_PURPLE);
                var node = (GridNode) gh.getGridNode(side);
                // If we couldn't get a world-accessible node, fall back to getting it via internal APIs
                if (node == null) {
                    if (gh instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
                        node = (GridNode) gridConnectedBlockEntity.getMainNode().getNode();
                        this.outputMessage(player, "Main node of IGridConnectedBlockEntity");
                    }
                }
                if (node != null) {
                    this.outputMessage(player, "-- Grid Details");
                    final Grid g = node.getInternalGrid();
                    final IGridNode center = g.getPivot();
                    this.outputPrimaryMessage(player, "Grid Powered",
                            String.valueOf(g.getEnergyService().isNetworkPowered()));
                    this.outputPrimaryMessage(player, "Grid Booted",
                            String.valueOf(!g.getPathingService().isNetworkBooting()));
                    this.outputPrimaryMessage(player, "Nodes in grid", String.valueOf(Iterables.size(g.getNodes())));
                    this.outputSecondaryMessage(player, "Grid Pivot Node", String.valueOf(center));

                    var tmc = (TickManagerService) g.getTickManager();
                    for (var c : g.getMachineClasses()) {
                        int o = 0;
                        long totalAverageTime = 0;
                        long singleMaximumTime = 0;

                        for (var oj : g.getMachineNodes(c)) {
                            o++;
                            totalAverageTime += tmc.getAverageTime(oj);
                            singleMaximumTime = Math.max(singleMaximumTime, tmc.getMaximumTime(oj));
                        }

                        String message = "#: " + o;

                        if (totalAverageTime > 0) {
                            message += "; average: " + Platform.formatTimeMeasurement((long) totalAverageTime);
                        }
                        if (singleMaximumTime > 0) {
                            message += "; max: " + Platform.formatTimeMeasurement(singleMaximumTime);
                        }

                        this.outputSecondaryMessage(player, c.getSimpleName(), message);
                    }

                    this.outputMessage(player, "-- Node Details");

                    this.outputPrimaryMessage(player, "This Node", String.valueOf(node));
                    this.outputPrimaryMessage(player, "This Node Active", String.valueOf(node.isActive()));
                    this.outputSecondaryMessage(player, "Node exposed on side", side.getName());

                    var pg = g.getPathingService();
                    if (pg.getControllerState() == ControllerState.CONTROLLER_ONLINE) {

                        Set<IGridNode> next = new HashSet<>();
                        next.add(node);

                        final int maxLength = 10000;

                        int length = 0;
                        outer: while (!next.isEmpty()) {
                            final Iterable<IGridNode> current = next;
                            next = new HashSet<>();

                            for (IGridNode n : current) {
                                if (n.getOwner() instanceof ControllerBlockEntity) {
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

                        this.outputSecondaryMessage(player, "Cable Distance", Integer.toString(length));
                    }

                    if (center.getOwner() instanceof P2PTunnelPart<?>tunnelPart) {
                        this.outputSecondaryMessage(player, "Freq", Integer.toString(tunnelPart.getFrequency()));
                    }
                } else {
                    this.outputMessage(player, "No Node Available.");
                }
            } else {
                this.outputMessage(player, "Not Networked Block");
            }

            var te = level.getBlockEntity(pos);
            if (te instanceof IPartHost partHost) {
                this.outputMessage(player, "-- CableBus Details");
                final IPart center = partHost.getPart(null);
                partHost.markForUpdate();
                if (center != null) {
                    final GridNode n = (GridNode) center.getGridNode();
                    this.outputSecondaryMessage(player, "Node Channels", Integer.toString(n.usedChannels()));
                    for (var entry : n.getInWorldConnections().entrySet()) {
                        this.outputSecondaryMessage(player, "Channels " + entry.getKey().getName(),
                                Integer.toString(entry.getValue().getUsedChannels()));
                    }
                }
            }

            if (te instanceof IAEPowerStorage ps) {
                this.outputMessage(player, "-- EnergyStorage Details");
                this.outputSecondaryMessage(player, "Energy", ps.getAECurrentPower() + " / " + ps.getAEMaxPower());

                if (gh != null) {
                    final IGridNode node = gh.getGridNode(side);
                    if (node != null) {
                        final IEnergyService eg = node.getGrid().getEnergyService();
                        this.outputSecondaryMessage(player, "GridEnergy",
                                +eg.getStoredPower() + " : " + eg.getEnergyDemand(Double.MAX_VALUE));
                    }
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void outputMessage(Entity player, String string, ChatFormatting... chatFormattings) {
        player.sendMessage(new TextComponent(string).withStyle(chatFormattings), Util.NIL_UUID);
    }

    private void outputMessage(Entity player, String string) {
        player.sendMessage(new TextComponent(string), Util.NIL_UUID);
    }

    private void outputPrimaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
    }

    private void outputSecondaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.GRAY);
    }

    private void outputLabeledMessage(Entity player, String label, String value,
            ChatFormatting... chatFormattings) {
        player.sendMessage(new TextComponent("")
                .append(new TextComponent(label + ": ").withStyle(chatFormattings))
                .append(value), Util.NIL_UUID);
    }

}
