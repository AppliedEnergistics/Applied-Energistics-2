package appeng.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;

import appeng.core.AppEng;

public final class AERenderPipelines {

    private AERenderPipelines() {
    }

    // RenderPipelines.MATRICES_PROJECTION_SNIPPET no longer exists. Uniforms are now grouped into
    // BindGroupLayouts; the equivalent of the old "Projection + DynamicTransforms" snippet is the pair of bind
    // group layouts below. The ordering (GLOBALS, PROJECTION, DYNAMIC_TRANSFORMS) mirrors vanilla's
    // RenderPipelines.DEBUG_FILLED_SNIPPET, which is the vanilla pipeline that uses the core/position_color
    // shader, so the bind group indices line up with what that shader expects.
    private static final RenderPipeline.Snippet MATRICES_PROJECTION_SNIPPET = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .buildSnippet();

    /**
     * Similar to {@link RenderPipelines#LINES}, but with inverted depth test.
     */
    // 26.3 switched to a reversed-Z depth buffer (depth is cleared to 0 and
    // DepthStencilState.DEFAULT is now GREATER_THAN_OR_EQUAL). Every explicit CompareOp in this file was
    // flipped accordingly: LESS_THAN_OR_EQUAL -> GREATER_THAN_OR_EQUAL and GREATER_THAN -> LESS_THAN.
    public static final RenderPipeline LINES_BEHIND_BLOCK = RenderPipelines.LINES.toBuilder()
            .withLocation(AppEng.makeId("pipeline/lines_behind_block"))
            // Vanilla split the old blended lines pipeline - RenderPipelines.LINES now carries a
            // plain ColorTargetState.DEFAULT with NO blend function, so inheriting it would draw these ghost
            // lines (ARGB.white(0.2f)) fully opaque. Restore blending explicitly.
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX = RenderPipeline
            .builder(MATRICES_PROJECTION_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            // withVertexFormat(format, mode) was split into withVertexBinding(index, format) and
            // withPrimitiveTopology(topology).
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX_SPARKLES = RenderPipeline
            .builder(MATRICES_PROJECTION_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox_sparkles"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public static final RenderPipeline AREA_OVERLAY_FACE = RenderPipeline
            .builder(MATRICES_PROJECTION_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_face"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line"))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE_OCCLUDED = RenderPipeline
            .builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line_occluded"))
            // LINES_SNIPPET carries no color target state and the 26.3 builder no longer supplies an
            // implicit one (a pipeline built without any active color target writes to no color attachment at
            // all), so one must be set explicitly. It has to be the BLENDED variant: these lines are drawn with
            // 0x30ffffff (alpha 48) and ColorTargetState.DEFAULT does not blend in 26.3, which would make them
            // solid white. Vanilla's equivalent is RenderPipelines.LINES_TRANSLUCENT.
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    public static final RenderPipeline STORAGE_CELL_LEDS = RenderPipeline
            .builder(MATRICES_PROJECTION_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/storage_cell_leds"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            // See AREA_OVERLAY_LINE_OCCLUDED - an explicit color target is now required for the
            // pipeline to write any color at all.
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    public static final RenderPipeline LIGHTNING_FX = RenderPipeline
            .builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/lightning_fx"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();

}
