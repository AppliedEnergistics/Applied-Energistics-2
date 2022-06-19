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

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.IAE2DataProvider;

public class BlockDropProvider extends BlockLoot implements IAE2DataProvider {
    private final Map<Block, Function<Block, LootTable.Builder>> overrides = ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
            .put(AEBlocks.MATRIX_FRAME.block(), $ -> LootTable.lootTable())
            .put(AEBlocks.QUARTZ_ORE.block(), BlockDropProvider::createQuartzOreLootTable)
            .put(AEBlocks.DEEPSLATE_QUARTZ_ORE.block(), BlockDropProvider::createQuartzOreLootTable)
            // Budding quartz degrades by 1 with silk touch, and degrades entirely without silk touch.
            .put(AEBlocks.FLAWLESS_BUDDING_QUARTZ.block(), b -> buddingQuartz(AEBlocks.FLAWED_BUDDING_QUARTZ))
            .put(AEBlocks.FLAWED_BUDDING_QUARTZ.block(), b -> buddingQuartz(AEBlocks.CHIPPED_BUDDING_QUARTZ))
            .put(AEBlocks.CHIPPED_BUDDING_QUARTZ.block(), b -> buddingQuartz(AEBlocks.DAMAGED_BUDDING_QUARTZ))
            .put(AEBlocks.DAMAGED_BUDDING_QUARTZ.block(), b -> createSingleItemTable(AEBlocks.QUARTZ_BLOCK))
            // Quartz buds drop themselves with silk touch, and 1 dust without silk touch.
            .put(AEBlocks.SMALL_QUARTZ_BUD.block(), BlockDropProvider::quartzBud)
            .put(AEBlocks.MEDIUM_QUARTZ_BUD.block(), BlockDropProvider::quartzBud)
            .put(AEBlocks.LARGE_QUARTZ_BUD.block(), BlockDropProvider::quartzBud)
            // Quartz clusters drop themselves with silk touch, and some crystals without silk touch.
            .put(AEBlocks.QUARTZ_CLUSTER.block(), BlockDropProvider::quartzCluster)
            .build();

    private final Path outputFolder;

    public BlockDropProvider(Path outputFolder) {
        this.outputFolder = outputFolder;
    }

    @Override
    public void run(CachedOutput cache) throws IOException {
        for (Map.Entry<ResourceKey<Block>, Block> entry : Registry.BLOCK.entrySet()) {
            LootTable.Builder builder;
            if (entry.getKey().location().getNamespace().equals(AppEng.MOD_ID)) {
                builder = overrides.getOrDefault(entry.getValue(), this::defaultBuilder).apply(entry.getValue());

                DataProvider.saveStable(cache, toJson(builder), getPath(outputFolder, entry.getKey().location()));
            }
        }

        DataProvider.saveStable(cache, toJson(LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 3))
                        .add(LootItem.lootTableItem(AEBlocks.SKY_STONE_BLOCK)))),
                getPath(outputFolder, AppEng.makeId("chests/meteorite")));
    }

    private LootTable.Builder defaultBuilder(Block block) {
        Builder<?> entry = LootItem.lootTableItem(block);
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry)
                .when(ExplosionCondition.survivesExplosion());

        return LootTable.lootTable().withPool(pool);
    }

    private static LootTable.Builder createQuartzOreLootTable(Block b) {
        return createSilkTouchDispatchTable(b,
                LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_DUST)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                        .apply(ApplyExplosionDecay.explosionDecay())).withPool(
                                /*
                                 * Additional pool to add a chance for crystals when no silk touch is used.
                                 */
                                LootPool.lootPool()
                                        .when(HAS_NO_SILK_TOUCH)
                                        .when(
                                                /*
                                                 * 5% chance initially + 5% per level of fortune to drop *any* crystals
                                                 */
                                                BonusLevelTableCondition.bonusLevelFlatChance(
                                                        Enchantments.BLOCK_FORTUNE,
                                                        0.05F, 0.10F, 0.15F, 0.20F))
                                        .add(
                                                LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL)
                                                        .apply(SetItemCountFunction.setCount(
                                                                UniformGenerator.between(1.0F, 4.0F)))
                                                        .apply(ApplyBonusCount.addUniformBonusCount(
                                                                Enchantments.BLOCK_FORTUNE))
                                                        .apply(ApplyExplosionDecay.explosionDecay())));
    }

    private static LootTable.Builder buddingQuartz(BlockDefinition<?> degradedVersion) {
        return createSingleItemTableWithSilkTouch(degradedVersion.block(), AEBlocks.QUARTZ_BLOCK.block());
    }

    private static LootTable.Builder quartzBud(Block bud) {
        return createSingleItemTableWithSilkTouch(bud, AEItems.CERTUS_QUARTZ_DUST);
    }

    private static LootTable.Builder quartzCluster(Block cluster) {
        return createSilkTouchDispatchTable(cluster,
                LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                        .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                        .apply(ApplyExplosionDecay.explosionDecay()));
    }

    private Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/blocks/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootTables.serialize(finishBuilding(builder));
    }

    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.setParamSet(LootContextParamSets.BLOCK).build();
    }

    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Block Drops";
    }

}
