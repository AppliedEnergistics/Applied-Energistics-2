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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

import com.google.common.collect.ImmutableList;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

import appeng.block.storage.SkyChestBlock;
import appeng.block.storage.SkyChestBlock.SkyChestType;
import appeng.core.AppEng;
import appeng.tile.storage.SkyChestBlockEntity;

// This is mostly a copy&paste job of the vanilla chest TESR
@Environment(EnvType.CLIENT)
public class SkyChestTESR implements BlockEntityRenderer<SkyChestBlockEntity> {

    public static final SpriteIdentifier TEXTURE_STONE = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "models/skychest"));
    public static final SpriteIdentifier TEXTURE_BLOCK = new SpriteIdentifier(TexturedRenderLayers.CHEST_ATLAS_TEXTURE,
            new Identifier(AppEng.MOD_ID, "models/skyblockchest"));

    public static final ImmutableList<SpriteIdentifier> SPRITES = ImmutableList.of(TEXTURE_STONE, TEXTURE_BLOCK);

    private final ModelPart singleLid;
    private final ModelPart singleBottom;
    private final ModelPart singleLatch;

    public SkyChestTESR(BlockEntityRendererFactory.Context rendererDispatcherIn) {
        ModelPart.Cuboid[] singleBottomCuboids = {
                new ModelPart.Cuboid(0, 19, 1F, 0F, 1F, 14F, 10F, 14F, 0F, 0F, 0F, false, 64F, 64F)
        };
        this.singleBottom = new ModelPart(Arrays.asList(singleBottomCuboids), Collections.emptyMap());

        ModelPart.Cuboid[] singleLidCuboids = {
                new ModelPart.Cuboid(0, 0, 1F, 0F, 0F, 14F, 5F, 14F, 0F, 0F, 0F, false, 64F, 64F)
        };
        this.singleLid = new ModelPart(Arrays.asList(singleLidCuboids), Collections.emptyMap());
        this.singleLid.pivotY = 10F;
        this.singleLid.pivotZ = 1F;

        ModelPart.Cuboid[] singleLatchCuboids = {
                new ModelPart.Cuboid(0, 0, 7F, -1F, 15F, 2F, 4F, 1F, 0F, 0F, 0F, false, 64F, 64F)
        };
        this.singleLatch = new ModelPart(Arrays.asList(singleLatchCuboids), Collections.emptyMap());
        this.singleLatch.pivotY = 9F;
    }

    @Override
    public void render(SkyChestBlockEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn,
            VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
        matrixStackIn.push();
        float f = tileEntityIn.getForward().asRotation();
        matrixStackIn.translate(0.5D, 0.5D, 0.5D);
        matrixStackIn.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-f));
        matrixStackIn.translate(-0.5D, -0.5D, -0.5D);

        float f1 = tileEntityIn.getAnimationProgress(partialTicks);
        f1 = 1.0F - f1;
        f1 = 1.0F - f1 * f1 * f1;
        SpriteIdentifier material = this.getMaterial(tileEntityIn);
        VertexConsumer ivertexbuilder = material.getVertexConsumer(bufferIn, RenderLayer::getEntityCutout);
        this.renderModels(matrixStackIn, ivertexbuilder, this.singleLid, this.singleLatch, this.singleBottom, f1,
                combinedLightIn, combinedOverlayIn);

        matrixStackIn.pop();
    }

    // See ChestBlockEntityRenderer
    private void renderModels(MatrixStack matrixStackIn, VertexConsumer bufferIn, ModelPart chestLid,
            ModelPart chestLatch, ModelPart chestBottom, float lidAngle, int combinedLightIn, int combinedOverlayIn) {
        chestLid.pitch = -(lidAngle * 1.5707964F);
        chestLatch.pitch = chestLid.pitch;
        chestLid.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestLatch.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        chestBottom.render(matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    protected SpriteIdentifier getMaterial(SkyChestBlockEntity tileEntity) {
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

}
