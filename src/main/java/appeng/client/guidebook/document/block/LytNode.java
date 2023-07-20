package appeng.client.guidebook.document.block;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.style.Styleable;
import appeng.client.guidebook.style.TextStyle;
import appeng.libs.mdast.model.MdAstNode;

public abstract class LytNode implements Styleable {
    @Nullable
    protected LytNode parent;

    private TextStyle style = TextStyle.EMPTY;
    private TextStyle hoverStyle = TextStyle.EMPTY;

    /**
     * An optional association of this layout node with the node in the source document.
     */
    @Nullable
    private MdAstNode sourceNode;

    public void removeChild(LytNode node) {
    }

    public List<? extends LytNode> getChildren() {
        return Collections.emptyList();
    }

    /**
     * Get the document we're a part of, if any.
     */
    @Nullable
    public LytDocument getDocument() {
        var current = this;
        do {
            if (current instanceof LytDocument document) {
                return document;
            }
            current = current.parent;
        } while (current != null);
        return null;
    }

    @Nullable
    public final LytNode getParent() {
        return parent;
    }

    public abstract LytRect getBounds();

    public void onMouseEnter(@Nullable LytFlowContent hoveredContent) {
    }

    public void onMouseLeave() {
    }

    public void tick() {
    }

    @Nullable
    public LytNode pickNode(int x, int y) {
        if (!getBounds().contains(x, y)) {
            return null;
        }

        for (var child : getChildren()) {
            var node = child.pickNode(x, y);
            if (node != null) {
                return node;
            }
        }

        return this;
    }

    public final String getTextContent() {
        var visitor = new LytVisitor() {
            final StringBuilder builder = new StringBuilder();

            @Override
            public void text(String text) {
                builder.append(text);
            }
        };
        visit(visitor);
        return visitor.builder.toString();
    }

    public final LytVisitor.Result visit(LytVisitor visitor) {
        return visit(visitor, false);
    }

    /**
     * @param includeOutOfTreeContent If true, the visitor will also be passed content that is not part of the document
     *                                tree, such as tooltips, popups, etc.
     */
    public final LytVisitor.Result visit(LytVisitor visitor, boolean includeOutOfTreeContent) {
        var result = visitor.beforeNode(this);
        if (result == LytVisitor.Result.STOP) {
            return result;
        }
        if (result != LytVisitor.Result.SKIP_CHILDREN) {
            if (visitChildren(visitor, includeOutOfTreeContent) == LytVisitor.Result.STOP) {
                return LytVisitor.Result.STOP;
            }
        }
        return visitor.afterNode(this);
    }

    protected LytVisitor.Result visitChildren(LytVisitor visitor, boolean includeOutOfTreeContent) {
        for (var child : getChildren()) {
            if (child.visit(visitor, includeOutOfTreeContent) == LytVisitor.Result.STOP) {
                return LytVisitor.Result.STOP;
            }
        }
        return LytVisitor.Result.CONTINUE;
    }

    @Override
    public TextStyle getStyle() {
        return style;
    }

    @Override
    public void setStyle(TextStyle style) {
        this.style = style;
    }

    @Override
    public TextStyle getHoverStyle() {
        return hoverStyle;
    }

    @Override
    public void setHoverStyle(TextStyle style) {
        this.hoverStyle = style;
    }

    @Override
    public @Nullable Styleable getStylingParent() {
        return parent;
    }

    public @Nullable MdAstNode getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(@Nullable MdAstNode sourceNode) {
        this.sourceNode = sourceNode;
    }
}
