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
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import appeng.core.AppEng;
import appeng.core.definitions.AEDamageTypes;
import appeng.datagen.providers.AE2ParticleDescriptionProvider;
import appeng.datagen.providers.advancements.AdvancementGenerator;
import appeng.datagen.providers.datamaps.RaidHeroGiftsProvider;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.loot.AE2LootTableProvider;
import appeng.datagen.providers.models.AE2ModelProvider;
import appeng.datagen.providers.models.BlockModelProvider;
import appeng.datagen.providers.models.DecorationModelProvider;
import appeng.datagen.providers.models.ItemModelProvider;
import appeng.datagen.providers.models.PartModelProvider;
import appeng.datagen.providers.recipes.AE2RecipeProvider;
import appeng.datagen.providers.tags.BiomeTagsProvider;
import appeng.datagen.providers.tags.BlockTagsProvider;
import appeng.datagen.providers.tags.DataComponentTypeTagProvider;
import appeng.datagen.providers.tags.FluidTagsProvider;
import appeng.datagen.providers.tags.ItemTagsProvider;
import appeng.datagen.providers.tags.PoiTypeTagsProvider;
import appeng.datagen.providers.tags.VillagerTradeTagsProvider;
import appeng.init.InitVillager;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitDimensionTypes;
import appeng.init.worldgen.InitStructures;

@EventBusSubscriber(modid = AppEng.MOD_ID)
public class AE2DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        var generator = event.getGenerator();
        var localization = new LocalizationProvider(generator);

        // GatherDataEvent lost getLookupProvider() and now distinguishes the "world"
        // datapack registry layer from the "reloadable" one. Loot tables, advancements and recipes
        // all became reloadable datapack registries in 26.3, so they are no longer standalone
        // DataProviders - they are registered through a RegistrySetBuilder here instead. The
        // generated files land in exactly the same places (data/ae2/loot_table, data/ae2/advancement
        // and data/ae2/recipe) because those are the registries' element directories.
        event.createWorldRegistryObjects(createWorldEntriesBuilder(), Set.of(AppEng.MOD_ID));
        event.createReloadableRegistryObjects(createReloadableEntriesBuilder(localization),
                Set.of(AppEng.MOD_ID));

        // This now includes AE2's own world-layer datapack entries, which is what
        // RegistryPatchGenerator.createLookup(...).full() used to provide.
        var registries = event.getWorldLookupProvider();

        var pack = generator.getVanillaPack(true);

        // Tags
        var blockTagsProvider = pack
                .addProvider(packOutput -> new BlockTagsProvider(packOutput, registries));
        pack.addProvider(
                packOutput -> new ItemTagsProvider(packOutput, registries, blockTagsProvider.contentsGetter()));
        pack.addProvider(packOutput -> new FluidTagsProvider(packOutput, registries));
        pack.addProvider(packOutput -> new BiomeTagsProvider(packOutput, registries));
        pack.addProvider(packOutput -> new PoiTypeTagsProvider(packOutput, registries));
        pack.addProvider(packOutput -> new VillagerTradeTagsProvider(packOutput, registries));
        pack.addProvider(packOutput -> new DataComponentTypeTagProvider(packOutput, registries,
                localization));

        // Models
        pack.addProvider(AE2ModelProvider.create(
                AppEng.MOD_ID,
                BlockModelProvider::new,
                DecorationModelProvider::new,
                ItemModelProvider::new,
                PartModelProvider::new));

        // Misc
        pack.addProvider(AE2ParticleDescriptionProvider::new);

        // DataMaps
        pack.addProvider(bindRegistries(RaidHeroGiftsProvider::new, registries));

        // Must run last
        pack.addProvider(packOutput -> localization);
    }

    private static RegistrySetBuilder createWorldEntriesBuilder() {
        return new RegistrySetBuilder()
                .add(Registries.DIMENSION_TYPE, InitDimensionTypes::init)
                .add(Registries.STRUCTURE, InitStructures::initDatagenStructures)
                .add(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets)
                .add(Registries.BIOME, InitBiomes::init)
                .add(Registries.DAMAGE_TYPE, AEDamageTypes::init)
                .add(Registries.TRADE_SET, InitVillager::bootstrapTradeSets)
                .add(Registries.VILLAGER_TRADE, InitVillager::bootstrapTrades);
    }

    private static RegistrySetBuilder createReloadableEntriesBuilder(LocalizationProvider localization) {
        return new RegistrySetBuilder()
                .add(Registries.LOOT_TABLE, new AE2LootTableProvider())
                .add(Registries.ADVANCEMENT, new AdvancementProvider(List.<AdvancementSubProvider.Factory>of(
                        output -> new AdvancementGenerator(output, localization))))
                .add(AE2RecipeProvider.bootstrap());
    }

    private static <T extends DataProvider> DataProvider.Factory<T> bindRegistries(
            BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, T> factory,
            CompletableFuture<HolderLookup.Provider> factories) {
        return packOutput -> factory.apply(packOutput, factories);
    }
}
