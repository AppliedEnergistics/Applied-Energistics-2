package appeng.client.guidebook.document;

import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.flow.FlowBuilder;
import appeng.client.guidebook.render.SimpleRenderContext;

public class LytParagraph extends LytBlock implements LytFlowContainer {
    protected final FlowBuilder content = new FlowBuilder();

    protected int paddingLeft = 5;
    protected int paddingTop;
    protected int paddingRight = 5;
    protected int paddingBottom;

    @Override
    public void append(LytFlowContent child) {
        content.append(child);
    }

    @Override
    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        // Apply padding to paragraph content

        var bounds = content.computeLayout(context, x, y, availableWidth);
        if (paddingBottom != 0) {
            return bounds.withHeight(bounds.height() + paddingBottom);
        }
        return bounds;
    }

    @Override
    public void render(SimpleRenderContext context) {
        content.render(context);
    }
}
