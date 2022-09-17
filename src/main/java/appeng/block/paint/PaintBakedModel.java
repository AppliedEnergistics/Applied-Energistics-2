package appeng.block.paint;


import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;
import com.google.common.collect.ImmutableList;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;


/**
 * Renders paint blocks, which render multiple "splotches" that have been applied to the sides of adjacent blocks using
 * a
 * matter cannon with paint balls.
 */
class PaintBakedModel implements IBakedModel {

    private static final ResourceLocation TEXTURE_PAINT1 = new ResourceLocation(AppEng.MOD_ID, "blocks/paint1");
    private static final ResourceLocation TEXTURE_PAINT2 = new ResourceLocation(AppEng.MOD_ID, "blocks/paint2");
    private static final ResourceLocation TEXTURE_PAINT3 = new ResourceLocation(AppEng.MOD_ID, "blocks/paint3");

    private final VertexFormat vertexFormat;

    private final TextureAtlasSprite[] textures;

    PaintBakedModel(VertexFormat vertexFormat, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        this.vertexFormat = vertexFormat;
        this.textures = new TextureAtlasSprite[]{
                bakedTextureGetter.apply(TEXTURE_PAINT1),
                bakedTextureGetter.apply(TEXTURE_PAINT2),
                bakedTextureGetter.apply(TEXTURE_PAINT3)
        };
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) {
            return Collections.emptyList();
        }

        if (!(state instanceof IExtendedBlockState)) {
            // This is the inventory model which should usually not be used other than in special cases
            List<BakedQuad> quads = new ArrayList<>(1);
            CubeBuilder builder = new CubeBuilder(this.vertexFormat, quads);
            builder.setTexture(this.textures[0]);
            builder.addCube(0, 0, 0, 16, 16, 16);
            return quads;
        }

        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        PaintSplotches splotchesState = extendedBlockState.getValue(BlockPaint.SPLOTCHES);

        if (splotchesState == null) {
            return Collections.emptyList();
        }

        List<Splotch> splotches = splotchesState.getSplotches();

        CubeBuilder builder = new CubeBuilder(this.vertexFormat);

        float offsetConstant = 0.001f;
        for (final Splotch s : splotches) {

            if (s.isLumen()) {
                builder.setColorRGB(s.getColor().whiteVariant);
                builder.setRenderFullBright(true);
            } else {
                builder.setColorRGB(s.getColor().mediumVariant);
                builder.setRenderFullBright(false);
            }

            float offset = offsetConstant;
            offsetConstant += 0.001f;

            final float buffer = 0.1f;

            float pos_x = s.x();
            float pos_y = s.y();

            pos_x = Math.max(buffer, Math.min(1.0f - buffer, pos_x));
            pos_y = Math.max(buffer, Math.min(1.0f - buffer, pos_y));

            TextureAtlasSprite ico = this.textures[s.getSeed() % this.textures.length];
            builder.setTexture(ico);
            builder.setCustomUv(s.getSide().getOpposite(), 0, 0, 16, 16);

            switch (s.getSide()) {
                case UP:
                    offset = 1.0f - offset;
                    builder.addQuad(EnumFacing.DOWN, pos_x - buffer, offset, pos_y - buffer,
                            pos_x + buffer, offset, pos_y + buffer);
                    break;

                case DOWN:
                    builder.addQuad(EnumFacing.UP, pos_x - buffer, offset, pos_y - buffer,
                            pos_x + buffer, offset, pos_y + buffer);
                    break;

                case EAST:
                    offset = 1.0f - offset;
                    builder.addQuad(EnumFacing.WEST, offset, pos_x - buffer, pos_y - buffer,
                            offset, pos_x + buffer, pos_y + buffer);
                    break;

                case WEST:
                    builder.addQuad(EnumFacing.EAST, offset, pos_x - buffer, pos_y - buffer,
                            offset, pos_x + buffer, pos_y + buffer);
                    break;

                case SOUTH:
                    offset = 1.0f - offset;
                    builder.addQuad(EnumFacing.NORTH, pos_x - buffer, pos_y - buffer, offset,
                            pos_x + buffer, pos_y + buffer, offset);
                    break;

                case NORTH:
                    builder.addQuad(EnumFacing.SOUTH, pos_x - buffer, pos_y - buffer, offset,
                            pos_x + buffer, pos_y + buffer, offset);
                    break;

                default:
            }
        }

        return builder.getOutput();
    }

    @Override
    public boolean isAmbientOcclusion() {
        return false;
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
        return this.textures[0];
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }

    static List<ResourceLocation> getRequiredTextures() {
        return ImmutableList.of(
                TEXTURE_PAINT1, TEXTURE_PAINT2, TEXTURE_PAINT3);
    }
}
