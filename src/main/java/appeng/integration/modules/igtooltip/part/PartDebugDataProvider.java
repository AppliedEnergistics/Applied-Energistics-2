package appeng.integration.modules.igtooltip.part;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.integration.modules.igtooltip.DebugTooltip;
import appeng.parts.AEBasePart;

/**
 * Add debug info to the ingame tooltip if the user is holding a debug card and hovers over a part.
 */
public class PartDebugDataProvider implements InGameTooltipProvider<AEBasePart> {
    @Override
    public void buildTooltip(AEBasePart object, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        DebugTooltip.addToTooltip(context.serverData(), tooltip);
    }

    @Override
    public void provideServerData(ServerPlayer player, AEBasePart part, CompoundTag serverData) {
        if (DebugTooltip.isVisible(player)) {
            DebugTooltip.addServerDataMainNode(serverData, part.getMainNode());
            DebugTooltip.addServerDataNode(serverData, "External Node", part.getExternalFacingNode());
        }
    }

}
