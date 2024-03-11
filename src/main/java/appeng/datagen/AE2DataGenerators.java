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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import appeng.core.AppEng;
import appeng.core.definitions.AEDamageTypes;
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

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AE2DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var registries = event.getLookupProvider();
        var localization = new LocalizationProvider(generator);
        var pack = generator.getVanillaPack(true);
        var existingFileHelper = event.getExistingFileHelper();

        // Worldgen et al
        pack.addProvider(output -> new DatapackBuiltinEntriesProvider(output, registries,
                createDatapackEntriesBuilder(), Set.of(AppEng.MOD_ID)));

        // Loot
        pack.addProvider(BlockDropProvider::new);

        // Tags
        var blockTagsProvider = pack
                .addProvider(packOutput -> new BlockTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(
                packOutput -> new ItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter(),
                        existingFileHelper));
        pack.addProvider(packOutput -> new FluidTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new BiomeTagsProvider(packOutput, registries, existingFileHelper));
        pack.addProvider(packOutput -> new PoiTypeTagsProvider(packOutput, registries, existingFileHelper));

        // Models
        pack.addProvider(packOutput -> new BlockModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new DecorationModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new ItemModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new CableModelProvider(packOutput, existingFileHelper));
        pack.addProvider(packOutput -> new PartModelProvider(packOutput, existingFileHelper));

        // Misc
        pack.addProvider(packOutput -> new AdvancementProvider(packOutput, registries, existingFileHelper, List.of(
                new AdvancementGenerator(localization))));

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

    private static RegistrySetBuilder createDatapackEntriesBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.DIMENSION_TYPE, InitDimensionTypes::init)
                .add(Registries.STRUCTURE, InitStructures::initDatagenStructures)
                .add(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets)
                .add(Registries.BIOME, InitBiomes::init)
                .add(Registries.DAMAGE_TYPE, AEDamageTypes::init);
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory,
            CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> factory.apply(packOutput, factories);
    }
}
