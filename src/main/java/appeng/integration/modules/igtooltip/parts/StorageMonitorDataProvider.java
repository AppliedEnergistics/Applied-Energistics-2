package appeng.integration.modules.igtooltip.parts;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.reporting.AbstractMonitorPart;

/**
 * Displays the stack if present and if the monitor is locked.
 */
public final class StorageMonitorDataProvider implements BodyProvider<AbstractMonitorPart> {
    @Override
    public void buildTooltip(AbstractMonitorPart monitor, TooltipContext context, TooltipBuilder tooltip) {
        var displayed = monitor.getDisplayed();
        var isLocked = monitor.isLocked();

        if (displayed != null) {
            tooltip.addLine(InGameTooltip.Showing.text().append(": ")
                    .append(displayed.getDisplayName()));
        }

        tooltip.addLine(isLocked ? InGameTooltip.Locked.text() : InGameTooltip.Unlocked.text());
    }
}
