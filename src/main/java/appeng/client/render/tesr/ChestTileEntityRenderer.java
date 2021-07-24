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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.world.item.Item;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import appeng.block.storage.DriveSlotsState;
import appeng.client.render.BakedModelUnwrapper;
import appeng.client.render.DelegateBakedModel;
import appeng.client.render.FacingToRotation;
import appeng.client.render.model.DriveBakedModel;
import appeng.core.definitions.AEBlocks;
import appeng.tile.storage.ChestTileEntity;

/**
 * The tile entity renderer for ME chests takes care of rendering the right model for the inserted cell, as well as the
 * LED.
 */
public class ChestTileEntityRenderer implements BlockEntityRenderer<ChestTileEntity> {

    private final ModelManager modelManager;

    private final ModelBlockRenderer blockRenderer;

    public ChestTileEntityRenderer(BlockEntityRendererProvider.Context context) {
        Minecraft client = Minecraft.getInstance();
        modelManager = client.getModelManager();
        blockRenderer = client.getBlockRenderer().getModelRenderer();
    }

    @Override
    public void render(ChestTileEntity chest, float partialTicks, PoseStack matrices, MultiBufferSource buffers,
                       int combinedLight, int combinedOverlay) {

        Level world = chest.getLevel();
        if (world == null) {
            return;
        }

        DriveSlotsState driveSlotState = DriveSlotsState.fromChestOrDrive(chest);

        Item cellItem = driveSlotState.getCell(0);
        if (cellItem == null || cellItem == Items.AIR) {
            return; // No cell inserted into chest
        }

        // Try to get the right cell chassis model from the drive model since it already
        // loads them all
        DriveBakedModel driveModel = getDriveModel();
        if (driveModel == null) {
            return;
        }
        BakedModel cellModel = driveModel.getCellChassisModel(cellItem);

        matrices.pushPose();
        matrices.translate(0.5, 0.5, 0.5);
        FacingToRotation rotation = FacingToRotation.get(chest.getForward(), chest.getUp());
        rotation.push(matrices);
        matrices.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        matrices.translate(5 / 16.0, 4 / 16.0, 0);

        // Render the cell model as-if it was a block model
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());
        // We "fake" the position here to make it use the light-value in front of the
        // drive
        FaceRotatingModel rotatedModel = new FaceRotatingModel(cellModel, rotation);
        blockRenderer.renderModel(world, rotatedModel, chest.getBlockState(), chest.getBlockPos(), matrices, buffer, false,
                new Random(), 0L, combinedOverlay, EmptyModelData.INSTANCE);

        VertexConsumer ledBuffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, matrices, partialTicks);

        matrices.popPose();
    }

    private DriveBakedModel getDriveModel() {
        BakedModel driveModel = modelManager.getBlockModelShaper()
                .getBlockModel(AEBlocks.DRIVE.block().defaultBlockState());
        return BakedModelUnwrapper.unwrap(driveModel, DriveBakedModel.class);
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the faces will not be correctly rotated so
     * the incorrect lighting data would be used to apply diffuse lighting and the lightmap texture.
     */
    private static class FaceRotatingModel extends DelegateBakedModel {
        private final FacingToRotation r;

        protected FaceRotatingModel(BakedModel base, FacingToRotation r) {
            super(base);
            this.r = r;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
                                        @Nonnull IModelData extraData) {
            if (side != null) {
                side = r.resultingRotate(side); // This fixes the incorrect lightmap position
            }
            List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                quads.set(i, new BakedQuad(quad.getVertices(), quad.getTintIndex(), r.rotate(quad.getDirection()),
                        quad.getSprite(), quad.isShade()));
            }

            return quads;
        }
    }

}
