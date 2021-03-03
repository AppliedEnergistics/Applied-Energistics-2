package appeng.client.render.tesr;

import java.util.List;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.features.InscriberProcessType;
import appeng.client.render.FacingToRotation;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.tile.misc.InscriberBlockEntity;

/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberTESR extends BlockEntityRenderer<InscriberBlockEntity> {

    private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

    private static final SpriteIdentifier TEXTURE_INSIDE = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "block/inscriber_inside"));

    public static final ImmutableList<SpriteIdentifier> SPRITES = ImmutableList.of(TEXTURE_INSIDE);

    public InscriberTESR(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(InscriberBlockEntity tile, float partialTicks, MatrixStack ms, VertexConsumerProvider buffers,
            int combinedLight, int combinedOverlay) {

        // render inscriber

        ms.push();
        ms.translate(0.5F, 0.5F, 0.5F);
        FacingToRotation.get(tile.getForward(), tile.getUp()).push(ms);
        ms.translate(-0.5F, -0.5F, -0.5F);

        // render sides of stamps

        long absoluteProgress = 0;

        if (tile.isSmash()) {
            final long currentTime = System.currentTimeMillis();
            absoluteProgress = currentTime - tile.getClientStart();
            if (absoluteProgress > 800) {
                tile.setSmash(false);
            }
        }

        final float relativeProgress = absoluteProgress % 800 / 400.0f;
        float progress = relativeProgress;

        if (progress > 1.0f) {
            progress = 1.0f - (easeDecompressMotion(progress - 1.0f));
        } else {
            progress = easeCompressMotion(progress);
        }

        float press = 0.2f;
        press -= progress / 5.0f;

        float middle = 0.5f;
        middle += 0.02f;
        final float TwoPx = 2.0f / 16.0f;
        final float base = 0.4f;

        final Sprite tas = TEXTURE_INSIDE.getSprite();

        VertexConsumer buffer = buffers.getBuffer(RenderLayer.getSolid());

        // Bottom of Top Stamp
        addVertex(buffer, ms, tas, TwoPx, middle + press, TwoPx, 2, 13, combinedOverlay, combinedLight, Direction.DOWN);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, TwoPx, 14, 13, combinedOverlay, combinedLight,
                Direction.DOWN);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 14, 2, combinedOverlay, combinedLight,
                Direction.DOWN);
        addVertex(buffer, ms, tas, TwoPx, middle + press, 1.0f - TwoPx, 2, 2, combinedOverlay, combinedLight,
                Direction.DOWN);

        // Front of Top Stamp
        addVertex(buffer, ms, tas, TwoPx, middle + base, TwoPx, 2, 3 - 16 * (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + base, TwoPx, 14, 3 - 16 * (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, TwoPx, 14, 3, combinedOverlay, combinedLight,
                Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle + press, TwoPx, 2, 3, combinedOverlay, combinedLight, Direction.NORTH);

        // Top of Bottom Stamp
        middle -= 2.0f * 0.02f;
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, TwoPx, 2, 13, combinedOverlay, combinedLight,
                Direction.UP);
        addVertex(buffer, ms, tas, TwoPx, middle - press, TwoPx, 14, 13, combinedOverlay, combinedLight, Direction.UP);
        addVertex(buffer, ms, tas, TwoPx, middle - press, 1.0f - TwoPx, 14, 2, combinedOverlay, combinedLight,
                Direction.UP);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 2, 2, combinedOverlay, combinedLight,
                Direction.UP);

        // Front of Bottom Stamp
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + -base, TwoPx, 2, 3 - 16 * (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle - base, TwoPx, 14, 3 - 16 * (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle - press, TwoPx, 14, 3, combinedOverlay, combinedLight,
                Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, TwoPx, 2, 3, combinedOverlay, combinedLight,
                Direction.NORTH);

        // render items.

        FixedItemInv tileInv = tile.getInternalInventory();

        int items = 0;
        if (!tileInv.getInvStack(0).isEmpty()) {
            items++;
        }
        if (!tileInv.getInvStack(1).isEmpty()) {
            items++;
        }
        if (!tileInv.getInvStack(2).isEmpty()) {
            items++;
        }

        boolean renderPresses;
        if (relativeProgress > 1.0f || items == 0) {
            // When crafting completes, dont render the presses (they mave have been
            // consumed, see below)
            renderPresses = false;

            ItemStack is = tileInv.getInvStack(3);

            if (is.isEmpty()) {
                final InscriberRecipe ir = tile.getTask();
                if (ir != null) {
                    // The "PRESS" type will consume the presses so they should not render after
                    // completing
                    // the press animation
                    renderPresses = ir.getProcessType() == InscriberProcessType.INSCRIBE;
                    is = ir.getOutput().copy();
                }
            }
            this.renderItem(ms, is, 0.0f, buffers, combinedLight, combinedOverlay);
        } else {
            renderPresses = true;
            this.renderItem(ms, tileInv.getInvStack(2), 0.0f, buffers, combinedLight, combinedOverlay);
        }

        if (renderPresses) {
            this.renderItem(ms, tileInv.getInvStack(0), press, buffers, combinedLight, combinedOverlay);
            this.renderItem(ms, tileInv.getInvStack(1), -press, buffers, combinedLight, combinedOverlay);
        }

        ms.pop();
    }

    private static void addVertex(VertexConsumer vb, MatrixStack ms, Sprite sprite, float x, float y, float z,
            double texU, double texV, int overlayUV, int lightmapUV, Direction front) {
        vb.vertex(ms.peek().getModel(), x, y, z);
        vb.color(1.0f, 1.0f, 1.0f, 1.0f);
        vb.texture(sprite.getFrameU(texU), sprite.getFrameV(texV));
        vb.overlay(overlayUV);
        vb.light(lightmapUV);
        vb.normal(ms.peek().getNormal(), front.getOffsetX(), front.getOffsetY(), front.getOffsetZ());
        vb.next();
    }

    private void renderItem(MatrixStack ms, final ItemStack stack, final float o, VertexConsumerProvider buffers,
            int combinedLight, int combinedOverlay) {
        if (!stack.isEmpty()) {
            ms.push();
            // move to center
            ms.translate(0.5f, 0.5f + o, 0.5f);
            ms.multiply(new Quaternion(90, 0, 0, true));
            // set scale
            ms.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

            ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

            // heuristic to scale items down much further than blocks,
            // the assumption here is that the generated item models will return their faces
            // for direction=null, while a block-model will have their faces for
            // cull-faces, but not direction=null
            BakedModel model = itemRenderer.getModels().getModel(stack);
            List<BakedQuad> quads = model.getQuads(null, null, new Random());
            // Note: quads may be null for mods implementing FabricBakedModel without caring about getQuads.
            if (quads != null && quads.isEmpty()) {
                ms.scale(0.5f, 0.5f, 0.5f);
            }

            itemRenderer.renderItem(stack, ModelTransformation.Mode.FIXED, combinedLight, combinedOverlay, ms, buffers);
            ms.pop();
        }
    }

    // See https://easings.net/#easeOutBack
    private static float easeCompressMotion(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;

        return (float) (1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2));
    }

    // See https://easings.net/#easeInQuint
    private static float easeDecompressMotion(float x) {
        return x * x * x * x * x;
    }

}
