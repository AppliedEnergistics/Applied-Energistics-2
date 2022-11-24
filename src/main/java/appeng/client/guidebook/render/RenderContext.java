package appeng.client.guidebook.render;

import appeng.client.Point;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.client.guidebook.style.ResolvedTextStyle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec2;

public interface RenderContext {

    GuideScreen screen();

    PoseStack poseStack();

    LytRect viewport();

    int resolveColor(ColorRef ref);

    void fillRect(LytRect rect, ColorRef topLeft, ColorRef topRight, ColorRef bottomRight, ColorRef bottomLeft);

    void fillTriangle(Vec2 p1, Vec2 p2, Vec2 p3, ColorRef color);

    default Font font() {
        return Minecraft.getInstance().font;
    }

    default float getAdvance(int codePoint, ResolvedTextStyle style) {
        return font().getFontSet(style.font()).getGlyphInfo(codePoint, false)
                .getAdvance(Boolean.TRUE.equals(style.bold()));
    }

    default float getWidth(String text, ResolvedTextStyle style) {
        return (float) text.codePoints()
                .mapToDouble(cp -> getAdvance(cp, style))
                .sum();
    }

    default void renderText(String text, ResolvedTextStyle style, float x, float y) {
        var bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        renderTextInBatch(text, style, x, y, bufferSource);
        bufferSource.endBatch();
    }

    default void renderTextInBatch(String text, ResolvedTextStyle style, float x, float y, MultiBufferSource buffers) {
        var effectiveStyle = Style.EMPTY
                .withBold(style.bold())
                .withItalic(style.italic())
                .withUnderlined(style.underlined())
                .withStrikethrough(style.strikethrough())
                .withFont(style.font());

        var matrix = poseStack().last().pose();
        if (style.fontScale() != 1) {
            matrix = matrix.copy();

            matrix.multiplyWithTranslation(style.fontScale(), style.fontScale(), 1);
            matrix.translate(new Vector3f(x, y, 0));
            x = 0;
            y = 0;
        }

        font().drawInBatch(Component.literal(text).withStyle(effectiveStyle), x, y, resolveColor(style.color()), false,
                matrix, buffers, false, 0, LightTexture.FULL_BRIGHT);
    }

    default void fillRect(int x, int y, int width, int height, ColorRef color) {
        fillRect(new LytRect(x, y, width, height), color);
    }

    default void fillRect(LytRect rect, ColorRef color) {
        fillRect(rect, color, color, color, color);
    }

    default void fillGradientVertical(LytRect rect, ColorRef top, ColorRef bottom) {
        fillRect(rect, top, top, bottom, bottom);
    }

    default void fillGradientVertical(int x, int y, int width, int height, ColorRef top, ColorRef bottom) {
        fillGradientVertical(new LytRect(x, y, width, height), top, bottom);
    }

    default void fillGradientHorizontal(LytRect rect, ColorRef left, ColorRef right) {
        fillRect(rect, left, right, right, left);
    }

    default void fillGradientHorizontal(int x, int y, int width, int height, ColorRef left, ColorRef right) {
        fillGradientHorizontal(new LytRect(x, y, width, height), left, right);
    }

    default MultiBufferSource.BufferSource beginBatch() {
        return MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    }

    default void endBatch(MultiBufferSource.BufferSource batch) {
        batch.endBatch();
    }
}
