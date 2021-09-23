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

import static appeng.datagen.providers.recipes.RecipeCriterions.criterionName;

import java.util.function.Consumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;

public class DecorationBlockRecipes extends AE2RecipeProvider {

    public DecorationBlockRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_BLOCK)
                .pattern("aa")
                .pattern("aa")
                .define('a', ConventionTags.CERTUS_QUARTZ)
                .unlockedBy("has_certus_crystal", has(ConventionTags.CERTUS_QUARTZ))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_block"));

        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_PILLAR, 2)
                .pattern("a")
                .pattern("a")
                .define('a', AEBlocks.QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_BLOCK), has(AEBlocks.QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_pillar"));

        ShapedRecipeBuilder.shaped(AEBlocks.CHISELED_QUARTZ_BLOCK, 2)
                .pattern("aa")
                .define('a', AEBlocks.QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_BLOCK), has(AEBlocks.QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/chiseled_quartz_block"));

        ShapedRecipeBuilder.shaped(AEBlocks.FLUIX_BLOCK)
                .pattern("aa")
                .pattern("aa")
                .define('a', AEItems.FLUIX_CRYSTAL)
                .unlockedBy(criterionName(AEItems.FLUIX_CRYSTAL), has(AEItems.FLUIX_CRYSTAL))
                .save(consumer, AppEng.makeId("decorative/fluix_block"));

        ShapedRecipeBuilder.shaped(AEBlocks.LIGHT_DETECTOR)
                .pattern("ab")
                .define('a', ConventionTags.ALL_NETHER_QUARTZ)
                .define('b', Items.IRON_INGOT)
                .unlockedBy("has_nether_quartz", has(ConventionTags.ALL_NETHER_QUARTZ))
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(consumer, AppEng.makeId("decorative/light_detector"));

        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_FIXTURE, 2)
                .pattern("ab")
                .define('a', AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .define('b', Items.IRON_INGOT)
                .unlockedBy(criterionName(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                        has(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(consumer, AppEng.makeId("decorative/quartz_fixture"));

        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_GLASS, 4)
                .pattern("aba")
                .pattern("bab")
                .pattern("aba")
                .define('a', ConventionTags.ALL_QUARTZ_DUST)
                .define('b', ConventionTags.GLASS)
                .unlockedBy("has_quartz_dust", has(ConventionTags.ALL_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("decorative/quartz_glass"));

        ShapedRecipeBuilder.shaped(AEBlocks.QUARTZ_VIBRANT_GLASS)
                .pattern("aba")
                .define('a', Items.GLOWSTONE_DUST)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_GLASS), has(AEBlocks.QUARTZ_GLASS))
                .save(consumer, AppEng.makeId("decorative/quartz_vibrant_glass"));

        // Circular conversion recipes
        ShapelessRecipeBuilder.shapeless(AEBlocks.SKY_STONE_BRICK)
                .requires(AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .unlockedBy(criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), has(AEBlocks.SMOOTH_SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_brick"));

        ShapelessRecipeBuilder.shapeless(AEBlocks.SKY_STONE_SMALL_BRICK)
                .requires(AEBlocks.SKY_STONE_BRICK)
                .unlockedBy(criterionName(AEBlocks.SKY_STONE_BRICK), has(AEBlocks.SKY_STONE_BRICK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_small_brick"));

        ShapelessRecipeBuilder.shapeless(AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .requires(AEBlocks.SKY_STONE_SMALL_BRICK)
                .unlockedBy(criterionName(AEBlocks.SKY_STONE_SMALL_BRICK), has(AEBlocks.SKY_STONE_SMALL_BRICK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_smooth"));

    }

}
