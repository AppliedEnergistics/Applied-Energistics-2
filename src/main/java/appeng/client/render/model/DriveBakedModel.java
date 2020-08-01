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

package appeng.client.render.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import appeng.block.storage.DriveSlotsState;
import appeng.client.render.DelegateBakedModel;

public class DriveBakedModel extends DelegateBakedModel {
    private final Map<Item, IBakedModel> bakedCells;
    private final IBakedModel defaultCell;

    public DriveBakedModel(IBakedModel bakedBase, Map<Item, IBakedModel> cellModels, IBakedModel defaultCell) {
        super(bakedBase);
        this.bakedCells = cellModels;
        this.defaultCell = defaultCell;
    }

    /**
     * Calculates the origin of a drive slot for positioning a cell model into it.
     */
    public static void getSlotOrigin(int row, int col, Vector3f translation) {
        // Position this drive model copy at the correct slot. The transform is based on
        // the cell-model being in slot 0,0,0 while the upper left slot's origin is at
        // 9,13,1
        float xOffset = (9 - col * 8) / 16.0f;
        float yOffset = (13 - row * 3) / 16.0f;
        float zOffset = 1 / 16.0f;
        translation.set(xOffset, yOffset, zOffset);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand,
            @Nonnull IModelData extraData) {

        List<BakedQuad> result = new ArrayList<>(super.getQuads(state, side, rand, extraData));

        if (!(extraData instanceof DriveModelData)) {
            return result;
        }
        DriveModelData driveModelData = (DriveModelData) extraData;

        DriveSlotsState slotsState = driveModelData.getSlotsState();

        Vector3f slotTranslation = new Vector3f();
        if (slotsState != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    Matrix4f transform = new Matrix4f();

                    getSlotOrigin(row, col, slotTranslation);
                    transform.setTranslation(slotTranslation.getX(), slotTranslation.getY(), slotTranslation.getZ());

                    int slot = row * 2 + col;

                    // Add the drive chassis
                    Item cell = slotsState.getCell(slot);
                    IBakedModel cellChassisModel = getCellChassisModel(cell);
                    addModel(state, rand, extraData, result, side, cellChassisModel, transform);
                }
            }
        }

        return result;
    }

    @Override
    public boolean isAmbientOcclusion() {
        // We have faces inside the chassis that are facing east, but should not receive
        // ambient occlusion from the east-side, but sadly this cannot be fine-tuned on
        // a face-by-face basis.
        return false;
    }

    // Determine which drive chassis to show based on the used cell
    public IBakedModel getCellChassisModel(Item cell) {
        if (cell == null) {
            return bakedCells.get(Items.AIR);
        }
        final IBakedModel model = bakedCells.get(cell);

        return model != null ? model : defaultCell;
    }

    private static void addModel(@Nullable BlockState state, @Nonnull Random rand, @Nonnull IModelData extraData,
            List<BakedQuad> result, Direction side, IBakedModel bakedCell, Matrix4f transform) {
        MatrixVertexTransformer transformer = new MatrixVertexTransformer(transform);
        for (BakedQuad bakedQuad : bakedCell.getQuads(state, side, rand, extraData)) {
            BakedQuadBuilder builder = new BakedQuadBuilder();
            transformer.setParent(builder);
            transformer.setVertexFormat(builder.getVertexFormat());
            bakedQuad.pipe(transformer);
            result.add(builder.build());
        }
    }

}
