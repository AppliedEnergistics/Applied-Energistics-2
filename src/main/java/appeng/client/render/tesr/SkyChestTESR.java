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

package appeng.client.render.tesr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyChestBlock.SkyChestType;
import appeng.blockentity.storage.SkyChestBlockEntity;
import appeng.core.AppEng;

// This is mostly a copy&paste job of the vanilla chest TESR
@OnlyIn(Dist.CLIENT)
public class SkyChestTESR implements BlockEntityRenderer<SkyChestBlockEntity> {

    public static ModelLayerLocation MODEL_LAYER = new ModelLayerLocation(AppEng.makeId("sky_chest"), "main");

    public static final Material TEXTURE_STONE = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation(AppEng.MOD_ID, "models/skychest"));
    public static final Material TEXTURE_BLOCK = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation(AppEng.MOD_ID, "models/skyblockchest"));

    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public SkyChestTESR(BlockEntityRendererProvider.Context context) {
        var modelpart = context.bakeLayer(MODEL_LAYER);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
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

    @Override
    public void render(SkyChestBlockEntity blockEntity, float partialTicks, PoseStack matrixStackIn,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        float f = blockEntity.getForward().toYRot();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = blockEntity.getOpenNess(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        Material material = this.getRenderMaterial(blockEntity);
        VertexConsumer ivertexbuilder = material.buffer(bufferIn, RenderType::entityCutout);
        this.renderModels(matrixStackIn, ivertexbuilder, this.lid, this.lock, this.bottom, f1,
                combinedLightIn, combinedOverlayIn);

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

    protected Material getRenderMaterial(SkyChestBlockEntity blockEntity) {
        SkyChestType type = SkyChestType.BLOCK;
        if (blockEntity.getLevel() != null) {
            Block blockType = blockEntity.getBlockState().getBlock();

            if (blockType instanceof SkyChestBlock) {
                type = ((SkyChestBlock) blockType).type;
            }
        }

        return switch (type) {
            case STONE -> TEXTURE_STONE;
            case BLOCK -> TEXTURE_BLOCK;
        };
    }

    public static void registerTextures(TextureStitchEvent.Pre evt) {
        if (evt.getMap().location().equals(Sheets.CHEST_SHEET)) {
            evt.addSprite(TEXTURE_STONE.texture());
            evt.addSprite(TEXTURE_BLOCK.texture());
        }
    }

}
