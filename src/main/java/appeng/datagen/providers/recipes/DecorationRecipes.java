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
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.tags.ConventionTags;

public class DecorationRecipes extends AE2RecipeProvider {

    BlockDefinition<?>[][] blocks = {
            { AEBlocks.SKY_STONE_BLOCK, AEBlocks.SKY_STONE_SLAB, AEBlocks.SKY_STONE_STAIRS,
                    AEBlocks.SKY_STONE_WALL },
            { AEBlocks.SMOOTH_SKY_STONE_BLOCK, AEBlocks.SMOOTH_SKY_STONE_SLAB, AEBlocks.SMOOTH_SKY_STONE_STAIRS,
                    AEBlocks.SMOOTH_SKY_STONE_WALL },
            { AEBlocks.SKY_STONE_BRICK, AEBlocks.SKY_STONE_BRICK_SLAB, AEBlocks.SKY_STONE_BRICK_STAIRS,
                    AEBlocks.SKY_STONE_BRICK_WALL },
            { AEBlocks.SKY_STONE_SMALL_BRICK, AEBlocks.SKY_STONE_SMALL_BRICK_SLAB,
                    AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS,
                    AEBlocks.SKY_STONE_SMALL_BRICK_WALL },
            { AEBlocks.FLUIX_BLOCK, AEBlocks.FLUIX_SLAB, AEBlocks.FLUIX_STAIRS, AEBlocks.FLUIX_WALL },
            { AEBlocks.QUARTZ_BLOCK, AEBlocks.QUARTZ_SLAB, AEBlocks.QUARTZ_STAIRS, AEBlocks.QUARTZ_WALL },
            { AEBlocks.CUT_QUARTZ_BLOCK, AEBlocks.CUT_QUARTZ_SLAB, AEBlocks.CUT_QUARTZ_STAIRS,
                    AEBlocks.CUT_QUARTZ_WALL },
            { AEBlocks.SMOOTH_QUARTZ_BLOCK, AEBlocks.SMOOTH_QUARTZ_SLAB, AEBlocks.SMOOTH_QUARTZ_STAIRS,
                    AEBlocks.SMOOTH_QUARTZ_WALL },
            { AEBlocks.QUARTZ_BRICKS, AEBlocks.QUARTZ_BRICK_SLAB, AEBlocks.QUARTZ_BRICK_STAIRS,
                    AEBlocks.QUARTZ_BRICK_WALL },
            { AEBlocks.CHISELED_QUARTZ_BLOCK, AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_STAIRS,
                    AEBlocks.CHISELED_QUARTZ_WALL },
            { AEBlocks.QUARTZ_PILLAR, AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR_STAIRS,
                    AEBlocks.QUARTZ_PILLAR_WALL }, };

    public DecorationRecipes(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        for (var block : blocks) {
            slabRecipe(consumer, block[0], block[1]);
            stairRecipe(consumer, block[0], block[2]);
            wallRecipe(consumer, block[0], block[3]);
        }

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEBlocks.NOT_SO_MYSTERIOUS_CUBE, 4)
                .pattern("ScS")
                .pattern("eCl")
                .pattern("SsS")
                .define('S', AEBlocks.SMOOTH_SKY_STONE_BLOCK)
                .define('C', AEBlocks.CONTROLLER)
                .define('c', AEItems.CALCULATION_PROCESSOR_PRESS)
                .define('e', AEItems.ENGINEERING_PROCESSOR_PRESS)
                .define('l', AEItems.LOGIC_PROCESSOR_PRESS)
                .define('s', AEItems.SILICON_PRESS)
                .unlockedBy("press", has(ConventionTags.INSCRIBER_PRESSES))
                .save(consumer, AppEng.makeId("shaped/not_so_mysterious_cube"));
    }

    private void slabRecipe(Consumer<FinishedRecipe> consumer, BlockDefinition<?> block, BlockDefinition<?> slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputBlock, 6).pattern("###").define('#', inputBlock)
                .unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("shaped/slabs/", block.id()));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(inputBlock), RecipeCategory.MISC, outputBlock, 2)
                .unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("block_cutter/slabs/", slabs.id()));
    }

    private void stairRecipe(Consumer<FinishedRecipe> consumer, BlockDefinition<?> block, BlockDefinition<?> stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputBlock, 4).pattern("#  ").pattern("## ").pattern("###")
                .define('#', inputBlock).unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("shaped/stairs/", block.id()));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(inputBlock), RecipeCategory.MISC, outputBlock)
                .unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("block_cutter/stairs/", stairs.id()));

    }

    private void wallRecipe(Consumer<FinishedRecipe> consumer, BlockDefinition<?> block, BlockDefinition<?> wall) {
        Block inputBlock = block.block();
        Block outputBlock = wall.block();

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputBlock, 6).pattern("###").pattern("###")
                .define('#', inputBlock).unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("shaped/walls/", block.id()));

        SingleItemRecipeBuilder.stonecutting(Ingredient.of(inputBlock), RecipeCategory.MISC, outputBlock)
                .unlockedBy(criterionName(block), has(inputBlock))
                .save(consumer, prefix("block_cutter/walls/", wall.id()));

    }

    private ResourceLocation prefix(String prefix, ResourceLocation id) {
        return new ResourceLocation(
                id.getNamespace(),
                prefix + id.getPath());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Decorative Blocks";
    }

}
