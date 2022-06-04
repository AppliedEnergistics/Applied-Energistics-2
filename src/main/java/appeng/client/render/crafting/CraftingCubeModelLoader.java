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

package appeng.client.render.crafting;

import java.util.Locale;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

import appeng.block.crafting.CraftingUnitType;

/**
 * Loader that allows access to the built-in crafting cube model from block-model JSONs.
 */
public class CraftingCubeModelLoader implements IModelLoader<CraftingCubeModel> {

    public static final CraftingCubeModelLoader INSTANCE = new CraftingCubeModelLoader();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public CraftingCubeModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        CraftingUnitType unitType = null;

        JsonElement typeEl = modelContents.get("type");
        if (typeEl != null) {
            String typeName = deserializationContext.deserialize(typeEl, String.class);
            if (typeName != null) {
                unitType = CraftingUnitType.valueOf(typeName.toUpperCase(Locale.ROOT));
            }
        }
        if (unitType == null) {
            throw new JsonParseException("type property is missing");
        }

        return new CraftingCubeModel(new CraftingUnitModelProvider(unitType));
    }

}
