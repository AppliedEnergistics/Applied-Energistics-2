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

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.container.implementations.CellWorkbenchContainer;
import appeng.container.implementations.ChestContainer;
import appeng.container.implementations.CondenserContainer;
import appeng.container.implementations.DriveContainer;
import appeng.container.implementations.FormationPlaneContainer;
import appeng.container.implementations.GrinderContainer;
import appeng.container.implementations.IOBusContainer;
import appeng.container.implementations.IOPortContainer;
import appeng.container.implementations.InscriberContainer;
import appeng.container.implementations.InterfaceContainer;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.container.implementations.LevelEmitterContainer;
import appeng.container.implementations.MolecularAssemblerContainer;
import appeng.container.implementations.PriorityContainer;
import appeng.container.implementations.QNBContainer;
import appeng.container.implementations.QuartzKnifeContainer;
import appeng.container.implementations.SecurityStationContainer;
import appeng.container.implementations.SkyChestContainer;
import appeng.container.implementations.SpatialAnchorContainer;
import appeng.container.implementations.SpatialIOPortContainer;
import appeng.container.implementations.StorageBusContainer;
import appeng.container.implementations.VibrationChamberContainer;
import appeng.container.implementations.WirelessContainer;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.container.me.crafting.CraftConfirmContainer;
import appeng.container.me.crafting.CraftingCPUContainer;
import appeng.container.me.crafting.CraftingStatusContainer;
import appeng.container.me.fluids.FluidTerminalContainer;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.me.items.MEPortableCellContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.me.items.WirelessTermContainer;
import appeng.container.me.networktool.NetworkStatusContainer;
import appeng.container.me.networktool.NetworkToolContainer;
import appeng.fluids.container.FluidFormationPlaneContainer;
import appeng.fluids.container.FluidIOBusContainer;
import appeng.fluids.container.FluidInterfaceContainer;
import appeng.fluids.container.FluidLevelEmitterContainer;
import appeng.fluids.container.FluidStorageBusContainer;

public final class InitContainerTypes {

    private InitContainerTypes() {
    }

    public static void init(IForgeRegistry<ContainerType<?>> registry) {
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
                FormationPlaneContainer.TYPE,
                GrinderContainer.TYPE,
                InscriberContainer.TYPE,
                InterfaceContainer.TYPE,
                InterfaceTerminalContainer.TYPE,
                IOPortContainer.TYPE,
                LevelEmitterContainer.TYPE,
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
                StorageBusContainer.TYPE,
                IOBusContainer.EXPORT_TYPE,
                IOBusContainer.IMPORT_TYPE,
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
