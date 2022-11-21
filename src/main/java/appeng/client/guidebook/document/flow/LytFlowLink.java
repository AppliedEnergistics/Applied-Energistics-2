package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.screen.GuideScreen;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LytFlowLink extends LytFlowSpan implements InteractiveElement {
    private String title;
    private String url;

    @Nullable
    private GuideTooltip tooltip;

    public LytFlowLink() {
        modifyStyle(style -> style.color(SymbolicColor.LINK.ref()));
        modifyHoverStyle(style -> style.underlined(true));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        return false;
    }

    @Override
    public Optional<GuideTooltip> getTooltip() {
        return Optional.ofNullable(tooltip);
    }

    public void setTooltip(@Nullable GuideTooltip tooltip) {
        this.tooltip = tooltip;
    }
}
