package appeng.client.render;

import static net.minecraft.client.renderer.RenderStateShard.ITEM_ENTITY_TARGET;
import static net.minecraft.client.renderer.RenderStateShard.NO_LIGHTMAP;
import static net.minecraft.client.renderer.RenderStateShard.NO_TEXTURE;
import static net.minecraft.client.renderer.RenderStateShard.VIEW_OFFSET_Z_LAYERING;

import java.util.OptionalDouble;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class AERenderTypes {

    private AERenderTypes() {
    }

    /**
     * Similar to {@link RenderType#LINES}, but with inverted depth test.
     */
    public static final RenderType LINES_BEHIND_BLOCK = RenderType.create(
            "ae2:lines_behind_block",
            1536,
            AERenderPipelines.LINES_BEHIND_BLOCK,
            RenderType.CompositeState.builder()
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .createCompositeState(false));

    public static final RenderType SPATIAL_SKYBOX = RenderType.CompositeRenderType.create(
            "ae2_spatial_sky_skybox",
            8192,
            AERenderPipelines.SPATIAL_SKYBOX,
            RenderType.CompositeState.builder().createCompositeState(false));

    public static final RenderType LIGHTNING_FX = RenderType.CompositeRenderType.create(
            "ae2_lightning_fx",
            8192,
            AERenderPipelines.LIGHTNING_FX,
            RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_PARTICLES, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false));

    private static final RenderStateShard.LineStateShard LINE_3 = new RenderStateShard.LineStateShard(
            OptionalDouble.of(3.0));

    /**
     * This is based on the area render of https://github.com/TeamPneumatic/pnc-repressurized/
     */
    public static final RenderType AREA_OVERLAY_FACE = RenderType.CompositeRenderType.create(
            "ae2_area_overlay_face",
            65535,
            AERenderPipelines.AREA_OVERLAY_FACE,
            RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.EmptyTextureStateShard.NO_TEXTURE)
                    .setLightmapState(RenderStateShard.LightmapStateShard.NO_LIGHTMAP)
                    .createCompositeState(false));
    public static final RenderType AREA_OVERLAY_LINE = RenderType.CompositeRenderType.create(
            "ae2_area_overlay_line",
            65535,
            AERenderPipelines.AREA_OVERLAY_LINE,
            RenderType.CompositeState.builder().setLineState(LINE_3)
                    .setTextureState(NO_TEXTURE)
                    .setLightmapState(NO_LIGHTMAP)
                    .createCompositeState(false));

    public static final RenderType AREA_OVERLAY_LINE_OCCLUDED = RenderType.CompositeRenderType.create(
            "ae2_area_overlay_line_occluded",
            65535,
            AERenderPipelines.AREA_OVERLAY_LINE_OCCLUDED,
            RenderType.CompositeState.builder().setLineState(LINE_3)
                    .setTextureState(NO_TEXTURE)
                    .setLightmapState(NO_LIGHTMAP)
                    .createCompositeState(false));

    public static final RenderType STORAGE_CELL_LEDS = RenderType.create(
            "ae2_drive_leds",
            32565,
            AERenderPipelines.STORAGE_CELL_LEDS,
            RenderType.CompositeState.builder().createCompositeState(false));

}
