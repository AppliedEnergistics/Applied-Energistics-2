
package appeng.block.qnb;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.Block;
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

import appeng.client.render.cablebus.CubeBuilder;
import appeng.core.Api;
import appeng.core.AppEng;

class QnbFormedBakedModel implements BakedModel, FabricBakedModel {

    private static final SpriteIdentifier TEXTURE_LINK = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/quantum_link"));
    private static final SpriteIdentifier TEXTURE_RING = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/quantum_ring"));
    private static final SpriteIdentifier TEXTURE_RING_LIGHT = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "block/quantum_ring_light"));
    private static final SpriteIdentifier TEXTURE_RING_LIGHT_CORNER = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier(AppEng.MOD_ID, "block/quantum_ring_light_corner"));
    private static final SpriteIdentifier TEXTURE_CABLE_GLASS = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEX,
            new Identifier(AppEng.MOD_ID, "part/cable/glass/transparent"));
    private static final SpriteIdentifier TEXTURE_COVERED_CABLE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEX, new Identifier(AppEng.MOD_ID, "part/cable/covered/transparent"));

    private static final float DEFAULT_RENDER_MIN = 2.0f;
    private static final float DEFAULT_RENDER_MAX = 14.0f;

    private static final float CORNER_POWERED_RENDER_MIN = 3.9f;
    private static final float CORNER_POWERED_RENDER_MAX = 12.1f;

    private static final float CENTER_POWERED_RENDER_MIN = -0.01f;
    private static final float CENTER_POWERED_RENDER_MAX = 16.01f;

    private final BakedModel baseModel;

    private final Block linkBlock;

    private final Sprite linkTexture;
    private final Sprite ringTexture;
    private final Sprite glassCableTexture;
    private final Sprite coveredCableTexture;
    private final Sprite lightTexture;
    private final Sprite lightCornerTexture;

    public QnbFormedBakedModel(BakedModel baseModel, Function<SpriteIdentifier, Sprite> bakedTextureGetter) {
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
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos,
            Supplier<Random> randomSupplier, RenderContext context) {
        QnbFormedState formedState = getState(blockView, pos);

        if (formedState == null) {
            context.fallbackConsumer().accept(this.baseModel);
            return;
        }

        buildQuads(context.getEmitter(), formedState, state);

    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return baseModel.getQuads(state, face, random);
    }

    private static QnbFormedState getState(BlockRenderView view, BlockPos pos) {
        if (!(view instanceof RenderAttachedBlockView)) {
            return null;
        }
        Object attachment = ((RenderAttachedBlockView) view).getBlockEntityRenderAttachment(pos);
        if (attachment instanceof QnbFormedState) {
            return (QnbFormedState) attachment;
        }
        return null;
    }

    private void buildQuads(QuadEmitter emitter, QnbFormedState formedState, BlockState state) {
        CubeBuilder builder = new CubeBuilder(emitter);

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
                        float xOffset = Math.abs(facing.getOffsetX() * 0.01f);
                        float yOffset = Math.abs(facing.getOffsetY() * 0.01f);
                        float zOffset = Math.abs(facing.getOffsetZ() * 0.01f);

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
                        float xOffset = Math.abs(facing.getOffsetX() * 0.01f);
                        float yOffset = Math.abs(facing.getOffsetY() * 0.01f);
                        float zOffset = Math.abs(facing.getOffsetZ() * 0.01f);

                        builder.setDrawFaces(EnumSet.of(facing));
                        builder.addCube(-xOffset, -yOffset, -zOffset, 16 + xOffset, 16 + yOffset, 16 + zOffset);
                    }
                }
            }
        }
    }

    private void renderCableAt(CubeBuilder builder, float thickness, Sprite texture, float pull,
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
    public ModelTransformation getTransformation() {
        return ModelTransformation.NONE;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.baseModel.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return false;
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public Sprite getSprite() {
        return this.baseModel.getSprite();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.baseModel.getOverrides();
    }

    public static List<SpriteIdentifier> getRequiredTextures() {
        return ImmutableList.of(TEXTURE_LINK, TEXTURE_RING, TEXTURE_CABLE_GLASS, TEXTURE_COVERED_CABLE,
                TEXTURE_RING_LIGHT, TEXTURE_RING_LIGHT_CORNER);
    }
}
