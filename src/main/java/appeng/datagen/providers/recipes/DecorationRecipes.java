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

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.RecipeProvider;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class DecorationRecipes extends RecipeProvider implements IAE2DataProvider {

    BlockDefinition[][] blocks = {
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
            { AEBlocks.CHISELED_QUARTZ_BLOCK, AEBlocks.CHISELED_QUARTZ_SLAB, AEBlocks.CHISELED_QUARTZ_STAIRS,
                    AEBlocks.CHISELED_QUARTZ_WALL },
            { AEBlocks.QUARTZ_PILLAR, AEBlocks.QUARTZ_PILLAR_SLAB, AEBlocks.QUARTZ_PILLAR_STAIRS,
                    AEBlocks.QUARTZ_PILLAR_WALL }, };

    public DecorationRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    public void registerRecipes(@Nonnull Consumer<IFinishedRecipe> consumer) {
        for (BlockDefinition[] block : blocks) {
            slabRecipe(consumer, block[0], block[1]);
            stairRecipe(consumer, block[0], block[2]);
            wallRecipe(consumer, block[0], block[3]);
        }
    }

    private void slabRecipe(Consumer<IFinishedRecipe> consumer, BlockDefinition block, BlockDefinition slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeBuilder.shapedRecipe(slabs.block(), 6).patternLine("###").key('#', inputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("shaped/slabs/", block.id()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock, 2)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("block_cutter/slabs/", slabs.id()));
    }

    private void stairRecipe(Consumer<IFinishedRecipe> consumer, BlockDefinition block, BlockDefinition stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeBuilder.shapedRecipe(outputBlock, 4).patternLine("#  ").patternLine("## ").patternLine("###")
                .key('#', inputBlock).addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("shaped/stairs/", block.id()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("block_cutter/stairs/", stairs.id()));

    }

    private void wallRecipe(Consumer<IFinishedRecipe> consumer, BlockDefinition block, BlockDefinition wall) {
        Block inputBlock = block.block();
        Block outputBlock = wall.block();

        ShapedRecipeBuilder.shapedRecipe(outputBlock, 6).patternLine("###").patternLine("###")
                .key('#', inputBlock).addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("shaped/walls/", block.id()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, prefix("block_cutter/walls/", wall.id()));

    }

    private ResourceLocation prefix(String prefix, ResourceLocation id) {
        return new ResourceLocation(
                id.getNamespace(),
                prefix + id.getPath());
    }

    private String criterionName(BlockDefinition block) {
        return String.format("has_%s", block.id().getPath());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Decorative Blocks";
    }

}
