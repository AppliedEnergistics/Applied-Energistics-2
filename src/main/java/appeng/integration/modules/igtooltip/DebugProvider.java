package appeng.integration.modules.igtooltip;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.me.InWorldGridNode;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.AEBasePart;
import appeng.util.Platform;

public final class DebugProvider {
    private static final String TAG_NODES = "debugNodes";
    private static final String TAG_NODE_NAME = "nodeName";
    private static final String TAG_TICK_TIME = "tickTime";
    private static final String TAG_TICK_SLEEPING = "tickSleeping";
    private static final String TAG_TICK_ALERTABLE = "tickAlertable";
    private static final String TAG_TICK_AWAKE = "tickAwake";
    private static final String TAG_TICK_QUEUED = "tickQueued";
    private static final String TAG_TICK_CURRENT_RATE = "tickCurrentRate";
    private static final String TAG_TICK_LAST_TICK = "tickLastTick";
    private static final String TAG_NODE_EXPOSED = "exposedSides";

    private DebugProvider() {
    }

    public static void provideBlockEntityBody(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        var player = context.player();

        if (!DebugProvider.isVisible(player)) {
            return;
        }

        DebugProvider.addBlockEntityRotation(object, tooltip);
        DebugProvider.addToTooltip(context.serverData(), tooltip);
    }

    public static void provideBlockEntityData(ServerPlayer player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IGridConnectedBlockEntity gridConnected && DebugProvider.isVisible(player)) {
            DebugProvider.addServerDataMainNode(serverData, gridConnected.getMainNode());
        }
    }

    public static void providePartBody(AEBasePart object, TooltipContext context, TooltipBuilder tooltip) {
        DebugProvider.addToTooltip(context.serverData(), tooltip);
    }

    public static void providePartData(ServerPlayer player, AEBasePart part, CompoundTag serverData) {
        if (DebugProvider.isVisible(player)) {
            DebugProvider.addServerDataMainNode(serverData, part.getMainNode());
            DebugProvider.addServerDataNode(serverData, "External Node", part.getExternalFacingNode());
        }
    }

    private static void addBlockEntityRotation(BlockEntity blockEntity, TooltipBuilder tooltip) {
        if (blockEntity instanceof AEBaseBlockEntity be && be.canBeRotated()) {
            var up = be.getUp();
            var forward = be.getForward();
            tooltip.addLine(
                    Component.literal("")
                            .append(Component.literal("Up: ").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(up.name()))
                            .append(Component.literal(" Forward: ").withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(forward.name())));
        }
    }

    private static void addToTooltip(CompoundTag serverData, TooltipBuilder tooltip) {
        var nodes = serverData.getList(TAG_NODES, Tag.TAG_COMPOUND);

        for (var node : nodes) {
            var nodeCompound = (CompoundTag) node;
            if (nodes.size() > 1) {
                var nodeName = ((CompoundTag) node).getString(TAG_NODE_NAME);
                tooltip.addLine(Component.literal(nodeName).withStyle(ChatFormatting.ITALIC));
            }
            addNodeToTooltip(nodeCompound, tooltip);
        }
    }

    private static void addNodeToTooltip(CompoundTag tag, TooltipBuilder tooltip) {
        if (tag.contains(TAG_TICK_TIME, Tag.TAG_LONG_ARRAY)) {
            long[] tickTimes = tag.getLongArray(TAG_TICK_TIME);
            if (tickTimes.length == 3) {
                var avg = tickTimes[0];
                var max = tickTimes[1];
                var sum = tickTimes[2];

                tooltip.addLine(
                        Component.literal("")
                                .append(Component.literal("Tick Time: ").withStyle(ChatFormatting.WHITE))
                                .append(Component.literal("Avg: ").withStyle(ChatFormatting.ITALIC))
                                .append(Component.literal(Platform.formatTimeMeasurement(avg))
                                        .withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" Max: ").withStyle(ChatFormatting.ITALIC))
                                .append(Component.literal(Platform.formatTimeMeasurement(max))
                                        .withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" Sum: ").withStyle(ChatFormatting.ITALIC))
                                .append(Component.literal(Platform.formatTimeMeasurement(sum))
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

            tooltip.addLine(
                    Component.literal("")
                            .append(Component.literal("Tick Status: ").withStyle(ChatFormatting.WHITE))
                            .append(String.join(", ", status)));
            tooltip.addLine(
                    Component.literal("")
                            .append(Component.literal("Tick Rate: ").withStyle(ChatFormatting.WHITE))
                            .append(String.valueOf(tag.getInt(TAG_TICK_CURRENT_RATE)))
                            .append(Component.literal(" Last: ").withStyle(ChatFormatting.WHITE))
                            .append(tag.getInt(TAG_TICK_LAST_TICK) + " ticks ago"));
        }

        if (tag.contains(TAG_NODE_EXPOSED, Tag.TAG_INT)) {
            var exposedSides = tag.getInt(TAG_NODE_EXPOSED);
            var line = Component.literal("Node Exposed: ").withStyle(ChatFormatting.WHITE);
            for (Direction value : Direction.values()) {
                var sideText = Component.literal(value.name().substring(0, 1));
                if ((exposedSides & (1 << value.ordinal())) == 0) {
                    sideText.withStyle(ChatFormatting.GRAY);
                } else {
                    sideText.withStyle(ChatFormatting.GREEN);
                }
                line.append(sideText);
            }
            tooltip.addLine(line);
        }
    }

    private static void addServerDataMainNode(CompoundTag tag, IManagedGridNode managedGridNode) {
        addServerDataNode(tag, "Main Node", managedGridNode.getNode());
    }

    private static void addServerDataNode(CompoundTag tag, String name, @Nullable IGridNode node) {
        var nodeTag = toServerData(node, name);
        if (nodeTag != null) {
            var nodes = (ListTag) tag.get(TAG_NODES);
            if (nodes == null) {
                nodes = new ListTag();
                tag.put(TAG_NODES, nodes);
            }
            nodes.add(nodeTag);
        }
    }

    private static CompoundTag toServerData(IGridNode node, String name) {
        if (node == null) {
            return null;
        }

        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_NODE_NAME, name);

        if (node.getService(IGridTickable.class) != null) {
            var tickManager = (TickManagerService) node.getGrid().getTickManager();
            var avg = tickManager.getAverageTime(node);
            var max = tickManager.getMaximumTime(node);
            var sum = tickManager.getOverallTime(node);

            tag.putLongArray(TAG_TICK_TIME, new long[] { avg, max, sum });

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
