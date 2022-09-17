/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.loot;


import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class ChestLoot {

    @SubscribeEvent
    public void loadLootTable(LootTableLoadEvent event) {
        if (event.getName() == LootTableList.CHESTS_ABANDONED_MINESHAFT) {
            // TODO 1.9.4 aftermath - All these loot quality, pools and stuff. Figure it out and balance it.
            final IMaterials materials = AEApi.instance().definitions().materials();
            materials.certusQuartzCrystal().maybeStack(1).ifPresent(is ->
            {
                event.getTable()
                        .addPool(new LootPool(new LootEntry[]{
                                new LootEntryItem(is.getItem(), 2, 3, new LootFunction[]{
                                        new SetMetadata(null, new RandomValueRange(is.getItemDamage()))}, new LootCondition[]{
                                        new RandomChance(1)}, "AE2 Crystal_" + is.getItemDamage())
                        }, new LootCondition[0], new RandomValueRange(1, 4), new RandomValueRange(0, 2), "AE2 Crystals"));
            });

            materials.certusQuartzDust().maybeStack(1).ifPresent(is ->
            {
                event.getTable()
                        .addPool(new LootPool(new LootEntryItem[]{
                                new LootEntryItem(is.getItem(), 2, 3, new LootFunction[]{
                                        new SetMetadata(null, new RandomValueRange(is.getItemDamage()))}, new LootCondition[]{
                                        new RandomChance(1)}, "AE2 Dust_" + is.getItemDamage())
                        }, new LootCondition[0], new RandomValueRange(1, 4), new RandomValueRange(0, 2), "AE2 DUSTS"));
            });

        }
    }

}