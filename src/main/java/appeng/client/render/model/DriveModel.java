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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import appeng.api.client.StorageCellModels;
import appeng.client.render.BasicUnbakedModel;
import appeng.init.internal.InitStorageCells;

public class DriveModel implements BasicUnbakedModel {

    private static final ResourceLocation MODEL_BASE = ResourceLocation.parse(
            "ae2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = ResourceLocation.parse(
            "ae2:block/drive/drive_cell_empty");

    @Nullable
    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelTransform) {
        final Map<Item, BakedModel> cellModels = new IdentityHashMap<>();

        // Load the base model and the model for each cell model.
        for (var entry : StorageCellModels.models().entrySet()) {
            var cellModel = baker.bake(entry.getValue(), modelTransform);
            cellModels.put(entry.getKey(), cellModel);
        }

        final BakedModel baseModel = baker.bake(MODEL_BASE, modelTransform);
        final BakedModel defaultCell = baker.bake(StorageCellModels.getDefaultModel(), modelTransform);
        cellModels.put(Items.AIR, baker.bake(MODEL_CELL_EMPTY, modelTransform));

        return new DriveBakedModel(modelTransform.getRotation(), baseModel, cellModels, defaultCell);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.<ResourceLocation>builder().add(StorageCellModels.getDefaultModel())
                .addAll(InitStorageCells.getModels())
                .addAll(StorageCellModels.models().values()).build();
    }

}
