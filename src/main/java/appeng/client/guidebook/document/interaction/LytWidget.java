package appeng.client.guidebook.document.interaction;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.screen.GuideScreen;

/**
 * Wraps an {@link AbstractWidget} for use within the guidebook layout tree.
 */
public class LytWidget extends LytBlock implements InteractiveElement {
    private final AbstractWidget widget;

    public LytWidget(AbstractWidget widget) {
        this.widget = widget;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(
                x, y,
                widget.getWidth(), widget.getHeight());
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {
        widget.setX(widget.getX() + deltaX);
        widget.setY(widget.getY() + deltaY);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        updateWidgetPosition();

        var minecraft = Minecraft.getInstance();

        if (!(minecraft.screen instanceof GuideScreen guideScreen)) {
            return; // Can't render if we can't translate
        }

        var mouseX = (minecraft.mouseHandler.xpos() * minecraft.getWindow().getGuiScaledWidth()
                / minecraft.getWindow().getScreenWidth());
        var mouseY = (minecraft.mouseHandler.ypos() * minecraft.getWindow().getGuiScaledHeight()
                / minecraft.getWindow().getScreenHeight());

        var mouseDocPos = guideScreen.getDocumentPoint(mouseX, mouseY);

        widget.render(
                context.guiGraphics(),
                mouseDocPos != null ? mouseDocPos.getX() : -100,
                mouseDocPos != null ? mouseDocPos.getY() : -100,
                minecraft.getDeltaFrameTime());
    }

    private void updateWidgetPosition() {
        widget.setPosition(bounds.x(), bounds.y());
    }

    @Override
    public boolean mouseMoved(GuideScreen screen, int x, int y) {
        widget.mouseMoved(x, y);
        return true;
    }

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        return widget.mouseClicked(x, y, button);
    }

    @Override
    public boolean mouseReleased(GuideScreen screen, int x, int y, int button) {
        return widget.mouseReleased(x, y, button);
    }

    @Override
    public Optional<GuideTooltip> getTooltip(float x, float y) {
        return Optional.empty();
    }

    public AbstractWidget getWidget() {
        return widget;
    }
}
