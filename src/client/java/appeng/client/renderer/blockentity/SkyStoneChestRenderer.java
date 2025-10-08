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
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
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

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public SkyStoneChestRenderer(BlockEntityRendererProvider.Context context) {
        var modelpart = context.bakeLayer(MODEL_LAYER);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
    }

    @Override
    public SkyStoneChestRenderState createRenderState() {
        return new SkyStoneChestRenderState();
    }

    @Override
    public void extractRenderState(SkyStoneChestBlockEntity be, SkyStoneChestRenderState state, float partialTicks,
            Vec3 cameraPos, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPos, crumblingOverlay);
    }

    @Override
    public void submit(SkyStoneChestRenderState state, PoseStack poseStack, SubmitNodeCollector nodes,
            CameraRenderState cameraRenderState) {
    }

    @Override
    public AABB getRenderBoundingBox(SkyStoneChestBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return AABB.encapsulatingFullBlocks(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
    }

    public static LayerDefinition createSingleBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bottom",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("lid",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F),
                PartPose.offset(0.0F, 10.0F, 1.0F));
        partdefinition.addOrReplaceChild("lock",
                CubeListBuilder.create().texOffs(0, 0).addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offset(0.0F, 9.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // TODO 1.21.9
    public void render(SkyStoneChestBlockEntity blockEntity, float partialTicks, PoseStack matrixStackIn,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn, Vec3 cameraPosition) {
        matrixStackIn.pushPose();
        float f = blockEntity.getFront().toYRot();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.mulPose(new Quaternionf().rotationY(Mth.DEG_TO_RAD * -f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = blockEntity.getOpenNess(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        Material material = this.getRenderMaterial(blockEntity);
        // TODO 1.21.9 VertexConsumer ivertexbuilder = material.buffer(bufferIn, RenderType::entityCutout);
        // TODO 1.21.9 this.renderModels(matrixStackIn, ivertexbuilder, this.lid, this.lock, this.bottom, f1,
        // TODO 1.21.9 combinedLightIn, combinedOverlayIn);

        matrixStackIn.popPose();
    }

    private void renderModels(PoseStack matrixStackIn, VertexConsumer bufferIn, ModelPart chestLid,
            ModelPart chestLatch, ModelPart chestBottom, float lidAngle, int combinedLightIn,
            int combinedOverlayIn) {
        chestLid.xRot = -(lidAngle * 1.5707964F);
        chestLatch.xRot = chestLid.xRot;
        chestLid.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestLatch.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestBottom.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
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
