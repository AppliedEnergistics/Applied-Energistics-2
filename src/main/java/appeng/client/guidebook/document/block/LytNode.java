package appeng.client.guidebook.document.block;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.style.Styleable;
import appeng.client.guidebook.style.TextStyle;

public abstract class LytNode implements Styleable {
    @Nullable
    protected LytNode parent;

    private TextStyle style = TextStyle.EMPTY;
    private TextStyle hoverStyle = TextStyle.EMPTY;

    public void removeChild(LytNode node) {
    }

    public List<? extends LytNode> getChildren() {
        return Collections.emptyList();
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
}
