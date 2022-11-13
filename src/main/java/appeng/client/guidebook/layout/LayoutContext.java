package appeng.client.guidebook.layout;

import appeng.client.guidebook.document.LytRect;
import net.minecraft.network.chat.Style;

public interface LayoutContext {
    LytRect available();

    LytRect viewport();

    LayoutContext withAvailable(LytRect available);

    float getAdvance(int codePoint, Style style);

    int getLineHeight(Style style);

    default int viewportWidth() {
        return viewport().width();
    }

    default int viewportHeight() {
        return viewport().height();
    }
}
