package appeng.client.guidebook.layout;

import net.minecraft.client.gui.Font;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.style.ResolvedTextStyle;

public record SimpleLayoutContext(
        Font font,
        @Override LytRect viewport) implements LayoutContext {
    @Override
    public float getAdvance(int codePoint, ResolvedTextStyle style) {
        return font.getFontSet(style.font()).getGlyphInfo(codePoint, false)
                .getAdvance(Boolean.TRUE.equals(style.bold()));
    }

    @Override
    public int getLineHeight(ResolvedTextStyle style) {
        return (int) Math.ceil(font.lineHeight * style.fontScale());
    }
}
