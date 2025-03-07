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

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import appeng.recipes.transform.TransformRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import net.minecraft.world.level.ItemLike;

public class TransformRecipes extends AE2RecipeProvider {
    public TransformRecipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    public void buildRecipes() {
        TransformCircumstance water = TransformCircumstance.fluid(FluidTags.WATER);

        // Fluix crystals
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/fluix_crystals"),
                AEItems.FLUIX_CRYSTAL, 2, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, Items.REDSTONE,
                Items.QUARTZ);

        // Recycle dust back into crystals
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/certus_quartz_crystals"),
                AEItems.CERTUS_QUARTZ_CRYSTAL, 2, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEItems.CERTUS_QUARTZ_DUST);
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/fluix_crystal"),
                AEItems.FLUIX_CRYSTAL, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED, AEItems.FLUIX_DUST);

        // Restore budding quartz
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/damaged_budding_quartz"),
                AEBlocks.DAMAGED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.QUARTZ_BLOCK);
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/chipped_budding_quartz"),
                AEBlocks.CHIPPED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.DAMAGED_BUDDING_QUARTZ);
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/flawed_budding_quartz"),
                AEBlocks.FLAWED_BUDDING_QUARTZ, 1, water, AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                AEBlocks.CHIPPED_BUDDING_QUARTZ);

        // Entangled Singularities
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/entangled_singularity"),
                AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(items.getOrThrow(ConventionTags.ENDER_PEARL_DUST)));
        TransformRecipeBuilder.transform(output, AppEng.makeId("transform/entangled_singularity_from_pearl"),
                AEItems.QUANTUM_ENTANGLED_SINGULARITY, 2, TransformCircumstance.EXPLOSION,
                Ingredient.of(AEItems.SINGULARITY), Ingredient.of(items.getOrThrow(ConventionTags.ENDER_PEARL)));
    }

    private static ItemStack toStack(ItemLike item, int count) {
        var stack = item.asItem().getDefaultInstance();
        stack.setCount(count);
        return stack;
    }
}
