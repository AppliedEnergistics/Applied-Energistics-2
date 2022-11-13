package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FlowBuilder {
    private final List<Line> lines = new ArrayList<>();
    private final LytFlowSpan rootSpan = new LytFlowSpan();

    public void append(LytFlowContent content) {
        rootSpan.append(content);
    }

    public LytRect computeLayout(LayoutContext context) {

        lines.clear();
        var lineBuilder = new LineBuilder(context, lines);
        visitInDocumentOrder(rootSpan, lineBuilder);
        lineBuilder.end();

        // Build bounding box around all lines
        return lines.stream()
                .map(Line::bounds)
                .reduce(LytRect.empty(), LytRect::union);
    }

    public void render(RenderContext context) {
        for (var line : lines) {
            for (var el = line.firstElement(); el != null; el = el.next) {
                el.render(context);
            }
        }
    }

    private void visitInDocumentOrder(LytFlowContent content, Consumer<LytFlowContent> visitor) {
        if (content instanceof LytFlowSpan flowSpan) {
            for (var child : flowSpan.getChildren()) {
                visitInDocumentOrder(child, visitor);
            }
        } else {
            visitor.accept(content);
        }
    }

}
