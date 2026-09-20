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

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.core.ConventionTags;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.stats.AdvancementTriggers;
import appeng.datagen.providers.localization.LocalizationProvider;

// Advancements are a reloadable datapack registry now. AdvancementSubProvider became an
// abstract class built from a BootstrapContext<Advancement>, its generate() takes no arguments, and
// Advancement.Builder.save() writes to that context instead of a Consumer<AdvancementHolder>.
public class AdvancementGenerator extends AdvancementSubProvider {
    private final LocalizationProvider localization;
    private final HolderGetter<Item> items;

    public AdvancementGenerator(BootstrapContext<Advancement> output, LocalizationProvider localization) {
        super(output);
        this.localization = localization;
        this.items = output.lookup(Registries.ITEM);
    }

    @Override
    public void generate() {
        var root = Advancement.Builder.advancement()
                // Builder.display() lost its background parameter; the root advancement
                // uses rootDisplay() instead. The background is serialized the same way
                // (ClientAsset.ResourceTexture.CODEC is a bare Identifier codec).
                .rootDisplay(
                        AEItems.CERTUS_QUARTZ_CRYSTAL.asItem(),
                        localization.component("achievement.ae2.Root", "Applied Energistics"),
                        localization.component("achievement.ae2.Root.desc",
                                "When a chest is simply not enough. Acquire Copper to start your AE2 adventure."),
                        AppEng.makeId("block/sky_stone_brick"),
                        AdvancementType.TASK,
                        false /* showToast */,
                        false /* announceChat */,
                        false /* hidden */
                )
                .addCriterion("copper", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COPPER_INGOT))
                .save(this.output, "ae2:main/root");

        var charger = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CHARGER.asItem(),
                        localization.component("achievement.ae2.Charger", "It's Chargin' Time !"),
                        localization.component("achievement.ae2.Charger.desc", "Craft a Charger"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(root)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CHARGER))
                .save(this.output, "ae2:main/charger");

        var compass = Advancement.Builder.advancement()
                .display(
                        AEItems.METEORITE_COMPASS.asItem(),
                        localization.component("achievement.ae2.Compass", "Meteorite Hunter"),
                        localization.component("achievement.ae2.Compass.desc", "Craft a Meteorite Compass"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(charger)
                .addCriterion("compass", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.METEORITE_COMPASS))
                .save(this.output, "ae2:main/compass");

        var chargedQuartz = Advancement.Builder.advancement()
                .display(
                        AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED.asItem(),
                        localization.component("achievement.ae2.ChargedQuartz", "Shocking"),
                        localization.component("achievement.ae2.ChargedQuartz.desc", "Charge Quartz with a Charger"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(charger)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED))
                .save(this.output, "ae2:main/charged_quartz");

        var pressesBuilder = Advancement.Builder.advancement()
                .display(
                        AEItems.LOGIC_PROCESSOR_PRESS.asItem(),
                        localization.component("achievement.ae2.Presses", "Unknown Technology"),
                        localization.component("achievement.ae2.Presses.desc", "Find all Processor Presses"),
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
        var presses = pressesBuilder.save(this.output, "ae2:main/presses");

        var controller = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CONTROLLER.asItem(),
                        localization.component("achievement.ae2.Controller", "Networking Switchboard"),
                        localization.component("achievement.ae2.Controller.desc", "Craft a Controller"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(presses)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CONTROLLER))
                .save(this.output, "ae2:main/controller");

        var storageCell = Advancement.Builder.advancement()
                .display(
                        AEItems.ITEM_CELL_64K.asItem(),
                        localization.component("achievement.ae2.StorageCell", "Better Than Chests"),
                        localization.component("achievement.ae2.StorageCell.desc", "Craft a Storage Cell"),
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
                .save(this.output, "ae2:main/storage_cell");

        var ioport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.IO_PORT.asItem(),
                        localization.component("achievement.ae2.IOPort", "Storage Cell Shuffle"),
                        localization.component("achievement.ae2.IOPort.desc", "Craft an IO Port"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(storageCell)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.IO_PORT))
                .save(this.output, "ae2:main/ioport");

        var craftingTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.CRAFTING_TERMINAL.asItem(),
                        localization.component("achievement.ae2.CraftingTerminal", "A (Much) Bigger Table"),
                        localization.component("achievement.ae2.CraftingTerminal.desc", "Craft a Crafting Terminal"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.CRAFTING_TERMINAL))
                .save(this.output, "ae2:main/crafting_terminal");

        var patternTerminal = Advancement.Builder.advancement()
                .display(
                        AEParts.PATTERN_ENCODING_TERMINAL.asItem(),
                        localization.component("achievement.ae2.PatternTerminal", "Crafting Maestro"),
                        localization.component("achievement.ae2.PatternTerminal.desc",
                                "Craft a Pattern Encoding Terminal"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(craftingTerminal)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.PATTERN_ENCODING_TERMINAL))
                .save(this.output, "ae2:main/pattern_encoding_terminal");

        var craftingCpu = Advancement.Builder.advancement()
                .display(
                        AEBlocks.CRAFTING_STORAGE_64K.asItem(),
                        localization.component("achievement.ae2.CraftingCPU", "Next Gen Crafting"),
                        localization.component("achievement.ae2.CraftingCPU.desc", "Craft a Crafting Unit"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(patternTerminal)
                .addCriterion("cu", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.CRAFTING_UNIT))
                .requirements(AdvancementRequirements.Strategy.OR)
                .save(this.output, "ae2:main/crafting_cpu");

        var fluix = Advancement.Builder.advancement()
                .display(
                        AEItems.FLUIX_CRYSTAL.asItem(),
                        localization.component("achievement.ae2.Fluix", "Unnatural"),
                        localization.component("achievement.ae2.Fluix.desc", "Create Fluix Crystals"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(chargedQuartz)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FLUIX_CRYSTAL))
                .save(this.output, "ae2:main/fluix");

        var glassCable = Advancement.Builder.advancement()
                .display(
                        AEParts.GLASS_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.GlassCable", "Fluix Energy Connection"),
                        localization.component("achievement.ae2.GlassCable.desc", "Craft ME Glass Cable"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance
                                .hasItems(ItemPredicate.Builder.item().of(items, ConventionTags.GLASS_CABLE).build()))
                .save(this.output, "ae2:main/glass_cable");

        var facade = Advancement.Builder.advancement()
                .display(
                        AEItems.FACADE.get().createFacadeTemplate(Items.STONE.builtInRegistryHolder()),
                        localization.component("achievement.ae2.Facade", "Network Aesthetics"),
                        localization.component("achievement.ae2.Facade.desc", "Craft a Cable Facade"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("facade", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.FACADE))
                .save(this.output, "ae2:main/facade");

        var growthAccelerator = Advancement.Builder.advancement()
                .display(
                        AEBlocks.GROWTH_ACCELERATOR.asItem(),
                        localization.component("achievement.ae2.CrystalGrowthAccelerator",
                                "Accelerator is an understatement"),
                        localization.component("achievement.ae2.CrystalGrowthAccelerator.desc",
                                "Craft a Crystal Growth Accelerator"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(fluix)
                .addCriterion("certus",
                        InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.GROWTH_ACCELERATOR))
                .save(this.output, "ae2:main/growth_accelerator");

        var network1 = Advancement.Builder.advancement()
                .display(
                        AEParts.COVERED_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking1", "Network Apprentice"),
                        localization.component("achievement.ae2.Networking1.desc",
                                "Reach 8 channels using devices on a network."),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("cable", AdvancementTriggers.networkApprenticeCriterion())
                .save(this.output, "ae2:main/network1");

        var network2 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking2", "Network Engineer"),
                        localization.component("achievement.ae2.Networking2.desc",
                                "Reach 128 channels using devices on a network."),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network1)
                .addCriterion("cable", AdvancementTriggers.networkEngineerCriterion())
                .save(this.output, "ae2:main/network2");

        var network3 = Advancement.Builder.advancement()
                .display(
                        AEParts.SMART_DENSE_CABLE.item(AEColor.TRANSPARENT),
                        localization.component("achievement.ae2.Networking3", "Network Administrator"),
                        localization.component("achievement.ae2.Networking3.desc",
                                "Reach 2048 channels using devices on a network."),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(network2)
                .addCriterion("cable", AdvancementTriggers.networkAdminCriterion())
                .save(this.output, "ae2:main/network3");

        var networkTool = Advancement.Builder.advancement()
                .display(
                        AEItems.NETWORK_TOOL.asItem(),
                        localization.component("achievement.ae2.NetworkTool", "Network Diagnostics"),
                        localization.component("achievement.ae2.NetworkTool.desc", "Craft a Network Tool"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(controller)
                .addCriterion("network_tool", InventoryChangeTrigger.TriggerInstance.hasItems(AEItems.NETWORK_TOOL))
                .save(this.output, "ae2:main/network_tool");

        var p2p = Advancement.Builder.advancement()
                .display(
                        AEParts.ME_P2P_TUNNEL.asItem(),
                        localization.component("achievement.ae2.P2P", "Point to Point Networking"),
                        localization.component("achievement.ae2.P2P.desc", "Craft a P2P Tunnel"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.ME_P2P_TUNNEL))
                .save(this.output, "ae2:main/p2p");

        var portableCell = Advancement.Builder.advancement()
                .display(
                        AEItems.PORTABLE_ITEM_CELL1K.asItem(),
                        localization.component("achievement.ae2.PortableCell", "Storage Nomad"),
                        localization.component("achievement.ae2.PortableCell.desc", "Craft a Portable Cell"),
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
                .save(this.output, "ae2:main/portable_cell");

        var qnb = Advancement.Builder.advancement()
                .display(
                        AEBlocks.QUANTUM_LINK.asItem(),
                        localization.component("achievement.ae2.QNB", "Quantum Tunneling"),
                        localization.component("achievement.ae2.QNB.desc", "Craft a Quantum Link"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(p2p)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.QUANTUM_LINK))
                .save(this.output, "ae2:main/qnb");

        var spatialIoport = Advancement.Builder.advancement()
                .display(
                        AEBlocks.SPATIAL_IO_PORT.asItem(),
                        localization.component("achievement.ae2.SpatialIO", "Spatial Coordination"),
                        localization.component("achievement.ae2.SpatialIO.desc", "Craft a Spatial IO Port"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(ioport)
                .addCriterion("certus", InventoryChangeTrigger.TriggerInstance.hasItems(AEBlocks.SPATIAL_IO_PORT))
                .save(this.output, "ae2:main/spatial_ioport");

        var spatialExplorer = Advancement.Builder.advancement()
                .display(
                        AEItems.SPATIAL_128_CELL_COMPONENT.asItem(),
                        localization.component("achievement.ae2.SpatialIOExplorer", "To boldly go"),
                        localization.component("achievement.ae2.SpatialIOExplorer.desc",
                                "Get stored in a spatial storage cell"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .parent(spatialIoport)
                .addCriterion("explorer", AdvancementTriggers.spatialExplorerCriterion())
                .save(this.output, "ae2:main/spatial_explorer");

        var storageBus = Advancement.Builder.advancement()
                .display(
                        AEParts.STORAGE_BUS.asItem(),
                        localization.component("achievement.ae2.StorageBus", "Limitless Potential"),
                        localization.component("achievement.ae2.StorageBus.desc", "Craft a Storage Bus"),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(glassCable)
                .addCriterion("part", InventoryChangeTrigger.TriggerInstance.hasItems(AEParts.STORAGE_BUS))
                .save(this.output, "ae2:main/storage_bus");

        var storageBusOnInterface = Advancement.Builder.advancement()
                .display(
                        AEBlocks.INTERFACE.asItem(),
                        localization.component("achievement.ae2.Recursive", "Recursive Networking"),
                        localization.component("achievement.ae2.Recursive.desc",
                                "Place a Storage Bus on an Interface."),
                        AdvancementType.TASK,
                        true /* showToast */,
                        true /* announceChat */,
                        false /* hidden */
                )
                .parent(storageBus)
                .addCriterion("recursive", AdvancementTriggers.recursiveCriterion())
                .save(this.output, "ae2:main/recursive");

    }
}
