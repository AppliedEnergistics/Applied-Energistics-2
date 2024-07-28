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

package appeng.datagen.providers.advancements;

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

import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.stats.AdvancementTriggers;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.datagen.providers.tags.ConventionTags;

public class AdvancementGenerator implements AdvancementProvider.AdvancementGenerator {
    private final LocalizationProvider localization;

    public AdvancementGenerator(LocalizationProvider localization) {
        this.localization = localization;
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer,
            ExistingFileHelper existingFileHelper) {
        var root = Advancement.Builder.advancement()
                .display(
                        AEItems.CERTUS_QUARTZ_CRYSTAL,
                        localization.component("achievement.ae2.Root", "Applied Energistics"),
                        localization.component("achievement.ae2.Root.desc",
                                "When a chest is simply not enough. Acquire Copper to start your AE2 adventure."),
                        AppEng.makeId("textures/block/sky_stone_brick.png"),
                        AdvancementType.TASK,
                        false /* showToast */,
                        false /* announceChat */,
                        false /* hidden */
                )
                .addCriterion("copper", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
                .save(consumer, "ae2:main/root");

        var charger = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CHARGER,
                        localization.component("achievement.ae2.Charger", "It's Chargin' Time !"),
                        localization.component("achievement.ae2.Charger.desc", "Craft a Charger"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CHARGER))
                .save(consumer, "ae2:main/charger");

        var compass = Advancement.Builder.advancement()
                .display(
                        AEItems.METEORITE_COMPASS,
                        localization.component("achievement.ae2.Compass", "Meteorite Hunter"),
                        localization.component("achievement.ae2.Compass.desc", "Craft a Meteorite Compass"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(charger)
                .addCriterion("compass", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.METEORITE_COMPASS))
                .save(consumer, "ae2:main/compass");

        var chargedQuartz = Advancement.Builder.advancement()
                .display(
                        AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                        localization.component("achievement.ae2.ChargedQuartz", "Shocking"),
                        localization.component("achievement.ae2.ChargedQuartz.desc", "Charge Quartz with a Charger"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(charger)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(consumer, "ae2:main/charged_quartz");

        var pressesBuilder = Advancement.Builder.advancement()
                .display(
                        AEItems.LOGIC_PROCESSOR_PRESS,
                        localization.component("achievement.ae2.Presses", "Unknown Technology"),
                        localization.component("achievement.ae2.Presses.desc", "Find all Processor Presses"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("calculation",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CALCULATION_PROCESSOR_PRESS))
                .addCriterion("engineering",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ENGINEERING_PROCESSOR_PRESS))
                .addCriterion("logic", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.LOGIC_PROCESSOR_PRESS))
                .addCriterion("silicon", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.SILICON_PRESS));
        var presses = pressesBuilder.save(consumer, "ae2:main/presses");

        var controller = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CONTROLLER,
                        localization.component("achievement.ae2.Controller", "Networking Switchboard"),
                        localization.component("achievement.ae2.Controller.desc", "Craft a Controller"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(presses)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CONTROLLER))
                .save(consumer, "ae2:main/controller");

        var storageCell = Advancement.Builder.advancement()
                .display(
                        AEItems.ITEM_CELL_64K,
                        localization.component("achievement.ae2.StorageCell", "Better Than Chests"),
                        localization.component("achievement.ae2.StorageCell.desc", "Craft a Storage Cell"),
                        null /* background */,
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(controller)
                .addCriterion("c1k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ITEM_CELL_1K))
                .addCriterion("c4k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ITEM_CELL_4K))
                .addCriterion("c16k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ITEM_CELL_16K))
                .addCriterion("c64k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ITEM_CELL_64K))
                .addCriterion("c256k", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.ITEM_CELL_256K))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(consumer, "ae2:main/storage_cell");

        var ioport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.IO_PORT,
                        localization.component("achievement.ae2.IOPort", "Storage Cell Shuffle"),
                        localization.component("achievement.ae2.IOPort.desc", "Craft an IO Port"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(storageCell)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.IO_PORT))
                .save(consumer, "ae2:main/ioport");

        var craftingTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.CRAFTING_TERMINAL,
                        localization.component("achievement.ae2.CraftingTerminal", "A (Much) Bigger Table"),
                        localization.component("achievement.ae2.CraftingTerminal.desc", "Craft a Crafting Terminal"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.CRAFTING_TERMINAL))
                .save(consumer, "ae2:main/crafting_terminal");

        var patternTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.PATTERN_ENCODING_TERMINAL,
                        localization.component("achievement.ae2.PatternTerminal", "Crafting Maestro"),
                        localization.component("achievement.ae2.PatternTerminal.desc",
                                "Craft a Pattern Encoding Terminal"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(craftingTerminal)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.PATTERN_ENCODING_TERMINAL))
                .save(consumer, "ae2:main/pattern_encoding_terminal");

        var craftingCpu = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CRAFTING_STORAGE_64K,
                        localization.component("achievement.ae2.CraftingCPU", "Next Gen Crafting"),
                        localization.component("achievement.ae2.CraftingCPU.desc", "Craft a Crafting Unit"),
                        null /* background */,
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(patternTerminal)
                .addCriterion("cu", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_UNIT))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(consumer, "ae2:main/crafting_cpu");

        var fluix = Advancement.Builder.advancement()
                .display(
                        AEItems.FLUIX_CRYSTAL,
                        localization.component("achievement.ae2.Fluix", "Unnatural"),
                        localization.component("achievement.ae2.Fluix.desc", "Create Fluix Crystals"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(chargedQuartz)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FLUIX_CRYSTAL))
                .save(consumer, "ae2:main/fluix");

        var glassCable = Advancement.Builder.advancement()
                .display(
                        AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.GlassCable", "Fluix Energy Connection"),
                        localization.component("achievement.ae2.GlassCable.desc", "Craft ME Glass Cable"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ItemPredicate.Builder.item().of(ConventionTags.GLASS_CABLE).build()))
                .save(consumer, "ae2:main/glass_cable");

        var facade = Advancement.Builder.advancement()
                .display(
                        AEItems.FACADE.get().createFacadeForItemUnchecked(new ItemStack(Items.STONE)),
                        localization.component("achievement.ae2.Facade", "Network Aesthetics"),
                        localization.component("achievement.ae2.Facade.desc", "Craft a Cable Facade"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("facade", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FACADE))
                .save(consumer, "ae2:main/facade");

        var growthAccelerator = Advancement.Builder.advancement()
                .display(
                        AEBlocks.GROWTH_ACCELERATOR,
                        localization.component("achievement.ae2.CrystalGrowthAccelerator",
                                "Accelerator is an understatement"),
                        localization.component("achievement.ae2.CrystalGrowthAccelerator.desc",
                                "Craft a Crystal Growth Accelerator"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.GROWTH_ACCELERATOR))
                .save(consumer, "ae2:main/growth_accelerator");

        var network1 = Advancement.Builder.advancement()
                .display(
                        AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking1", "Network Apprentice"),
                        localization.component("achievement.ae2.Networking1.desc",
                                "Reach 8 channels using devices on a network."),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("cable", AdvancementTriggers.networkApprenticeCriterion())
                .save(consumer, "ae2:main/network1");

        var network2 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking2", "Network Engineer"),
                        localization.component("achievement.ae2.Networking2.desc",
                                "Reach 128 channels using devices on a network."),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network1)
                .addCriterion("cable", AdvancementTriggers.networkEngineerCriterion())
                .save(consumer, "ae2:main/network2");

        var network3 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking3", "Network Administrator"),
                        localization.component("achievement.ae2.Networking3.desc",
                                "Reach 2048 channels using devices on a network."),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network2)
                .addCriterion("cable", AdvancementTriggers.networkAdminCriterion())
                .save(consumer, "ae2:main/network3");

        var networkTool = Advancement.Builder.advancement()
                .display(
                        AEItems.NETWORK_TOOL,
                        localization.component("achievement.ae2.NetworkTool", "Network Diagnostics"),
                        localization.component("achievement.ae2.NetworkTool.desc", "Craft a Network Tool"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("network_tool", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.NETWORK_TOOL))
                .save(consumer, "ae2:main/network_tool");

        var p2p = Advancement.Builder.advancement()
                .display(
                        AEParts.ME_P2P_TUNNEL,
                        localization.component("achievement.ae2.P2P", "Point to Point Networking"),
                        localization.component("achievement.ae2.P2P.desc", "Craft a P2P Tunnel"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.ME_P2P_TUNNEL))
                .save(consumer, "ae2:main/p2p");

        var portableCell = Advancement.Builder.advancement()
                .display(
                        AEItems.PORTABLE_ITEM_CELL1K,
                        localization.component("achievement.ae2.PortableCell", "Storage Nomad"),
                        localization.component("achievement.ae2.PortableCell.desc", "Craft a Portable Cell"),
                        null /* background */,
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(storageCell)
                .addCriterion("pc_1k",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_ITEM_CELL1K))
                .addCriterion("pc_4k",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_ITEM_CELL4K))
                .addCriterion("pc_16k",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_ITEM_CELL16K))
                .addCriterion("pc_64k",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_ITEM_CELL64K))
                .addCriterion("pc_256k",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.PORTABLE_ITEM_CELL256K))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(consumer, "ae2:main/portable_cell");

        var qnb = Advancement.Builder.advancement()
                .display(
                        AEBlocks.QUANTUM_LINK,
                        localization.component("achievement.ae2.QNB", "Quantum Tunneling"),
                        localization.component("achievement.ae2.QNB.desc", "Craft a Quantum Link"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(p2p)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.QUANTUM_LINK))
                .save(consumer, "ae2:main/qnb");

        var spatialIoport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.SPATIAL_IO_PORT,
                        localization.component("achievement.ae2.SpatialIO", "Spatial Coordination"),
                        localization.component("achievement.ae2.SpatialIO.desc", "Craft a Spatial IO Port"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(ioport)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.SPATIAL_IO_PORT))
                .save(consumer, "ae2:main/spatial_ioport");

        var spatialExplorer = Advancement.Builder.advancement()
                .display(
                        AEItems.SPATIAL_128_CELL_COMPONENT,
                        localization.component("achievement.ae2.SpatialIOExplorer", "To boldly go"),
                        localization.component("achievement.ae2.SpatialIOExplorer.desc",
                                "Get stored in a spatial storage cell"),
                        null /* background */,
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(spatialIoport)
                .addCriterion("explorer", AdvancementTriggers.spatialExplorerCriterion())
                .save(consumer, "ae2:main/spatial_explorer");

        var storageBus = Advancement.Builder.advancement()
                .display(
                        AEParts.STORAGE_BUS,
                        localization.component("achievement.ae2.StorageBus", "Limitless Potential"),
                        localization.component("achievement.ae2.StorageBus.desc", "Craft a Storage Bus"),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("part", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.STORAGE_BUS))
                .save(consumer, "ae2:main/storage_bus");

        var storageBusOnInterface = Advancement.Builder.advancement()
                .display(
                        AEBlocks.INTERFACE,
                        localization.component("achievement.ae2.Recursive", "Recursive Networking"),
                        localization.component("achievement.ae2.Recursive.desc",
                                "Place a Storage Bus on an Interface."),
                        null /* background */,
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(storageBus)
                .addCriterion("recursive", AdvancementTriggers.recursiveCriterion())
                .save(consumer, "ae2:main/recursive");

    }
}
