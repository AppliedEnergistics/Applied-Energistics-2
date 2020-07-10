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
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.api.AEApi;

public class DriveModel implements IModelGeometry<DriveModel> {

    private static final ResourceLocation MODEL_BASE = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_base");
    private static final ResourceLocation MODEL_CELL_EMPTY = new ResourceLocation(
            "appliedenergistics2:block/drive/drive_cell_empty");

    @Override
    public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform,
            ItemOverrideList overrides, ResourceLocation modelLocation) {
        Map<Item, IBakedModel> cellModels = new IdentityHashMap<>();

        // Load the base model and the model for each cell model.
        for (Entry<Item, ResourceLocation> entry : AEApi.instance().client().cells().models().entrySet()) {
            IBakedModel cellModel = bakery.getBakedModel(entry.getValue(), modelTransform, spriteGetter);
            cellModels.put(entry.getKey(), cellModel);
        }

        IBakedModel baseModel = bakery.getBakedModel(MODEL_BASE, modelTransform, spriteGetter);
        cellModels.put(Items.AIR, bakery.getBakedModel(MODEL_CELL_EMPTY, modelTransform, spriteGetter));

        return new DriveBakedModel(baseModel, cellModels);
    }

    @Override
    public Collection<Material> getTextures(IModelConfiguration owner,
            Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        return Collections.emptyList();
    }

}
