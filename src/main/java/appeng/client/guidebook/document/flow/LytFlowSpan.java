package appeng.client.guidebook.document.flow;

import appeng.client.guidebook.document.DefaultStyles;
import appeng.client.guidebook.style.ResolvedTextStyle;
import appeng.client.guidebook.style.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Attaches properties to a span of {@link LytFlowContent}, such as links
 * or formatting.
 */
public class LytFlowSpan extends LytFlowContent implements LytFlowContainer {

    private TextStyle style = TextStyle.EMPTY;

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

    public void modifyStyle(Consumer<TextStyle.Builder> customizer) {
        var builder = style.toBuilder();
        customizer.accept(builder);
        this.style = builder.build();
    }

    public ResolvedTextStyle resolveStyle() {
        if (getParentSpan() != null) {
            return style.mergeWith(getParentSpan().resolveStyle());
        }

        return style.mergeWith(DefaultStyles.BASE_STYLE);
    }
}
