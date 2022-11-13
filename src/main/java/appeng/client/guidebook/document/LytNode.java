package appeng.client.guidebook.document;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

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
}
