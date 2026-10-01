package appeng.client.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.minecraft.client.renderer.BindGroupLayouts;
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
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    public static final RenderPipeline SPATIAL_SKYBOX = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
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
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
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
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
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
            .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN, false))
            .build();

    public static final RenderPipeline STORAGE_CELL_LEDS = RenderPipeline
            .builder(RenderPipelines.GLOBALS_SNIPPET)
            .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
            .withLocation(AppEng.makeId("pipeline/storage_cell_leds"))
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

}
