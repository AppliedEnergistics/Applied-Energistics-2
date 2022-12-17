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

import static appeng.datagen.providers.recipes.RecipeCriteria.criterionName;

import java.util.function.Consumer;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.core.definitions.ItemDefinition;
import appeng.datagen.providers.tags.ConventionTags;

public class DecorationBlockRecipes extends AE2RecipeProvider {

    public DecorationBlockRecipes(PackOutput output) {
        super(output);
    }

    @Override
    public String getName() {
        return "AE2 Decoration Blocks";
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {

        crystalBlock(consumer, AEItems.CERTUS_QUARTZ_CRYSTAL, AEBlocks.QUARTZ_BLOCK);
        crystalBlock(consumer, AEItems.FLUIX_CRYSTAL, AEBlocks.FLUIX_BLOCK);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.SKY_STONE_BRICK, 4)
                .pattern("aa")
                .pattern("aa")
                .define('a', AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .unlockedBy(criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), has(AEBlocks.SMOOTH_SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_brick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.SKY_STONE_SMALL_BRICK, 4)
                .pattern("aa")
                .pattern("aa")
                .define('a', AEBlocks.SKY_STONE_BRICK)
                .unlockedBy(criterionName(AEBlocks.SKY_STONE_BRICK), has(AEBlocks.SKY_STONE_BRICK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_small_brick"));

        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.SMOOTH_SKY_STONE_BLOCK), RecipeCategory.MISC,
                        AEBlocks.SKY_STONE_BRICK)
                .unlockedBy(criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), has(AEBlocks.SMOOTH_SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_brick_from_stonecutting"));
        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.SMOOTH_SKY_STONE_BLOCK), RecipeCategory.MISC,
                        AEBlocks.SKY_STONE_SMALL_BRICK)
                .unlockedBy(criterionName(AEBlocks.SMOOTH_SKY_STONE_BLOCK), has(AEBlocks.SMOOTH_SKY_STONE_BLOCK))
                .save(consumer, AppEng.makeId("decorative/sky_stone_small_brick_from_stonecutting"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.CUT_QUARTZ_BLOCK, 4)
                .pattern("aa")
                .pattern("aa")
                .define('a', AEBlocks.QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_BLOCK), has(AEBlocks.QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/cut_quartz_block"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.QUARTZ_BRICKS, 4)
                .pattern("aa")
                .pattern("aa")
                .define('a', AEBlocks.CUT_QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_bricks"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.QUARTZ_PILLAR, 2)
                .pattern("a")
                .pattern("a")
                .define('a', AEBlocks.CUT_QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_pillar"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.CHISELED_QUARTZ_BLOCK, 2)
                .pattern("a")
                .pattern("a")
                .define('a', AEBlocks.CUT_QUARTZ_SLAB)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_SLAB), has(AEBlocks.CUT_QUARTZ_SLAB))
                .save(consumer, AppEng.makeId("decorative/chiseled_quartz_block"));

        SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(AEBlocks.CUT_QUARTZ_BLOCK), RecipeCategory.MISC, AEBlocks.SMOOTH_QUARTZ_BLOCK,
                        .1f, 200)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/smooth_quartz_block"));

        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.QUARTZ_BLOCK), RecipeCategory.MISC, AEBlocks.CUT_QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_BLOCK), has(AEBlocks.QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/cut_quartz_block_from_stonecutting"));
        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.CUT_QUARTZ_BLOCK), RecipeCategory.MISC, AEBlocks.QUARTZ_BRICKS)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_bricks_from_stonecutting"));
        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.CUT_QUARTZ_BLOCK), RecipeCategory.MISC, AEBlocks.QUARTZ_PILLAR)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/certus_quartz_pillar_from_stonecutting"));
        SingleItemRecipeBuilder
                .stonecutting(Ingredient.of(AEBlocks.CUT_QUARTZ_BLOCK), RecipeCategory.MISC,
                        AEBlocks.CHISELED_QUARTZ_BLOCK)
                .unlockedBy(criterionName(AEBlocks.CUT_QUARTZ_BLOCK), has(AEBlocks.CUT_QUARTZ_BLOCK))
                .save(consumer, AppEng.makeId("decorative/chiseled_quartz_block_from_stonecutting"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.LIGHT_DETECTOR)
                .pattern("ab")
                .define('a', ConventionTags.ALL_NETHER_QUARTZ)
                .define('b', Items.IRON_INGOT)
                .unlockedBy("has_nether_quartz", has(ConventionTags.ALL_NETHER_QUARTZ))
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(consumer, AppEng.makeId("decorative/light_detector"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.QUARTZ_FIXTURE, 2)
                .pattern("ab")
                .define('a', AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED)
                .define('b', Items.IRON_INGOT)
                .unlockedBy(criterionName(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED),
                        has(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(consumer, AppEng.makeId("decorative/quartz_fixture"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.QUARTZ_GLASS, 4)
                .pattern("aba")
                .pattern("bab")
                .pattern("aba")
                .define('a', ConventionTags.ALL_QUARTZ_DUST)
                .define('b', ConventionTags.GLASS)
                .unlockedBy("has_quartz_dust", has(ConventionTags.ALL_QUARTZ_DUST))
                .save(consumer, AppEng.makeId("decorative/quartz_glass"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.QUARTZ_VIBRANT_GLASS)
                .pattern("aba")
                .define('a', Items.GLOWSTONE_DUST)
                .define('b', AEBlocks.QUARTZ_GLASS)
                .unlockedBy(criterionName(AEBlocks.QUARTZ_GLASS), has(AEBlocks.QUARTZ_GLASS))
                .save(consumer, AppEng.makeId("decorative/quartz_vibrant_glass"));
    }

    private void crystalBlock(Consumer<FinishedRecipe> consumer, ItemDefinition<?> crystal, BlockDefinition<?> block) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, block)
                .pattern("aa")
                .pattern("aa")
                .define('a', crystal)
                .unlockedBy(criterionName(crystal), has(crystal))
                .save(consumer, AppEng.makeId("decorative/" + block.id().getPath()));
    }
}
