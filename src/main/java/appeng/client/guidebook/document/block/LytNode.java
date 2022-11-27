package appeng.client.guidebook.document.block;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;

public abstract class LytNode {
    @Nullable
    LytNode parent;

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
    public LytNode hitTestNode(int x, int y) {
        if (!getBounds().contains(x, y)) {
            return null;
        }

        for (var child : getChildren()) {
            var node = child.hitTestNode(x, y);
            if (node != null) {
                return node;
            }
        }

        return this;
    }
}
