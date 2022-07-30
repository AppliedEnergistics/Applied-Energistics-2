package appeng.integration.modules.igtooltip.parts;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.api.parts.IPart;
import appeng.integration.modules.igtooltip.GridNodeState;

/**
 * Provide info about the grid connection status of a part.
 */
public final class GridNodeStateProvider implements BodyProvider<IPart>, ServerDataProvider<IPart> {
    private static final String TAG_STATE = "gridNodeState";

    @Override
    public void buildTooltip(IPart object, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        if (serverData.contains(TAG_STATE, Tag.TAG_BYTE)) {
            var state = GridNodeState.values()[serverData.getByte(TAG_STATE)];
            tooltip.addLine(state.textComponent().withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void provideServerData(ServerPlayer player, IPart part, CompoundTag serverData) {
        var state = GridNodeState.fromNode(part.getGridNode());
        serverData.putByte(TAG_STATE, (byte) state.ordinal());
    }
}
