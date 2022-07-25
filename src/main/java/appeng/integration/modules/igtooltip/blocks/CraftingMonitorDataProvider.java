package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.localization.InGameTooltip;

/**
 * Shows the name of the item being crafted.
 */
public final class CraftingMonitorDataProvider implements InGameTooltipProvider<CraftingMonitorBlockEntity> {
    @Override
    public void buildTooltip(CraftingMonitorBlockEntity monitor, InGameTooltipContext context,
            InGameTooltipBuilder tooltip) {
        var displayStack = monitor.getJobProgress();

        if (displayStack != null) {
            tooltip.addLine(InGameTooltip.Crafting.text(displayStack.what().getDisplayName()));
        }
    }
}
