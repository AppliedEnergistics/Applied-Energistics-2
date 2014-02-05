package appeng.core.localization;

import net.minecraft.util.StatCollector;
import appeng.core.AELog;

public enum GuiText
{
	inventory("container"), // mc's default Inventory localization.

	Chest, StoredEnergy, Of, Condenser, Drive, GrindStone,

	VibrationChamber, SpatialIOPort, LevelEmitter, Terminal,

	Interface, Config, StoredItems, Patterns, ImportBus, ExportBus,

	CellWorkbench, NetworkDetails, StorageCells, IOBuses,

	IOPort, BytesUsed, Types, QuantumLinkChamber, PortableCell,

	NetworkTool, PowerUsageRate, PowerInputRate, Installed, EnergyDrain,

	StorageBus, Priority, Security, Encoded, Blank, Unlinked, Linked,

	SecurityCardEditor, NoPermissions, WirelessTerminal, Wireless,

	CraftingTerminal,

	METunnel, ItemTunnel, RedstoneTunnel, MJTunnel, EUTunnel, FluidTunnel;

	String root;

	GuiText() {
		root = "gui.appliedenergistics2";
		AELog.localization( "gui", getUnlocalized() );
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
