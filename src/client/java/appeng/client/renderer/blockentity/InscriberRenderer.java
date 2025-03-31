/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberRenderer implements BlockEntityRenderer<InscriberBlockEntity> {

    private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

    private static final Material TEXTURE_INSIDE = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/inscriber_inside"));

    public InscriberRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(InscriberBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffers,
            int combinedLight, int combinedOverlay, Vec3 cameraPosition) {

        // render inscriber

        ms.pushPose();
        ms.translate(0.5F, 0.5F, 0.5F);
        BlockOrientation orientation = BlockOrientation.get(blockEntity);
        ms.mulPose(orientation.getQuaternion());
        ms.translate(-0.5F, -0.5F, -0.5F);

        // render sides of stamps

        long absoluteProgress = 0;

        if (blockEntity.isSmash()) {
            final long currentTime = System.currentTimeMillis();
            absoluteProgress = currentTime - blockEntity.getClientStart();
            if (absoluteProgress > 800) {
                blockEntity.setSmash(false);
                if (blockEntity.isRepeatSmash()) {
                    blockEntity.setSmash(true);
                }
            }
        }

        final float relativeProgress = absoluteProgress % 800 / 400.0f;
        float progress = relativeProgress;

        if (progress > 1.0f) {
            progress = 1.0f - easeDecompressMotion(progress - 1.0f);
        } else {
            progress = easeCompressMotion(progress);
        }

        float press = 0.2f;
        press -= progress / 5.0f;

        float middle = 0.5f;
        middle += 0.02f;
        final float TwoPx = 2.0f / 16.0f;
        final float base = 0.4f;

        final TextureAtlasSprite tas = TEXTURE_INSIDE.sprite();

        VertexConsumer buffer = buffers.getBuffer(RenderType.solid());

        // Bottom of Top Stamp
        addVertex(buffer, ms, tas, TwoPx, middle + press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                Direction.DOWN);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                Direction.DOWN);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 0.125f, 0.875f, combinedOverlay,
                combinedLight,
                Direction.DOWN);
        addVertex(buffer, ms, tas, TwoPx, middle + press, 1.0f - TwoPx, 0.875f, 0.875f, combinedOverlay, combinedLight,
                Direction.DOWN);

        // Front of Top Stamp
        addVertex(buffer, ms, tas, TwoPx, middle + base, TwoPx, 0.125f, 0.125f - (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + base, TwoPx, 0.875f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle + press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                Direction.NORTH);

        // Rear of Top Stamp
        addVertex(buffer, ms, tas, TwoPx, middle + base, 1.0f - TwoPx, 0.125f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.SOUTH);
        addVertex(buffer, ms, tas, TwoPx, middle + press, 1.0f - TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                Direction.SOUTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 0.875f, 0.125f, combinedOverlay,
                combinedLight,
                Direction.SOUTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle + base, 1.0f - TwoPx, 0.875f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.SOUTH);

        // Top of Bottom Stamp
        middle -= 2.0f * 0.02f;
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                Direction.UP);
        addVertex(buffer, ms, tas, TwoPx, middle - press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                Direction.UP);
        addVertex(buffer, ms, tas, TwoPx, middle - press, 1.0f - TwoPx, 0.125f, 0.875f, combinedOverlay, combinedLight,
                Direction.UP);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 0.875f, 0.875f, combinedOverlay,
                combinedLight,
                Direction.UP);

        // Front of Bottom Stamp
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - base, TwoPx, 0.125f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle - base, TwoPx, 0.875f, 0.125f - (press - base), combinedOverlay,
                combinedLight, Direction.NORTH);
        addVertex(buffer, ms, tas, TwoPx, middle - press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                Direction.NORTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                Direction.NORTH);

        // Rear of Bottom Stamp
        addVertex(buffer, ms, tas, TwoPx, middle - press, 1.0f - TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                Direction.SOUTH);
        addVertex(buffer, ms, tas, TwoPx, middle - base, 1.0f - TwoPx, 0.875f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.SOUTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - base, 1.0f - TwoPx, 0.125f, 0.125f - (press - base),
                combinedOverlay,
                combinedLight, Direction.SOUTH);
        addVertex(buffer, ms, tas, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 0.125f, 0.125f, combinedOverlay,
                combinedLight,
                Direction.SOUTH);

        // render items.

        var inv = blockEntity.getInternalInventory();

        int items = 0;
        if (!inv.getStackInSlot(0).isEmpty()) {
            items++;
        }
        if (!inv.getStackInSlot(1).isEmpty()) {
            items++;
        }
        if (!inv.getStackInSlot(2).isEmpty()) {
            items++;
        }

        boolean renderPresses;
        if (relativeProgress > 1.0f || items == 0) {
            // When crafting completes, dont render the presses (they mave have been
            // consumed, see below)
            renderPresses = false;

            ItemStack is = inv.getStackInSlot(3);

            if (is.isEmpty()) {
                final InscriberRecipe ir = blockEntity.getTask();
                if (ir != null) {
                    // The "PRESS" type will consume the presses so they should not render after
                    // completing
                    // the press animation
                    renderPresses = ir.getProcessType() == InscriberProcessType.INSCRIBE;
                    is = ir.getResultItem().copy();
                }
            }
            this.renderItem(ms, is, 0.0f, buffers, combinedLight, combinedOverlay, blockEntity.getLevel());
        } else {
            renderPresses = true;
            this.renderItem(ms, inv.getStackInSlot(2), 0.0f, buffers, combinedLight, combinedOverlay,
                    blockEntity.getLevel());
        }

        if (renderPresses) {
            this.renderItem(ms, inv.getStackInSlot(0), press, buffers, combinedLight, combinedOverlay,
                    blockEntity.getLevel());
            this.renderItem(ms, inv.getStackInSlot(1), -press, buffers, combinedLight, combinedOverlay,
                    blockEntity.getLevel());
        }

        ms.popPose();
    }

    private static void addVertex(VertexConsumer vb, PoseStack ms, TextureAtlasSprite sprite, float x, float y,
            float z, float texU, float texV, int overlayUV, int lightmapUV, Direction front) {
        vb.addVertex(ms.last().pose(), x, y, z);
        vb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        vb.setUv(sprite.getU(texU), sprite.getV(texV));
        vb.setOverlay(overlayUV);
        vb.setLight(lightmapUV);
        vb.setNormal(ms.last(), front.getStepX(), front.getStepY(), front.getStepZ());
    }

    private void renderItem(PoseStack ms, ItemStack stack, float o, MultiBufferSource buffers,
            int combinedLight, int combinedOverlay, Level level) {
        if (!stack.isEmpty()) {
            ms.pushPose();
            // move to center
            ms.translate(0.5f, 0.5f + o, 0.5f);
            ms.mulPose(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 90));
            // set scale
            ms.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

            // heuristic to scale items down much further than blocks,
            // the assumption here is that the generated item models will return their faces
            // for direction=null, while a block-model will have their faces for
            // cull-faces, but not direction=null
            ms.scale(0.5f, 0.5f, 0.5f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, ms,
                    buffers, level, 0);
            ms.popPose();
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
