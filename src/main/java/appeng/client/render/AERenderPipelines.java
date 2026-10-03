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
import net.minecraft.client.renderer.oit.OitPipelineSet;

import appeng.core.AppEng;

public final class AERenderPipelines {

    private AERenderPipelines() {
    }

    /**
     * Similar to {@link RenderPipelines#LINES_TRANSLUCENT}, but with inverted depth test.
     */
    public static final RenderPipeline LINES_BEHIND_BLOCK = RenderPipelines.LINES_TRANSLUCENT.toBuilder()
            .withLocation(AppEng.makeId("pipeline/lines_behind_block"))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX_SPARKLES = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withLocation(AppEng.makeId("pipeline/spatial_skybox_sparkles"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    public static final RenderPipeline AREA_OVERLAY_FACE = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withLocation(AppEng.makeId("pipeline/area_overlay_face"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .build();

    /**
     * Pipelines used for {@link #AREA_OVERLAY_FACE} when improved transparency (OIT) is enabled.
     */
    public static final OitPipelineSet OIT_AREA_OVERLAY_FACE = OitPipelineSet
            .builder(AppEng.makeId("area_overlay_face"),
                    RenderPipeline.builder(RenderPipelines.OIT_DEBUG_FILLED_SNIPPET)
                            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL))
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line"))
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
            .build();

    /**
     * Pipelines used for {@link #AREA_OVERLAY_LINE} when improved transparency (OIT) is enabled. The lines are blended
     * additively, so they do not contribute to the transmittance.
     */
    public static final OitPipelineSet OIT_AREA_OVERLAY_LINE = OitPipelineSet
            .builder(AppEng.makeId("area_overlay_line"), RenderPipeline.builder(RenderPipelines.OIT_LINES_SNIPPET)
                    .withShaderDefine("OIT_ADDITIVE"))
            .build();

    public static final RenderPipeline AREA_OVERLAY_LINE_OCCLUDED = RenderPipeline
            .builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/area_overlay_line_occluded"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    /**
     * Pipelines used for {@link #AREA_OVERLAY_LINE_OCCLUDED} and {@link #LINES_BEHIND_BLOCK} when improved transparency
     * (OIT) is enabled. Like the non-OIT pipelines, this only renders the parts of the lines that are hidden behind
     * other geometry.
     */
    public static final OitPipelineSet OIT_LINES_OCCLUDED = OitPipelineSet
            .builder(AppEng.makeId("lines_occluded"),
                    RenderPipeline.builder(RenderPipelines.OIT_LINES_SNIPPET))
            .withDepthBoundsModifier(AERenderPipelines::invertDepthTest)
            .withTransmittanceModifier(AERenderPipelines::invertDepthTest)
            .withAccumulateModifier(AERenderPipelines::invertDepthTest)
            .build();

    public static final RenderPipeline STORAGE_CELL_LEDS = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.DYNAMIC_TRANSFORMS)
            .withBindGroupLayout(BindGroupLayouts.PROJECTION)
            .withLocation(AppEng.makeId("pipeline/storage_cell_leds"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
            .withPrimitiveTopology(PrimitiveTopology.QUADS)
            .withDepthStencilState(DepthStencilState.DEFAULT)
            .build();

    public static final RenderPipeline LIGHTNING_FX = RenderPipeline
            .builder(RenderPipelines.PARTICLE_SNIPPET)
            .withLocation(AppEng.makeId("pipeline/lightning_fx"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();

    /**
     * Pipelines used for {@link #LIGHTNING_FX} when improved transparency (OIT) is enabled.
     */
    public static final OitPipelineSet OIT_LIGHTNING_FX = OitPipelineSet
            .builder(AppEng.makeId("lightning_fx"), RenderPipeline.builder(RenderPipelines.OIT_PARTICLE_SNIPPET)
                    .withCull(false))
            .build();

    private static void invertDepthTest(RenderPipeline.Builder builder) {
        builder.withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false));
    }

}
