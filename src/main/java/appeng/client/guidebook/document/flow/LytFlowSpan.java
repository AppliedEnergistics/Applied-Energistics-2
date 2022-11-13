package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.DefaultStyles;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * Attaches properties to a span of {@link LytFlowContent}, such as links
 * or formatting.
 */
public class LytFlowSpan extends LytFlowContent implements LytFlowContainer {

    private Style style = DefaultStyles.BODY_TEXT;

    private final List<LytFlowContent> children = new ArrayList<>();

    public List<LytFlowContent> getChildren() {
        return children;
    }

    @Override
    public void append(LytFlowContent child) {
        if (child.getParentSpan() != null) {
            throw new IllegalStateException("Child is already owned by other span");
        }
        child.setParentSpan(this);
        children.add(child);
    }

    public Style getEffectiveTextStyle() {
        return style;
    }
}
