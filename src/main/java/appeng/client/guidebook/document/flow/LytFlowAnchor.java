package appeng.client.guidebook.document.flow;

import java.util.OptionalInt;

/**
 * Zero-Width Flow-Content that can be referred to by links.
 */
public class LytFlowAnchor extends LytFlowContent {
    private final String name;

    private int layoutY;

    public LytFlowAnchor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public OptionalInt getLayoutY() {
        return layoutY >= 0 ? OptionalInt.of(layoutY) : OptionalInt.empty();
    }

    public void setLayoutY(int layoutY) {
        this.layoutY = layoutY;
    }
}
