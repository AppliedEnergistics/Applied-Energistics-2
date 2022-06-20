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

package appeng.recipes.handlers;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nullable;

public class TransformRecipeSerializer implements RecipeSerializer<TransformRecipe> {

    public static final TransformRecipeSerializer INSTANCE = new TransformRecipeSerializer();

    private TransformRecipeSerializer() {
    }

    @Override
    public TransformRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient ingredients = Ingredient.fromJson(json.get("ingredients"));
        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        return new TransformRecipe(recipeId, ingredients, result.getItem(), result.getCount());
    }

    @Nullable
    @Override
    public TransformRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        ItemStack output = buffer.readItem();
        Ingredient ingredients = Ingredient.fromNetwork(buffer);

        return new TransformRecipe(recipeId, ingredients, output.getItem(), output.getCount());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, TransformRecipe recipe) {
        buffer.writeItem(new ItemStack(recipe.output, recipe.count));
        recipe.ingredients.toNetwork(buffer);
    }

}
