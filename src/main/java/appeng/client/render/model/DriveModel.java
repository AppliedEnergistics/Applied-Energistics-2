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

import appeng.api.client.ICellModelRegistry;
import appeng.client.render.BasicUnbakedModel;
import appeng.core.Api;
import appeng.core.api.client.ApiCellModelRegistry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class DriveModel implements BasicUnbakedModel {

    private static final Identifier MODEL_BASE = new Identifier(
            "appliedenergistics2:block/drive/drive_base");
    private static final Identifier MODEL_CELL_EMPTY = new Identifier(
            "appliedenergistics2:block/drive/drive_cell_empty");

    @Nullable
    @Override
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        final ICellModelRegistry cellRegistry = Api.instance().client().cells();
        final Map<Item, BakedModel> cellModels = new IdentityHashMap<>();

        // Load the base model and the model for each cell model.
        for (Entry<Item, Identifier> entry : cellRegistry.models().entrySet()) {
            BakedModel cellModel = loader.bake(entry.getValue(), rotationContainer);
            cellModels.put(entry.getKey(), cellModel);
        }

        final BakedModel baseModel = loader.bake(MODEL_BASE, rotationContainer);
        final BakedModel defaultCell = loader.bake(cellRegistry.getDefaultModel(), rotationContainer);
        cellModels.put(Items.AIR, loader.bake(MODEL_CELL_EMPTY, rotationContainer));

        return new DriveBakedModel(baseModel, cellModels, defaultCell);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        ICellModelRegistry cells = Api.instance().client().cells();
        return ImmutableSet.<Identifier>builder()
            .add(cells.getDefaultModel())
            .addAll(ApiCellModelRegistry.getModels())
            .addAll(cells.models().values())
            .build();
    }

}
