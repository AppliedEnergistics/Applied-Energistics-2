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


import appeng.block.storage.DriveSlotState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;


public class DriveModel implements IModel {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation("appliedenergistics2:block/drive_base");

    private static final Map<DriveSlotState, ResourceLocation> MODELS_CELLS = ImmutableMap.of(
            DriveSlotState.EMPTY, new ResourceLocation("appliedenergistics2:block/drive_cell_empty"),
            DriveSlotState.OFFLINE, new ResourceLocation("appliedenergistics2:block/drive_cell_off"),
            DriveSlotState.ONLINE, new ResourceLocation("appliedenergistics2:block/drive_cell_on"),
            DriveSlotState.TYPES_FULL, new ResourceLocation("appliedenergistics2:block/drive_cell_types_full"),
            DriveSlotState.FULL, new ResourceLocation("appliedenergistics2:block/drive_cell_full"));

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableList.<ResourceLocation>builder().add(MODEL_BASE).addAll(MODELS_CELLS.values()).build();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return Collections.emptyList();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        EnumMap<DriveSlotState, IBakedModel> cellModels = new EnumMap<>(DriveSlotState.class);

        // Load the base model and the model for each cell state.
        IModel baseModel;
        try {
            baseModel = ModelLoaderRegistry.getModel(MODEL_BASE);
            for (DriveSlotState slotState : MODELS_CELLS.keySet()) {
                IModel model = ModelLoaderRegistry.getModel(MODELS_CELLS.get(slotState));
                cellModels.put(slotState, model.bake(state, format, bakedTextureGetter));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        IBakedModel bakedBase = baseModel.bake(state, format, bakedTextureGetter);
        return new DriveBakedModel(bakedBase, cellModels);
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}
