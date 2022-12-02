package appeng.client.guidebook.document.flow;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.screen.GuideScreen;

public class LytFlowLink extends LytTooltipSpan {
    @Nullable
    private Consumer<GuideScreen> clickCallback;

    public LytFlowLink() {
        modifyStyle(style -> style.color(SymbolicColor.LINK.ref()));
        modifyHoverStyle(style -> style.underlined(true));
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
}
