package appeng.client.guidebook.layout.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;

public class FlowBuilder {
    private final List<Line> lines = new ArrayList<>();
    private final LytFlowSpan rootSpan = new LytFlowSpan();

    public LytFlowSpan getRootSpan() {
        return rootSpan;
    }

    public void append(LytFlowContent content) {
        rootSpan.append(content);
    }

    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {

        lines.clear();
        var lineBuilder = new LineBuilder(context, x, y, availableWidth, lines);
        visitInDocumentOrder(rootSpan, lineBuilder);
        lineBuilder.end();

        // Build bounding box around all lines
        return lines.stream()
                .map(Line::bounds)
                .reduce(LytRect.empty(), LytRect::union);
    }

    public void renderBatch(RenderContext context, MultiBufferSource buffers, @Nullable LytFlowContent hoveredContent) {
        for (var line : lines) {
            for (var el = line.firstElement(); el != null; el = el.next) {
                el.containsMouse = hoveredContent != null && hoveredContent.isInclusiveAncestor(el.getFlowContent());
                el.renderBatch(context, buffers);
            }
        }
    }

    public void render(RenderContext context, @Nullable LytFlowContent hoveredContent) {
        for (var line : lines) {
            for (var el = line.firstElement(); el != null; el = el.next) {
                el.containsMouse = hoveredContent != null && hoveredContent.isInclusiveAncestor(el.getFlowContent());
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

    @Nullable
    public LineElement hitTest(int x, int y) {
        for (var line : lines) {
            if (line.bounds().contains(x, y)) {
                for (var el = line.firstElement(); el != null; el = el.next) {
                    if (el.bounds.contains(x, y)) {
                        return el;
                    }
                }
            }
        }

        return null;
    }

    public Stream<LytRect> enumerateContentBounds(LytFlowContent content) {
        return lines.stream()
                .flatMap(Line::elements)
                .filter(el -> el.getFlowContent() == content)
                .map(el -> el.bounds);
    }
}
