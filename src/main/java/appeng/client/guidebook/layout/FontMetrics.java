package appeng.client.guidebook.layout;

import appeng.client.guidebook.style.ResolvedTextStyle;

public interface FontMetrics {
    float getAdvance(int codePoint, ResolvedTextStyle style);

    int getLineHeight(ResolvedTextStyle style);
}
