package appeng.items.parts;

import java.util.EnumSet;

import appeng.api.parts.IPart;
import appeng.core.features.AEFeature;
import appeng.parts.automation.PartAnnihilationPlane;
import appeng.parts.automation.PartExportBus;
import appeng.parts.automation.PartFormationPlane;
import appeng.parts.automation.PartImportBus;
import appeng.parts.automation.PartLevelEmitter;
import appeng.parts.misc.PartCableAnchor;
import appeng.parts.misc.PartInvertedToggleBus;
import appeng.parts.misc.PartP2PTunnel;
import appeng.parts.misc.PartStorageBus;
import appeng.parts.misc.PartToggleBus;
import appeng.parts.networking.PartCableCovered;
import appeng.parts.networking.PartCableGlass;
import appeng.parts.networking.PartCableSmart;
import appeng.parts.networking.PartQuartzFiber;
import appeng.parts.reporting.PartConversionMonitor;
import appeng.parts.reporting.PartCraftingMonitor;
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

	CableAnchor(AEFeature.Core, PartCableAnchor.class),

	QuartzFiber(AEFeature.Core, PartQuartzFiber.class),

	Monitior(AEFeature.Core, PartMonitor.class),

	SemiDarkMonitor(AEFeature.Core, PartSemiDarkMonitor.class),

	DarkMonitor(AEFeature.Core, PartDarkMonitor.class),

	StorageBus(AEFeature.StorageBus, PartStorageBus.class),

	ImportBus(AEFeature.ImportBus, PartImportBus.class),

	ExportBus(AEFeature.ExportBus, PartExportBus.class),

	LevelEmitter(AEFeature.LevelEmiter, PartLevelEmitter.class),

	AnnihilationPlane(AEFeature.AnnihilationPlane, PartAnnihilationPlane.class),

	FormationPlane(AEFeature.FormationPlane, PartFormationPlane.class),

	P2PTunnel(AEFeature.P2PTunnel, PartP2PTunnel.class),

	CraftingMonitor(AEFeature.Crafting, PartCraftingMonitor.class),

	PatternTerminal(AEFeature.CraftingTerminal, PartPatternTerminal.class),

	CraftingTerminal(AEFeature.CraftingTerminal, PartCraftingTerminal.class),

	Terminal(AEFeature.Core, PartTerminal.class),

	StorageMonitor(AEFeature.StorageMonitor, PartStorageMonitor.class),

	ConversionMonitor(AEFeature.PartConversionMonitor, PartConversionMonitor.class);

	private EnumSet<AEFeature> features;
	private Class<? extends IPart> myPart;

	PartType(AEFeature part, Class<? extends IPart> c) {
		features = EnumSet.of( part );
		myPart = c;
	}

	public EnumSet<AEFeature> getFeature()
	{
		return features;
	}

	public Class<? extends IPart> getPart()
	{
		return myPart;
	}

}
