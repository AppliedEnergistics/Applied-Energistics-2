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

import javax.annotation.Nullable;

import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import appeng.client.render.DelegateBakedModel;
import appeng.thirdparty.fabric.MutableQuadView;
import appeng.thirdparty.fabric.RenderContext;

public class DriveBakedModel extends DelegateBakedModel {
    private final Map<Item, BakedModel> cellModels;
    private final BakedModel defaultCellModel;

    private final RenderContext.QuadTransform[] slotTransforms;

    public DriveBakedModel(BakedModel bakedBase, Map<Item, BakedModel> cellModels, BakedModel defaultCell) {
        super(bakedBase);
        this.defaultCellModel = defaultCell;
        this.slotTransforms = buildSlotTransforms();
        this.cellModels = cellModels;
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

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand,
            ModelData extraData, RenderType renderType) {
        List<BakedQuad> result = new ArrayList<>(super.getQuads(state, side, rand, extraData, renderType));

        var cells = extraData.get(DriveModelData.STATE);

        // Add cell models on top of the base model, if possible
        if (cells != null) {
            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 2; col++) {
                    int slot = getSlotIndex(row, col);

                    // Add the cell chassis
                    Item cell = slot < cells.length ? cells[slot] : null;
                    BakedModel cellChassisModel = getCellChassisModel(cell);

                    var quadView = MutableQuadView.getInstance();
                    for (BakedQuad quad : cellChassisModel.getQuads(state, side, rand, ModelData.EMPTY, renderType)) {
                        quadView.fromVanilla(quad, side);
                        slotTransforms[slot].transform(quadView);
                        result.add(quadView.toBlockBakedQuad());
                    }
                }
            }
        }

        return result;
    }

    @Override
    public boolean useAmbientOcclusion() {
        // We have faces inside the chassis that are facing east, but should not receive
        // ambient occlusion from the east-side, but sadly this cannot be fine-tuned on
        // a face-by-face basis.
        return false;
    }

    // Determine which drive chassis to show based on the used cell
    public BakedModel getCellChassisModel(Item cell) {
        if (cell == null) {
            return cellModels.get(Items.AIR);
        }
        final BakedModel model = cellModels.get(cell);

        return model != null ? model : defaultCellModel;
    }

    private RenderContext.QuadTransform[] buildSlotTransforms() {
        RenderContext.QuadTransform[] result = new RenderContext.QuadTransform[5 * 2];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 2; col++) {

                Vector3f translation = new Vector3f();
                getSlotOrigin(row, col, translation);

                result[getSlotIndex(row, col)] = new QuadTranslator(translation.x(), translation.y(),
                        translation.z());
            }
        }

        return result;
    }

    private static int getSlotIndex(int row, int col) {
        return row * 2 + col;
    }

    private static class QuadTranslator implements RenderContext.QuadTransform {
        private final float x;
        private final float y;
        private final float z;

        public QuadTranslator(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean transform(MutableQuadView quad) {
            Vector3f target = new Vector3f();
            for (int i = 0; i < 4; i++) {
                quad.copyPos(i, target);
                target.add(x, y, z);
                quad.pos(i, target);
            }
            return true;
        }
    }
}
