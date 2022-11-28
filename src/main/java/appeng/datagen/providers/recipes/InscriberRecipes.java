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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.tags.ConventionTags;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;

public class InscriberRecipes extends AE2RecipeProvider {
    public InscriberRecipes(DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildAE2CraftingRecipes(Consumer<FinishedRecipe> consumer) {

        // Silicon Press Copying & Printing
        InscriberRecipeBuilder.inscribe(Items.IRON_BLOCK, AEItems.SILICON_PRESS, 1)
                .setTop(Ingredient.of(AEItems.SILICON_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/silicon_press"));
        InscriberRecipeBuilder.inscribe(ConventionTags.SILICON, AEItems.SILICON_PRINT, 1)
                .setTop(Ingredient.of(AEItems.SILICON_PRESS))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/silicon_print"));

        processor(consumer, "calculation_processor",
                AEItems.CALCULATION_PROCESSOR_PRESS,
                AEItems.CALCULATION_PROCESSOR_PRINT,
                AEItems.CALCULATION_PROCESSOR,
                Ingredient.of(AEItems.CERTUS_QUARTZ_CRYSTAL));

        processor(consumer, "engineering_processor",
                AEItems.ENGINEERING_PROCESSOR_PRESS,
                AEItems.ENGINEERING_PROCESSOR_PRINT,
                AEItems.ENGINEERING_PROCESSOR,
                Ingredient.of(ConventionTags.DIAMOND));

        processor(consumer, "logic_processor",
                AEItems.LOGIC_PROCESSOR_PRESS,
                AEItems.LOGIC_PROCESSOR_PRINT,
                AEItems.LOGIC_PROCESSOR,
                Ingredient.of(ConventionTags.GOLD_INGOT));

        // Crystal -> Dust Recipes
        InscriberRecipeBuilder.inscribe(ConventionTags.FLUIX_CRYSTAL, AEItems.FLUIX_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/fluix_dust"));
        InscriberRecipeBuilder.inscribe(ConventionTags.CERTUS_QUARTZ, AEItems.CERTUS_QUARTZ_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/certus_quartz_dust"));
        InscriberRecipeBuilder.inscribe(AEBlocks.SKY_STONE_BLOCK, AEItems.SKY_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/sky_stone_dust"));
        InscriberRecipeBuilder.inscribe(Items.ENDER_PEARL, AEItems.ENDER_DUST, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/ender_dust"));
    }

    private void processor(Consumer<FinishedRecipe> consumer,
            String name,
            ItemLike press,
            ItemLike print,
            ItemLike processor,
            Ingredient printMaterial) {
        // Making the print
        InscriberRecipeBuilder.inscribe(printMaterial, print, 1)
                .setTop(Ingredient.of(press))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/" + name + "_print"));

        // Making the processor
        InscriberRecipeBuilder.inscribe(Items.REDSTONE, processor, 1)
                .setTop(Ingredient.of(print))
                .setBottom(Ingredient.of(AEItems.SILICON_PRINT))
                .setMode(InscriberProcessType.PRESS)
                .save(consumer, AppEng.makeId("inscriber/" + name));

        // Copying the press
        InscriberRecipeBuilder.inscribe(Items.IRON_BLOCK, press, 1)
                .setTop(Ingredient.of(press))
                .setMode(InscriberProcessType.INSCRIBE)
                .save(consumer, AppEng.makeId("inscriber/" + name + "_press"));
    }
}
