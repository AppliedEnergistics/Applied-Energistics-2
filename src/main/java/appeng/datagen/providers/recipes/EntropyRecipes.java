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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import appeng.core.AppEng;
import appeng.recipes.entropy.EntropyRecipeBuilder;

public class EntropyRecipes extends AE2RecipeProvider {
    public EntropyRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {
        buildCoolRecipes(consumer);
        buildHeatRecipes(consumer);
    }

    private void buildCoolRecipes(Consumer<FinishedRecipe> consumer) {

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/flowing_water_snowball"))
                .setInputFluid(Fluids.FLOWING_WATER)
                .setDrops(new ItemStack(Items.SNOWBALL))
                .save(consumer);

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/grass_block_dirt"))
                .setInputBlock(Blocks.GRASS_BLOCK)
                .setOutputBlock(Blocks.DIRT)
                .save(consumer);

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/lava_obsidian"))
                .setInputFluid(Fluids.LAVA)
                .setOutputBlock(Blocks.OBSIDIAN)
                .save(consumer);

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/stone_bricks_cracked_stone_bricks"))
                .setInputBlock(Blocks.STONE_BRICKS)
                .setOutputBlock(Blocks.CRACKED_STONE_BRICKS)
                .save(consumer);

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/stone_cobblestone"))
                .setInputBlock(Blocks.STONE)
                .setOutputBlock(Blocks.COBBLESTONE)
                .save(consumer);

        EntropyRecipeBuilder.cool(AppEng.makeId("entropy/cool/water_ice"))
                .setInputFluid(Fluids.WATER)
                .setOutputBlock(Blocks.ICE)
                .save(consumer);

    }

    private void buildHeatRecipes(Consumer<FinishedRecipe> consumer) {

        EntropyRecipeBuilder.heat(AppEng.makeId("entropy/heat/cobblestone_stone"))
                .setInputBlock(Blocks.COBBLESTONE)
                .setOutputBlock(Blocks.STONE)
                .save(consumer);

        EntropyRecipeBuilder.heat(AppEng.makeId("entropy/heat/ice_water"))
                .setInputBlock(Blocks.ICE)
                .setOutputFluid(Fluids.WATER)
                .save(consumer);

        EntropyRecipeBuilder.heat(AppEng.makeId("entropy/heat/snow_water"))
                .setInputBlock(Blocks.SNOW)
                .setOutputFluid(Fluids.FLOWING_WATER)
                .save(consumer);

        EntropyRecipeBuilder.heat(AppEng.makeId("entropy/heat/water_air"))
                .setInputFluid(Fluids.WATER)
                .setOutputBlock(Blocks.AIR)
                .save(consumer);

    }
}
