package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;

import java.util.List;

public class LytList extends LytVBox {
    private boolean ordered;
    private int start;

    public LytList(boolean ordered, int start) {
        this.ordered = ordered;
        this.start = start;
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Pad to the right for the list bullet

        return super.computeLayout(context, x, y, availableWidth);
    }

    @Override
    public void render(SimpleRenderContext context) {
        // Render the list bullet

        super.render(context);
    }
}
