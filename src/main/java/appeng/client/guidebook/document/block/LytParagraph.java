package appeng.client.guidebook.document.block;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.flow.FlowBuilder;
import appeng.client.guidebook.render.RenderContext;

public class LytParagraph extends LytBlock implements LytFlowContainer {
    protected final FlowBuilder content = new FlowBuilder();

    protected int paddingLeft = 5;
    protected int paddingTop;
    protected int paddingRight = 5;
    protected int paddingBottom;

    @Nullable
    protected LytFlowContent hoveredContent;

    @Override
    public void append(LytFlowContent child) {
        content.append(child);
    }

    @Override
    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Apply padding to paragraph content
        x += paddingLeft;
        availableWidth -= paddingLeft + paddingRight;
        y += paddingTop;

        var bounds = content.computeLayout(context, x, y, availableWidth);
        if (paddingBottom != 0) {
            return bounds.withHeight(bounds.height() + paddingBottom);
        }
        return bounds;
    }

    @Override
    public void onMouseEnter(@Nullable LytFlowContent hoveredContent) {
        super.onMouseEnter(hoveredContent);
        this.hoveredContent = hoveredContent;
    }

    @Override
    public void onMouseLeave() {
        super.onMouseLeave();
        this.hoveredContent = null;
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        content.renderBatch(context, buffers, hoveredContent);
    }

    @Override
    public void render(RenderContext context) {
        content.render(context, hoveredContent);
    }

    @Override
    public @Nullable LytFlowContent pickContent(int x, int y) {
        var lineEl = content.hitTest(x, y);
        return lineEl != null ? lineEl.getFlowContent() : null;
    }

    @Override
    public Stream<LytRect> enumerateContentBounds(LytFlowContent content) {
        return this.content.enumerateContentBounds(content);
    }
}
