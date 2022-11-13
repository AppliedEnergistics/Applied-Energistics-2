package appeng.client.guidebook.layout;

import appeng.client.guidebook.document.LytRect;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

public record SimpleLayoutContext(
        Font font,
        @Override LytRect viewport,
        @Override LytRect available
) implements LayoutContext {
    @Override
    public LayoutContext withAvailable(LytRect available) {
        return new SimpleLayoutContext(
                font,
                viewport,
                available
        );
    }

    @Override
    public float getAdvance(int codePoint, Style style) {
        return font.getFontSet(style.getFont()).getGlyphInfo(codePoint, false).getAdvance(style.isBold());
    }

    @Override
    public int getLineHeight(Style style) {
        return font.lineHeight;
    }
}
