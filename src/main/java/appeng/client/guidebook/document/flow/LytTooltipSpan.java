package appeng.client.guidebook.document.flow;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.siteexport.ExportableResourceProvider;
import appeng.siteexport.ResourceExporter;

/**
 * An inline span that allows a tooltip to be shown on hover.
 */
public class LytTooltipSpan extends LytFlowSpan implements InteractiveElement, ExportableResourceProvider {
    @Nullable
    private GuideTooltip tooltip;

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.ofNullable(tooltip);
    }

    public void setTooltip(@Nullable GuideTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public void exportResources(ResourceExporter exporter) {
        if (tooltip != null) {
            tooltip.exportResources(exporter);
        }
    }
}
