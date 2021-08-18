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

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistryEntry;

import appeng.api.features.InscriberProcessType;

public class InscriberRecipeSerializer extends ForgeRegistryEntry<RecipeSerializer<?>>
        implements RecipeSerializer<InscriberRecipe> {

    public static final InscriberRecipeSerializer INSTANCE = new InscriberRecipeSerializer();

    static {
        INSTANCE.setRegistryName(InscriberRecipe.TYPE_ID);
    }

    private InscriberRecipeSerializer() {
    }

    private static InscriberProcessType getMode(JsonObject json) {
        String mode = GsonHelper.getAsString(json, "mode", "inscribe");
        return switch (mode) {
            case "inscribe" -> InscriberProcessType.INSCRIBE;
            case "press" -> InscriberProcessType.PRESS;
            default -> throw new IllegalStateException("Unknown mode for inscriber recipe: " + mode);
        };

    }

    @Override
    public InscriberRecipe fromJson(ResourceLocation recipeId, JsonObject json) {

        InscriberProcessType mode = getMode(json);

        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        // Deserialize the three parts of the input
        JsonObject ingredients = GsonHelper.getAsJsonObject(json, "ingredients");
        Ingredient middle = Ingredient.fromJson(ingredients.get("middle"));
        Ingredient top = Ingredient.EMPTY;
        if (ingredients.has("top")) {
            top = Ingredient.fromJson(ingredients.get("top"));
        }
        Ingredient bottom = Ingredient.EMPTY;
        if (ingredients.has("bottom")) {
            bottom = Ingredient.fromJson(ingredients.get("bottom"));
        }

        return new InscriberRecipe(recipeId, middle, result, top, bottom, mode);
    }

    @Nullable
    @Override
    public InscriberRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient middle = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        Ingredient top = Ingredient.fromNetwork(buffer);
        Ingredient bottom = Ingredient.fromNetwork(buffer);
        InscriberProcessType mode = buffer.readEnum(InscriberProcessType.class);

        return new InscriberRecipe(recipeId, middle, result, top, bottom, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, InscriberRecipe recipe) {
        recipe.getMiddleInput().toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem());
        recipe.getTopOptional().toNetwork(buffer);
        recipe.getBottomOptional().toNetwork(buffer);
        buffer.writeEnum(recipe.getProcessType());
    }

}
