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

package appeng.init.internal;

import appeng.api.config.Upgrades;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.localization.GuiText;

public final class InitUpgrades {

    private InitUpgrades() {
    }

    public static void init() {
        // Block and part interface have different translation keys, but support the
        // same upgrades
        String interfaceGroup = GuiText.Interface.getTranslationKey();
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();
        String fluidIoBusGroup = GuiText.IOBusesFluids.getTranslationKey();
        String storageCellGroup = GuiText.StorageCells.getTranslationKey();

        // Interface
        Upgrades.CRAFTING.registerItem(AEParts.ITEM_INTERFACE, 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(AEBlocks.ITEM_INTERFACE, 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(AEParts.FLUID_INTERFACE, 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(AEBlocks.FLUID_INTERFACE, 1, interfaceGroup);

        // IO Port!
        Upgrades.SPEED.registerItem(AEBlocks.IO_PORT, 3);
        Upgrades.REDSTONE.registerItem(AEBlocks.IO_PORT, 1);

        // Item Level Emitter!
        Upgrades.FUZZY.registerItem(AEParts.ITEM_LEVEL_EMITTER, 1);
        Upgrades.CRAFTING.registerItem(AEParts.ITEM_LEVEL_EMITTER, 1);

        // Fluid Level Emitter!
        Upgrades.CRAFTING.registerItem(AEParts.FLUID_LEVEL_EMITTER, 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(AEParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(AEParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(AEParts.IMPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(AEParts.IMPORT_BUS, 4, itemIoBusGroup);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(AEParts.FLUID_IMPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(AEParts.FLUID_IMPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(AEParts.FLUID_IMPORT_BUS, 4, fluidIoBusGroup);

        // Export Bus
        Upgrades.FUZZY.registerItem(AEParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(AEParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(AEParts.EXPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(AEParts.EXPORT_BUS, 4, itemIoBusGroup);
        Upgrades.CRAFTING.registerItem(AEParts.EXPORT_BUS, 1, itemIoBusGroup);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(AEParts.FLUID_EXPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(AEParts.FLUID_EXPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(AEParts.FLUID_EXPORT_BUS, 4, fluidIoBusGroup);
        Upgrades.CRAFTING.registerItem(AEParts.FLUID_EXPORT_BUS, 1, fluidIoBusGroup);

        // Storage Cells
        Upgrades.FUZZY.registerItem(AEItems.CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.CELL1K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(AEItems.CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.CELL4K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(AEItems.CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.CELL16K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(AEItems.CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(AEItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(AEItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(AEItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(AEItems.PORTABLE_CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.PORTABLE_CELL64K, 1, storageCellGroup);

        Upgrades.INVERTER.registerItem(AEItems.FLUID_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.FLUID_CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.FLUID_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(AEItems.FLUID_CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(AEItems.VIEW_CELL, 1);
        Upgrades.INVERTER.registerItem(AEItems.VIEW_CELL, 1);

        // Storage Bus
        Upgrades.FUZZY.registerItem(AEParts.ITEM_STORAGE_BUS, 1);
        Upgrades.INVERTER.registerItem(AEParts.ITEM_STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(AEParts.ITEM_STORAGE_BUS, 5);

        // Storage Bus Fluids
        Upgrades.INVERTER.registerItem(AEParts.FLUID_STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(AEParts.FLUID_STORAGE_BUS, 5);

        // Formation Plane
        Upgrades.FUZZY.registerItem(AEParts.FORMATION_PLANE, 1);
        Upgrades.INVERTER.registerItem(AEParts.FORMATION_PLANE, 1);
        Upgrades.CAPACITY.registerItem(AEParts.FORMATION_PLANE, 5);

        // Matter Cannon
        Upgrades.FUZZY.registerItem(AEItems.MASS_CANNON, 1);
        Upgrades.INVERTER.registerItem(AEItems.MASS_CANNON, 1);
        Upgrades.SPEED.registerItem(AEItems.MASS_CANNON, 4);

        // Molecular Assembler
        Upgrades.SPEED.registerItem(AEBlocks.MOLECULAR_ASSEMBLER, 5);

        // Inscriber
        Upgrades.SPEED.registerItem(AEBlocks.INSCRIBER, 3);
    }

}
