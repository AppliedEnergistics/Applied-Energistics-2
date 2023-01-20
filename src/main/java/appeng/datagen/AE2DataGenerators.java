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
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import appeng.core.AppEng;
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

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AE2DataGenerators {

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent dataEvent) {
        onGatherData(dataEvent.getGenerator(), dataEvent.getExistingFileHelper());
    }

    public static void onGatherData(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var registries = createAppEngProvider(registryAccess);

        var localization = new LocalizationProvider(generator);

        var mainPack = generator.getVanillaPack(true);

        // Worldgen et al
        mainPack.addProvider(bindRegistries(WorldGenProvider::new, registries));

        // Loot
        mainPack.addProvider(BlockDropProvider::new);

        // Tags
        var blockTagsProvider = mainPack
                .addProvider(packOutput -> new BlockTagsProvider(packOutput, registries, existingFileHelper));
        mainPack.addProvider(
                packOutput -> new ItemTagsProvider(packOutput, registries, blockTagsProvider, existingFileHelper));
        mainPack.addProvider(packOutput -> new FluidTagsProvider(packOutput, registries, existingFileHelper));
        mainPack.addProvider(packOutput -> new BiomeTagsProvider(packOutput, registries, existingFileHelper));
        mainPack.addProvider(packOutput -> new PoiTypeTagsProvider(packOutput, registries, existingFileHelper));

        // Models
        mainPack.addProvider(packOutput -> new BlockModelProvider(packOutput, existingFileHelper));
        mainPack.addProvider(packOutput -> new DecorationModelProvider(packOutput, existingFileHelper));
        mainPack.addProvider(packOutput -> new ItemModelProvider(packOutput, existingFileHelper));
        mainPack.addProvider(packOutput -> new CableModelProvider(packOutput, existingFileHelper));
        mainPack.addProvider(packOutput -> new PartModelProvider(packOutput, existingFileHelper));

        // Misc
        mainPack.addProvider(packOutput -> new AdvancementGenerator(packOutput, localization));

        // Recipes
        mainPack.addProvider(DecorationRecipes::new);
        mainPack.addProvider(DecorationBlockRecipes::new);
        mainPack.addProvider(MatterCannonAmmoProvider::new);
        mainPack.addProvider(EntropyRecipes::new);
        mainPack.addProvider(InscriberRecipes::new);
        mainPack.addProvider(SmeltingRecipes::new);
        mainPack.addProvider(CraftingRecipes::new);
        mainPack.addProvider(SmithingRecipes::new);
        mainPack.addProvider(TransformRecipes::new);
        mainPack.addProvider(ChargerRecipes::new);

        // Must run last
        mainPack.addProvider(packOutput -> localization);
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

        var extensions = List.of(
                new LookupExtension<>(Registries.DIMENSION_TYPE, InitDimensionTypes::init),
                new LookupExtension<>(Registries.STRUCTURE, InitStructures::initDatagenStructures),
                new LookupExtension<>(Registries.STRUCTURE_SET, InitStructures::initDatagenStructureSets),
                new LookupExtension<>(Registries.BIOME, InitBiomes::init));

        var builder = new RegistrySetBuilder();
        for (var entry : VanillaRegistries.BUILDER.entries) {
            var extension = extensions.stream()
                    .filter(e -> e.key() == entry.key())
                    .findFirst()
                    .orElse(null);
            addEntry(builder, entry, extension);
        }

        return CompletableFuture.completedFuture(builder.build(registryAccess));
    }

    private static <T> void addEntry(RegistrySetBuilder builder,
            RegistrySetBuilder.RegistryStub<T> entry,
            @Nullable LookupExtension<?> extension) {
        var typedExtension = extension != null ? extension.tryCast(entry.key()) : null;
        if (typedExtension == null) {
            builder.add(entry.key(), entry.lifecycle(), entry.bootstrap());
            return;
        }

        builder.add(entry.key(), entry.lifecycle(), bootstapContext -> {
            entry.bootstrap().run(bootstapContext);
            typedExtension.extender().accept(bootstapContext);
        });
    }

    record LookupExtension<T> (ResourceKey<Registry<T>> key, Consumer<BootstapContext<T>> extender) {
        @SuppressWarnings("unchecked")
        @Nullable
        public <U> LookupExtension<U> tryCast(ResourceKey<? extends Registry<U>> registryKey) {
            if (Objects.equals(registryKey, key)) {
                return (LookupExtension<U>) this;
            }
            return null;
        }
    }
}
