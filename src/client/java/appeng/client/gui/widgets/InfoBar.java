package appeng.client.gui.widgets;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.client.gui.style.Blitter;
import appeng.util.Icon;

public class InfoBar {
    private final List<Widget> widgets = new ArrayList<>();

    public void render(GuiGraphics guiGraphics, int x, int y) {
        var maxHeight = widgets.stream().mapToInt(Widget::getHeight).max().orElse(0);

        for (var widget : widgets) {
            widget.render(
                    guiGraphics,
                    x,
                    Math.round(y + maxHeight / 2.f - widget.getHeight() / 2.f));
            x += widget.getWidth();
        }

    }

    interface Widget {
        int getWidth();

        int getHeight();

        void render(GuiGraphics guiGraphics, int x, int y);
    }

    // TODO (RID): Added xPos and yPos to give me better control over render, but the code below might need refactoring
    void add(Icon icon, float scale, int xPos, int yPos) {
        widgets.add(new IconWidget(icon, scale, xPos, yPos));
    }

    void add(String text, int color, float scale, int xPos, int yPos) {
        widgets.add(new TextWidget(Component.literal(text), color, scale, xPos, yPos));
    }

    void add(Component text, int color, float scale, int xPos, int yPos) {
        widgets.add(new TextWidget(text, color, scale, xPos, yPos));
    }

    void add(AEKey what, float scale, int xPos, int yPos) {
        widgets.add(new StackWidget(what, scale, xPos, yPos));
    }

    void add(ItemLike what, float scale, int xPos, int yPos) {
        widgets.add(new StackWidget(AEItemKey.of(what), scale, xPos, yPos));
    }

    void addSpace(int width) {
        widgets.add(new SpaceWidget(width));
    }

    private record StackWidget(AEKey what, float scale, int xPos, int yPos) implements Widget {
        @Override
        public int getWidth() {
            return Math.round(16 * scale);
        }

        @Override
        public int getHeight() {
            return Math.round(16 * scale);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(xPos, yPos);
            poseStack.scale(scale);
            AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, 0, 0, what);
            poseStack.popMatrix();
        }
    }

    private record IconWidget(Icon icon, float scale, int xPos, int yPos) implements Widget {
        @Override
        public int getWidth() {
            return Math.round(16 * scale);
        }

        @Override
        public int getHeight() {
            return Math.round(16 * scale);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(xPos, yPos);
            poseStack.scale(scale);
            Blitter.icon(icon)
                    .dest(0, 0)
                    .blit(guiGraphics);
            poseStack.popMatrix();
        }
    }

    private static final class TextWidget implements Widget {
        private final Component text;
        private final int color;
        private final float scale;
        private final int xPos;
        private final int yPos;
        private final int width;
        private final int height;

        public TextWidget(Component text, int color, float scale, int xPos, int yPos) {
            this.text = text;
            this.color = color;
            this.scale = scale;
            this.xPos = xPos;
            this.yPos = yPos;
            var font = Minecraft.getInstance().font;
            this.width = Math.round(font.width(text) * scale);
            this.height = Math.round(font.lineHeight * scale);
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
            var poseStack = guiGraphics.pose();
            var font = Minecraft.getInstance().font;
            poseStack.pushMatrix();
            poseStack.translate(xPos, yPos);
            poseStack.scale(scale);
            guiGraphics.drawString(font, text, 0, 0, color, false);
            poseStack.popMatrix();
        }
    }

    private static final class SpaceWidget implements Widget {
        private final int width;

        public SpaceWidget(int width) {
            this.width = width;
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int x, int y) {
        }
    }
}
