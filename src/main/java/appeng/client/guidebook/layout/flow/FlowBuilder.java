package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.RenderContext;
import appeng.client.guidebook.style.TextAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FlowBuilder {
    private final List<Line> lines = new ArrayList<>();

    private final List<LytFlowContent> rootContent = new ArrayList<>();

    // Bounding rectangles for any floats in this flow
    private final List<LineBlock> floats = new ArrayList<>();

    public void append(LytFlowContent content) {
        this.rootContent.add(content);
    }

    public LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth, TextAlignment alignment) {
        lines.clear();
        floats.clear();
        var lineBuilder = new LineBuilder(context, x, y, availableWidth, lines, floats, alignment);
        for (var content : rootContent) {
            visitInDocumentOrder(content, lineBuilder);
        }
        lineBuilder.end();

        // Build bounding box around all lines
        return lineBuilder.getBounds();
    }

    public void renderBatch(RenderContext context, MultiBufferSource buffers, @Nullable LytFlowContent hoveredContent) {
        for (var line : lines) {
            for (var el = line.firstElement(); el != null; el = el.next) {
                el.containsMouse = hoveredContent != null && hoveredContent.isInclusiveAncestor(el.getFlowContent());
                el.renderBatch(context, buffers);
            }
        }
    }

    public void renderFloatsBatch(RenderContext context, MultiBufferSource buffers, @Nullable LytFlowContent hoveredContent) {
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

    public void renderFloats(RenderContext context, @Nullable LytFlowContent hoveredContent) {
        for (var el : floats) {
            el.containsMouse = hoveredContent != null && hoveredContent.isInclusiveAncestor(el.getFlowContent());
            el.render(context);
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
    public LineElement pick(int x, int y) {
        var floatEl = pickFloatingElement(x, y);
        if (floatEl != null) {
            return floatEl;
        }

        for (var line : lines) {
            // Floating content overflows the line-box, but still belongs to the line
            // otherwise only hit-test line-elements if the line itself is hit
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
        return Stream.concat(lines.stream().flatMap(Line::elements), floats.stream())
                .filter(el -> el.getFlowContent() == content)
                .map(el -> el.bounds);
    }

    @Nullable
    public LineBlock pickFloatingElement(int x, int y) {
        for (var el : floats) {
            if (el.bounds.contains(x, y)) {
                return el;
            }
        }
        return null;
    }

    public boolean floatsIntersect(LytRect bounds) {
        for (var el : floats) {
            if (el.bounds.intersects(bounds)) {
                return true;
            }
        }
        return false;
    }
}
