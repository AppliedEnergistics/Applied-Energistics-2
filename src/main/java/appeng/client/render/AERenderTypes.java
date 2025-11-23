package appeng.client.render;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class AERenderTypes {

    private AERenderTypes() {
    }

    /**
     * Similar to {@link RenderTypes#LINES}, but with inverted depth test.
     */
    public static final RenderType LINES_BEHIND_BLOCK = RenderType.create(
            "ae2:lines_behind_block",
            RenderSetup.builder(AERenderPipelines.LINES_BEHIND_BLOCK)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
                    .createRenderSetup());

    public static final RenderType SPATIAL_SKYBOX = RenderType.create(
            "ae2_spatial_sky_skybox",
            RenderSetup.builder(AERenderPipelines.SPATIAL_SKYBOX).bufferSize(8192).createRenderSetup());

    public static final RenderType LIGHTNING_FX = RenderType.create(
            "ae2_lightning_fx",
            RenderSetup.builder(AERenderPipelines.LIGHTNING_FX)
                    .bufferSize(8192)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .createRenderSetup());

    /**
     * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
     */
    public static final RenderType AREA_OVERLAY_FACE = RenderType.create(
            "ae2_area_overlay_face",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_FACE)
                    .bufferSize(65535)
                    .createRenderSetup());

    // TODO 1.21.11: Where this is used, the lineWidth vertex attribute has to be set to 3
    public static final RenderType AREA_OVERLAY_LINE = RenderType.create(
            "ae2_area_overlay_line",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_LINE)
                    .bufferSize(65535)
                    .createRenderSetup());

    // TODO 1.21.11: Where this is used, the lineWidth vertex attribute has to be set to 3
    public static final RenderType AREA_OVERLAY_LINE_OCCLUDED = RenderType.create(
            "ae2_area_overlay_line_occluded",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_LINE_OCCLUDED)
                    .bufferSize(65535)
                    .createRenderSetup());

    public static final RenderType STORAGE_CELL_LEDS = RenderType.create(
            "ae2_drive_leds",
            RenderSetup.builder(AERenderPipelines.STORAGE_CELL_LEDS).bufferSize(32565).createRenderSetup());

}
