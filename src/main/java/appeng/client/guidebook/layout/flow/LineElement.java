package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.render.RenderContext;
import org.jetbrains.annotations.Nullable;

public abstract class LineElement {
    /**
     * Next Element in flow direction.
     */
    @Nullable
    LineElement next;
    /**
     * Original content can be wrapped and result in one or many line elements.
     * These resulting line elements are joined in a linked-list fashion.
     */
    @Nullable
    LineElement wrappedPredecessor;
    /**
     * Original content can be wrapped and result in one or many line elements.
     * These resulting line elements are joined in a linked-list fashion.
     */
    @Nullable
    LineElement wrappedSuccessor;

    LytRect bounds = LytRect.empty();

    public abstract void render(RenderContext context);
}
