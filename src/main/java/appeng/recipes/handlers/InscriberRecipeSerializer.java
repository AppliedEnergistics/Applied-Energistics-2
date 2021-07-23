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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import appeng.api.features.InscriberProcessType;
import appeng.core.sync.BasePacket;

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
        switch (mode) {
            case "inscribe":
                return InscriberProcessType.INSCRIBE;
            case "press":
                return InscriberProcessType.PRESS;
            default:
                throw new IllegalStateException("Unknown mode for inscriber recipe: " + mode);
        }

    }

    @Override
    public InscriberRecipe fromJson(ResourceLocation recipeId, JsonObject json) {

        InscriberProcessType mode = getMode(json);

        String group = GsonHelper.getAsString(json, "group", "");
        net.minecraft.world.item.ItemStack result = ShapedRecipe.itemFromJson(GsonHelper.getAsJsonObject(json, "result"));

        // Deserialize the three parts of the input
        JsonObject ingredients = GsonHelper.getAsJsonObject(json, "ingredients");
        net.minecraft.world.item.crafting.Ingredient middle = Ingredient.fromJson(ingredients.get("middle"));
        net.minecraft.world.item.crafting.Ingredient top = Ingredient.EMPTY;
        if (ingredients.has("top")) {
            top = net.minecraft.world.item.crafting.Ingredient.fromJson(ingredients.get("top"));
        }
        net.minecraft.world.item.crafting.Ingredient bottom = Ingredient.EMPTY;
        if (ingredients.has("bottom")) {
            bottom = net.minecraft.world.item.crafting.Ingredient.fromJson(ingredients.get("bottom"));
        }

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Nullable
    @Override
    public InscriberRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String group = buffer.readUtf(BasePacket.MAX_STRING_LENGTH);
        net.minecraft.world.item.crafting.Ingredient middle = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        net.minecraft.world.item.crafting.Ingredient top = net.minecraft.world.item.crafting.Ingredient.fromNetwork(buffer);
        Ingredient bottom = net.minecraft.world.item.crafting.Ingredient.fromNetwork(buffer);
        InscriberProcessType mode = buffer.readEnum(InscriberProcessType.class);

        return new InscriberRecipe(recipeId, group, middle, result, top, bottom, mode);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, InscriberRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        recipe.getMiddleInput().toNetwork(buffer);
        buffer.writeItem(recipe.getResultItem());
        recipe.getTopOptional().toNetwork(buffer);
        recipe.getBottomOptional().toNetwork(buffer);
        buffer.writeEnum(recipe.getProcessType());
    }

}
