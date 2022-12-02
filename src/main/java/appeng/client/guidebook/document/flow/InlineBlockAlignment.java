package appeng.client.guidebook.document.flow;

/**
 * How an inline block element is supposed to be aligned within the flow layout.
 */
public enum InlineBlockAlignment {
    /**
     * Place it in the line like any other line element. This means text will not wrap around it to fill the height.
     */
    INLINE,
    /**
     * Float it to the left and wrap text around its right side.
     */
    FLOAT_LEFT,
    /**
     * Float it to the right and wrap text around its left side.
     */
    FLOAT_RIGHT
}
