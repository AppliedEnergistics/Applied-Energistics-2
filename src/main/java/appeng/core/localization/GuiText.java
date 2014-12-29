/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.core.localization;

import net.minecraft.util.StatCollector;

public enum GuiText
{
	inventory("container"), // mc's default Inventory localization.

	Chest, StoredEnergy, Of, Condenser, Drive, GrindStone, SkyChest,

	VibrationChamber, SpatialIOPort, LevelEmitter, Terminal,

	Interface, Config, StoredItems, Patterns, ImportBus, ExportBus,

	CellWorkbench, NetworkDetails, StorageCells, IOBuses,

	IOPort, BytesUsed, Types, QuantumLinkChamber, PortableCell,

	NetworkTool, PowerUsageRate, PowerInputRate, Installed, EnergyDrain,

	StorageBus, Priority, Security, Encoded, Blank, Unlinked, Linked,

	SecurityCardEditor, NoPermissions, WirelessTerminal, Wireless,

	CraftingTerminal, FormationPlane, Inscriber, QuartzCuttingKnife,

	METunnel, ItemTunnel, RedstoneTunnel, MJTunnel, EUTunnel, FluidTunnel,

	StoredSize, CopyMode, CopyModeDesc, PatternTerminal, CraftingPattern,

	ProcessingPattern, Crafts, Creates, And, With, MolecularAssembler,

	StoredPower, MaxPower, RequiredPower, Efficiency, InWorldCrafting,

	inWorldFluix, inWorldPurificationCertus, inWorldPurificationNether,

	inWorldPurificationFluix, inWorldSingularity, ChargedQuartz, OfSecondOutput,

	NoSecondOutput, RFTunnel, Stores, Next, SelectAmount, Lumen, Empty,

	ConfirmCrafting, Stored, Crafting, Scheduled, CraftingStatus, Cancel,

	FromStorage, ToCraft, CraftingPlan, CalculatingWait, Start, Bytes,

	CraftingCPU, Automatic, CoProcessors, Simulation, Missing,

	InterfaceTerminal, NoCraftingCPUs, LightTunnel, Clean, InvalidPattern,

	InvalidPatternEmpty, InvalidPatternUncraftable, InvalidPatternNoInput,

	InvalidPatternNoOutput, InvalidPatternBadItem,

	InterfaceTerminalHint, Range, TransparentFacades, TransparentFacadesHint,

	NoCraftingJobs, CPUs, FacadeCrafting, inWorldCraftingPresses, ChargedQuartzFind,
	
	Included, Excluded, Partitioned, Precise, Fuzzy;

	final String root;

	GuiText() {
		root = "gui.appliedenergistics2";
	}

	GuiText(String r) {
		root = r;
	}

	public String getUnlocalized()
	{
		return root + '.' + toString();
	}

	public String getLocal()
	{
		return StatCollector.translateToLocal( getUnlocalized() );
	}

}
