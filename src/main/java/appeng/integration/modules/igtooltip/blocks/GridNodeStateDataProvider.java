package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.integration.modules.igtooltip.GridNodeState;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * Provide info about the grid connection status of a machine.
 */
public final class GridNodeStateDataProvider implements BodyProvider<BlockEntity>, ServerDataProvider<BlockEntity> {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void buildTooltip(BlockEntity object, TooltipContext context, TooltipBuilder tooltip) {
        var tag = context.serverData();
        if (tag.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[tag.getByte(TAG_STATE)];
            tooltip.addLine(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            var state = GridNodeState.fromNode(gridConnectedBlockEntity.getActionableNode());
            serverData.putByte(TAG_STATE, (byte) state.ordinal());
        }
    }
}
