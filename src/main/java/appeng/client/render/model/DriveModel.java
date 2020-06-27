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

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.IModelTransform;
import net.minecraft.client.render.model.IUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.Material;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.block.storage.DriveSlotCellType;

public class DriveModel implements IModelGeometry<DriveModel> {

    private static final Identifier MODEL_BASE = new Identifier(
            "appliedenergistics2:block/drive/drive_base");

    private static final Map<DriveSlotCellType, Identifier> MODELS_CELLS = ImmutableMap.of(
            DriveSlotCellType.EMPTY, new Identifier("appliedenergistics2:block/drive/drive_cell_empty"),
            DriveSlotCellType.ITEM, new Identifier("appliedenergistics2:block/drive/drive_cell_items"),
            DriveSlotCellType.FLUID, new Identifier("appliedenergistics2:block/drive/drive_cell_fluids"));

    public static final Set<Identifier> DEPENDENCIES = ImmutableSet.<Identifier>builder()
            .addAll(MODELS_CELLS.values()).add(MODEL_BASE).build();

    @Override
    public BakedModel bake(IModelConfiguration owner, ModelLoader bakery,
                           Function<Material, Sprite> spriteGetter, IModelTransform modelTransform,
                           ModelOverrideList overrides, Identifier modelLocation) {
        EnumMap<DriveSlotCellType, BakedModel> cellModels = new EnumMap<>(DriveSlotCellType.class);

        // Load the base model and the model for each cell state.
        for (DriveSlotCellType cellType : MODELS_CELLS.keySet()) {
            BakedModel cellModel = bakery.getBakedModel(MODELS_CELLS.get(cellType), modelTransform, spriteGetter);
            cellModels.put(cellType, cellModel);
        }

        BakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);
        return new DriveBakedModel(baseModel, cellModels);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
                                            Function<Identifier, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

}
