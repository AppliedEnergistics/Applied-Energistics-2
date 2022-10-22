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
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;

public class TransformRecipes extends AE2RecipeProvider {
    public TransformRecipes(PackOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer) {
        TransformCircumstance water = TransformCircumstance.fluid(FluidTags.WATER);

        // Fluix crystals
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/fluix_crystals"),
                AEItems.FLUIX_CRYSTAL, 2, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, Items.REDSTONE,
                Items.QUARTZ);

        // Recycle dust back into crystals
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/certus_quartz_crystals"),
                AEItems.CERTUS_QUARTZ_CRYSTAL, 2, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEItems.CERTUS_QUARTZ_DUST);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/fluix_crystal"),
                AEItems.FLUIX_CRYSTAL, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.FLUIX_DUST);

        // Restore budding quartz
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/damaged_budding_quartz"),
                AEBlocks.DAMAGED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.QUARTZ_BLOCK);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/chipped_budding_quartz"),
                AEBlocks.CHIPPED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.DAMAGED_BUDDING_QUARTZ);
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/flawed_budding_quartz"),
                AEBlocks.FLAWED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.CHIPPED_BUDDING_QUARTZ);

        // Entangled Singularities
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/entangled_singularity"),
                AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(ConventionTags.ENDER_PEARL_DUST));
        TransformRecipeBuilder.transform(consumer, AppEng.makeId("transform/entangled_singularity_from_pearl"),
                AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(ConventionTags.ENDER_PEARL));
    }

    @Override
    public String getName() {
        return "AE2 Transform Recipes";
    }
}
