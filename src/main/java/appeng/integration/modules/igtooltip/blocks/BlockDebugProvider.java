package appeng.integration.modules.igtooltip.blocks;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.integration.modules.igtooltip.DebugTooltip;
import appeng.me.helpers.IGridConnectedBlockEntity;

public class BlockDebugProvider implements InGameTooltipProvider<BlockEntity> {
    @Override
    public void buildTooltip(BlockEntity object, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        var player = context.player();

        if (!DebugTooltip.isVisible(player)) {
            return;
        }

        DebugTooltip.addBlockEntityRotation(object, tooltip);
        DebugTooltip.addToTooltip(context.serverData(), tooltip);
    }

    @Override
    public void provideServerData(ServerPlayer player, BlockEntity object, CompoundTag serverData) {
        if (object instanceof IGridConnectedBlockEntity gridConnected && DebugTooltip.isVisible(player)) {
            DebugTooltip.addServerDataMainNode(serverData, gridConnected.getMainNode());
        }
    }
}
