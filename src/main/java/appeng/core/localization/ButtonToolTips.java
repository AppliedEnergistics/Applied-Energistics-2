package appeng.core.localization;

import net.minecraft.util.StatCollector;

public enum ButtonToolTips
{
	PowerUnits, IOMode, CondenserOutput, RedstoneMode, MatchingFuzzy,

	MatchingMode, TransferDirection, SortOrder, SortBy, View,

	PartitionStorage, Clear, FuzzyMode, OperationMode, TrashController,

	InterfaceBlockingMode, InterfaceCraftingMode, Trash, MatterBalls,

	Singularity, Read, Write, ReadWrite, AlwaysActive,

	ActiveWithoutSignal, ActiveWithSignal, ActiveOnPulse,

	EmitLevelsBelow, EmitLevelAbove, MatchingExact, TransferToNetwork,

	TransferToStorageCell, ToggleSortDirection, SearchMode_Auto,

	SearchMode_Standard, SearchMode_NEIAuto, SearchMode_NEIStandard,

	SearchMode, ItemName, NumberOfItems, PartitionStorageHint,

	ClearSettings, StoredItems, StoredCraftable, Craftable,

	FZPercent_25, FZPercent_50, FZPercent_75, FZPercent_99, FZIgnoreAll,

	MoveWhenEmpty, MoveWhenWorkIsDone, MoveWhenFull, Disabled, Enable,

	Blocking, NonBlocking,

	LevelType, LevelType_Energy, LevelType_Item, InventoryTweaks, TerminalStyle, TerminalStyle_Full, TerminalStyle_Tall, TerminalStyle_Small,

	Stash, StashDesc, Encode, EncodeDescription, Substitutions, SubstitutionsOn, SubstitutionsOff, SubstitutionsDesc, CraftOnly, CraftEither,

	Craft, Mod, DoesntDespawn, EmitterMode, CraftViaRedstone, EmitWhenCrafting, ReportInaccessibleItems, ReportInaccessibleItemsYes, ReportInaccessibleItemsNo;

	final String root;

	ButtonToolTips() {
		root = "gui.tooltips.appliedenergistics2";
	}

	ButtonToolTips(String r) {
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
