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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;

import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyChestBlock.SkyChestType;
import appeng.core.AppEng;
import appeng.tile.storage.SkyChestTileEntity;

// This is mostly a copy&paste job of the vanilla chest TESR
@OnlyIn(Dist.CLIENT)
public class SkyChestTESR extends TileEntityRenderer<SkyChestTileEntity> {

    public static final RenderMaterial TEXTURE_STONE = new RenderMaterial(Atlases.CHEST_ATLAS,
            new ResourceLocation(AppEng.MOD_ID, "models/skychest"));
    public static final RenderMaterial TEXTURE_BLOCK = new RenderMaterial(Atlases.CHEST_ATLAS,
            new ResourceLocation(AppEng.MOD_ID, "models/skyblockchest"));

    private final ModelRenderer singleLid;
    private final ModelRenderer singleBottom;
    private final ModelRenderer singleLatch;

    public SkyChestTESR(TileEntityRendererDispatcher rendererDispatcherIn) {
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
    public void render(SkyChestTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
            IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        float f = tileEntityIn.getForward().getHorizontalAngle();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = tileEntityIn.getLidAngle(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        RenderMaterial material = this.getRenderMaterial(tileEntityIn);
        IVertexBuilder ivertexbuilder = material.getBuffer(bufferIn, RenderType::getEntityCutout);
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

    protected RenderMaterial getRenderMaterial(SkyChestTileEntity tileEntity) {
        SkyChestType type = SkyChestType.BLOCK;
        if (tileEntity.getWorld() != null) {
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
        if (evt.getMap().getTextureLocation().equals(Atlases.CHEST_ATLAS)) {
            evt.addSprite(TEXTURE_STONE.getTextureLocation());
            evt.addSprite(TEXTURE_BLOCK.getTextureLocation());
        }
    }

}
