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

	InterfaceTerminalHint, Range, TransparentFacades, TransparentFacadesHint,

	NoCraftingJobs, CPUs, FacadeCrafting, inWorldCraftingPresses, ChargedQuartzFind,
	
	Included, Excluded, Partitioned, Precise, Fuzzy;

	String root;

	GuiText() {
		root = "gui.appliedenergistics2";
	}

	GuiText(String r) {
		root = r;
	}

	public String getUnlocalized()
	{
		return root + "." + toString();
	}

	public String getLocal()
	{
		return StatCollector.translateToLocal( getUnlocalized() );
	}

}
