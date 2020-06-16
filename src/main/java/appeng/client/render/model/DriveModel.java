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

import java.util.*;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.block.storage.DriveSlotCellType;
import appeng.block.storage.DriveSlotState;

public class DriveModel implements IModelGeometry<DriveModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_base");

    private static final Map<DriveSlotCellType, ResourceLocation> MODELS_CELLS = ImmutableMap.of(
            DriveSlotCellType.EMPTY, new ResourceLocation("appliedenergistics2:block/drive/drive_cell_empty"),
            DriveSlotCellType.ITEM, new ResourceLocation("appliedenergistics2:block/drive/drive_cell_items"),
            DriveSlotCellType.FLUID, new ResourceLocation("appliedenergistics2:block/drive/drive_cell_fluids"));

    public static final Set<ResourceLocation> DEPENDENCIES = ImmutableSet.<ResourceLocation>builder()
            .addAll(MODELS_CELLS.values()).add(MODEL_BASE).build();

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        EnumMap<DriveSlotCellType, IBakedModel> cellModels = new EnumMap<>(DriveSlotCellType.class);

        // Load the base model and the model for each cell state.
        for (DriveSlotCellType cellType : MODELS_CELLS.keySet()) {
            IBakedModel cellModel = bakery.getBakedModel(MODELS_CELLS.get(cellType), modelTransform, spriteGetter);
            cellModels.put(cellType, cellModel);
        }

        IBakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);
        return new DriveBakedModel(baseModel, cellModels);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

}
