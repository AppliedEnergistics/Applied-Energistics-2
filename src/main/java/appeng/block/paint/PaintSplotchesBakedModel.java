
package appeng.block.paint;

import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;
import com.google.common.collect.ImmutableList;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Renders paint blocks, which render multiple "splotches" that have been
 * applied to the sides of adjacent blocks using a matter cannon with paint
 * balls.
 */
class PaintSplotchesBakedModel implements BakedModel, FabricBakedModel {

    private static final SpriteIdentifier TEXTURE_PAINT1 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/paint1"));
    private static final SpriteIdentifier TEXTURE_PAINT2 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/paint2"));
    private static final SpriteIdentifier TEXTURE_PAINT3 = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/paint3"));

    private final Sprite[] textures;

    PaintSplotchesBakedModel(Function<SpriteIdentifier, Sprite> bakedTextureGetter) {
        this.textures = new Sprite[] { bakedTextureGetter.apply(TEXTURE_PAINT1),
                bakedTextureGetter.apply(TEXTURE_PAINT2), bakedTextureGetter.apply(TEXTURE_PAINT3) };
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {

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

            Sprite ico = this.textures[s.getSeed() % this.textures.length];
            builder.setTexture(ico);
            builder.setCustomUv(s.getSide().getOpposite(), 0, 0, 16, 16);

            switch (s.getSide()) {
                case UP:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.DOWN, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case DOWN:
                    builder.addQuad(Direction.UP, pos_x - buffer, offset, pos_y - buffer, pos_x + buffer, offset,
                            pos_y + buffer);
                    break;

                case EAST:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.WEST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case WEST:
                    builder.addQuad(Direction.EAST, offset, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer);
                    break;

                case SOUTH:
                    offset = 1.0f - offset;
                    builder.addQuad(Direction.NORTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
                            pos_y + buffer, offset);
                    break;

                case NORTH:
                    builder.addQuad(Direction.SOUTH, pos_x - buffer, pos_y - buffer, offset, pos_x + buffer,
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
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return this.textures[0];
    }

    @Override
    public ModelTransformation getTransformation() {
        return null;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    static List<SpriteIdentifier> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_PAINT1, TEXTURE_PAINT2, TEXTURE_PAINT3);
    }

}
