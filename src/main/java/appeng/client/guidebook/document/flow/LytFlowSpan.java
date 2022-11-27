package appeng.client.guidebook.document.flow;

import java.util.ArrayList;
import java.util.List;

import appeng.client.guidebook.style.Styleable;
import appeng.client.guidebook.style.TextStyle;
import org.jetbrains.annotations.Nullable;

/**
 * Attaches properties to a span of {@link LytFlowContent}, such as links or formatting.
 */
public class LytFlowSpan extends LytFlowContent implements LytFlowParent, Styleable {
    private final List<LytFlowContent> children = new ArrayList<>();

    public List<LytFlowContent> getChildren() {
        return children;
    }

    public void append(LytFlowContent child) {
        if (child.getParent() != null) {
            throw new IllegalStateException("Child is already owned by other span");
        }
        child.setParent(this);
        children.add(child);
    }
}
