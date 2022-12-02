package appeng.client.guidebook.layout.flow;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.render.RenderContext;

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

    boolean floating;

    @Nullable
    public LytFlowContent getFlowContent() {
        return flowContent;
    }

    /**
     * Render text content as part of batch rendering.
     */
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    /**
     * Render any other content individually.
     */
    public void render(RenderContext context) {
    }
}
