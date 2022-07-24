package appeng.integration.modules.igtooltip.part;

import appeng.api.integrations.igtooltip.InGameTooltipBuilder;
import appeng.api.integrations.igtooltip.InGameTooltipContext;
import appeng.api.integrations.igtooltip.InGameTooltipProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.reporting.AbstractMonitorPart;

/**
 * Displays the stack if present and if the monitor is locked.
 */
public final class StorageMonitorDataProvider implements InGameTooltipProvider<AbstractMonitorPart> {
    @Override
    public void buildTooltip(AbstractMonitorPart monitor, InGameTooltipContext context, InGameTooltipBuilder tooltip) {
        var displayed = monitor.getDisplayed();
        var isLocked = monitor.isLocked();

        if (displayed != null) {
            tooltip.addLine(InGameTooltip.Showing.text().append(": ")
                    .append(displayed.getDisplayName()));
        }

        tooltip.addLine(isLocked ? InGameTooltip.Locked.text() : InGameTooltip.Unlocked.text());
    }
}
