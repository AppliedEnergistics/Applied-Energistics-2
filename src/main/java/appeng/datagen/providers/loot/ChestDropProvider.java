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

import javax.annotation.Nonnull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.datagen.providers.IAE2DataProvider;
import appeng.loot.NeededPressType;
import appeng.loot.NeedsPressCondition;

public class ChestDropProvider implements IAE2DataProvider {

    private static final LootItemCondition.Builder NEEDS_CALCULATION_PROCESSOR_PRESS = () -> new NeedsPressCondition(
            NeededPressType.CALCULATION_PROCESSOR_PRESS);
    private static final LootItemCondition.Builder NEEDS_ENGINEERING_PROCESSOR_PRESS = () -> new NeedsPressCondition(
            NeededPressType.ENGINEERING_PROCESSOR_PRESS);
    private static final LootItemCondition.Builder NEEDS_LOGIC_PROCESSOR_PRESS = () -> new NeedsPressCondition(
            NeededPressType.LOGIC_PROCESSOR_PRESS);
    private static final LootItemCondition.Builder NEEDS_SILICON_PRESS = () -> new NeedsPressCondition(
            NeededPressType.SILICON_PRESS);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path outputFolder;

    public ChestDropProvider(GatherDataEvent dataEvent) {
        outputFolder = dataEvent.getGenerator().getOutputFolder();
    }

    @Override
    public void run(@Nonnull HashCache cache) throws IOException {
        var meteoriteChestTable = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .name("main")
                        .setRolls(UniformGenerator.between(1, 3))
                        .bonusRolls(1, 1)
                        .add(LootItem.lootTableItem(AEBlocks.SKY_STONE_BLOCK)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 12))))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 12)))))
                // This pool should grant one of the presses the player actually needs, with luck granting one
                // additional.
                .withPool(LootPool.lootPool()
                        .name("needed_presses")
                        .setRolls(UniformGenerator.between(1, 2))
                        .bonusRolls(1, 1)
                        .add(LootItem.lootTableItem(AEItems.CALCULATION_PROCESSOR_PRESS)
                                .when(NEEDS_CALCULATION_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.ENGINEERING_PROCESSOR_PRESS)
                                .when(NEEDS_ENGINEERING_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.LOGIC_PROCESSOR_PRESS)
                                .when(NEEDS_LOGIC_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.SILICON_PRESS)
                                .when(NEEDS_SILICON_PRESS)))
                // This loot pool should be active if the looter is not a player or they no
                // need a specific press.
                .withPool(LootPool.lootPool()
                        .name("presses")
                        .when(InvertedLootItemCondition.invert(
                                AlternativeLootItemCondition.alternative(
                                        NEEDS_CALCULATION_PROCESSOR_PRESS,
                                        NEEDS_ENGINEERING_PROCESSOR_PRESS,
                                        NEEDS_LOGIC_PROCESSOR_PRESS,
                                        NEEDS_SILICON_PRESS)))
                        .setRolls(UniformGenerator.between(1, 2))
                        .bonusRolls(1, 1)
                        .add(LootItem.lootTableItem(AEItems.CALCULATION_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.ENGINEERING_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.LOGIC_PROCESSOR_PRESS))
                        .add(LootItem.lootTableItem(AEItems.SILICON_PRESS)));
        DataProvider.save(GSON, cache, toJson(meteoriteChestTable), getPath(outputFolder, AppEng.makeId("meteorite")));
    }

    private Path getPath(Path root, ResourceLocation id) {
        return root.resolve("data/" + id.getNamespace() + "/loot_tables/chests/" + id.getPath() + ".json");
    }

    public JsonElement toJson(LootTable.Builder builder) {
        return LootTables.serialize(finishBuilding(builder));
    }

    @Nonnull
    public LootTable finishBuilding(LootTable.Builder builder) {
        return builder.setParamSet(LootContextParamSets.CHEST).build();
    }

    @Nonnull
    @Override
    public String getName() {
        return AppEng.MOD_NAME + " Chest Drops";
    }

}
