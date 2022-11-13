package appeng.client.guidebook.document;

import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;

public abstract class LytBlock extends LytNode {
    protected LytRect bounds;

    public final LytRect layout(LayoutContext context) {
        bounds = computeLayout(context);
        return bounds;
    }

    protected abstract LytRect computeLayout(LayoutContext context);

    public abstract void render(SimpleRenderContext context);
}
