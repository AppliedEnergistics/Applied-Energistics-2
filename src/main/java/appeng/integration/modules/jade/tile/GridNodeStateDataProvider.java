package appeng.integration.modules.jade.tile;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import appeng.api.integrations.waila.AEJadeIds;
import appeng.integration.modules.jade.BaseDataProvider;
import appeng.integration.modules.jade.GridNodeState;
import appeng.me.helpers.IGridConnectedBlockEntity;

/**
 * Provide info about the grid connection status of a machine.
 */
public final class GridNodeStateDataProvider extends BaseDataProvider {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public ResourceLocation getUid() {
        return AEJadeIds.GRID_NODE_STATE_PROVIDER;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var tag = accessor.getServerData();
        if (tag.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[tag.getByte(TAG_STATE)];
            tooltip.add(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level level, BlockEntity blockEntity,
            boolean showDetails) {
        if (blockEntity instanceof IGridConnectedBlockEntity gridConnectedBlockEntity) {
            var state = GridNodeState.fromNode(gridConnectedBlockEntity.getActionableNode());
            tag.putByte(TAG_STATE, (byte) state.ordinal());
        }
    }

}
