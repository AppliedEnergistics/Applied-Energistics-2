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


import appeng.core.AppEng;
import appeng.recipes.entropy.EntropyRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class EntropyRecipes extends AE2RecipeProvider {
    public EntropyRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        buildCoolRecipes();
        buildHeatRecipes();
    }

    private void buildCoolRecipes() {

        EntropyRecipeBuilder.cool()
                .setInputFluid(Fluids.FLOWING_WATER)
                .setDrops(new ItemStack(Items.SNOWBALL))
                .save(output, AppEng.makeId("entropy/cool/flowing_water_snowball"));

        EntropyRecipeBuilder.cool()
                .setInputBlock(Blocks.GRASS_BLOCK)
                .setOutputBlock(Blocks.DIRT)
                .save(output, AppEng.makeId("entropy/cool/grass_block_dirt"));

        EntropyRecipeBuilder.cool()
                .setInputFluid(Fluids.LAVA)
                .setOutputBlock(Blocks.OBSIDIAN)
                .save(output, AppEng.makeId("entropy/cool/lava_obsidian"));

        EntropyRecipeBuilder.cool()
                .setInputBlock(Blocks.STONE_BRICKS)
                .setOutputBlock(Blocks.CRACKED_STONE_BRICKS)
                .save(output, AppEng.makeId("entropy/cool/stone_bricks_cracked_stone_bricks"));

        EntropyRecipeBuilder.cool()
                .setInputBlock(Blocks.STONE)
                .setOutputBlock(Blocks.COBBLESTONE)
                .save(output, AppEng.makeId("entropy/cool/stone_cobblestone"));

        EntropyRecipeBuilder.cool()
                .setInputFluid(Fluids.WATER)
                .setOutputBlock(Blocks.ICE)
                .save(output, AppEng.makeId("entropy/cool/water_ice"));

    }

    private void buildHeatRecipes() {

        EntropyRecipeBuilder.heat()
                .setInputBlock(Blocks.COBBLESTONE)
                .setOutputBlock(Blocks.STONE)
                .save(output, AppEng.makeId("entropy/heat/cobblestone_stone"));

        EntropyRecipeBuilder.heat()
                .setInputBlock(Blocks.ICE)
                .setOutputFluid(Fluids.WATER)
                .save(output, AppEng.makeId("entropy/heat/ice_water"));

        EntropyRecipeBuilder.heat()
                .setInputBlock(Blocks.SNOW)
                .setOutputFluid(Fluids.FLOWING_WATER)
                .save(output, AppEng.makeId("entropy/heat/snow_water"));

        EntropyRecipeBuilder.heat()
                .setInputFluid(Fluids.WATER)
                .setOutputBlock(Blocks.AIR)
                .save(output, AppEng.makeId("entropy/heat/water_air"));

    }
}
