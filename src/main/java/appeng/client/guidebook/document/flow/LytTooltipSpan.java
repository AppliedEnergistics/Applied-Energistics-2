package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * An inline span that allows a tooltip to be shown on hover.
 */
public class LytTooltipSpan extends LytFlowSpan implements InteractiveElement {
    @Nullable
    private GuideTooltip tooltip;

    @Override
    public Optional<GuideTooltip> getTooltip() {
        return Optional.ofNullable(tooltip);
    }

    public void setTooltip(@Nullable GuideTooltip tooltip) {
        this.tooltip = tooltip;
    }
}
