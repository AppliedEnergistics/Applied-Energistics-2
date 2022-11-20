package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.render.RenderContext;
import org.jetbrains.annotations.Nullable;

public abstract class LineElement {
    /**
     * Next Element in flow direction.
     */
    @Nullable
    LineElement next;

    LytRect bounds = LytRect.empty();

    /**
     * The original flow content this line element is associated with.
     */
    @Nullable
    LytFlowContent flowContent;

    boolean containsMouse;

    @Nullable
    public LytFlowContent getFlowContent() {
        return flowContent;
    }

    public abstract void render(RenderContext context);
}
