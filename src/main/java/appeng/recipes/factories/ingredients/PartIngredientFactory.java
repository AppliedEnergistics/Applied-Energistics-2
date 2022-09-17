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

package appeng.recipes.factories.ingredients;


import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import com.google.gson.JsonObject;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;


public class PartIngredientFactory implements IIngredientFactory {

    @Nonnull
    @Override
    public net.minecraft.item.crafting.Ingredient parse(JsonContext context, JsonObject json) {
        final String partName = json.get("part").getAsString();
        final Object result = Api.INSTANCE.registries().recipes().resolveItem(AppEng.MOD_ID, partName);

        if (result instanceof ResolverResultSet) {
            final ResolverResultSet resolverResultSet = (ResolverResultSet) result;

            return net.minecraft.item.crafting.Ingredient
                    .fromStacks(resolverResultSet.results.toArray(new ItemStack[resolverResultSet.results.size()]));
        } else if (result instanceof ResolverResult) {
            final ResolverResult resolverResult = (ResolverResult) result;

            final Item item = Item.getByNameOrId(AppEng.MOD_ID + ":" + resolverResult.itemName);
            final ItemStack itemStack = new ItemStack(item, 1, resolverResult.damageValue, resolverResult.compound);

            return net.minecraft.item.crafting.Ingredient.fromStacks(itemStack);
        }

        AELog.warn("Looking for ingredient with name '" + partName + "' ended up with a null item!");
        return net.minecraft.item.crafting.Ingredient.EMPTY;
    }
}