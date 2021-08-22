package appeng.integration.modules.waila.tile;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.parts.IPart;
import appeng.core.definitions.AEItems;
import appeng.integration.modules.waila.BaseDataProvider;
import appeng.integration.modules.waila.part.IPartDataProvider;
import appeng.me.helpers.IGridConnectedBlockEntity;
import appeng.me.service.TickManagerService;
import appeng.parts.AEBasePart;
import appeng.util.Platform;

/**
 * Add debug info to the waila tooltip if the user is holding a debug card.
 */
public class DebugDataProvider extends BaseDataProvider implements IPartDataProvider {

    private static final String TAG_TICK_TIME = "debugTickTime";

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        addToTooltip(partTag, tooltip);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        addToTooltip(accessor.getServerData(), tooltip);
    }

    private static void addToTooltip(CompoundTag tag, ITooltip tooltip) {
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
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        if (isVisible(player) && part instanceof AEBasePart basePart) {
            addServerData(partTag, basePart.getMainNode());
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        if (isVisible(player) && blockEntity instanceof IGridConnectedBlockEntity gridConnected) {
            addServerData(tag, gridConnected.getMainNode());
        }
    }

    private static void addServerData(CompoundTag tag, IManagedGridNode managedGridNode) {
        var node = managedGridNode.getNode();
        if (node == null || node.getService(IGridTickable.class) == null) {
            return;
        }

        var tickManager = (TickManagerService) node.getGrid().getTickManager();
        var avg = tickManager.getAverageTime(node);
        var max = tickManager.getMaximumTime(node);
        var sum = tickManager.getOverallTime(node);

        tag.putLongArray(TAG_TICK_TIME, new long[] { avg, max, sum });
    }

    private static boolean isVisible(Player player) {
        return AEItems.DEBUG_CARD.isSameAs(player.getItemInHand(InteractionHand.OFF_HAND))
                || AEItems.DEBUG_CARD.isSameAs(player.getItemInHand(InteractionHand.MAIN_HAND));
    }

}
