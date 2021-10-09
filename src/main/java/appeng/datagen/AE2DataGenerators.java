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

package appeng.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.datagen.providers.advancements.AdvancementGenerator;
import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.loot.ChestDropProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.recipes.CraftingRecipes;
import appeng.datagen.providers.recipes.DecorationBlockRecipes;
import appeng.datagen.providers.recipes.DecorationRecipes;
import appeng.datagen.providers.recipes.EntropyRecipes;
import appeng.datagen.providers.recipes.InscriberRecipes;
import appeng.datagen.providers.recipes.MatterCannonAmmoProvider;
import appeng.datagen.providers.recipes.SmeltingRecipes;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.FluidTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;

public class AE2DataGenerators {

    public static void onGatherData(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        // Loot
        generator.addProvider(new BlockDropProvider(generator.getOutputFolder()));
        generator.addProvider(new ChestDropProvider(generator.getOutputFolder()));

        // Tags
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(generator);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new ItemTagsProvider(generator, blockTagsProvider));
        generator.addProvider(new FluidTagsProvider(generator));

        // Misc
        generator.addProvider(new DecorationModelProvider(generator, existingFileHelper));
        generator.addProvider(new AdvancementGenerator(generator));

        // Recipes
        generator.addProvider(new DecorationRecipes(generator));
        generator.addProvider(new DecorationBlockRecipes(generator));
        generator.addProvider(new MatterCannonAmmoProvider(generator));
        generator.addProvider(new EntropyRecipes(generator));
        generator.addProvider(new InscriberRecipes(generator));
        generator.addProvider(new SmeltingRecipes(generator));
        generator.addProvider(new CraftingRecipes(generator));
    }

}
