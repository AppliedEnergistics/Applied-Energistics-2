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
import com.google.common.math.StatsAccumulator;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import appeng.hooks.ticking.TickHandler;
import appeng.items.AEBaseItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.networking.CablePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class DebugCardItem extends AEBaseItem {

    public DebugCardItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (InteractionUtil.isInAlternateUseMode(player) && !level.isClientSide) {
            int grids = 0;

            var stats = new StatsAccumulator();
            for (Grid g : TickHandler.instance().getGridList()) {
                grids++;
                stats.add(g.size());
            }

            divider(player);
            outputMessage(player, "Grids", ChatFormatting.BOLD);
            this.outputSecondaryMessage(player, "Grids", Integer.toString(grids));
            if (stats.count() > 0) {
                this.outputSecondaryMessage(player, "Total Nodes", "" + (long) stats.sum());
                this.outputSecondaryMessage(player, "Mean Nodes", "" + (long) stats.mean());
                this.outputSecondaryMessage(player, "Max Nodes", "" + (long) stats.max());
            }
            divider(player);
            outputMessage(player, "Ticking", ChatFormatting.BOLD);
            this.outputSecondaryMessage(player, "Current Tick: ",
                    Long.toString(TickHandler.instance().getCurrentTick()));
            for (var line : TickHandler.instance().getBlockEntityReport()) {
                player.sendSystemMessage(line);
            }
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
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

        if (player == null || InteractionUtil.isInAlternateUseMode(player)) {
            return InteractionResult.PASS;
        }

        var gh = GridHelper.getNodeHost(level, pos);
        if (gh != null) {
            divider(player);
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
            outputSecondaryMessage(player, "In World", Boolean.toString(partHost.isInWorld()));
            outputSecondaryMessage(player, "Has Redstone", Boolean.toString(partHost.hasRedstone()));
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
            // Print which sides of the cable are connected
            if (center instanceof CablePart cablePart) {
                var msg = Component.literal("");
                for (var v : Direction.values()) {
                    msg.append(Component.literal(v.name().substring(0, 1))
                            .withStyle(cablePart.isConnected(v) ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
                }
                player.sendSystemMessage(Component.literal("Connected Sides: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(msg));
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

        if (te instanceof AEBaseBlockEntity be) {
            this.outputMessage(player, "-- Delayed Init Details");
            outputSecondaryMessage(player, "QueuedForReady", "" + be.getQueuedForReady());
            outputSecondaryMessage(player, "ReadyInvoked", "" + be.getReadyInvoked());
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void divider(Player player) {
        this.outputMessage(player, "---------------------------------------------", ChatFormatting.BOLD,
                ChatFormatting.DARK_PURPLE);
    }

    private void outputMessage(Entity player, String string, ChatFormatting... chatFormattings) {
        player.sendSystemMessage(Component.literal(string).withStyle(chatFormattings));
    }

    private void outputMessage(Entity player, String string) {
        player.sendSystemMessage(Component.literal(string));
    }

    private void outputPrimaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.BOLD, ChatFormatting.LIGHT_PURPLE);
    }

    private void outputSecondaryMessage(Entity player, String label, String value) {
        this.outputLabeledMessage(player, label, value, ChatFormatting.GRAY);
    }

    private void outputLabeledMessage(Entity player, String label, String value,
            ChatFormatting... chatFormattings) {
        player.sendSystemMessage(Component.literal("")
                .append(Component.literal(label + ": ").withStyle(chatFormattings))
                .append(value));
    }

}
