package appeng.client.guidebook.document.block;

import org.joml.Vector2i;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;

public abstract class LytBlock extends LytNode {
    /**
     * Content rectangle.
     */
    protected LytRect bounds = LytRect.empty();

    private int marginTop;
    private int marginLeft;
    private int marginRight;
    private int marginBottom;

    @Override
    public LytRect getBounds() {
        return bounds;
    }

    public boolean isCulled(LytRect viewport) {
        return !viewport.intersects(bounds);
    }

    public final void setLayoutPos(Vector2i point) {
        var deltaX = point.x - bounds.x();
        var deltaY = point.y - bounds.y();
        if (deltaX != 0 || deltaY != 0) {
            bounds = bounds.withX(point.x).withY(point.y);
            onLayoutMoved(deltaX, deltaY);
        }
    }

    public final LytRect layout(LayoutContext context, int x, int y, int availableWidth) {
        bounds = computeLayout(context, x, y, availableWidth);
        return bounds;
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public int getMarginStart(LytAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> getMarginLeft();
            case VERTICAL -> getMarginTop();
        };
    }

    public int getMarginEnd(LytAxis axis) {
        return switch (axis) {
            case HORIZONTAL -> getMarginRight();
            case VERTICAL -> getMarginBottom();
        };
    }

    protected abstract LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth);

    /**
     * Implement to react to layout previously computed by {@link #computeLayout} being moved.
     */
    protected abstract void onLayoutMoved(int deltaX, int deltaY);

    public abstract void renderBatch(RenderContext context, MultiBufferSource buffers);

    public abstract void render(RenderContext context);
}
