package appeng.client.guidebook.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import appeng.client.gui.Icon;
import appeng.client.gui.style.BackgroundGenerator;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.style.ResolvedTextStyle;

public interface RenderContext {

    LightDarkMode lightDarkMode();

    default boolean isDarkMode() {
        return lightDarkMode() == LightDarkMode.DARK_MODE;
    }

    PoseStack poseStack();

    LytRect viewport();

    int resolveColor(ColorRef ref);

    void fillRect(LytRect rect, ColorRef topLeft, ColorRef topRight, ColorRef bottomRight, ColorRef bottomLeft);

    default void fillTexturedRect(LytRect rect, AbstractTexture texture, ColorRef topLeft, ColorRef topRight,
            ColorRef bottomRight, ColorRef bottomLeft) {
        // Just use the entire texture by default
        fillTexturedRect(rect, texture, topLeft, topRight, bottomRight, bottomLeft, 0, 0, 1, 1);
    }

    void fillTexturedRect(LytRect rect, AbstractTexture texture, ColorRef topLeft, ColorRef topRight,
            ColorRef bottomRight, ColorRef bottomLeft, float u0, float v0, float u1, float v1);

    default void fillTexturedRect(LytRect rect, GuidePageTexture texture) {
        fillTexturedRect(rect, texture.use(), ColorRef.WHITE);
    }

    default void fillTexturedRect(LytRect rect, AbstractTexture texture) {
        fillTexturedRect(rect, texture, ColorRef.WHITE);
    }

    default void fillTexturedRect(LytRect rect, AbstractTexture texture, ColorRef color) {
        fillTexturedRect(rect, texture, color, color, color, color);
    }

    default void fillTexturedRect(LytRect rect, GuidePageTexture texture, ColorRef color) {
        fillTexturedRect(rect, texture.use(), color, color, color, color);
    }

    default void fillTexturedRect(LytRect rect, TextureAtlasSprite sprite, ColorRef color) {
        var texture = Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());
        fillTexturedRect(rect, texture, color, color, color, color,
                sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1());
    }

    default void drawIcon(int x, int y, Icon icon, ColorRef color) {
        var u0 = icon.x / (float) Icon.TEXTURE_WIDTH;
        var v0 = icon.y / (float) Icon.TEXTURE_HEIGHT;
        var u1 = (icon.x + icon.width) / (float) Icon.TEXTURE_WIDTH;
        var v1 = (icon.y + icon.height) / (float) Icon.TEXTURE_HEIGHT;

        var texture = Minecraft.getInstance().getTextureManager().getTexture(Icon.TEXTURE);
        fillTexturedRect(new LytRect(x, y, icon.width, icon.height), texture, color, color, color, color,
                u0, v0, u1, v1);
    }

    default void fillTexturedRect(LytRect rect, ResourceLocation textureId) {
        fillTexturedRect(rect, textureId, ColorRef.WHITE);
    }

    default void fillTexturedRect(LytRect rect, ResourceLocation textureId, ColorRef color) {
        var texture = Minecraft.getInstance().getTextureManager().getTexture(textureId);
        fillTexturedRect(rect, texture, color);
    }

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
            matrix = new Matrix4f(matrix);

            matrix.translate(style.fontScale(), style.fontScale(), 1);
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

    default void renderItem(ItemStack stack, int x, int y, float width, float height) {
        renderItem(stack, x, y, 0, width, height);
    }

    void renderItem(ItemStack stack, int x, int y, int z, float width, float height);

    default void renderPanel(LytRect bounds) {
        BackgroundGenerator.draw(bounds.width(), bounds.height(), poseStack(), 0, bounds.x(), bounds.y());
    }
}
