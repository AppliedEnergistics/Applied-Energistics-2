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

public enum ButtonToolTips implements LocalizationEnum {
    Amount("Amount: %d"),
    ActiveOnPulse("Activate once per pulse"),
    ActiveWithSignal("Active with signal"),
    ActiveWithoutSignal("Active without signal"),
    AlwaysActive("Always active"),
    Ascending("Ascending"),
    AutoExport("Auto-Export"),
    AutoExportOff("Produced items will be not be auto-exported."),
    AutoExportOn("Produced items will be auto-exported through any extraction side."),
    BlockPlacement("Block Placement"),
    BlockPlacementNo("Blocks will be dropped as item."),
    BlockPlacementYes("Blocks will be placed as block."),
    Blocking("Do not push crafting ingredients if inventory contains a pattern input."),
    Clear("Clear"),
    ClearSettings("Clear Config/Settings"),
    CondenserOutput("ME Condenser - Output"),
    Craft("Crafting Behavior"),
    CpuStatusCoProcessor("%s Co-Processor"),
    CpuStatusCoProcessors("%s Co-Processors"),
    CpuStatusCraftedIn("Crafted %s in %s"),
    CpuStatusCrafting("Crafting %s"),
    CpuStatusStorage("%s Storage"),
    LockCraftingMode("Lock Crafting"),
    LockCraftingModeNone("Never"),
    LockCraftingWhileRedstoneHigh("With redstone signal"),
    LockCraftingWhileRedstoneLow("Without redstone signal"),
    LockCraftingUntilRedstonePulse("Until redstone pulse is received"),
    LockCraftingUntilResultReturned("Until primary crafting result is returned"),
    DurationFormatDays("%sd"),
    DurationFormatHours("%sh"),
    DurationFormatMinutes("%sm"),
    DurationFormatSeconds("%ss"),
    CraftEither("Use stocked items, or craft items while exporting."),
    CraftOnly("Do not use stocked items, only craft items while exporting."),
    CraftViaRedstone("Emit Redstone to craft item."),
    Craftable("Craftable"),
    CpuSelectionMode("CPU Auto-Selection Mode"),
    CpuSelectionModeAny("For requests by players or automation"),
    CpuSelectionModePlayersOnly("Only for requests by players"),
    CpuSelectionModeAutomationOnly("Only for requests by automation"),
    Descending("Descending"),
    DoesntDespawn("This item won't de-spawn."),
    EmitLevelAbove("Emit when levels are above or equal to limit."),
    EmitLevelsBelow("Emit when levels are below limit."),
    EmitWhenCrafting("Emit Redstone while item is crafting."),
    EmitterMode("Crafting Emitter Mode"),
    Encode("Encode Pattern"),
    EncodeDescription("Write the entered pattern to the current encoded pattern, or to available blank pattern."),
    FZIgnoreAll("Match Any"),
    FZPercent_25("Split Damage at 25%"),
    FZPercent_50("Split Damage at 50%"),
    FZPercent_75("Split Damage at 75%"),
    FZPercent_99("Split Damaged Items"),
    FilterMode("Search BaseFilter Mode"),
    FilterModeClear("Clear on each opening."),
    FilterModeKeep("Restore previous search filter."),
    FilterOnExtract("Filtered Operations"),
    FilterOnExtractDisabled("BaseFilter on insertion only."),
    FilterOnExtractEnabled("BaseFilter on insert and extract."),
    TypeFilter("BaseFilter Types"),
    CycleProcessingOutput("Cycle Outputs"),
    CycleProcessingOutputTooltip("Change the primary output of this pattern"),
    FluidSubstitutions("Fluid Substitutions"),
    FluidSubstitutionsDescDisabled("Don't use fluids."),
    FluidSubstitutionsDescEnabled(
            "Try to use fluids instead of items.\nSubstitutable ingredients are marked in green."),
    FuzzyMode("Fuzzy Comparison"),
    IOMode("Input/Output Mode"),
    InscriberSideness("Automation Access Mode"),
    InscriberSidenessCombined("Automation can access any slot from any side. Input slots cannot be extracted from."),
    InscriberSidenessSeparate(
            "Automation can access slots from specific sides. Top and bottom slots can be extracted from."),
    InterfaceBlockingMode("Blocking Mode"),
    InterfaceCraftingMode("Crafting Mode"),
    InterfaceSetStockAmount("Set amount to stock"),
    InterfaceTerminalDisplayMode("Display Mode"),
    InventoryTweaks("Inventory Tweaks"),
    ItemName("Item name"),
    LevelType("Level Type"),
    LevelType_Energy("Energy"),
    LevelType_Item("Item"),
    MatterBalls("Condense Into Matter Balls\n%s per item"),
    Mod("Mod"),
    MoveWhenEmpty("Move to output when empty."),
    MoveWhenFull("Move to output when full."),
    MoveWhenWorkIsDone("Move to output when work is done."),
    NonBlocking("Ignore the contents of the target inventory."),
    NoSuchMessage("No Such Message"),
    NumberOfItems("Number of items"),
    Off("Off"),
    On("On"),
    OperationMode("Operation Mode"),
    OverlayMode("Overlay Mode"),
    OverlayModeNo("Loaded area is hidden."),
    OverlayModeYes("Shows the loaded area within the world."),
    PartitionStorage("Partition Storage"),
    PartitionStorageHint("Configures Partition based on currently stored items."),
    PowerUnits("Power Units"),
    Read("Extract Only"),
    ReadWrite("Bi-Directional"),
    RedstoneMode("Redstone Mode"),
    ReportInaccessibleFluids("Report Inaccessible Fluids"),
    ReportInaccessibleFluidsNo("No: Only extractable fluids will be visible."),
    ReportInaccessibleFluidsYes("Yes: Fluids that cannot be extracted will be visible."),
    ReportInaccessibleItems("Report Inaccessible Items"),
    ReportInaccessibleItemsNo("No: Only extractable items will be visible."),
    ReportInaccessibleItemsYes("Yes: Items that cannot be extracted will be visible."),
    RequestableAmount("Requestable: %s"),
    SchedulingMode("Scheduling Mode"),
    SchedulingModeDefault("Export the first item until the network is empty, then try the next ones."),
    SchedulingModeRandom("Export items in random mode."),
    SchedulingModeRoundRobin("Export using round robin mode."),
    SearchSettingsTooltip("Show Search Settings"),
    Serial("Serial: %d"),
    Singularity("Condense Into Singularities\n%s per item"),
    ShowAll("Show All"),
    ShowAllProviders("Show all Pattern Providers"),
    ShowNonFullProviders("Show visible Pattern Providers with empty slots"),
    ShowVisibleProviders("Show visible Pattern Providers"),
    ShowFluidsOnly("Show Fluids only"),
    ShowItemsOnly("Show Items only"),
    SortBy("Sort By"),
    SortOrder("Sort Order"),
    Stash("Store Items"),
    StashDesc("Return items on the crafting grid to network storage."),
    StashToPlayer("Take Items"),
    StashToPlayerDesc("Return items on the crafting grid to player inventory."),
    StoredAmount("Stored: %s"),
    StoredCraftable("Stored / Craftable"),
    StoredItems("Stored Items"),
    SubstitutionsDescDisabled("Click to allow alternate items in the recipe."),
    SubstitutionsDescEnabled("Click to prevent alternate items in the recipe."),
    SubstitutionsOff("Substitutions Disabled"),
    SubstitutionsOn("Substitutions Enabled"),
    TerminalSettings("Terminal Settings"),
    TerminalStyle("Terminal Style"),
    TerminalStyle_Small("Small Centered Terminal"),
    TerminalStyle_Medium("Medium Centered Terminal"),
    TerminalStyle_Tall("Tall Centered Terminal"),
    TerminalStyle_Full("Full-Height Terminal"),
    TransferDirection("Transfer Direction"),
    TransferToNetwork("Transfer data to Network"),
    TransferToStorageCell("Transfer data to Storage Cell"),
    Trash("Destroy Items"),
    TrashController("Deletion via Shift / Space Clicking."),
    View("View"),
    Write("Insert Only"),
    CanInsertFrom("Can insert from %s"),
    CanExtractFrom("Can extract from %s"),
    SideTop("top"),
    SideBottom("bottom"),
    SideLeft("left"),
    SideRight("right"),
    SideFront("front"),
    SideBack("back"),
    SideAny("any side"),
    LeftClick("Left-Click"),
    MiddleClick("Middle-Click"),
    RightClick("Right-Click"),
    MouseButton("Mouse %d"),
    StoreAction("%s: Store %s"),
    SetAction("%s: Set %s"),
    ModifyAmountAction("%s: Modify Amount"),
    SupportedBy("Supported by:"),
    ;

    private final String englishText;

    ButtonToolTips(String englishText) {
        this.englishText = englishText;
    }

    @Override
    public String getTranslationKey() {
        return "gui.tooltips.ae2." + name();
    }

    @Override
    public String getEnglishText() {
        return englishText;
    }

}
