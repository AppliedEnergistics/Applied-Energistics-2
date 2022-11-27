package appeng.client.guidebook.layout;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.style.ResolvedTextStyle;

public interface LayoutContext {
    LytRect viewport();

    float getAdvance(int codePoint, ResolvedTextStyle style);

    int getLineHeight(ResolvedTextStyle style);

    default int viewportWidth() {
        return viewport().width();
    }

    default int viewportHeight() {
        return viewport().height();
    }
}
