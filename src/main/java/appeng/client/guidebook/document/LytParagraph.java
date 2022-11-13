package appeng.client.guidebook.document;

import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.layout.flow.FlowBuilder;
import appeng.client.guidebook.render.SimpleRenderContext;

public class LytParagraph extends LytBlock implements LytFlowContainer {
    private final FlowBuilder content = new FlowBuilder();

    @Override
    public void append(LytFlowContent child) {
        content.append(child);
    }

    @Override
    public LytRect computeLayout(LayoutContext context) {
        return content.computeLayout(context);
    }

    @Override
    public void render(SimpleRenderContext context) {
        content.render(context);
    }
}
