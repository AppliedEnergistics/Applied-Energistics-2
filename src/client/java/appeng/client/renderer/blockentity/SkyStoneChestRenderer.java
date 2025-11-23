/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import appeng.block.storage.SkyStoneChestBlock;
import appeng.block.storage.SkyStoneChestBlock.Type;
import appeng.blockentity.storage.SkyStoneChestBlockEntity;
import appeng.core.AppEng;

// This is mostly a copy&paste job of the vanilla chest TESR
public class SkyStoneChestRenderer implements BlockEntityRenderer<SkyStoneChestBlockEntity, SkyStoneChestRenderState> {

    public static ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(AppEng.makeId("sky_chest"), "main");

    // The textures are in the block sheet due to the item model requiring them there
    public static final Material TEXTURE_STONE = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/skychest"));
    public static final Material TEXTURE_BLOCK = new Material(TextureAtlas.LOCATION_BLOCKS,
            AppEng.makeId("block/skyblockchest"));

    private final MaterialSet materialSet;
    private final SkyStoneChestModel model;

    public SkyStoneChestRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new SkyStoneChestModel(context.bakeLayer(MODEL_LAYER));
        this.materialSet = context.materials();
    }

    @Override
    public SkyStoneChestRenderState createRenderState() {
        return new SkyStoneChestRenderState();
    }

    @Override
    public void extractRenderState(SkyStoneChestBlockEntity be, SkyStoneChestRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
        state.angle = be.getFront().toYRot();
        state.open = be.getOpenNess(partialTicks);
        state.material = this.getRenderMaterial(be);
    }

    @Override
    public void submit(SkyStoneChestRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -state.angle));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        float lidAngle = state.open;
        lidAngle = 1.0F - lidAngle;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;

        var renderType = state.material.renderType(RenderTypes::entityCutout);
        var sprite = materialSet.get(state.material);

        nodes.submitModel(model, lidAngle, poseStack, renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                sprite, 0, state.breakProgress);

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(SkyStoneChestBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }

    protected Material getRenderMaterial(SkyStoneChestBlockEntity blockEntity) {
        Type type = Type.BLOCK;
        if (blockEntity.getLevel() != null) {
            Block blockType = blockEntity.getBlockState().getBlock();

            if (blockType instanceof SkyStoneChestBlock) {
                type = ((SkyStoneChestBlock) blockType).type;
            }
        }

        return switch (type) {
            case STONE -> TEXTURE_STONE;
            case BLOCK -> TEXTURE_BLOCK;
        };
    }

}
