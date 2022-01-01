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

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public enum ButtonToolTips {
    Amount("Amount: %d"),
    ActiveOnPulse("Activate once per pulse"),
    ActiveWithSignal("Active with signal"),
    ActiveWithoutSignal("Active without signal"),
    AlwaysActive("Always active"),
    Ascending("Ascending"),
    BlockPlacement("Block Placement"),
    BlockPlacementNo("Blocks will be dropped as item."),
    BlockPlacementYes("Blocks will be placed as block."),
    Blocking("Do not push crafting ingredients if inventory contains a pattern input."),
    Clear("Clear"),
    ClearSettings("Clear Config/Settings"),
    CondenserOutput("ME Condenser - Output"),
    Craft("Crafting Behavior"),
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
    FZPercent_99("Split Damage at 99%"),
    FilterMode("Search Filter Mode"),
    FilterModeClear("Clear on each opening."),
    FilterModeKeep("Restore previous search filter."),
    FilterOnExtract("Filtered Operations"),
    FilterOnExtractDisabled("Filter on insertion only."),
    FilterOnExtractEnabled("Filter on insert and extract."),
    TypeFilter("Filter Types"),
    FluidSubstitutions("Fluid Substitutions"),
    FluidSubstitutionsDescDisabled("Don't use fluids."),
    FluidSubstitutionsDescEnabled(
            "Try to use fluids instead of items.\nSubstitutable ingredients are marked in green."),
    FuzzyMode("Fuzzy Comparison"),
    IOMode("Input/Output Mode"),
    InterfaceBlockingMode("Blocking Mode"),
    InterfaceCraftingMode("Crafting Mode"),
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
    NumberOfItems("Number of items"),
    Off("Off"),
    On("On"),
    OperationMode("Operation Mode"),
    OverlayMode("Overlay Mode"),
    OverlayModeNo("Loaded area is hidden."),
    OverlayModeYes("Shows the loaded area within the world."),
    P2PFrequency("Frequency: %s"),
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
    SearchMode("Search Settings"),
    SearchMode_AutoFocus("Auto-Focus: %s"),
    SearchMode_JEI("Use JEI Search"),
    SearchMode_REI("Use REI Search"),
    SearchMode_RememberSearch("Remember Search: %s"),
    Singularity("Condense Into Singularities\n%s per item"),
    ShowAll("Show All"),
    ShowFluidsOnly("Show Fluids only"),
    ShowItemsOnly("Show Items only"),
    SortBy("Sort By"),
    SortOrder("Sort Order"),
    Stash("Store Items"),
    StashDesc("Return items on the crafting grid to network storage."),
    StoredAmount("Stored: %s"),
    StoredCraftable("Stored / Craftable"),
    StoredItems("Stored Items"),
    Substitutions("Item Substitutions"),
    SubstitutionsDescDisabled("Prevent using alternate items allowed by the recipe for crafting."),
    SubstitutionsDescEnabled("Allow using alternate items allowed by the recipe for crafting."),
    SubstitutionsOff("Substitutions Disabled"),
    SubstitutionsOn("Substitutions Enabled"),
    TerminalStyle("Terminal Style"),
    TerminalStyle_Full("Full Screen Terminal"),
    TerminalStyle_Small("Small Centered Terminal"),
    TerminalStyle_Tall("Tall Centered Terminal"),
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
    LeftClick("Left-Click"),
    MiddleClick("Middle-Click"),
    RightClick("Right-Click"),
    StoreAction("%s: Store %s"),
    SetAction("%s: Set %s"),
    ModifyAmountAction("%s: Modify Amount"),
    ;

    private final TranslatableComponent text;

    private final String englishText;

    ButtonToolTips(String englishText) {
        this.text = new TranslatableComponent(getTranslationKey());
        this.englishText = englishText;
    }

    public String getTranslationKey() {
        return "gui.tooltips.ae2." + name();
    }

    public String getEnglishText() {
        return englishText;
    }

    public Component text() {
        return text;
    }

    public MutableComponent text(Object... args) {
        return new TranslatableComponent(text.getKey(), args);
    }

}
