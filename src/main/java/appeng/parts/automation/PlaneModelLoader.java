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

package appeng.parts.automation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class PlaneModelLoader implements IGeometryLoader<PlaneModel> {

    public static final PlaneModelLoader INSTANCE = new PlaneModelLoader();

    @Override
    public PlaneModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
            throws JsonParseException {
        String frontTexture = modelContents.get("front").getAsString();
        String sidesTexture = modelContents.get("sides").getAsString();
        String backTexture = modelContents.get("back").getAsString();

        return new PlaneModel(new ResourceLocation(frontTexture), new ResourceLocation(sidesTexture),
                new ResourceLocation(backTexture));
    }

}
