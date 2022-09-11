package appeng.integration.modules.jei.widgets;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class Label implements Widget {
    public final float x;
    public final float y;
    public final Component text;
    private final Font font;
    @Nullable
    public Component tooltip;
    public int color = -1;
    public int maxWidth = -1;
    public boolean shadow = true;
    private LabelAlignment align = LabelAlignment.CENTER;
    private List<FormattedLine> formattedLines = null;

    public Label(float x, float y, Component text) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void draw(PoseStack stack) {
        for (var line : getLines()) {
            if (shadow) {
                font.drawShadow(stack, line.text, line.x, line.y, color);
            } else {
                font.draw(stack, line.text, line.x, line.y, color);
            }
        }
    }

    public Label bodyColor() {
        color = 0x7E7E7E;
        return this;
    }

    public Label alignLeft() {
        align = LabelAlignment.LEFT;
        return this;
    }

    public Label alignRight() {
        align = LabelAlignment.RIGHT;
        return this;
    }

    public Label tooltip(Component text) {
        this.tooltip = text;
        return this;
    }

    public Label noShadow() {
        shadow = false;
        return this;
    }

    public Label bodyText() {
        noShadow();
        bodyColor();
        return this;
    }

    @Override
    public boolean hitTest(double x, double y) {
        for (var line : getLines()) {
            if (x >= line.x && x < line.x + line.width
                    && y >= line.y && y < line.y + line.height) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Component> getTooltipLines() {
        if (tooltip != null) {
            return List.of(tooltip);
        }
        return List.of();
    }

    private float getAlignedX(int width) {
        return switch (align) {
            case LEFT -> x;
            case CENTER -> x - width / 2f;
            case RIGHT -> x - width;
        };
    }

    public Label maxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    private enum LabelAlignment {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * Lazily apply the max-width using current settings and split into a line-based layout.
     */
    private List<FormattedLine> getLines() {
        if (formattedLines != null) {
            return formattedLines;
        }

        if (maxWidth == -1) {
            var formattedText = text.getVisualOrderText();
            var width = font.width(formattedText);
            formattedLines = List.of(
                    new FormattedLine(formattedText, getAlignedX(width), y, width, font.lineHeight));
        } else {
            var splitLines = font.split(text, maxWidth);
            var formattedLines = new ArrayList<FormattedLine>(splitLines.size());
            for (int i = 0; i < splitLines.size(); i++) {
                var splitLine = splitLines.get(i);
                var width = font.width(splitLine);
                formattedLines.add(new FormattedLine(
                        splitLine,
                        getAlignedX(width),
                        y + i * font.lineHeight,
                        width,
                        font.lineHeight));
            }
            this.formattedLines = formattedLines;
        }

        return formattedLines;
    }

    private record FormattedLine(FormattedCharSequence text, float x, float y, int width, int height) {
    }
}
