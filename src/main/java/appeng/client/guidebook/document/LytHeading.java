package appeng.client.guidebook.document;

import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.SimpleRenderContext;

public class LytHeading extends LytBlock implements LytFlowContainer {
    private int depth;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public void append(LytFlowContent child) {

    }

    @Override
    protected LytRect computeLayout(LayoutContext context) {
        // TODO: Compute actual height
        return context.available().withHeight(20);
    }

    @Override
    public void render(SimpleRenderContext context) {

    }
}
