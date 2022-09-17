package appeng.block.qnb;


import appeng.api.AEApi;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;


class QnbFormedBakedModel implements IBakedModel {

    private static final ResourceLocation TEXTURE_LINK = new ResourceLocation(AppEng.MOD_ID, "blocks/quantum_link");
    private static final ResourceLocation TEXTURE_RING = new ResourceLocation(AppEng.MOD_ID, "blocks/quantum_ring");
    private static final ResourceLocation TEXTURE_RING_LIGHT = new ResourceLocation(AppEng.MOD_ID, "blocks/quantum_ring_light");
    private static final ResourceLocation TEXTURE_RING_LIGHT_CORNER = new ResourceLocation(AppEng.MOD_ID, "blocks/quantum_ring_light_corner");
    private static final ResourceLocation TEXTURE_CABLE_GLASS = new ResourceLocation(AppEng.MOD_ID, "parts/cable/glass/transparent");
    private static final ResourceLocation TEXTURE_COVERED_CABLE = new ResourceLocation(AppEng.MOD_ID, "parts/cable/covered/transparent");

    private static final float DEFAULT_RENDER_MIN = 2.0f;
    private static final float DEFAULT_RENDER_MAX = 14.0f;

    private static final float CORNER_POWERED_RENDER_MIN = 3.9f;
    private static final float CORNER_POWERED_RENDER_MAX = 12.1f;

    private static final float CENTER_POWERED_RENDER_MIN = -0.01f;
    private static final float CENTER_POWERED_RENDER_MAX = 16.01f;

    private final VertexFormat vertexFormat;

    private final IBakedModel baseModel;

    private final Block linkBlock;

    private final TextureAtlasSprite linkTexture;
    private final TextureAtlasSprite ringTexture;
    private final TextureAtlasSprite glassCableTexture;
    private final TextureAtlasSprite coveredCableTexture;
    private final TextureAtlasSprite lightTexture;
    private final TextureAtlasSprite lightCornerTexture;

    public QnbFormedBakedModel(VertexFormat vertexFormat, IBakedModel baseModel, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.vertexFormat = vertexFormat;
        this.baseModel = baseModel;
        this.linkTexture = bakedTextureGetter.apply(TEXTURE_LINK);
        this.ringTexture = bakedTextureGetter.apply(TEXTURE_RING);
        this.glassCableTexture = bakedTextureGetter.apply(TEXTURE_CABLE_GLASS);
        this.coveredCableTexture = bakedTextureGetter.apply(TEXTURE_COVERED_CABLE);
        this.lightTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT);
        this.lightCornerTexture = bakedTextureGetter.apply(TEXTURE_RING_LIGHT_CORNER);
        this.linkBlock = AEApi.instance().definitions().blocks().quantumLink().maybeBlock().orElse(null);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        // Get the correct base model
        if (!(state instanceof IExtendedBlockState)) {
            return this.baseModel.getQuads(state, side, rand);
        }

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        QnbFormedState formedState = extendedBlockState.getValue(BlockQuantumBase.FORMED_STATE);

        return this.getQuads(formedState, state, side, rand);
    }

    private List<BakedQuad> getQuads(QnbFormedState formedState, IBlockState state, EnumFacing side, long rand) {
        CubeBuilder builder = new CubeBuilder(this.vertexFormat);

        if (state.getBlock() == this.linkBlock) {
            Set<EnumFacing> sides = formedState.getAdjacentQuantumBridges();

            this.renderCableAt(builder, 0.11f * 16, this.glassCableTexture, 0.141f * 16, sides);

            this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.1875f * 16, sides);

            builder.setTexture(this.linkTexture);
            builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);
        } else {
            if (formedState.isCorner()) {
                this.renderCableAt(builder, 0.188f * 16, this.coveredCableTexture, 0.05f * 16, formedState.getAdjacentQuantumBridges());

                builder.setTexture(this.ringTexture);
                builder.addCube(DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MIN, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX, DEFAULT_RENDER_MAX);

                if (formedState.isPowered()) {
                    builder.setTexture(this.lightCornerTexture);
                    builder.setRenderFullBright(true);
                    for (EnumFacing facing : EnumFacing.values()) {
                        // Offset the face by a slight amount so that it is drawn over the already drawn ring texture
                        // (avoids z-fighting)
                        float xOffset = Math.abs(facing.getFrontOffsetX() * 0.01f);
                        float yOffset = Math.abs(facing.getFrontOffsetY() * 0.01f);
                        float zOffset = Math.abs(facing.getFrontOffsetZ() * 0.01f);

                        builder.setDrawFaces(EnumSet.of(facing));
                        builder.addCube(
                                DEFAULT_RENDER_MIN - xOffset, DEFAULT_RENDER_MIN - yOffset, DEFAULT_RENDER_MIN - zOffset,
                                DEFAULT_RENDER_MAX + xOffset, DEFAULT_RENDER_MAX + yOffset, DEFAULT_RENDER_MAX + zOffset);
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
                    for (EnumFacing facing : EnumFacing.values()) {
                        // Offset the face by a slight amount so that it is drawn over the already drawn ring texture
                        // (avoids z-fighting)
                        float xOffset = Math.abs(facing.getFrontOffsetX() * 0.01f);
                        float yOffset = Math.abs(facing.getFrontOffsetY() * 0.01f);
                        float zOffset = Math.abs(facing.getFrontOffsetZ() * 0.01f);

                        builder.setDrawFaces(EnumSet.of(facing));
                        builder.addCube(
                                -xOffset, -yOffset, -zOffset,
                                16 + xOffset, 16 + yOffset, 16 + zOffset);
                    }
                }
            }
        }

        return builder.getOutput();
    }

    private void renderCableAt(CubeBuilder builder, float thickness, TextureAtlasSprite texture, float pull, Set<EnumFacing> connections) {
        builder.setTexture(texture);

        if (connections.contains(EnumFacing.WEST)) {
            builder.addCube(0, 8 - thickness, 8 - thickness, 8 - thickness - pull, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(EnumFacing.EAST)) {
            builder.addCube(8 + thickness + pull, 8 - thickness, 8 - thickness, 16, 8 + thickness, 8 + thickness);
        }

        if (connections.contains(EnumFacing.NORTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 0, 8 + thickness, 8 + thickness, 8 - thickness - pull);
        }

        if (connections.contains(EnumFacing.SOUTH)) {
            builder.addCube(8 - thickness, 8 - thickness, 8 + thickness + pull, 8 + thickness, 8 + thickness, 16);
        }

        if (connections.contains(EnumFacing.DOWN)) {
            builder.addCube(8 - thickness, 0, 8 - thickness, 8 + thickness, 8 - thickness - pull, 8 + thickness);
        }

        if (connections.contains(EnumFacing.UP)) {
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
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.baseModel.getParticleTexture();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return this.baseModel.getItemCameraTransforms();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return this.baseModel.getOverrides();
    }

    public static List<ResourceLocation> getRequiredTextures() {
        return ImmutableList.of(
                TEXTURE_LINK, TEXTURE_RING, TEXTURE_CABLE_GLASS, TEXTURE_COVERED_CABLE, TEXTURE_RING_LIGHT, TEXTURE_RING_LIGHT_CORNER);
    }
}
