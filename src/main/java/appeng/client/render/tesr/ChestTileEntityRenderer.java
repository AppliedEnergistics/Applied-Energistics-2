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


import appeng.client.render.BakedModelUnwrapper;
import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.client.render.model.DriveBakedModel;
import appeng.tile.storage.ChestBlockEntity;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import appeng.api.client.ICellModelRegistry;
import appeng.block.storage.DriveSlotsState;
import appeng.client.render.FacingToRotation;
import appeng.core.Api;

/**
 * The tile entity renderer for ME chests takes care of rendering the right
 * model for the inserted cell, as well as the LED.
 */
public class ChestTileEntityRenderer extends BlockEntityRenderer<ChestBlockEntity> {

    private final ICellModelRegistry cellModelRegistry = Api.instance().client().cells();

    private final BakedModelManager modelManager;

    private final BlockModelRenderer blockRenderer;

    public ChestTileEntityRenderer(BlockEntityRenderDispatcher renderDispatcher) {
        super(renderDispatcher);
        MinecraftClient client = MinecraftClient.getInstance();
        modelManager = client.getBakedModelManager();
        blockRenderer = client.getBlockRenderManager().getModelRenderer();
    }

    @Override
    public void render(ChestBlockEntity chest, float partialTicks, MatrixStack matrices, VertexConsumerProvider buffers,
                       int combinedLight, int combinedOverlay) {

        World world = chest.getWorld();
        if (world == null) {
            return;
        }

        DriveSlotsState driveSlotState = DriveSlotsState.fromChestOrDrive(chest);

        Item cellItem = driveSlotState.getCell(0);
        if (cellItem == null || cellItem == Items.AIR) {
            return; // No cell inserted into chest
        }

        // Try to get the right cell chassis model from the drive model since it already loads them all
        DriveBakedModel driveModel = getDriveModel();
        if (driveModel == null) {
            return;
        }
        BakedModel cellModel = driveModel.getCellChassisModel(cellItem);

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        FacingToRotation rotation = FacingToRotation.get(chest.getForward(), chest.getUp());
        rotation.push(matrices);
        matrices.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        matrices.translate(5 / 16.0, 4 / 16.0, 0);

        // Render the cell model as-if it was a block model
        VertexConsumer buffer = buffers.getBuffer(RenderLayer.getCutout());
        // We "fake" the position here to make it use the light-value in front of the
        // drive
        BakedModel rotatedModel = new FaceRotatingModel(cellModel, rotation);
        blockRenderer.render(world, rotatedModel, chest.getCachedState(), chest.getPos(), matrices, buffer, false,
                new Random(), 0L, combinedOverlay);

        VertexConsumer ledBuffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, matrices, partialTicks);

        matrices.pop();
    }

    private DriveBakedModel getDriveModel() {
        BakedModel driveModel = modelManager.getBlockModels().getModel(Api.instance().definitions().blocks().drive().block().getDefaultState());
        return BakedModelUnwrapper.unwrap(driveModel, DriveBakedModel.class);
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the
     * faces will not be correctly rotated so the incorrect lighting data would be
     * used to apply diffuse lighting and the lightmap texture.
     */
    private static class FaceRotatingModel extends ForwardingBakedModel {
        private final FacingToRotation r;

        protected FaceRotatingModel(BakedModel base, FacingToRotation r) {
            this.wrapped = base;
            this.r = r;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
            if (side != null) {
                side = r.resultingRotate(side); // This fixes the incorrect lightmap position
            }
            List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand));

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                quads.set(i, new BakedQuad(quad.getVertexData(), quad.getColorIndex(), r.rotate(quad.getFace()),
                        /* FIXME FABRIC: sprite is protected but unused?? */ null, quad.hasShade()));
            }

            return quads;
        }
    }

}
