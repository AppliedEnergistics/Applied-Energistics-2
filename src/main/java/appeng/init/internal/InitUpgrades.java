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
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiParts;
import appeng.core.localization.GuiText;

public final class InitUpgrades {

    private InitUpgrades() {
    }

    public static void init() {
        // Block and part interface have different translation keys, but support the
        // same upgrades
        String interfaceGroup = ApiParts.INTERFACE.asItem().getTranslationKey();
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();
        String fluidIoBusGroup = GuiText.IOBusesFluids.getTranslationKey();
        String storageCellGroup = GuiText.StorageCells.getTranslationKey();

        // Interface
        Upgrades.CRAFTING.registerItem(ApiParts.INTERFACE, 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(ApiBlocks.INTERFACE, 1, interfaceGroup);

        // IO Port!
        Upgrades.SPEED.registerItem(ApiBlocks.IO_PORT, 3);
        Upgrades.REDSTONE.registerItem(ApiBlocks.IO_PORT, 1);

        // Level Emitter!
        Upgrades.FUZZY.registerItem(ApiParts.LEVEL_EMITTER, 1);
        Upgrades.CRAFTING.registerItem(ApiParts.LEVEL_EMITTER, 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(ApiParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(ApiParts.IMPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.IMPORT_BUS, 4, itemIoBusGroup);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_IMPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.FLUID_IMPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.FLUID_IMPORT_BUS, 4, fluidIoBusGroup);

        // Export Bus
        Upgrades.FUZZY.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(ApiParts.EXPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.EXPORT_BUS, 4, itemIoBusGroup);
        Upgrades.CRAFTING.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_EXPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.FLUID_EXPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.FLUID_EXPORT_BUS, 4, fluidIoBusGroup);

        // Storage Cells
        Upgrades.FUZZY.registerItem(ApiItems.CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL1K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL4K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL16K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL64K, 1, storageCellGroup);

        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.VIEW_CELL, 1);
        Upgrades.INVERTER.registerItem(ApiItems.VIEW_CELL, 1);

        // Storage Bus
        Upgrades.FUZZY.registerItem(ApiParts.STORAGE_BUS, 1);
        Upgrades.INVERTER.registerItem(ApiParts.STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.STORAGE_BUS, 5);

        // Storage Bus Fluids
        Upgrades.INVERTER.registerItem(ApiParts.FLUID_STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_STORAGE_BUS, 5);

        // Formation Plane
        Upgrades.FUZZY.registerItem(ApiParts.FORMATION_PLANE, 1);
        Upgrades.INVERTER.registerItem(ApiParts.FORMATION_PLANE, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.FORMATION_PLANE, 5);

        // Matter Cannon
        Upgrades.FUZZY.registerItem(ApiItems.MASS_CANNON, 1);
        Upgrades.INVERTER.registerItem(ApiItems.MASS_CANNON, 1);
        Upgrades.SPEED.registerItem(ApiItems.MASS_CANNON, 4);

        // Molecular Assembler
        Upgrades.SPEED.registerItem(ApiBlocks.MOLECULAR_ASSEMBLER, 5);

        // Inscriber
        Upgrades.SPEED.registerItem(ApiBlocks.INSCRIBER, 3);
    }

}
