package appeng.client.guidebook.document;

import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;

public abstract class LytBlock extends LytNode {
    protected LytRect bounds;

    private int marginTop;
    private int marginLeft;
    private int marginRight;
    private int marginBottom;

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

    protected abstract LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth);

    public abstract void render(SimpleRenderContext context);
}
