package appeng.client.guidebook.layout;

import appeng.client.guidebook.style.ResolvedTextStyle;

/**
 * Allows to use font metrics without actually loading fonts. Assume all characters are 10x10 and lines are 10 pixels
 * high.
 */
public class MockFontMetrics implements FontMetrics {
    @Override
    public float getAdvance(int codePoint, ResolvedTextStyle style) {
        return 10;
    }

    @Override
    public int getLineHeight(ResolvedTextStyle style) {
        return 10;
    }
}
