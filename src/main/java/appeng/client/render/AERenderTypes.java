package appeng.client.render;

import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class AERenderTypes {

    private AERenderTypes() {
    }

    // RenderSetup no longer has an output-target or a buffer-size setting.
    // - OutputTarget (and OutputTarget.ITEM_ENTITY_TARGET) was removed outright; vanilla's own
    // RenderTypes.LINES dropped its .setOutputTarget(ITEM_ENTITY_TARGET) call in 26.3 too, so the
    // .setOutputTarget(...) calls below are simply gone.
    // - bufferSize(int) was removed; RenderType now uses fixed buffer sizes internally
    // (RenderType.BIG_BUFFER_SIZE / SMALL_BUFFER_SIZE / TRANSIENT_BUFFER_SIZE), so the per-type sizes
    // AE2 used to request are no longer configurable.

    /**
     * Similar to {@link RenderTypes#LINES}, but with inverted depth test.
     */
    public static final RenderType LINES_BEHIND_BLOCK = RenderType.create(
            "ae2:lines_behind_block",
            RenderSetup.builder(AERenderPipelines.LINES_BEHIND_BLOCK)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType SPATIAL_SKYBOX = RenderType.create(
            "ae2_spatial_sky_skybox",
            RenderSetup.builder(AERenderPipelines.SPATIAL_SKYBOX).createRenderSetup());

    public static final RenderType LIGHTNING_FX = RenderType.create(
            "ae2_lightning_fx",
            RenderSetup.builder(AERenderPipelines.LIGHTNING_FX)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_PARTICLES)
                    .useLightmap()
                    .createRenderSetup());

    /**
     * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
     */
    public static final RenderType AREA_OVERLAY_FACE = RenderType.create(
            "ae2_area_overlay_face",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_FACE)
                    .createRenderSetup());

    public static final RenderType AREA_OVERLAY_LINE = RenderType.create(
            "ae2_area_overlay_line",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_LINE)
                    .createRenderSetup());

    public static final RenderType AREA_OVERLAY_LINE_OCCLUDED = RenderType.create(
            "ae2_area_overlay_line_occluded",
            RenderSetup.builder(AERenderPipelines.AREA_OVERLAY_LINE_OCCLUDED)
                    .createRenderSetup());

    public static final RenderType STORAGE_CELL_LEDS = RenderType.create(
            "ae2_drive_leds",
            RenderSetup.builder(AERenderPipelines.STORAGE_CELL_LEDS).createRenderSetup());

}
