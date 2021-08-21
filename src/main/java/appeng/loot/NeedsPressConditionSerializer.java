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

package appeng.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.Serializer;

/**
 * Serializer for {@link NeedsPressCondition}.
 */
public class NeedsPressConditionSerializer implements Serializer<NeedsPressCondition> {
    @Override
    public void serialize(JsonObject json, NeedsPressCondition condition, JsonSerializationContext context) {
        json.addProperty("press", condition.getNeeded().getCriterionName());
    }

    @Override
    public NeedsPressCondition deserialize(JsonObject json, JsonDeserializationContext context) {
        var typeStr = GsonHelper.getAsString(json, "press");
        for (NeededPressType type : NeededPressType.values()) {
            if (type.getCriterionName().equals(typeStr)) {
                return new NeedsPressCondition(type);
            }
        }

        throw new JsonParseException("Unknown press type: " + typeStr);
    }
}
