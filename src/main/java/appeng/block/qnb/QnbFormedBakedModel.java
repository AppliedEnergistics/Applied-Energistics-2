
package appeng.block.qnb;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.tile.qnb.QuantumBridgeTileEntity;

class QnbFormedBakedModel implements IDynamicBakedModel {

    private static final Material TEXTURE_LINK = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/quantum_link"));
    private static final Material TEXTURE_RING = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/quantum_ring"));
    private static final Material TEXTURE_RING_LIGHT = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/quantum_ring_light"));
    private static final Material TEXTURE_RING_LIGHT_CORNER = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/quantum_ring_light_corner"));
    private static final Material TEXTURE_CABLE_GLASS = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "parts/cable/glass/transparent"));
    private static final Material TEXTURE_COVERED_CABLE = new Material(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "parts/cable/covered/transparent"));

    private static final float DEFAULT_RENDER_MIN = 2.0f;
    private static final float DEFAULT_RENDER_MAX = 14.0f;

    private static final float CORNER_POWERED_RENDER_MIN = 3.9f;
    private static final float CORNER_POWERED_RENDER_MAX = 12.1f;

    private static final float CENTER_POWERED_RENDER_MIN = -0.01f;
    private static final float CENTER_POWERED_RENDER_MAX = 16.01f;

    private final IBakedModel baseModel;

    private final Block linkBlock;

    private final TextureAtlasSprite linkTexture;
    private final TextureAtlasSprite ringTexture;
    private final TextureAtlasSprite glassCableTexture;
    private final TextureAtlasSprite coveredCableTexture;
    private final TextureAtlasSprite lightTexture;
    private final TextureAtlasSprite lightCornerTexture;

    public QnbFormedBakedModel(IBakedModel baseModel, Function<Material, TextureAtlasSprite> bakedTextureGetter) {
        this.baseModel = baseModel;
        this.linkTexture = bakedTextureGetter.apply(TEXTURE_LINK);
        this.ringTexture = bakedTextureGetter.apply(TEXTURE_RING);
        this.glassCableTexture = bakedTextureGetter.apply(TEXTURE_CABLE_GLASS);
        this.coveredCableTexture = bakedTextureGetter.apply(TEXTURE_COVERED_CABLE);
        this.lightTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT);
        this.lightCornerTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT_CORNER);
        this.linkBlock = Api.instance().definitions().blocks().quantumLink().maybeBlock().orElse(null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand,
            IModelData modelData) {
        QnbFormedState formedState = modelData.getData(QuantumBridgeTileEntity.FORMED_STATE);

        if (formedState == null) {
            return this.baseModel.getQuads(state, side, rand);
        }

        if (side != null) {
            return Collections.emptyList();
        }

        return this.getQuads(formedState, state);
    }

    private List<BakedQuad> getQuads(QnbFormedState formedState, BlockState state) {
        CubeBuilder builder = new CubeBuilder();

        if (state.getBlock() == this.linkBlock) {
            Set<Direction> sides = formedState.getAdjacentQuantumBridges();

            this.renderCableAt(builder, 0.11f * 16, this.glassCableTexture, 0.141f * 16, sides);

            this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.1875f * 16, sides);

            builder.setTexture(this.linkTexture);
            builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX,
                    DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);
        } else {
            if (formedState.isCorner()) {
                this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.05f * 16,
                        formedState.getAdjacentQuantumBridges());

                builder.setTexture(this.ringTexture);
                builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX,
                        DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);

                if (formedState.isPowered()) {
                    builder.setTexture(this.lightCornerTexture);
                    builder.setRenderFullBright(true);
                    for (Direction facing : Direction.values()) {
                        // Offset the face by a slight amount so that it is drawn over the already drawn
                        // ring texture
                        // (avoids z-fighting)
                        float xOffset = Math.abs(facing.getXOffset() * 0.01f);
                        float yOffset = Math.abs(facing.getYOffset() * 0.01f);
                        float zOffset = Math.abs(facing.getZOffset() * 0.01f);

                        builder.setDrawFaces(EnumSet.of(facing));
                        builder.addCube(DEFAULT_RENDER_MIN - xOffset, DEFAULT_RENDER_MIN - yOffset,
                                DEFAULT_RENDER_MIN - zOffset, DEFAULT_RENDER_MAX + xOffset,
                                DEFAULT_RENDER_MAX + yOffset, DEFAULT_RENDER_MAX + zOffset);
                    }
                }
            } else {
                builder.setTexture(this.ringTexture);

                builder.addCube(0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 16, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);

                builder.addCube(DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, 16, DEFAULT_RENDER_MAX);

                builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, 0, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, 16);

                if (formedState.isPowered()) {
                    builder.setTexture(this.lightTexture);
                    builder.setRenderFullBright(true);
                    for (Direction facing : Direction.values()) {
                        // Offset the face by a slight amount so that it is drawn over the already drawn
                        // ring texture
                        // (avoids z-fighting)
                        float xOffset = Math.abs(facing.getXOffset() * 0.01f);
                        float yOffset = Math.abs(facing.getYOffset() * 0.01f);
                        float zOffset = Math.abs(facing.getZOffset() * 0.01f);

                        builder.setDrawFaces(EnumSet.of(facing));
                        builder.addCube(-xOffset, -yOffset, -zOffset, 16 + xOffset, 16 + yOffset, 16 + zOffset);
                    }
                }
            }
        }

        return builder.getOutput();
    }

    private void renderCableAt(CubeBuilder builder, float thickness, TextureAtlasSprite texture, float pull,
            Set<Direction> connections) {
        builder.setTexture(texture);

        if (connections.contains(Direction.WEST)) {
            builder.addCube(0, 8 - thickness, 8 - thickness, 8 - thickness - pull, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(Direction.EAST)) {
            builder.addCube(8 + thickness + pull, 8 - thickness, 8 - thickness, 16, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(Direction.NORTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 0, 8 + thickness, 8 + thickness, 8 - thickness - pull);
        }

        if (connections.contains(Direction.SOUTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 8 + thickness + pull, 8 + thickness, 8 + thickness, 16);
        }

        if (connections.contains(Direction.DOWN)) {
            builder.addCube(8 - thickness, 0, 8 - thickness, 8 + thickness, 8 - thickness - pull, 8 + thickness);
        }

        if (connections.contains(Direction.UP)) {
            builder.addCube(8 - thickness, 8 + thickness + pull, 8 - thickness, 8 + thickness, 16, 8 + thickness);
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return this.baseModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean func_230044_c_() {
        return false;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.baseModel.getOverrides();
    }

    public static List<Material> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_LINK, TEXTURE_RING, TEXTURE_CABLE_GLASS, TEXTURE_COVERED_CABLE,
                TEXTURE_RING_LIGHT, TEXTURE_RING_LIGHT_CORNER);
    }
}
