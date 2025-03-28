package appeng.client.render;

import appeng.core.AppEng;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.renderer.RenderPipelines;

public final class AERenderPipelines {

    private AERenderPipelines() {
    }

    /**
     * Similar to {@link RenderPipelines#LINES}, but with inverted depth test.
     */
    public static final RenderPipeline LINES_BEHIND_BLOCK = RenderPipelines.LINES.toBuilder()
            .withLocation(AppEng.makeId("pipeline/lines_behind_block"))
            .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
            .build();
}
