package appeng.client.guidebook.document.block;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;
import appeng.client.guidebook.render.SymbolicColor;
import net.minecraft.client.renderer.MultiBufferSource;

public class LytThematicBreak extends LytBlock {
    @Override
    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        return new LytRect(x, y, availableWidth, 6);
    }

    @Override
    public void renderBatch(SimpleRenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(SimpleRenderContext context) {
        var line = bounds.withHeight(2).centerVerticallyIn(bounds);

        context.fillRect(line, SymbolicColor.THEMATIC_BREAK.ref());
    }
}
