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

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

import appeng.core.AEConfig;
import appeng.core.sync.BasePacket;

public class GrinderRecipeSerializer implements IRecipeSerializer<GrinderRecipe> {

    public static final GrinderRecipeSerializer INSTANCE = new GrinderRecipeSerializer();

    private GrinderRecipeSerializer() {
    }

    @Override
    public GrinderRecipe read(ResourceLocation recipeId, JsonObject json) {
        String group = JSONUtils.getString(json, "group", "");
        JsonObject inputObj = JSONUtils.getJsonObject(json, "input");
        Ingredient ingredient = Ingredient.deserialize(inputObj);
        int ingredientCount = 1;
        if (inputObj.has("count")) {
            ingredientCount = inputObj.get("count").getAsInt();
        }

        JsonObject result = JSONUtils.getJsonObject(json, "result");
        ItemStack primaryResult = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(result, "primary"));
        JsonArray optionalResultsJson = JSONUtils.getJsonArray(result, "optional", null);
        List<GrinderOptionalResult> optionalResults = Collections.emptyList();
        if (optionalResultsJson != null) {
            optionalResults = new ArrayList<>(optionalResultsJson.size());
            for (JsonElement optionalResultJson : optionalResultsJson) {
                if (!optionalResultJson.isJsonObject()) {
                    throw new IllegalStateException("Entry in optional result list should be an object.");
                }
                ItemStack optionalResultItem = ShapedRecipe.deserializeItem(optionalResultJson.getAsJsonObject());
                float optionalChance = JSONUtils.getFloat(optionalResultJson.getAsJsonObject(), "percentageChance",
                        AEConfig.instance().getOreDoublePercentage()) / 100.0f;
                optionalResults.add(new GrinderOptionalResult(optionalChance, optionalResultItem));
            }
        }

        int turns = JSONUtils.getInt(json, "turns", 8);

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, primaryResult, turns, optionalResults);
    }

    @Nullable
    @Override
    public GrinderRecipe read(ResourceLocation recipeId, PacketBuffer buffer) {

        String group = buffer.readString(BasePacket.MAX_STRING_LENGTH);
        Ingredient ingredient = Ingredient.read(buffer);
        int ingredientCount = buffer.readVarInt();
        ItemStack result = buffer.readItemStack();
        int turns = buffer.readVarInt();
        int optionalResultsCount = buffer.readVarInt();
        List<GrinderOptionalResult> optionalResults = new ArrayList<>(optionalResultsCount);
        for (int i = 0; i < optionalResultsCount; i++) {
            float chance = buffer.readFloat();
            ItemStack optionalResult = buffer.readItemStack();
            optionalResults.add(new GrinderOptionalResult(chance, optionalResult));
        }

        return new GrinderRecipe(recipeId, group, ingredient, ingredientCount, result, turns, optionalResults);
    }

    @Override
    public void write(PacketBuffer buffer, GrinderRecipe recipe) {
        buffer.writeString(recipe.getGroup());
        recipe.getIngredient().write(buffer);
        buffer.writeVarInt(recipe.getIngredientCount());
        buffer.writeItemStack(recipe.getRecipeOutput());
        buffer.writeVarInt(recipe.getTurns());
        List<GrinderOptionalResult> optionalResults = recipe.getOptionalResults();
        buffer.writeVarInt(optionalResults.size());
        for (GrinderOptionalResult optionalResult : optionalResults) {
            buffer.writeFloat(optionalResult.getChance());
            buffer.writeItemStack(optionalResult.getResult());
        }
    }

}
