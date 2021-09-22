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

package appeng.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.menu.implementations.*;
import appeng.menu.me.crafting.CraftAmountMenu;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPUMenu;
import appeng.menu.me.crafting.CraftingStatusMenu;
import appeng.menu.me.fluids.FluidTerminalMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.ItemTerminalMenu;
import appeng.menu.me.items.MEPortableCellMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.menu.me.items.WirelessTermMenu;
import appeng.menu.me.networktool.NetworkStatusMenu;
import appeng.menu.me.networktool.NetworkToolMenu;

public final class InitMenuTypes {

    private InitMenuTypes() {
    }

    public static void init(IForgeRegistry<MenuType<?>> registry) {
        registry.registerAll(
                CellWorkbenchMenu.TYPE,
                ChestMenu.TYPE,
                CondenserMenu.TYPE,
                CraftAmountMenu.TYPE,
                CraftConfirmMenu.TYPE,
                CraftingCPUMenu.TYPE,
                CraftingStatusMenu.TYPE,
                CraftingTermMenu.TYPE,
                DriveMenu.TYPE,
                ItemFormationPlaneMenu.TYPE,
                GrinderMenu.TYPE,
                InscriberMenu.TYPE,
                ItemInterfaceMenu.TYPE,
                InterfaceTerminalMenu.TYPE,
                IOPortMenu.TYPE,
                ItemLevelEmitterMenu.TYPE,
                EnergyLevelEmitterMenu.TYPE,
                PatternProviderMenu.TYPE,
                MolecularAssemblerMenu.TYPE,
                ItemTerminalMenu.TYPE,
                MEPortableCellMenu.TYPE,
                NetworkStatusMenu.TYPE,
                NetworkToolMenu.TYPE,
                PatternTermMenu.TYPE,
                PriorityMenu.TYPE,
                QNBMenu.TYPE,
                QuartzKnifeMenu.TYPE,
                SecurityStationMenu.TYPE,
                SkyChestMenu.TYPE,
                SpatialIOPortMenu.TYPE,
                SpatialAnchorMenu.TYPE,
                ItemStorageBusMenu.TYPE,
                ItemIOBusMenu.EXPORT_TYPE,
                ItemIOBusMenu.IMPORT_TYPE,
                VibrationChamberMenu.TYPE,
                WirelessMenu.TYPE,
                WirelessTermMenu.TYPE,
                FluidFormationPlaneMenu.TYPE,
                FluidIOBusMenu.EXPORT_TYPE,
                FluidIOBusMenu.IMPORT_TYPE,
                FluidInterfaceMenu.TYPE,
                FluidLevelEmitterMenu.TYPE,
                FluidStorageBusMenu.TYPE,
                FluidTerminalMenu.TYPE);
    }

}
