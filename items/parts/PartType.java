package appeng.items.parts;

import java.util.EnumSet;

import appeng.api.parts.IPart;
import appeng.api.util.AEColor;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartCableAnchor;
import appeng.parts.misc.PartInterface;
import appeng.parts.misc.PartInvertedToggleBus;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.misc.PartToggleBus;
import appeng.parts.networking.PartCableCovered;
import appeng.parts.networking.PartCableGlass;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartDenseCable;
import appeng.parts.networking.PartQuartzFiber;
import appeng.parts.p2p.PartP2PBCPower;
import appeng.parts.p2p.PartP2PIC2Power;
import appeng.parts.p2p.PartP2PItems;
import appeng.parts.p2p.PartP2PLiquids;
import appeng.parts.p2p.PartP2PRFPower;
import appeng.parts.p2p.PartP2PRedstone;
import appeng.parts.p2p.PartP2PTunnelME;
import appeng.parts.reporting.PartConversionMonitor;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartDarkMonitor;
import appeng.parts.reporting.PartMonitor;
import appeng.parts.reporting.PartPatternTerminal;
import appeng.parts.reporting.PartSemiDarkMonitor;
import appeng.parts.reporting.PartStorageMonitor;
import appeng.parts.reporting.PartTerminal;

public enum PartType
{
	ToggleBus(AEFeature.Core, PartToggleBus.class),

	InvertedToggleBus(AEFeature.Core, PartInvertedToggleBus.class),

	CableSmart(AEFeature.Core, PartCableSmart.class), CableCovered(AEFeature.Core, PartCableCovered.class), CableGlass(AEFeature.Core, PartCableGlass.class),

	CableDense(AEFeature.DenseCables, PartDenseCable.class),

	CableAnchor(AEFeature.Core, PartCableAnchor.class),

	QuartzFiber(AEFeature.Core, PartQuartzFiber.class),

	Monitor(AEFeature.Core, PartMonitor.class),

	SemiDarkMonitor(AEFeature.Core, PartSemiDarkMonitor.class),

	DarkMonitor(AEFeature.Core, PartDarkMonitor.class),

	StorageBus(AEFeature.StorageBus, PartStorageBus.class),

	ImportBus(AEFeature.ImportBus, PartImportBus.class),

	ExportBus(AEFeature.ExportBus, PartExportBus.class),

	LevelEmitter(AEFeature.LevelEmiter, PartLevelEmitter.class),

	AnnihilationPlane(AEFeature.AnnihilationPlane, PartAnnihilationPlane.class),

	FormationPlane(AEFeature.FormationPlane, PartFormationPlane.class),

	P2PTunnelME(AEFeature.P2PTunnelME, PartP2PTunnelME.class, GuiText.METunnel),

	P2PTunnelRedstone(AEFeature.P2PTunnelRedstone, PartP2PRedstone.class, GuiText.RedstoneTunnel),

	P2PTunnelItems(AEFeature.P2PTunnelItems, PartP2PItems.class, GuiText.ItemTunnel),

	P2PTunnelLiquids(AEFeature.P2PTunnelLiquids, PartP2PLiquids.class, GuiText.FluidTunnel),

	P2PTunnelMJ(AEFeature.P2PTunnelMJ, PartP2PBCPower.class, GuiText.MJTunnel),

	P2PTunnelEU(AEFeature.P2PTunnelEU, PartP2PIC2Power.class, GuiText.EUTunnel),

	// CraftingMonitor(AEFeature.Crafting, PartCraftingMonitor.class),

	PatternTerminal(AEFeature.CraftingTerminal, PartPatternTerminal.class),

	CraftingTerminal(AEFeature.CraftingTerminal, PartCraftingTerminal.class),

	Terminal(AEFeature.Core, PartTerminal.class),

	StorageMonitor(AEFeature.StorageMonitor, PartStorageMonitor.class),

	ConversionMonitor(AEFeature.PartConversionMonitor, PartConversionMonitor.class),

	Interface(AEFeature.Core, PartInterface.class),

	P2PTunnelRF(AEFeature.P2PTunnelRF, PartP2PRFPower.class, GuiText.RFTunnel);

	private final EnumSet<AEFeature> features;
	private final Class<? extends IPart> myPart;
	private final GuiText extraName;

	PartType(AEFeature part, Class<? extends IPart> c) {
		this( part, c, null );
	}

	PartType(AEFeature part, Class<? extends IPart> c, GuiText en) {
		features = EnumSet.of( part );
		myPart = c;
		extraName = en;
	}

	public Enum[] getVarients()
	{
		return (this == CableSmart || this == CableCovered || this == CableGlass || this == CableDense) ? AEColor.values() : null;
	}

	public EnumSet<AEFeature> getFeature()
	{
		return features;
	}

	public Class<? extends IPart> getPart()
	{
		return myPart;
	}

	public GuiText getExtraName()
	{
		return extraName;
	}

}
