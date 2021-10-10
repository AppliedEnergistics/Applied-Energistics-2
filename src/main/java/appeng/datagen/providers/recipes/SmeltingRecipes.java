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

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

public class SmeltingRecipes extends AE2RecipeProvider {

    // This is from the default recipe serializer for smelting
    private static final int DEFAULT_SMELTING_TIME = 200;

    public SmeltingRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(ConventionTags.CERTUS_QUARTZ_DUST), AEItems.SILICON, .35f,
                        DEFAULT_SMELTING_TIME)
                .unlockedBy("has_certus_quartz_dust", has(ConventionTags.CERTUS_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("smelting/silicon_from_certus_quartz_dust"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(AEBlocks.SKY_STONE_BLOCK), AEBlocks.SMOOTH_SKY_STONE_BLOCK, 0.35f,
                        DEFAULT_SMELTING_TIME)
                .unlockedBy("has_sky_stone_block", has(AEBlocks.SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("smelting/smooth_sky_stone_block"));

        SimpleCookingRecipeBuilder
                .blasting(Ingredient.of(ConventionTags.ENDER_PEARL), AEItems.ENDER_DUST, 1f, DEFAULT_SMELTING_TIME)
                .unlockedBy("has_ender_pearls", has(ConventionTags.ENDER_PEARL))
                .save(consumer, AppEng.makeId("blasting/ender_dust"));

    }
}
