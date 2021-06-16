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

package appeng.datagen.providers.loot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.block.Block;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.ApplyBonus;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockDropProvider extends BlockLootTables implements IAE2DataProvider {
    private Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(ApiBlocks.matrixFrame.block(), $ -> LootTable.builder())
            .put(ApiBlocks.quartzOre.block(),
                    b -> droppingWithSilkTouch(ApiBlocks.quartzOre.block(),
                            withExplosionDecay(ApiBlocks.quartzOre.block(),
                                    ItemLootEntry.builder(ApiItems.CERTUS_QUARTZ_CRYSTAL.item())
                                            .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                            .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .put(ApiBlocks.quartzOreCharged.block(),
                    b -> droppingWithSilkTouch(ApiBlocks.quartzOreCharged.block(),
                            withExplosionDecay(ApiBlocks.quartzOreCharged.block(),
                                    ItemLootEntry.builder(ApiItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.item())
                                            .acceptFunction(SetCount.builder(RandomValueRange.of(1.0F, 2.0F)))
                                            .acceptFunction(ApplyBonus.uniformBonusCount(Enchantments.FORTUNE)))))
            .build();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    public BlockDropProvider(GatherDataEvent dataEvent) {
        outputFolder = dataEvent.getGenerator().getOutputFolder();
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) throws IOException {
        for (Map.Entry<RegistryKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            LootTable.Builder builder;
            if (entry.getKey().getLocation().getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                IDataProvider.save(GSON, cache, toJson(builder), getPath(outputFolder, entry.getKey().getLocation()));
            }
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        LootEntry.Builder<?> entry = ItemLootEntry.builder(block);
        LootPool.Builder pool = LootPool.builder().name("main").rolls(ConstantRange.of(1)).addEntry(entry)
                .acceptCondition(SurvivesExplosion.builder());

        return LootTable.builder().addLootPool(pool);
    }

    private Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootTableManager.toJson(finishBuilding(builder));
    }

    @Nonnull
    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.setParameterSet(LootParameterSets.BLOCK).build();
    }

    @Nonnull
    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Drops";
    }

}
