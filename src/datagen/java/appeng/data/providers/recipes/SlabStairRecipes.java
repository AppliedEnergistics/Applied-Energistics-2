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

package appeng.data.providers.recipes;

import static net.minecraft.data.RecipeProvider.*;

import java.nio.file.Path;
import java.util.function.Consumer;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.ShapedRecipeBuilder;
import net.minecraft.data.SingleItemRecipeBuilder;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import appeng.api.definitions.IBlockDefinition;
import appeng.core.AppEng;
import appeng.data.providers.IAE2DataProvider;

public class SlabStairRecipes implements IAE2DataProvider {

    IBlockDefinition[][] blocks = { { BLOCKS.skyStoneBlock(), BLOCKS.skyStoneSlab(), BLOCKS.skyStoneStairs() },
            { BLOCKS.smoothSkyStoneBlock(), BLOCKS.smoothSkyStoneSlab(), BLOCKS.smoothSkyStoneStairs() },
            { BLOCKS.skyStoneBrick(), BLOCKS.skyStoneBrickSlab(), BLOCKS.skyStoneBrickStairs() },
            { BLOCKS.skyStoneSmallBrick(), BLOCKS.skyStoneSmallBrickSlab(), BLOCKS.skyStoneSmallBrickStairs() },
            { BLOCKS.fluixBlock(), BLOCKS.fluixSlab(), BLOCKS.fluixStairs() },
            { BLOCKS.quartzBlock(), BLOCKS.quartzSlab(), BLOCKS.quartzStairs() },
            { BLOCKS.chiseledQuartzBlock(), BLOCKS.chiseledQuartzSlab(), BLOCKS.chiseledQuartzStairs() },
            { BLOCKS.quartzPillar(), BLOCKS.quartzPillarSlab(), BLOCKS.quartzPillarStairs() }, };

    private final Path outputPath;

    private final Consumer<IFinishedRecipe> consumer;

    private DirectoryCache cache;

    public SlabStairRecipes(Path outputPath) {
        this.outputPath = outputPath;
        this.consumer = this::provideRecipe;
    }

    public void act(DirectoryCache cache) {
        this.cache = cache;
        for (IBlockDefinition[] block : blocks) {
            slabRecipe(block[0], block[1]);
            stairRecipe(block[0], block[2]);
        }
    }

    private void slabRecipe(IBlockDefinition block, IBlockDefinition slabs) {
        Block inputBlock = block.block();
        Block outputBlock = slabs.block();

        ShapedRecipeBuilder.shapedRecipe(slabs.block(), 6).patternLine("###").key('#', inputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/slabs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock, 2)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/slabs/" + slabs.identifier()));
    }

    private void stairRecipe(IBlockDefinition block, IBlockDefinition stairs) {
        Block inputBlock = block.block();
        Block outputBlock = stairs.block();

        ShapedRecipeBuilder.shapedRecipe(outputBlock, 4).patternLine("#  ").patternLine("## ").patternLine("###")
                .key('#', inputBlock).addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "shaped/stairs/" + block.identifier()));

        SingleItemRecipeBuilder.stonecuttingRecipe(Ingredient.fromItems(inputBlock), outputBlock)
                .addCriterion(criterionName(block), hasItem(inputBlock))
                .build(consumer, new ResourceLocation(AppEng.MOD_ID, "block_cutter/stairs/" + stairs.identifier()));

    }

    private void provideRecipe(IFinishedRecipe recipeJsonProvider) {
        saveRecipe(cache, recipeJsonProvider.getRecipeJson(),
                outputPath.resolve("data/" + recipeJsonProvider.getID().getNamespace() + "/recipes/"
                        + recipeJsonProvider.getID().getPath() + ".json"));
        JsonObject jsonObject = recipeJsonProvider.getAdvancementJson();
        if (jsonObject != null) {
            saveRecipeAdvancement(cache, jsonObject,
                    outputPath.resolve("data/" + recipeJsonProvider.getID().getNamespace() + "/advancements/"
                            + recipeJsonProvider.getAdvancementID().getPath() + ".json"));
        }
    }

    private String criterionName(IBlockDefinition block) {
        return String.format("has_%s", block.identifier());
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Slabs and Stairs";
    }

}
