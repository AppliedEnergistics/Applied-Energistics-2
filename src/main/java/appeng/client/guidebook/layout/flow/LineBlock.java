package appeng.client.guidebook.layout.flow;

import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.render.RenderContext;

/**
 * Standalone block in-line with other content.
 */
public class LineBlock extends LineElement {

    private final LytBlock block;

    public LineBlock(LytBlock block) {
        this.block = block;
    }

    @Override
    public void render(RenderContext context) {
        context.poseStack().pushPose();
        context.poseStack().translate(bounds.x(), bounds.y(), 0);
        block.render(context);
        context.poseStack().popPose();
    }
}
