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

import appeng.menu.implementations.CellWorkbenchContainer;
import appeng.menu.implementations.ChestContainer;
import appeng.menu.implementations.CondenserContainer;
import appeng.menu.implementations.DriveContainer;
import appeng.menu.implementations.FluidFormationPlaneContainer;
import appeng.menu.implementations.FluidIOBusContainer;
import appeng.menu.implementations.FluidInterfaceContainer;
import appeng.menu.implementations.FluidLevelEmitterContainer;
import appeng.menu.implementations.FluidStorageBusContainer;
import appeng.menu.implementations.GrinderContainer;
import appeng.menu.implementations.IOPortContainer;
import appeng.menu.implementations.InscriberContainer;
import appeng.menu.implementations.InterfaceTerminalContainer;
import appeng.menu.implementations.ItemFormationPlaneContainer;
import appeng.menu.implementations.ItemIOBusContainer;
import appeng.menu.implementations.ItemInterfaceContainer;
import appeng.menu.implementations.ItemLevelEmitterContainer;
import appeng.menu.implementations.ItemStorageBusContainer;
import appeng.menu.implementations.MolecularAssemblerContainer;
import appeng.menu.implementations.PriorityContainer;
import appeng.menu.implementations.QNBContainer;
import appeng.menu.implementations.QuartzKnifeContainer;
import appeng.menu.implementations.SecurityStationContainer;
import appeng.menu.implementations.SkyChestContainer;
import appeng.menu.implementations.SpatialAnchorContainer;
import appeng.menu.implementations.SpatialIOPortContainer;
import appeng.menu.implementations.VibrationChamberContainer;
import appeng.menu.implementations.WirelessContainer;
import appeng.menu.me.crafting.CraftAmountContainer;
import appeng.menu.me.crafting.CraftConfirmContainer;
import appeng.menu.me.crafting.CraftingCPUContainer;
import appeng.menu.me.crafting.CraftingStatusContainer;
import appeng.menu.me.fluids.FluidTerminalContainer;
import appeng.menu.me.items.CraftingTermContainer;
import appeng.menu.me.items.ItemTerminalContainer;
import appeng.menu.me.items.MEPortableCellContainer;
import appeng.menu.me.items.PatternTermContainer;
import appeng.menu.me.items.WirelessTermContainer;
import appeng.menu.me.networktool.NetworkStatusContainer;
import appeng.menu.me.networktool.NetworkToolContainer;

public final class InitContainerTypes {

    private InitContainerTypes() {
    }

    public static void init(IForgeRegistry<MenuType<?>> registry) {
        registry.registerAll(
                CellWorkbenchContainer.TYPE,
                ChestContainer.TYPE,
                CondenserContainer.TYPE,
                CraftAmountContainer.TYPE,
                CraftConfirmContainer.TYPE,
                CraftingCPUContainer.TYPE,
                CraftingStatusContainer.TYPE,
                CraftingTermContainer.TYPE,
                DriveContainer.TYPE,
                ItemFormationPlaneContainer.TYPE,
                GrinderContainer.TYPE,
                InscriberContainer.TYPE,
                ItemInterfaceContainer.TYPE,
                InterfaceTerminalContainer.TYPE,
                IOPortContainer.TYPE,
                ItemLevelEmitterContainer.TYPE,
                MolecularAssemblerContainer.TYPE,
                ItemTerminalContainer.TYPE,
                MEPortableCellContainer.TYPE,
                NetworkStatusContainer.TYPE,
                NetworkToolContainer.TYPE,
                PatternTermContainer.TYPE,
                PriorityContainer.TYPE,
                QNBContainer.TYPE,
                QuartzKnifeContainer.TYPE,
                SecurityStationContainer.TYPE,
                SkyChestContainer.TYPE,
                SpatialIOPortContainer.TYPE,
                SpatialAnchorContainer.TYPE,
                ItemStorageBusContainer.TYPE,
                ItemIOBusContainer.EXPORT_TYPE,
                ItemIOBusContainer.IMPORT_TYPE,
                VibrationChamberContainer.TYPE,
                WirelessContainer.TYPE,
                WirelessTermContainer.TYPE,
                FluidFormationPlaneContainer.TYPE,
                FluidIOBusContainer.EXPORT_TYPE,
                FluidIOBusContainer.IMPORT_TYPE,
                FluidInterfaceContainer.TYPE,
                FluidLevelEmitterContainer.TYPE,
                FluidStorageBusContainer.TYPE,
                FluidTerminalContainer.TYPE);
    }

}
