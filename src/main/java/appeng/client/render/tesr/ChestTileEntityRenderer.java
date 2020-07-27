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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.client.ICellModelRegistry;
import appeng.block.storage.DriveSlotsState;
import appeng.client.render.DelegateBakedModel;
import appeng.client.render.FacingToRotation;
import appeng.core.Api;
import appeng.tile.storage.ChestTileEntity;

/**
 * The tile entity renderer for ME chests takes care of rendering the right
 * model for the inserted cell, as well as the LED.
 */
public class ChestTileEntityRenderer extends TileEntityRenderer<ChestTileEntity> {

    private final ICellModelRegistry cellModelRegistry = Api.instance().client().cells();

    private final ModelManager modelManager;

    private final BlockModelRenderer blockRenderer;

    public ChestTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
        Minecraft client = Minecraft.getInstance();
        modelManager = client.getModelManager();
        blockRenderer = client.getBlockRendererDispatcher().getBlockModelRenderer();
    }

    @Override
    public void render(ChestTileEntity chest, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffers,
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

        ResourceLocation cellModelLocation = cellModelRegistry.model(cellItem);
        if (cellModelLocation == null) {
            cellModelLocation = cellModelRegistry.getDefaultModel();
        }

        IBakedModel model = modelManager.getModel(cellModelLocation);

        matrices.push();
        matrices.translate(0.5, 0.5, 0.5);
        FacingToRotation rotation = FacingToRotation.get(chest.getForward(), chest.getUp());
        rotation.push(matrices);
        matrices.translate(-0.5, -0.5, -0.5);

        // The models are created for the top-left slot of the drive model,
        // we need to move them into place for the slot on the ME chest
        matrices.translate(5 / 16.0, 4 / 16.0, 0);

        // Render the cell model as-if it was a block model
        IVertexBuilder buffer = buffers.getBuffer(RenderType.getCutout());
        // We "fake" the position here to make it use the light-value in front of the
        // drive
        FaceRotatingModel rotatedModel = new FaceRotatingModel(model, rotation);
        blockRenderer.renderModel(world, rotatedModel, chest.getBlockState(), chest.getPos(), matrices, buffer, false,
                new Random(), 0L, combinedOverlay, EmptyModelData.INSTANCE);

        IVertexBuilder ledBuffer = buffers.getBuffer(CellLedRenderer.RENDER_LAYER);
        CellLedRenderer.renderLed(chest, 0, ledBuffer, matrices, partialTicks);

        matrices.pop();
    }

    /**
     * The actual vertex data will be transformed using the matrix stack, but the
     * faces will not be correctly rotated so the incorrect lighting data would be
     * used to apply diffuse lighting and the lightmap texture.
     */
    private static class FaceRotatingModel extends DelegateBakedModel {
        private final FacingToRotation r;

        protected FaceRotatingModel(IBakedModel base, FacingToRotation r) {
            super(base);
            this.r = r;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
                @Nonnull IModelData extraData) {
            if (side != null) {
                side = r.rotate(side); // This fixes the incorrect lightmap position
            }
            List<BakedQuad> quads = new ArrayList<>(super.getQuads(state, side, rand, extraData));

            for (int i = 0; i < quads.size(); i++) {
                BakedQuad quad = quads.get(i);
                quads.set(i, new BakedQuad(quad.getVertexData(), quad.getTintIndex(), r.rotate(quad.getFace()),
                        quad.func_187508_a(), quad.func_239287_f_()));
            }

            return quads;
        }
    }

}
