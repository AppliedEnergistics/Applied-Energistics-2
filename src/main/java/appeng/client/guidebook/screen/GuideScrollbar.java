package appeng.client.guidebook.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class GuideScrollbar extends AbstractWidget {
    private static final int WIDTH = 8;
    private int contentHeight;
    private int scrollAmount;
    private Double thumbHeldAt;

    public GuideScrollbar() {
        super(0, 0, 0, 0, Component.empty());
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {
    }

    protected int getMaxScrollAmount() {
        return Math.max(0, contentHeight - (this.height - 4));
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        var maxScrollAmount = getMaxScrollAmount();
        if (maxScrollAmount <= 0) {
            return;
        }

        int thumbHeight = getThumbHeight();
        int left = x;
        int right = x + 8;
        int top = y + getThumbTop();
        int bottom = top + thumbHeight;
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(left, bottom, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(right, bottom, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(right, top, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(left, top, 0.0).color(128, 128, 128, 255).endVertex();
        bufferBuilder.vertex(left, bottom - 1, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(right - 1, bottom - 1, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(right - 1, top, 0.0).color(192, 192, 192, 255).endVertex();
        bufferBuilder.vertex(left, top, 0.0).color(192, 192, 192, 255).endVertex();
        tesselator.end();
    }

    /**
     * The thumb is the draggable rectangle representing the current viewport being manipulated by the scrollbar.
     */
    private int getThumbTop() {
        if (getMaxScrollAmount() == 0) {
            return 0;
        }
        return Math.max(0, scrollAmount * (height - getThumbHeight()) / getMaxScrollAmount());
    }

    private int getThumbHeight() {
        if (contentHeight <= 0) {
            return 0;
        }
        return Mth.clamp((int) ((float) (this.height * this.height) / (float) contentHeight), 32, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.visible || button != 0) {
            return false;
        }

        var thumbTop = y + getThumbTop();
        var thumbBottom = thumbTop + getThumbHeight();

        boolean thumbHit = mouseX >= x
                && mouseX <= x + WIDTH
                && mouseY >= thumbTop
                && mouseY < thumbBottom;
        if (thumbHit) {
            this.thumbHeldAt = mouseY - thumbTop;
            return true;
        } else {
            this.thumbHeldAt = null;
            return false;
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseReleased(mouseX, mouseY, button);
        }

        this.thumbHeldAt = null;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.visible && this.thumbHeldAt != null) {

            var thumbY = (int) Math.round(mouseY - y - thumbHeldAt);
            var maxThumbY = height - getThumbHeight();
            var scrollAmount = (int) Math.round(thumbY / (double) maxThumbY * getMaxScrollAmount());
            setScrollAmount(scrollAmount);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.visible) {
            this.setScrollAmount((int) (this.scrollAmount - delta * 10));
            return true;
        } else {
            return false;
        }
    }

    public void move(int x, int y, int height) {
        this.x = x;
        this.y = y;
        this.width = WIDTH;
        this.height = height;
    }

    public void setContentHeight(int contentHeight) {
        this.contentHeight = contentHeight;
    }

    public int getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(int scrollAmount) {
        this.scrollAmount = Mth.clamp(scrollAmount, 0, getMaxScrollAmount());
    }
}
