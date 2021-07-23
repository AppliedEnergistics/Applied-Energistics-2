/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

/**
 * Provides our custom {@link EncodedPatternBakedModel encoded pattern item model}.
 */
public class EncodedPatternModelLoader implements IModelLoader<EncodedPatternModel> {

    public static final EncodedPatternModelLoader INSTANCE = new EncodedPatternModelLoader();

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
    }

    @Override
    public EncodedPatternModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        modelContents.remove("loader"); // Avoid recursion
        ResourceLocation baseModel = new ResourceLocation(GsonHelper.getAsString(modelContents, "baseModel"));
        return new EncodedPatternModel(baseModel);
    }

}
