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
import net.minecraft.world.level.block.Block;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyChestBlock.SkyChestType;
import appeng.core.AppEng;
import appeng.tile.storage.SkyChestTileEntity;

// This is mostly a copy&paste job of the vanilla chest TESR
@OnlyIn(Dist.CLIENT)
public class SkyChestTESR extends BlockEntityRenderer<SkyChestTileEntity> {

    public static final Material TEXTURE_STONE = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation(AppEng.MOD_ID, "models/skychest"));
    public static final Material TEXTURE_BLOCK = new Material(Sheets.CHEST_SHEET,
            new ResourceLocation(AppEng.MOD_ID, "models/skyblockchest"));

    private final ModelPart singleLid;
    private final ModelPart singleBottom;
    private final ModelPart singleLatch;

    public SkyChestTESR(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        this.singleBottom = new ModelPart(64, 64, 0, 19);
        this.singleBottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.singleLid = new ModelPart(64, 64, 0, 0);
        this.singleLid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.singleLid.y = 10.0F;
        this.singleLid.z = 1.0F;
        this.singleLatch = new ModelPart(64, 64, 0, 0);
        this.singleLatch.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.singleLatch.y = 9.0F;
    }

    @Override
    public void render(SkyChestTileEntity tileEntityIn, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.pushPose();
        float f = tileEntityIn.getForward().toYRot();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(-f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = tileEntityIn.getOpenNess(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        Material material = this.getRenderMaterial(tileEntityIn);
        VertexConsumer ivertexbuilder = material.buffer(bufferIn, RenderType::entityCutout);
        this.renderModels(matrixStackIn, ivertexbuilder, this.singleLid, this.singleLatch, this.singleBottom, f1,
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

    protected Material getRenderMaterial(SkyChestTileEntity tileEntity) {
        SkyChestType type = SkyChestType.BLOCK;
        if (tileEntity.getLevel() != null) {
            Block blockType = tileEntity.getBlockState().getBlock();

            if (blockType instanceof SkyChestBlock) {
                type = ((SkyChestBlock) blockType).type;
            }
        }

        switch (type) {
            case STONE:
                return TEXTURE_STONE;
            default:
            case BLOCK:
                return TEXTURE_BLOCK;
        }
    }

    public static void registerTextures(TextureStitchEvent.Pre evt) {
        if (evt.getMap().location().equals(Sheets.CHEST_SHEET)) {
            evt.addSprite(TEXTURE_STONE.texture());
            evt.addSprite(TEXTURE_BLOCK.texture());
        }
    }

}
