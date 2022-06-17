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
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.loot.ChestDropProvider;
import appeng.datagen.providers.models.BlockModelProvider;
import appeng.datagen.providers.models.CableModelProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.models.ItemModelProvider;
import appeng.datagen.providers.models.PartModelProvider;
import appeng.datagen.providers.recipes.CraftingRecipes;
import appeng.datagen.providers.recipes.DecorationBlockRecipes;
import appeng.datagen.providers.recipes.DecorationRecipes;
import appeng.datagen.providers.recipes.EntropyRecipes;
import appeng.datagen.providers.recipes.InscriberRecipes;
import appeng.datagen.providers.recipes.MatterCannonAmmoProvider;
import appeng.datagen.providers.recipes.SmeltingRecipes;
import appeng.datagen.providers.recipes.SmithingRecipes;
import appeng.datagen.providers.tags.BiomeTagsProvider;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.FluidTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;

public class AE2DataGenerators {

    public static void onGatherData(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        var localization = new LocalizationProvider(generator);

        // Loot
        generator.addProvider(true, new BlockDropProvider(generator.getOutputFolder()));
        generator.addProvider(true, new ChestDropProvider(generator.getOutputFolder()));

        // Tags
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(generator);
        generator.addProvider(true, blockTagsProvider);
        generator.addProvider(true, new ItemTagsProvider(generator, blockTagsProvider));
        generator.addProvider(true, new FluidTagsProvider(generator));
        generator.addProvider(true, new BiomeTagsProvider(generator));

        // Models
        generator.addProvider(true, new BlockModelProvider(generator, existingFileHelper));
        generator.addProvider(true, new DecorationModelProvider(generator, existingFileHelper));
        generator.addProvider(true, new ItemModelProvider(generator, existingFileHelper));
        generator.addProvider(true, new CableModelProvider(generator, existingFileHelper));
        generator.addProvider(true, new PartModelProvider(generator, existingFileHelper));

        // Misc
        generator.addProvider(true, new AdvancementGenerator(generator, localization));

        // Recipes
        generator.addProvider(true, new DecorationRecipes(generator));
        generator.addProvider(true, new DecorationBlockRecipes(generator));
        generator.addProvider(true, new MatterCannonAmmoProvider(generator));
        generator.addProvider(true, new EntropyRecipes(generator));
        generator.addProvider(true, new InscriberRecipes(generator));
        generator.addProvider(true, new SmeltingRecipes(generator));
        generator.addProvider(true, new CraftingRecipes(generator));
        generator.addProvider(true, new SmithingRecipes(generator));

        // Must run last
        generator.addProvider(true, localization);
    }

}
