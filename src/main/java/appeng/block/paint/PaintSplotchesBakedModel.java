package appeng.block.paint;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;

/**
 * Renders paint blocks, which render multiple "splotches" that have been applied to the sides of adjacent blocks using
 * a matter cannon with paint balls.
 */
class PaintSplotchesBakedModel implements IBakedModel, FabricBakedModel {

    private static final RenderMaterial TEXTURE_PAINT1 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/paint1"));
    private static final RenderMaterial TEXTURE_PAINT2 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/paint2"));
    private static final RenderMaterial TEXTURE_PAINT3 = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/paint3"));

    private final TextureAtlasSprite[] textures;

    PaintSplotchesBakedModel(Function<RenderMaterial, TextureAtlasSprite> bakedTextureGetter) {
        this.textures = new TextureAtlasSprite[] { bakedTextureGetter.apply(TEXTURE_PAINT1),
                bakedTextureGetter.apply(TEXTURE_PAINT2), bakedTextureGetter.apply(TEXTURE_PAINT3) };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(IBlockDisplayReader blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {

        Object renderAttachment = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if (!(renderAttachment instanceof PaintSplotches)) {
            return;
        }
        PaintSplotches splotchesState = (PaintSplotches) renderAttachment;

        List<Splotch> splotches = splotchesState.getSplotches();

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        float offsetConstant = 0.001f;
        for (final Splotch s : splotches) {

            if (s.isLumen()) {
                builder.setColorRGB(s.getColor().whiteVariant);
                builder.setEmissiveMaterial(true);
            } else {
                builder.setColorRGB(s.getColor().mediumVariant);
                builder.setEmissiveMaterial(false);
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
                case field_11036:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.field_11033, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case field_11033:
                    builder.addQuad(Direction.field_11036, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case field_11034:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.field_11039, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case field_11039:
                    builder.addQuad(Direction.field_11034, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case field_11035:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.field_11043, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                case field_11043:
                    builder.addQuad(Direction.field_11035, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                default:
            }
        }
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return Collections.emptyList();
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
        return null;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    static List<RenderMaterial> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_PAINT1, TEXTURE_PAINT2, TEXTURE_PAINT3);
    }

}
