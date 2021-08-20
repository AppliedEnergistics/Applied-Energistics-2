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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import appeng.core.AEConfig;

public class GrinderRecipeSerializer implements RecipeSerializer<GrinderRecipe> {

    public static final int DEFAULT_TURNS = 8;

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonObject inputObj = GsonHelper.getAsJsonObject(json, "input");
        Ingredient ingredient = Ingredient.fromJson(inputObj);
        int ingredientCount = 1;
        if (inputObj.has("count")) {
            ingredientCount = inputObj.get("count").getAsInt();
        }

        JsonObject result = GsonHelper.getAsJsonObject(json, "result");
        ItemStack primaryResult = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(result, "primary"));
        JsonArray optionalResultsJson = GsonHelper.getAsJsonArray(result, "optional", null);
        List<GrinderOptionalResult> optionalResults = Collections.emptyList();
        if (optionalResultsJson != null) {
            optionalResults = new ArrayList<>(optionalResultsJson.size());
            for (JsonElement optionalResultJson : optionalResultsJson) {
                if (!optionalResultJson.isJsonObject()) {
                    throw new IllegalStateException("Entry in optional result list should be an object.");
                }
                ItemStack optionalResultItem = ShapedRecipe.itemStackFromJson(optionalResultJson.getAsJsonObject());
                float optionalChance = GsonHelper.getAsFloat(optionalResultJson.getAsJsonObject(), "percentageChance",
                        AEConfig.instance().getOreDoublePercentage()) / 100.0f;
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = GsonHelper.getAsInt(json, "turns", DEFAULT_TURNS);

        return new GrinderRecipe(recipeId, ingredient, ingredientCount, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {

        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        int ingredientCount = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        int turns = buffer.readVarInt();
        int optionalResultsCount = buffer.readVarInt();
        List<GrinderOptionalResult> optionalResults = new ArrayList<>(optionalResultsCount);
        for (int i = 0; i < optionalResultsCount; i++) {
            float chance = buffer.readFloat();
            ItemStack optionalResult = buffer.readItem();
            optionalResults.add(new GrinderOptionalResult(chance, optionalResult));
        }

        return new GrinderRecipe(recipeId, ingredient, ingredientCount, result, turns, optionalResults);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GrinderRecipe recipe) {
        recipe.getIngredient().toNetwork(buffer);
        buffer.writeVarInt(recipe.getIngredientCount());
        buffer.writeItem(recipe.getResultItem());
        buffer.writeVarInt(recipe.getTurns());
        List<GrinderOptionalResult> optionalResults = recipe.getOptionalResults();
        buffer.writeVarInt(optionalResults.size());
        for (GrinderOptionalResult optionalResult : optionalResults) {
            buffer.writeFloat(optionalResult.getChance());
            buffer.writeItem(optionalResult.getResult());
        }
    }

}
