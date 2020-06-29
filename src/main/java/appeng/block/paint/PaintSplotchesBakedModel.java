
package appeng.block.paint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.data.IDynamicBakedModel;


import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.AppEng;
import appeng.helpers.Splotch;
import appeng.tile.misc.PaintSplotchesBlockEntity;

/**
 * Renders paint blocks, which render multiple "splotches" that have been
 * applied to the sides of adjacent blocks using a matter cannon with paint
 * balls.
 */
class PaintSplotchesBakedModel implements IDynamicBakedModel {

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

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
            @Nonnull IModelData extraData) {

        if (side != null) {
            return Collections.emptyList();
        }

        PaintSplotches splotchesState = extraData.getData(PaintSplotchesBlockEntity.SPLOTCHES);

        if (splotchesState == null) {
            // This is the inventory model which should usually not be used other than in
            // special cases
            List<BakedQuad> quads = new ArrayList<>(1);
            CubeBuilder builder = new CubeBuilder(quads);
            builder.setTexture(this.textures[0]);
            builder.addCube(0, 0, 0, 16, 16, 16);
            return quads;
        }

        List<Splotch> splotches = splotchesState.getSplotches();

        CubeBuilder builder = new CubeBuilder();

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

        return builder.getOutput();
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
