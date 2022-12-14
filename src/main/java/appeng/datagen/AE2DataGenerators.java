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

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.datagen.providers.WorldGenProvider;
import appeng.datagen.providers.advancements.AdvancementGenerator;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.loot.BlockDropProvider;
import appeng.datagen.providers.models.BlockModelProvider;
import appeng.datagen.providers.models.CableModelProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.models.ItemModelProvider;
import appeng.datagen.providers.models.PartModelProvider;
import appeng.datagen.providers.recipes.ChargerRecipes;
import appeng.datagen.providers.recipes.CraftingRecipes;
import appeng.datagen.providers.recipes.DecorationBlockRecipes;
import appeng.datagen.providers.recipes.DecorationRecipes;
import appeng.datagen.providers.recipes.EntropyRecipes;
import appeng.datagen.providers.recipes.InscriberRecipes;
import appeng.datagen.providers.recipes.MatterCannonAmmoProvider;
import appeng.datagen.providers.recipes.SmeltingRecipes;
import appeng.datagen.providers.recipes.SmithingRecipes;
import appeng.datagen.providers.recipes.TransformRecipes;
import appeng.datagen.providers.tags.BiomeTagsProvider;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.FluidTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;
import appeng.datagen.providers.tags.PoiTypeTagsProvider;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitDimensionTypes;
import appeng.init.worldgen.InitStructures;

public class AE2DataGenerators {
    public static void onGatherData(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        // for use on Forge
        onGatherData(generator, existingFileHelper, generator.getVanillaPack(true));
    }

    public static void onGatherData(DataGenerator generator, ExistingFileHelper existingFileHelper,
            DataGenerator.PackGenerator pack) {
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var registries = createAppEngProvider(registryAccess);

        var localization = new LocalizationProvider(generator);

        // Worldgen et al
        pack.addProvider(bindRegistries(WorldGenProvider::new, registries));

        // Loot
        pack.addProvider(BlockDropProvider::new);

        // Tags
        var blockTagsProvider = pack.addProvider(bindRegistries(BlockTagsProvider::new, registries));
        pack.addProvider(packOutput -> new ItemTagsProvider(packOutput, registries, blockTagsProvider));
        pack.addProvider(bindRegistries(FluidTagsProvider::new, registries));
        pack.addProvider(bindRegistries(BiomeTagsProvider::new, registries));
        pack.addProvider(bindRegistries(PoiTypeTagsProvider::new, registries));

        // Models
        pack.addProvider(packOutput -> new BlockModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new DecorationModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new ItemModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new CableModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new PartModelProvider(packOutput, existingFileHelper));

        // Misc
        pack.addProvider(packOutput -> new AdvancementGenerator(packOutput, localization));

        // Recipes
        pack.addProvider(DecorationRecipes::new);
        pack.addProvider(DecorationBlockRecipes::new);
        pack.addProvider(MatterCannonAmmoProvider::new);
        pack.addProvider(EntropyRecipes::new);
        pack.addProvider(InscriberRecipes::new);
        pack.addProvider(SmeltingRecipes::new);
        pack.addProvider(CraftingRecipes::new);
        pack.addProvider(SmithingRecipes::new);
        pack.addProvider(TransformRecipes::new);
        pack.addProvider(ChargerRecipes::new);

        // Must run last
        pack.addProvider(packOutput -> localization);
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory,
            CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> factory.apply(packOutput, factories);
    }

    /**
     * See {@link VanillaRegistries#createLookup()}
     */
    private static CompletableFuture<HolderLookup.Provider> createAppEngProvider(RegistryAccess registryAccess) {

        var vanillaLookup = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());

        return vanillaLookup.thenApply(provider -> {
            var builder = new RegistrySetBuilder()
                    .add(Registries.DIMENSION_TYPE, InitDimensionTypes::init)
                    .add(Registries.STRUCTURE, InitStructures::initDatagenStructures)
                    .add(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets)
                    .add(Registries.BIOME, InitBiomes::init);

            return builder.buildPatch(registryAccess, provider);
        });
    }
}
