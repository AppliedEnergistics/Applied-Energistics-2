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

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.BlockDefinition;
import appeng.datagen.providers.tags.ConventionTags;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer.Builder;
import net.minecraft.world.level.storage.loot.entries.TagEntry;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BlockDropProvider extends BlockLootSubProvider {
    private final Map<Block, Function<Block, LootTable.Builder>> overrides = createOverrides();

    @NotNull
    private ImmutableMap<Block, Function<Block, LootTable.Builder>> createOverrides() {
        return ImmutableMap.<Block, Function<Block, LootTable.Builder>>builder()
                .put(AEBlocks.MATRIX_FRAME.block(), $ -> LootTable.lootTable())
                .put(AEBlocks.MYSTERIOUS_CUBE.block(), this::mysteriousCube)
                // Flawless budding quartz always degrades by 1.
                .put(AEBlocks.FLAWLESS_BUDDING_QUARTZ.block(), flawlessBuddingQuartz())
                // Imperfect budding quartz degrades by 1 without silk touch, and does not degrade with silk touch.
                .put(AEBlocks.FLAWED_BUDDING_QUARTZ.block(), buddingQuartz(AEBlocks.CHIPPED_BUDDING_QUARTZ))
                .put(AEBlocks.CHIPPED_BUDDING_QUARTZ.block(), buddingQuartz(AEBlocks.DAMAGED_BUDDING_QUARTZ))
                .put(AEBlocks.DAMAGED_BUDDING_QUARTZ.block(), buddingQuartz(AEBlocks.QUARTZ_BLOCK))
                // Quartz buds drop themselves with silk touch, and 1 dust without silk touch.
                .put(AEBlocks.SMALL_QUARTZ_BUD.block(), this::quartzBud)
                .put(AEBlocks.MEDIUM_QUARTZ_BUD.block(), this::quartzBud)
                .put(AEBlocks.LARGE_QUARTZ_BUD.block(), this::quartzBud)
                // Quartz clusters drop themselves with silk touch, and some crystals without silk touch.
                .put(AEBlocks.QUARTZ_CLUSTER.block(), this::quartzCluster)
                .build();
    }

    public BlockDropProvider(HolderLookup.Provider providers) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), providers);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK
                .stream()
                .filter(entry -> entry.getLootTable().location().getNamespace().equals(AppEng.MOD_ID))
                .toList();
    }

    @Override
    public void generate() {
        for (var block : getKnownBlocks()) {
            add(block, overrides.getOrDefault(block, this::defaultBuilder).apply(block));
        }
    }

    private LootTable.Builder defaultBuilder(Block block) {
        Builder<?> entry = LootItem.lootTableItem(block);
        LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(entry)
                .when(ExplosionCondition.survivesExplosion());

        return LootTable.lootTable().withPool(pool);
    }

    private Function<Block, LootTable.Builder> flawlessBuddingQuartz() {
        return b -> createSingleItemTable(AEBlocks.FLAWED_BUDDING_QUARTZ.block());
    }

    private Function<Block, LootTable.Builder> buddingQuartz(BlockDefinition<?> degradedVersion) {
        return b -> createSingleItemTableWithSilkTouch(b, degradedVersion);
    }

    private LootTable.Builder quartzBud(Block bud) {
        return createSingleItemTableWithSilkTouch(bud, AEItems.CERTUS_QUARTZ_DUST);
    }

    private LootTable.Builder quartzCluster(Block cluster) {
        return createSilkTouchDispatchTable(cluster,
                LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                        .apply(ApplyBonusCount.addUniformBonusCount(getEnchantment(Enchantments.FORTUNE)))
                        .apply(ApplyExplosionDecay.explosionDecay()));
    }

    private LootTable.Builder mysteriousCube(Block block) {
        return createSilkTouchDispatchTable(block, TagEntry.tagContents(ConventionTags.INSCRIBER_PRESSES)
                .when(ExplosionCondition.survivesExplosion()))
                .withPool(
                        LootPool.lootPool().when(doesNotHaveSilkTouch()).setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(AEItems.TABLET)));
    }

    protected final Holder<Enchantment> getEnchantment(ResourceKey<Enchantment> key) {
        return registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }
}
