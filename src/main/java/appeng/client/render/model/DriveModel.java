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
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import appeng.api.client.StorageCellModels;
import appeng.client.render.BasicUnbakedModel;
import appeng.init.internal.InitStorageCells;

public class DriveModel implements BasicUnbakedModel<DriveModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "ae2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = new ResourceLocation(
            "ae2:block/drive/drive_cell_empty");

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform,
            ItemOverrides overrides, ResourceLocation modelLocation) {
        final Map<Item, BakedModel> cellModels = new IdentityHashMap<>();

        // Load the base model and the model for each cell model.
        for (Entry<Item, ResourceLocation> entry : StorageCellModels.models().entrySet()) {
            BakedModel cellModel = bakery.bake(entry.getValue(), modelTransform, spriteGetter);
            cellModels.put(entry.getKey(), cellModel);
        }

        final BakedModel baseModel = bakery.bake(MODEL_BASE, modelTransform, spriteGetter);
        final BakedModel defaultCell = bakery.bake(StorageCellModels.getDefaultModel(), modelTransform,
                spriteGetter);
        cellModels.put(Items.AIR, bakery.bake(MODEL_CELL_EMPTY, modelTransform, spriteGetter));

        return new DriveBakedModel(baseModel, cellModels, defaultCell);
    }

    @Override
    public Collection<ResourceLocation> getModelDependencies() {
        return ImmutableSet.<ResourceLocation>builder().add(StorageCellModels.getDefaultModel())
                .addAll(InitStorageCells.getModels())
                .addAll(StorageCellModels.models().values()).build();
    }

}
