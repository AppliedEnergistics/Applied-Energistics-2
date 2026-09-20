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

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.init.InitVillager;

public class RaidHeroGiftLootProvider implements LootTableSubProvider {
    private final LootTableSubProvider.Context output;

    public RaidHeroGiftLootProvider(LootTableSubProvider.Context output) {
        this.output = output;
    }

    @Override
    public void run() {
        this.output.accept(
                InitVillager.LOOT_TABLE_KEY,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ContextIntProviders.exactly(1))
                                .add(LootItem.lootTableItem(AEItems.CERTUS_QUARTZ_CRYSTAL))
                                .add(LootItem.lootTableItem(AEItems.FLUIX_CRYSTAL))
                                .add(LootItem.lootTableItem(AEBlocks.SKY_STONE_BLOCK))));
    }
}
