/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.recipes.factories.conditions;


import appeng.core.Api;
import appeng.core.AppEng;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.function.BooleanSupplier;


public class MaterialExists implements IConditionFactory {
    private static final String JSON_MATERIAL_KEY = "material";

    @Override
    public BooleanSupplier parse(JsonContext jsonContext, JsonObject jsonObject) {
        final boolean result;

        if (JsonUtils.isString(jsonObject, JSON_MATERIAL_KEY)) {
            final String material = JsonUtils.getString(jsonObject, JSON_MATERIAL_KEY);
            final Object item = Api.INSTANCE.registries().recipes().resolveItem(AppEng.MOD_ID, material);

            result = item != null;
        } else {
            result = false;
        }

        return () -> result;

    }
}