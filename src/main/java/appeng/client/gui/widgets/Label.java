package appeng.client.gui.widgets;

import appeng.client.gui.style.TextAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

/**
 * Label widget, usable on any composite widget panel.
 * Supports vertical and horizontal alignment, scaling, drop shadow and text clip
 */
public class Label extends AbstractStringWidget {
    private boolean dropShadow;
    private TextAlignment alignX;
    private TextAlignment alignY;
    private boolean clipWidth;
    private float scale;

    public Label(Component message, Font font) {
        this(0, 0, font.width(message.getVisualOrderText()), font.lineHeight, message, font);
    }

    public Label(int width, int height, Component message, Font font) {
        this(0, 0, width, height, message, font);
    }

    public Label(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);
        this.active = false;
        setDropShadow(false);
        setAlignX(TextAlignment.LEFT);
        setAlignY(TextAlignment.LEFT);
        setClipWidth(false);
        setScale(1.0f);
    }

    public boolean getDropShadow() {
        return dropShadow;
    }

    public TextAlignment getAlignX() {
        return alignX;
    }

    public TextAlignment getAlignY() {
        return alignY;
    }

    public boolean getClipWidth() {
        return clipWidth;
    }

    public float getScale() {
        return scale;
    }

    public Label setDropShadow(boolean dropShadow) {
        this.dropShadow = dropShadow;
        return this;
    }

    public Label setAlignX(TextAlignment alignX) {
        this.alignX = alignX;
        return this;
    }

    /**
     * @param alignY `LEFT` means `TOP`, `RIGHT` means `BOTTOM`
     */
    public Label setAlignY(TextAlignment alignY) {
        this.alignY = alignY;
        return this;
    }

    public Label setClipWidth(boolean clip) {
        this.clipWidth = clip;
        return this;
    }

    public Label setScale(float scale) {
        if (!Float.isFinite(scale) || scale <= 0.0)
            throw new IllegalArgumentException("`scale` must be finite positive value");
        this.scale = scale;
        return this;
    }

    public @NotNull Label setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        var message = this.getMessage();
        var font = this.getFont();

        var labelWidth = this.getWidth();
        var labelHeight = this.getHeight();

        var textWidth = font.width(message);
        var textHeight = font.lineHeight;

        var offsetX = alignToOffset(alignX);
        var offsetY = alignToOffset(alignY);

        if (scale == 1) {
            FormattedCharSequence drawText;

            if (clipWidth && textWidth > labelWidth) {
                drawText = this.clipText(message, labelWidth);
                textWidth = labelWidth;
            }
            else {
                drawText = message.getVisualOrderText();
            }

            var drawX = this.getX() + Math.round(offsetX * (labelWidth - textWidth));
            var drawY = this.getY() + Math.round(offsetY * (labelHeight - textHeight));

            guiGraphics.drawString(font, drawText, drawX, drawY, this.getColor(), dropShadow);
        } else {
            var labelLogicWidth = Math.round(labelWidth / scale);
            FormattedCharSequence drawText;
            if (clipWidth && textWidth > labelLogicWidth) {
                drawText = this.clipText(message, labelLogicWidth);
                textWidth = labelWidth;
            }
            else {
                drawText = message.getVisualOrderText();
                textWidth = Math.round(textWidth * scale);
            }

            textHeight = Math.round(textHeight * scale);

            var drawX = this.getX() + Math.round(offsetX * (labelWidth - textWidth));
            var drawY = this.getY() + Math.round(offsetY * (labelHeight - textHeight));

            guiGraphics.pose().pushPose();

            guiGraphics.pose().translate(drawX, drawY, 0);
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.drawString(font, drawText, 0, 0, this.getColor(), dropShadow);
            guiGraphics.pose().popPose();
        }
    }

    private FormattedCharSequence clipText(Component message, int width) {
        var font = this.getFont();
        var formattedtext = font.substrByWidth(message, width - font.width(CommonComponents.ELLIPSIS));
        return Language.getInstance().getVisualOrder(FormattedText.composite(formattedtext, CommonComponents.ELLIPSIS));
    }

    private static float alignToOffset(TextAlignment alignment) {
        return switch (alignment) {
            case LEFT -> 0.0f;
            case CENTER -> 0.5f;
            case RIGHT -> 1.0f;
        };
    }
}
