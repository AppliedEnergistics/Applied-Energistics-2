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

package appeng.datagen.providers.recipes;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import appeng.datagen.providers.IAE2DataProvider;

public abstract class AE2RecipeProvider extends net.minecraft.data.recipes.RecipeProvider implements IAE2DataProvider {

    private final Path outputFolder;

    public AE2RecipeProvider(DataGenerator generator) {
        super(generator);
        this.outputFolder = generator.getOutputFolder();
    }

    public static JsonObject toJson(ItemStack stack) {
        var stackObj = new JsonObject();
        stackObj.addProperty("item", Registry.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            stackObj.addProperty("count", stack.getCount());
        }
        return stackObj;
    }

    public void run(CachedOutput cache) {
        Path path = outputFolder;
        Set<ResourceLocation> set = Sets.newHashSet();
        buildAE2CraftingRecipes((finishedRecipe) -> {
            if (!set.add(finishedRecipe.getId())) {
                throw new IllegalStateException("Duplicate recipe " + finishedRecipe.getId());
            } else {
                JsonObject json = finishedRecipe.serializeRecipe();
                String modId = finishedRecipe.getId().getNamespace();
                saveRecipe(cache, json,
                        path.resolve("data/" + modId + "/recipes/" + finishedRecipe.getId().getPath() + ".json"));
                JsonObject jsonObject = finishedRecipe.serializeAdvancement();
                if (jsonObject != null) {
                    modId = finishedRecipe.getId().getNamespace();
                    saveAdvancement(cache, jsonObject, path.resolve("data/" + modId + "/advancements/"
                            + finishedRecipe.getAdvancementId().getPath() + ".json"));
                }

            }
        });
    }

    protected abstract void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer);
}
