package appeng.integration.modules.jade.part;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import snownee.jade.api.ITooltip;

import appeng.api.parts.IPart;
import appeng.integration.modules.jade.GridNodeState;

/**
 * Provide info about the grid connection status of a part.
 */
public final class GridNodeStateProvider implements IPartDataProvider {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void appendBodyTooltip(IPart part, CompoundTag partTag, ITooltip tooltip) {
        if (partTag.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[partTag.getByte(TAG_STATE)];
            tooltip.add(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void appendServerData(ServerPlayer player, IPart part, CompoundTag partTag) {
        var state = GridNodeState.fromNode(part.getGridNode());
        partTag.putByte(TAG_STATE, (byte) state.ordinal());
    }

}
