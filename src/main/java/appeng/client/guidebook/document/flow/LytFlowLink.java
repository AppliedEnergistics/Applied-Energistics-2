package appeng.client.guidebook.document.flow;

import java.util.Optional;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.screen.GuideScreen;

public class LytFlowLink extends LytFlowSpan implements InteractiveElement {
    private String title;

    @Nullable
    private GuideTooltip tooltip;

    @Nullable
    private Consumer<GuideScreen> clickCallback;

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

    public void setClickCallback(@Nullable Consumer<GuideScreen> clickCallback) {
        this.clickCallback = clickCallback;
    }

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        if (button == 0 && clickCallback != null) {
            clickCallback.accept(screen);
            return true;
        }
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
