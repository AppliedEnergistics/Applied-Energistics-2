package appeng.client.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderPipelines;

import appeng.core.AppEng;

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

    public static final RenderPipeline SPATIAL_SKYBOX = RenderPipeline.builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withoutBlend()
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX_SPARKLES = RenderPipeline
            .builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox_sparkles"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.ADDITIVE)
            .withDepthWrite(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderPipeline AREA_OVERLAY_FACE = RenderPipeline
            .builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_face"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withDepthWrite(false)
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS)
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line"))
            .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
            .withBlend(BlendFunction.ADDITIVE)
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE_OCCLUDED = RenderPipeline
            .builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line_occluded"))
            .withDepthTestFunction(DepthTestFunction.GREATER_DEPTH_TEST)
            .withDepthWrite(false)
            .build();

    public static final RenderPipeline STORAGE_CELL_LEDS = RenderPipeline
            .builder(RenderPipelines.MATRICES_COLOR_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/storage_cell_leds"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
            .build();

}
