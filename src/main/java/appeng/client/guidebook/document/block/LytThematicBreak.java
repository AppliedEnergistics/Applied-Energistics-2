package appeng.client.guidebook.document.block;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;

public class LytThematicBreak extends LytBlock {
    @Override
    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, availableWidth, 6);
    }

    @Override
    protected void onLayoutMoved(int deltaX, int deltaY) {
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        var line = bounds.withHeight(2).centerVerticallyIn(bounds);

        context.fillRect(line, SymbolicColor.THEMATIC_BREAK);
    }
}
