/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.recipes.factories.recipes;


import appeng.api.AEApi;
import appeng.api.recipes.ResolverResult;
import appeng.core.AELog;
import appeng.core.AppEng;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Map;
import java.util.Set;


/**
 * @author GuntherDW
 */
public class PartRecipeFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String type = JsonUtils.getString(json, "type");
        if (type.contains("shaped")) {
            return shapedFactory(context, json);
        } else if (type.contains("shapeless")) {
            return shapelessFactory(context, json);
        } else {
            throw new JsonSyntaxException("Applied Energistics 2 was given a custom recipe that it does not know how to handle!\n" + "Type should either be '" + AppEng.MOD_ID + ":shapeless' or '" + AppEng.MOD_ID + ":shaped', got '" + type + "'!");
        }
    }

    public static ItemStack getResult(JsonObject json, JsonContext context) {
        return getResult(json, context, "result");
    }

    public static ItemStack getResult(JsonObject json, JsonContext context, String name) {
        JsonObject resultObject = JsonUtils.getJsonObject(json, name);

        if (resultObject.has("part")) {
            return getPart(resultObject);
        } else if (resultObject.has("item")) {
            return CraftingHelper.getItemStack(resultObject, context);
        } else {
            throw new JsonSyntaxException("Result has no 'part' or 'item' property.");
        }
    }

    private static ItemStack getPart(JsonObject resultObject) {
        String ingredient = JsonUtils.getString(resultObject, "part");
        Object result = AEApi.instance().registries().recipes().resolveItem(AppEng.MOD_ID, ingredient);
        if (result instanceof ResolverResult) {
            ResolverResult resolverResult = (ResolverResult) result;

            Item item = Item.getByNameOrId(AppEng.MOD_ID + ":" + resolverResult.itemName);

            if (item == null) {
                AELog.warn("item was null for " + resolverResult.itemName + " ( " + ingredient + " )!");
                throw new JsonSyntaxException("Got a null item for " + resolverResult.itemName + " ( " + ingredient + " ). This should never happen!");
            }

            return new ItemStack(item, JsonUtils.getInt(resultObject, "count", 1), resolverResult.damageValue, resolverResult.compound);
        } else {
            throw new JsonSyntaxException("Couldn't find the resulting item in AE. This means AE was provided a recipe that it shouldn't be handling.\n" + "Was looking for : '" + ingredient + "'.");
        }
    }

    // Copied from ShapedOreRecipe.java, modified a bit.
    private static ShapedOreRecipe shapedFactory(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");

        Map<Character, Ingredient> ingMap = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(json, "key").entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }

            ingMap.put(entry.getKey().toCharArray()[0], CraftingHelper.getIngredient(entry.getValue(), context));
        }

        ingMap.put(' ', net.minecraft.item.crafting.Ingredient.EMPTY);

        JsonArray patternJ = JsonUtils.getJsonArray(json, "pattern");

        if (patternJ.size() == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }

        String[] pattern = new String[patternJ.size()];
        for (int x = 0; x < pattern.length; ++x) {
            String line = JsonUtils.getString(patternJ.get(x), "pattern[" + x + "]");
            if (x > 0 && pattern[0].length() != line.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must  be the same width");
            }
            pattern[x] = line;
        }

        CraftingHelper.ShapedPrimer primer = new CraftingHelper.ShapedPrimer();
        primer.width = pattern[0].length();
        primer.height = pattern.length;
        primer.mirrored = JsonUtils.getBoolean(json, "mirrored", true);
        primer.input = NonNullList.withSize(primer.width * primer.height, net.minecraft.item.crafting.Ingredient.EMPTY);

        Set<Character> keys = Sets.newHashSet(ingMap.keySet());
        keys.remove(' ');

        int x = 0;
        for (String line : pattern) {
            for (char chr : line.toCharArray()) {
                net.minecraft.item.crafting.Ingredient ing = ingMap.get(chr);
                if (ing == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + chr + "' but it's not defined in the key");
                }
                primer.input.set(x++, ing);
                keys.remove(chr);
            }
        }

        if (!keys.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + keys);
        }

        return new ShapedOreRecipe(group.isEmpty() ? null : new ResourceLocation(group), getResult(json, context), primer);
    }

    // Copied from ShapelessOreRecipe.java, modified a bit.
    private static ShapelessOreRecipe shapelessFactory(JsonContext context, JsonObject json) {
        String group = JsonUtils.getString(json, "group", "");

        NonNullList<Ingredient> ings = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients")) {
            ings.add(CraftingHelper.getIngredient(ele, context));
        }

        if (ings.isEmpty()) {
            throw new JsonParseException("No ingredients for shapeless recipe");
        }

        return new ShapelessOreRecipe(group.isEmpty() ? null : new ResourceLocation(group), ings, getResult(json, context));
    }
}
