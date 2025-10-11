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

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.orientation.BlockOrientation;
import appeng.blockentity.misc.InscriberBlockEntity;
import appeng.core.AppEng;
import appeng.recipes.handlers.InscriberProcessType;

/**
 * Renders the dynamic parts of an inscriber (the presses, the animation and the item being smashed)
 */
public final class InscriberRenderer implements BlockEntityRenderer<InscriberBlockEntity, InscriberRenderState> {

    private static final float ITEM_RENDER_SCALE = 1.0f / 1.2f;

    private static final Material TEXTURE_INSIDE = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/inscriber_inside"));

    private final ItemModelResolver itemModelResolver;
    private final MaterialSet materials;

    public InscriberRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.materials = context.materials();
    }

    @Override
    public InscriberRenderState createRenderState() {
        return new InscriberRenderState();
    }

    @Override
    public void extractRenderState(InscriberBlockEntity be, InscriberRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
        // Calculate the lightlevel in front of the drive for lighting the exposed cell model.
        if (be.getLevel() != null) {
            var frontPos = be.getBlockPos().relative(be.getFront());
            state.frontLightCoords = LevelRenderer.getLightColor(be.getLevel(), frontPos);
        } else {
            state.frontLightCoords = LightTexture.FULL_BRIGHT;
        }

        state.orientation = BlockOrientation.get(be);

        long absoluteProgress = 0;

        if (be.isSmash()) {
            final long currentTime = System.currentTimeMillis();
            absoluteProgress = currentTime - be.getClientStart();
            if (absoluteProgress > 800) {
                be.setSmash(false);
                if (be.isRepeatSmash()) {
                    be.setSmash(true);
                }
            }
        }

        final float relativeProgress = absoluteProgress % 800 / 400.0f;
        float progress = relativeProgress;

        if (progress > 1.0f) {
            state.progress = 1.0f - easeDecompressMotion(progress - 1.0f);
        } else {
            state.progress = easeCompressMotion(progress);
        }

        var inv = be.getInternalInventory();

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
        ItemStack middleItem;
        if (relativeProgress > 1.0f || items == 0) {
            // When crafting completes, dont render the presses (they mave have been
            // consumed, see below)
            renderPresses = false;

            middleItem = inv.getStackInSlot(3);

            if (middleItem.isEmpty()) {
                var ir = be.getTask();
                if (ir != null) {
                    // The "PRESS" type will consume the presses so they should not render after
                    // completing the press animation
                    renderPresses = ir.getProcessType() == InscriberProcessType.INSCRIBE;
                    middleItem = ir.getResultItem().copy();
                }
            }
        } else {
            renderPresses = true;
            middleItem = inv.getStackInSlot(2);
        }
        itemModelResolver.updateForTopItem(state.middleItem, middleItem, ItemDisplayContext.ON_SHELF, be.getLevel(),
                null, 0);

        if (renderPresses) {
            itemModelResolver.updateForTopItem(state.topItem, inv.getStackInSlot(0), ItemDisplayContext.ON_SHELF,
                    be.getLevel(), null, 0);
            itemModelResolver.updateForTopItem(state.bottomItem, inv.getStackInSlot(1), ItemDisplayContext.ON_SHELF,
                    be.getLevel(), null, 0);
        } else {
            state.topItem.clear();
            state.bottomItem.clear();
        }

    }

    @Override
    public void submit(InscriberRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {

        // render inscriber
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(state.orientation.getQuaternion());
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        var tas = materials.get(TEXTURE_INSIDE);

        var combinedOverlay = OverlayTexture.NO_OVERLAY;
        var combinedLight = state.frontLightCoords;

        float press = 0.2f - state.progress / 5.0f;

        nodes.submitCustomGeometry(poseStack, Sheets.translucentItemSheet(), (pose, buffer) -> {
            // render sides of stamps

            float middle = 0.5f;
            middle += 0.02f;
            final float TwoPx = 2.0f / 16.0f;
            final float base = 0.4f;

            // Bottom of Top Stamp
            addVertex(buffer, pose, tas, TwoPx, middle + press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                    Direction.DOWN);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + press, TwoPx, 0.125f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.DOWN);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 0.125f, 0.875f, combinedOverlay,
                    combinedLight,
                    Direction.DOWN);
            addVertex(buffer, pose, tas, TwoPx, middle + press, 1.0f - TwoPx, 0.875f, 0.875f, combinedOverlay,
                    combinedLight,
                    Direction.DOWN);

            // Front of Top Stamp
            addVertex(buffer, pose, tas, TwoPx, middle + base, TwoPx, 0.125f, 0.125f - (press - base), combinedOverlay,
                    combinedLight, Direction.NORTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + base, TwoPx, 0.875f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.NORTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + press, TwoPx, 0.875f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.NORTH);
            addVertex(buffer, pose, tas, TwoPx, middle + press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                    Direction.NORTH);

            // Rear of Top Stamp
            addVertex(buffer, pose, tas, TwoPx, middle + base, 1.0f - TwoPx, 0.125f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.SOUTH);
            addVertex(buffer, pose, tas, TwoPx, middle + press, 1.0f - TwoPx, 0.125f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.SOUTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + press, 1.0f - TwoPx, 0.875f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.SOUTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle + base, 1.0f - TwoPx, 0.875f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.SOUTH);

            // Top of Bottom Stamp
            middle -= 2.0f * 0.02f;
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - press, TwoPx, 0.875f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.UP);
            addVertex(buffer, pose, tas, TwoPx, middle - press, TwoPx, 0.125f, 0.125f, combinedOverlay, combinedLight,
                    Direction.UP);
            addVertex(buffer, pose, tas, TwoPx, middle - press, 1.0f - TwoPx, 0.125f, 0.875f, combinedOverlay,
                    combinedLight,
                    Direction.UP);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 0.875f, 0.875f, combinedOverlay,
                    combinedLight,
                    Direction.UP);

            // Front of Bottom Stamp
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - base, TwoPx, 0.125f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.NORTH);
            addVertex(buffer, pose, tas, TwoPx, middle - base, TwoPx, 0.875f, 0.125f - (press - base), combinedOverlay,
                    combinedLight, Direction.NORTH);
            addVertex(buffer, pose, tas, TwoPx, middle - press, TwoPx, 0.875f, 0.125f, combinedOverlay, combinedLight,
                    Direction.NORTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - press, TwoPx, 0.125f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.NORTH);

            // Rear of Bottom Stamp
            addVertex(buffer, pose, tas, TwoPx, middle - press, 1.0f - TwoPx, 0.875f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.SOUTH);
            addVertex(buffer, pose, tas, TwoPx, middle - base, 1.0f - TwoPx, 0.875f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.SOUTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - base, 1.0f - TwoPx, 0.125f, 0.125f - (press - base),
                    combinedOverlay,
                    combinedLight, Direction.SOUTH);
            addVertex(buffer, pose, tas, 1.0f - TwoPx, middle - press, 1.0f - TwoPx, 0.125f, 0.125f, combinedOverlay,
                    combinedLight,
                    Direction.SOUTH);
        });

        // render items.
        renderItem(poseStack, state.middleItem, 0, combinedLight, nodes);

        if (!state.topItem.isEmpty()) {
            renderItem(poseStack, state.topItem, press, combinedLight, nodes);
        }
        if (!state.bottomItem.isEmpty()) {
            renderItem(poseStack, state.bottomItem, press, combinedLight, nodes);
        }

        poseStack.popPose();
    }

    private static void addVertex(VertexConsumer vb, PoseStack.Pose pose, TextureAtlasSprite sprite, float x, float y,
            float z, float texU, float texV, int overlayUV, int lightmapUV, Direction front) {
        vb.addVertex(pose, x, y, z);
        vb.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        vb.setUv(sprite.getU(texU), sprite.getV(texV));
        vb.setOverlay(overlayUV);
        vb.setLight(lightmapUV);
        vb.setNormal(pose, front.getStepX(), front.getStepY(), front.getStepZ());
    }

    private void renderItem(PoseStack poseStack, ItemStackRenderState stack, float o, int lightCoords,
            SubmitNodeCollector nodes) {

        if (!stack.isEmpty()) {
            poseStack.pushPose();
            // move to center
            poseStack.translate(0.5f, 0.5f + o, 0.5f);
            poseStack.mulPose(new Quaternionf().rotationX(Mth.DEG_TO_RAD * 90));
            // set scale
            poseStack.scale(ITEM_RENDER_SCALE, ITEM_RENDER_SCALE, ITEM_RENDER_SCALE);

            // heuristic to scale items down much further than blocks,
            // the assumption here is that the generated item models will return their faces
            // for direction=null, while a block-model will have their faces for
            // cull-faces, but not direction=null
            poseStack.scale(0.5f, 0.5f, 0.5f);
            stack.submit(poseStack, nodes, lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
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
