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

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

public class SmeltingRecipes extends AE2RecipeProvider {

    // This is from the default recipe serializer for smelting
    private static final int DEFAULT_SMELTING_TIME = 200;

    public SmeltingRecipes(PackOutput output) {
        super(output);
    }

    @Override
    public String getName() {
        return "AE2 Smelting Recipes";
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST), RecipeCategory.MISC, AEItems.SILICON, .35f,
                        DEFAULT_SMELTING_TIME)
                .unlockedBy("has_certus_quartz_dust", has(ConventionTags.CERTUS_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("smelting/silicon_from_certus_quartz_dust"));
        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST), RecipeCategory.MISC, AEItems.SILICON, .35f,
                        DEFAULT_SMELTING_TIME / 2)
                .unlockedBy("has_certus_quartz_dust", has(ConventionTags.CERTUS_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("blasting/silicon_from_certus_quartz_dust"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(AEBlocks.SKY_STONE_BLOCK), RecipeCategory.MISC, AEBlocks.SMOOTH_SKY_STONE_BLOCK,
                        0.35f,
                        DEFAULT_SMELTING_TIME)
                .unlockedBy("has_sky_stone_block", has(AEBlocks.SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("smelting/smooth_sky_stone_block"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(AEItems.SKY_DUST), RecipeCategory.MISC, AEBlocks.SKY_STONE_BLOCK, 0,
                        DEFAULT_SMELTING_TIME)
                .unlockedBy("has_sky_stone_dust", has(AEItems.SKY_DUST))
                .save(consumer, AppEng.makeId("blasting/sky_stone_block"));
    }
}
