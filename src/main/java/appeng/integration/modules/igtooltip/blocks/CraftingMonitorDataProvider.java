package appeng.integration.modules.igtooltip.blocks;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.localization.InGameTooltip;

/**
 * Shows the name of the item being crafted.
 */
public final class CraftingMonitorDataProvider implements BodyProvider<CraftingMonitorBlockEntity> {
    @Override
    public void buildTooltip(CraftingMonitorBlockEntity monitor, TooltipContext context,
            TooltipBuilder tooltip) {
        var displayStack = monitor.getJobProgress();

        if (displayStack != null) {
            tooltip.addLine(InGameTooltip.Crafting.text(displayStack.what().getDisplayName()));
        }
    }
}
