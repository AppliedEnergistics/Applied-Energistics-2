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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.event.TextureStitchEvent;

import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyChestBlock.SkyChestType;
import appeng.core.AppEng;
import appeng.tile.storage.SkyChestBlockEntity;

// This is mostly a copy&paste job of the vanilla chest TESR
@Environment(EnvType.CLIENT)
public class SkyChestTESR extends BlockEntityRenderer<SkyChestBlockEntity> {

    public static final Material TEXTURE_STONE = new Material(Atlases.CHEST_ATLAS,
            new Identifier(AppEng.MOD_ID, "models/skychest"));
    public static final Material TEXTURE_BLOCK = new Material(Atlases.CHEST_ATLAS,
            new Identifier(AppEng.MOD_ID, "models/skyblockchest"));

    private final ModelRenderer singleLid;
    private final ModelRenderer singleBottom;
    private final ModelRenderer singleLatch;

    public SkyChestTESR(BlockEntityRenderDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        this.singleBottom = new ModelRenderer(64, 64, 0, 19);
        this.singleBottom.addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F, 0.0F);
        this.singleLid = new ModelRenderer(64, 64, 0, 0);
        this.singleLid.addBox(1.0F, 0.0F, 0.0F, 14.0F, 5.0F, 14.0F, 0.0F);
        this.singleLid.rotationPointY = 9.0F;
        this.singleLid.rotationPointZ = 1.0F;
        this.singleLatch = new ModelRenderer(64, 64, 0, 0);
        this.singleLatch.addBox(7.0F, -1.0F, 15.0F, 2.0F, 4.0F, 1.0F, 0.0F);
        this.singleLatch.rotationPointY = 8.0F;
    }

    @Override
    public void render(SkyChestBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
                       VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        float f = tileEntityIn.getForward().getHorizontalAngle();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = tileEntityIn.getLidAngle(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        Material material = this.getMaterial(tileEntityIn);
        IVertexBuilder ivertexbuilder = material.getBuffer(bufferIn, RenderLayer::getEntityCutout);
        this.renderModels(matrixStackIn, ivertexbuilder, this.singleLid, this.singleLatch, this.singleBottom, f1,
                combinedLightIn, combinedOverlayIn);

        matrixStackIn.pop();
    }

    private void renderModels(MatrixStack matrixStackIn, IVertexBuilder bufferIn, ModelRenderer chestLid,
            ModelRenderer chestLatch, ModelRenderer chestBottom, float lidAngle, int combinedLightIn,
            int combinedOverlayIn) {
        chestLid.rotateAngleX = -(lidAngle * 1.5707964F);
        chestLatch.rotateAngleX = chestLid.rotateAngleX;
        chestLid.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestLatch.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestBottom.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    protected Material getMaterial(SkyChestBlockEntity tileEntity) {
        SkyChestType type = SkyChestType.BLOCK;
        if (tileEntity.getWorld() != null) {
            Block blockType = tileEntity.getCachedState().getBlock();

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
        if (evt.getMap().getTextureLocation().equals(Atlases.CHEST_ATLAS)) {
            evt.addSprite(TEXTURE_STONE.getTextureLocation());
            evt.addSprite(TEXTURE_BLOCK.getTextureLocation());
        }
    }

}
