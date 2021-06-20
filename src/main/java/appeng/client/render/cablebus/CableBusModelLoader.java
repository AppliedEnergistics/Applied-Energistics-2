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

package appeng.client.render.cablebus;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.model.IModelLoader;

import appeng.core.registries.PartModels;

public class CableBusModelLoader implements IModelLoader<CableBusModel> {

    private final PartModels partModels;

    public CableBusModelLoader(PartModels partModels) {
        this.partModels = partModels;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
    }

    @Override
    public CableBusModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
        return new CableBusModel(partModels);
    }

}
