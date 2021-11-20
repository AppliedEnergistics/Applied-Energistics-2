/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.integration.modules.waila.tile;

import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPart;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.waila.BaseDataProvider;
import appeng.integration.modules.waila.part.IPartDataProvider;
import appeng.me.InWorldGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.AEBasePart;
import appeng.util.Platform;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Add debug info to the waila tooltip if the user is holding a debug card.
 */
public class DebugDataProvider extends BaseDataProvider implements IPartDataProvider {

    private static final String TAG_MAIN_NODE = "debugMainNode";
    private static final String TAG_EXTERNAL_NODE = "debugExposedNode";
    private static final String TAG_TICK_TIME = "tickTime";
    private static final String TAG_TICK_SLEEPING = "tickSleeping";
    private static final String TAG_TICK_ALERTABLE = "tickAlertable";
    private static final String TAG_TICK_AWAKE = "tickAwake";
    private static final String TAG_TICK_QUEUED = "tickQueued";
    private static final String TAG_TICK_CURRENT_RATE = "tickCurrentRate";
    private static final String TAG_TICK_LAST_TICK = "tickLastTick";
    private static final String TAG_NODE_EXPOSED = "exposedSides";

    @Override
    public void appendBody(IPart part, CompoundTag partTag, List<Component> tooltip) {
        addToTooltip(partTag, tooltip);
    }

    @Override
    public void appendBody(List<Component> tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (!isVisible(accessor.getPlayer())) {
            return;
        }

        addBlockEntityRotation(tooltip, accessor);
        addToTooltip(accessor.getServerData(), tooltip);
    }

    private static void addBlockEntityRotation(List<Component> tooltip, IBlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof AEBaseBlockEntity be && be.canBeRotated()) {
            var up = be.getUp();
            var forward = be.getForward();
            tooltip.add(
                    new TextComponent("")
                            .append(new TextComponent("Up: ").withStyle(ChatFormatting.WHITE))
                            .append(new TextComponent(up.name()))
                            .append(new TextComponent(" Forward: ").withStyle(ChatFormatting.WHITE))
                            .append(new TextComponent(forward.name())));
        }
    }

    private static void addToTooltip(CompoundTag serverData, List<Component> tooltip) {
        boolean hasMainNode = serverData.contains(TAG_MAIN_NODE, Tag.TAG_COMPOUND);
        boolean hasExternalNode = serverData.contains(TAG_EXTERNAL_NODE, Tag.TAG_COMPOUND);

        if (hasMainNode) {
            if (hasExternalNode) {
                tooltip.add(new TextComponent("Main Node").withStyle(ChatFormatting.ITALIC));
            }
            addNodeToTooltip(serverData.getCompound(TAG_MAIN_NODE), tooltip);
        }
        if (hasExternalNode) {
            if (hasMainNode) {
                tooltip.add(new TextComponent("External Node").withStyle(ChatFormatting.ITALIC));
            }
            addNodeToTooltip(serverData.getCompound(TAG_EXTERNAL_NODE), tooltip);
        }
    }

    private static void addNodeToTooltip(CompoundTag tag, List<Component> tooltip) {
        if (tag.contains(TAG_TICK_TIME, Tag.TAG_LONG_ARRAY)) {
            long[] tickTimes = tag.getLongArray(TAG_TICK_TIME);
            if (tickTimes.length == 3) {
                var avg = tickTimes[0];
                var max = tickTimes[1];
                var sum = tickTimes[2];

                tooltip.add(
                        new TextComponent("")
                                .append(new TextComponent("Tick Time: ").withStyle(ChatFormatting.WHITE))
                                .append(new TextComponent("Avg: ").withStyle(ChatFormatting.ITALIC))
                                .append(new TextComponent(Platform.formatTimeMeasurement(avg))
                                        .withStyle(ChatFormatting.WHITE))
                                .append(new TextComponent(" Max: ").withStyle(ChatFormatting.ITALIC))
                                .append(new TextComponent(Platform.formatTimeMeasurement(max))
                                        .withStyle(ChatFormatting.WHITE))
                                .append(new TextComponent(" Sum: ").withStyle(ChatFormatting.ITALIC))
                                .append(new TextComponent(Platform.formatTimeMeasurement(sum))
                                        .withStyle(ChatFormatting.WHITE)));
            }
        }

        if (tag.contains(TAG_TICK_QUEUED)) {
            var status = new ArrayList<String>();
            if (tag.getBoolean(TAG_TICK_SLEEPING)) {
                status.add("Sleeping");
            }
            if (tag.getBoolean(TAG_TICK_ALERTABLE)) {
                status.add("Alertable");
            }
            if (tag.getBoolean(TAG_TICK_AWAKE)) {
                status.add("Awake");
            }
            if (tag.getBoolean(TAG_TICK_QUEUED)) {
                status.add("Queued");
            }

            tooltip.add(
                    new TextComponent("")
                            .append(new TextComponent("Tick Status: ").withStyle(ChatFormatting.WHITE))
                            .append(String.join(", ", status)));
            tooltip.add(
                    new TextComponent("")
                            .append(new TextComponent("Tick Rate: ").withStyle(ChatFormatting.WHITE))
                            .append(String.valueOf(tag.getInt(TAG_TICK_CURRENT_RATE)))
                            .append(new TextComponent(" Last: ").withStyle(ChatFormatting.WHITE))
                            .append(tag.getInt(TAG_TICK_LAST_TICK) + " ticks ago"));
        }

        if (tag.contains(TAG_NODE_EXPOSED, Tag.TAG_INT)) {
            var exposedSides = tag.getInt(TAG_NODE_EXPOSED);
            var line = new TextComponent("Node Exposed: ").withStyle(ChatFormatting.WHITE);
            for (Direction value : Direction.values()) {
                var sideText = new TextComponent(value.name().substring(0, 1));
                if ((exposedSides & (1 << value.ordinal())) == 0) {
                    sideText.withStyle(ChatFormatting.GRAY);
                } else {
                    sideText.withStyle(ChatFormatting.GREEN);
                }
                line.append(sideText);
            }
            tooltip.add(line);
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        if (isVisible(player) && part instanceof AEBasePart basePart) {
            addServerDataMainNode(partTag, basePart.getMainNode());

            CompoundTag externalNodeTag = toServerData(basePart.getExternalFacingNode());
            if (externalNodeTag != null) {
                partTag.put(TAG_EXTERNAL_NODE, externalNodeTag);
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity) {
        if (isVisible(player) && blockEntity instanceof IGridConnectedBlockEntity gridConnected) {
            addServerDataMainNode(tag, gridConnected.getMainNode());
        }
    }

    private static void addServerDataMainNode(CompoundTag tag, IManagedGridNode managedGridNode) {
        var node = managedGridNode.getNode();
        if (node != null) {
            var nodeTag = toServerData(managedGridNode.getNode());
            if (nodeTag != null) {
                tag.put(TAG_MAIN_NODE, nodeTag);
            }
        }
    }

    private static CompoundTag toServerData(IGridNode node) {
        if (node == null) {
            return null;
        }

        CompoundTag tag = new CompoundTag();
        if (node.getService(IGridTickable.class) != null) {
            var tickManager = (TickManagerService) node.getGrid().getTickManager();
            var avg = tickManager.getAverageTime(node);
            var max = tickManager.getMaximumTime(node);
            var sum = tickManager.getOverallTime(node);

            tag.putLongArray(TAG_TICK_TIME, new long[]{avg, max, sum});

            var status = tickManager.getStatus(node);
            tag.putBoolean(TAG_TICK_SLEEPING, status.sleeping());
            tag.putBoolean(TAG_TICK_ALERTABLE, status.alertable());
            tag.putBoolean(TAG_TICK_AWAKE, status.awake());
            tag.putBoolean(TAG_TICK_QUEUED, status.queued());
            tag.putInt(TAG_TICK_CURRENT_RATE, status.currentRate());
            tag.putLong(TAG_TICK_LAST_TICK, status.lastTick());
        }

        if (node instanceof InWorldGridNode) {
            // Record on which sides the node is exposed
            int exposedSides = 0;
            for (Direction value : Direction.values()) {
                if (node.isExposedOnSide(value)) {
                    exposedSides |= 1 << value.ordinal();
                }
            }
            tag.putInt(TAG_NODE_EXPOSED, exposedSides);
        }

        return tag;
    }

    private static boolean isVisible(Player player) {
        return AEItems.DEBUG_CARD.isSameAs(player.getItemInHand(InteractionHand.OFF_HAND))
                || AEItems.DEBUG_CARD.isSameAs(player.getItemInHand(InteractionHand.MAIN_HAND));
    }

}
