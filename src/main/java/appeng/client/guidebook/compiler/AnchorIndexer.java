package appeng.client.guidebook.compiler;

import java.util.Locale;
import java.util.Stack;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.block.LytDocument;
import appeng.client.guidebook.document.block.LytHeading;
import appeng.client.guidebook.document.block.LytNode;
import appeng.client.guidebook.document.block.LytVisitor;
import appeng.client.guidebook.document.flow.LytFlowAnchor;
import appeng.client.guidebook.document.flow.LytFlowContent;

/**
 * Indexes all anchors within a page to allow faster navigation to them.
 */
public final class AnchorIndexer {

    private final LytDocument document;

    public AnchorIndexer(LytDocument document) {
        this.document = document;
    }

    public AnchorTarget get(String anchor) {
        var visitor = new LytVisitor() {
            final Stack<LytNode> nodeStack = new Stack<>();
            AnchorTarget target;

            @Override
            public Result beforeNode(LytNode node) {
                if (node instanceof LytHeading heading) {
                    var headingAnchor = normalizeAnchor(heading.getTextContent());
                    if (headingAnchor.equals(anchor)) {
                        target = new AnchorTarget(node, null);
                        return Result.STOP;
                    }
                }

                nodeStack.push(node);
                return Result.CONTINUE;
            }

            @Override
            public Result afterNode(LytNode node) {
                nodeStack.pop();
                return Result.CONTINUE;
            }

            @Override
            public Result beforeFlowContent(LytFlowContent content) {
                if (content instanceof LytFlowAnchor flowAnchor) {
                    if (anchor.equals(flowAnchor.getName())) {
                        target = new AnchorTarget(nodeStack.peek(), content);
                        return Result.STOP;
                    }
                }
                return Result.CONTINUE;
            }
        };
        document.visit(visitor);
        return visitor.target;
    }

    private String normalizeAnchor(String anchor) {
        return anchor.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", "-");
    }

    public record AnchorTarget(LytNode blockNode, @Nullable LytFlowContent flowContent) {
    }
}
