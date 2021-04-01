package appeng.client.render.tesr;

import java.util.List;
import java.util.Random;

import appeng.tile.misc.InscriberTileEntity;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import alexiil.mc.lib.attributes.item.FixedItemInv;

import appeng.api.features.InscriberProcessType;
import appeng.client.render.FacingToRotation;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberRecipe;

/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberTESR extends TileEntityRenderer<InscriberTileEntity> {

    private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

    private static final RenderMaterial TEXTURE_INSIDE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS_TEXTURE,
            new ResourceLocation(AppEng.MOD_ID, "block/inscriber_inside"));

    public static final ImmutableList<RenderMaterial> SPRITES = ImmutableList.of(TEXTURE_INSIDE);

    public InscriberTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(InscriberTileEntity tile, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
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

        final TextureAtlasSprite tas = TEXTURE_INSIDE.getSprite();

        IVertexBuilder buffer = buffers.getBuffer(RenderType.getSolid());

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
                    is = ir.getRecipeOutput().copy();
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

    private static void addVertex(IVertexBuilder vb, MatrixStack ms, TextureAtlasSprite sprite, float x, float y, float z,
            double texU, double texV, int overlayUV, int lightmapUV, Direction front) {
        vb.pos(ms.getLast().getMatrix(), x, y, z);
        vb.color(1.0f, 1.0f, 1.0f, 1.0f);
        vb.tex(sprite.getInterpolatedU(texU), sprite.getInterpolatedV(texV));
        vb.overlay(overlayUV);
        vb.lightmap(lightmapUV);
        vb.normal(ms.getLast().getNormal(), front.getXOffset(), front.getYOffset(), front.getZOffset());
        vb.endVertex();
    }

    private void renderItem(MatrixStack ms, final ItemStack stack, final float o, IRenderTypeBuffer buffers,
            int combinedLight, int combinedOverlay) {
        if (!stack.isEmpty()) {
            ms.push();
            // move to center
            ms.translate(0.5f, 0.5f + o, 0.5f);
            ms.rotate(new Quaternion(90, 0, 0, true));
            // set scale
            ms.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            // heuristic to scale items down much further than blocks,
            // the assumption here is that the generated item models will return their faces
            // for direction=null, while a block-model will have their faces for
            // cull-faces, but not direction=null
            IBakedModel model = itemRenderer.getItemModelMesher().getItemModel(stack);
            List<BakedQuad> quads = model.getQuads(null, null, new Random());
            // Note: quads may be null for mods implementing FabricBakedModel without caring about getQuads.
            if (quads != null && !quads.isEmpty()) {
                ms.scale(0.5f, 0.5f, 0.5f);
            }

            itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.field_4319, combinedLight, combinedOverlay, ms, buffers);
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
