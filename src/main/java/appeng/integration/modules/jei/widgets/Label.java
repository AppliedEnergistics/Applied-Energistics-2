package appeng.integration.modules.jei.widgets;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

public class Label implements Widget {
    public final float x;
    public final float y;
    public final Component text;
    private final int width;
    private final Font font;
    @Nullable
    public Component tooltip;
    public int color = -1;
    public boolean shadow = true;
    private LabelAlignment align = LabelAlignment.CENTER;

    public Label(float x, float y, Component text) {
        this.x = x;
        this.y = y;
        this.text = text;
        font = Minecraft.getInstance().font;
        this.width = font.width(text);
    }

    @Override
    public void draw(PoseStack stack) {
        float alignedX = getAlignedX();

        if (shadow) {
            font.drawShadow(stack, text, alignedX, y, color);
        } else {
            font.draw(stack, text, alignedX, y, color);
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

    @Override
    public boolean hitTest(double x, double y) {
        var alignedX = getAlignedX();
        return x >= alignedX && x < alignedX + width
                && y >= this.y && y < this.y + font.lineHeight;
    }

    @Override
    public List<Component> getTooltipLines() {
        if (tooltip != null) {
            return List.of(tooltip);
        }
        return List.of();
    }

    private float getAlignedX() {
        return switch (align) {
            case LEFT -> x;
            case CENTER -> x - width / 2f;
            case RIGHT -> x - width;
        };
    }

    private enum LabelAlignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
