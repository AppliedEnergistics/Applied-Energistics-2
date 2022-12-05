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

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;

public enum GuiText implements LocalizationEnum {
    inventory(null, "container"), // mc's default Inventory localization.
    AdjacentToDifferentMachines("Adjacent to Different Machines"),
    And("and"),
    Or("or"),
    AttachedTo("Attached to: %s"),
    AutoCrafting("Auto-Crafting"),
    Automatic("Automatic"),
    Black("Black"),
    Blank("Blank"),
    Blue("Blue"),
    Brown("Brown"),
    BytesUsed("%s Bytes Used"),
    CPUs("CPU"),
    CalculatingWait("Calculating Please Wait..."),
    CanBeEnchanted("Can be enchanted"),
    Cancel("Cancel"),
    CantStoreItems("Can't Store Contents!"),
    CellWorkbench("Cell Workbench"),
    CertusQuartzObtain(
            "Certus Quartz is grown by the various Budding Certus Quartz blocks, which can be found in meteors, or crafted from regular Certus Quartz blocks."),
    ChannelEnergyDrain("Channel Passive Drain: %s"),
    Chest("ME Chest"),
    Clean("Clean"),
    CompatibleUpgrade("%s (%s)"),
    CompatibleUpgrades("Compatible Upgrades:"),
    Condenser("Matter Condenser"),
    Config("Config"),
    ConfirmCraftCpuStatus("Storage: %s : Co Processors: %s"),
    ConfirmCraftNoCpu("Storage: N/A : Co Processors: N/A"),
    CopyMode("Copy Mode"),
    CopyModeDesc("Controls if the contents of the configuration pane are cleared when you remove the cell."),
    CraftErrorIncompletePlan("An incomplete plan cannot be submitted."),
    CraftErrorNoCpuFound("There are no crafting CPUs on the network."),
    CraftErrorNoSuitableCpu("None of the crafting CPUs are suitable for this job."),
    CraftErrorNoSuitableCpuOffline("%d offline"),
    CraftErrorNoSuitableCpuBusy("%d busy"),
    CraftErrorNoSuitableCpuTooSmall("%d too small"),
    CraftErrorNoSuitableCpuExcluded("%d excluded"),
    CraftErrorCpuBusy("The selected CPU is already working on another job."),
    CraftErrorCpuOffline("The selected CPU is offline."),
    CraftErrorCpuTooSmall("The selected CPU does not have enough storage."),
    CraftErrorMissingIngredient("Some ingredients could not be extracted from the network."),
    CraftErrorReplan("Replan"),
    CraftErrorRetry("Retry"),
    Crafting("Crafting: %s"),
    CraftingCPUs("Crafting CPUs"),
    CraftingInterface("ME Pattern Provider"),
    CraftingPattern("Crafting Patterns"),
    CraftingPlan("Crafting Plan - %s"),
    CraftingStatus("Crafting Status"),
    CraftingTerminal("Crafting Terminal"),
    Crafts("Crafts"),
    Cyan("Cyan"),
    Deprecated("Deprecated"),
    Drive("ME Drive"),
    ETAFormat("HH:mm:ss"),
    Efficiency("Efficiency: %s%%"),
    Empty("Empty"),
    Encoded("Encoded"),
    EnergyDrain("Passive Drain: %s"),
    EnergyLevelEmitter("ME Energy Level Emitter"),
    Excluded("Excluded"),
    ExportBus("ME Export Bus"),
    ExportBusFluids("ME Fluid Export Bus"),
    ExternalStorage("External Storage (%s)"),
    FacadeCrafting("Facade Crafting"),
    Fluids("Fluids"),
    Fluix("Fluix"),
    FormationPlane("Formation Plane"),
    FromStorage("Available: %s"),
    Fuzzy("Fuzzy"),
    RestoredGenericSettingUpgrades("upgrades"),
    RestoredGenericSettingSettings("settings"),
    RestoredGenericSettingConfigInv("config inventory"),
    RestoredGenericSettingPriority("priority"),
    Gray("Gray"),
    Green("Green"),
    IOBuses("ME Import/Export Bus"),
    IOPort("ME IO Port"),
    ImportBus("ME Import Bus"),
    ImportBusFluids("ME Fluid Import Bus"),
    InWorldCrafting("AE2 In World Crafting"),
    Included("Included"),
    IncreasedEnergyUseFromEnchants("Enchants increase energy use"),
    Inscriber("Inscriber"),
    Installed("Installed: %s"),
    Interface("ME Interface"),
    Interfaces("ME Interfaces"),
    IntrinsicEnchant("Always has at least %s"),
    InvalidNumber("Please enter a number or a mathematical expression e.g. : 3*4"),
    InvalidPattern("Invalid Pattern"),
    Items("Items"),
    // Used in a terminal to indicate that an item is craftable
    LargeFontCraft("+"),
    LevelEmitter("ME Level Emitter"),
    LightBlue("Light Blue"),
    LightGray("Light Gray"),
    Lime("Lime"),
    Linked("Linked"),
    Lumen("Lumen"),
    MENetworkStorage("ME Network Storage"),
    Magenta("Magenta"),
    MaxPower("Max Power: %s"),
    Missing("Missing: %s"),
    MolecularAssembler("Molecular Assembler"),
    MultipleOutputs("%1$d%% second, %2$d%% third output."),
    MysteriousQuote("\"Through others we become ourselves.\""),
    NetworkDetails("Network Details (%d Channels)"),
    NetworkTool("Network Tool"),
    Next("Next"),
    No("No"),
    NoCraftingCPUs("No Crafting CPUs are Available"),
    NoCraftingJobs("No Crafting Job Active"),
    NoPermissions("No Permissions Selected"),
    NoSecondOutput("No Secondary Output"),
    Nothing("Nothing"),
    NotSoMysteriousQuote("\"So far, no matter how close.\""),
    NumberGreaterThanMaxValue("Please enter a number less than or equal to %s"),
    NumberLessThanMinValue("Please enter a number greater than or equal to %s"),
    NumberNonInteger("Must be whole number"),
    OCTunnel("OpenComputers"),
    Of("of"),
    OfSecondOutput("%1$d%% Chance for second output."),
    Orange("Orange"),
    P2PAttunementEnergy("Portable Energy Storage (i.e. Batteries)"),
    P2PAttunementFluid("Portable Fluid Storage (i.e. Tanks, Buckets)"),
    PartialPlan("Partial Plan (Missing Ingredients)"),
    Partitioned("Partitioned"),
    PatternAccessTerminal("Pattern Access Terminal"),
    PatternAccessTerminalHint("Show Or Hide on Pattern Access Terminal."),
    PatternAccessTerminalShort("Pattern A. Terminal"),
    PatternEncoding("Pattern Encoding"),
    Patterns("Patterns"),
    Pink("Pink"),
    PortableCell("Portable Cell"),
    PowerInputRate("Energy Generation: %s"),
    PowerUsageRate("Energy Usage: %s"),
    Precise("Precise"),
    PressureTunnel("Pressure"),
    Priority("Priority"),
    PriorityExtractionHint("Extraction: Lower priority first"),
    PriorityInsertionHint("Insertion: Higher priority first"),
    ProcessingPattern("Processing Patterns"),
    Produces("Produces"),
    Purple("Purple"),
    QuantumLinkChamber("Quantum Link Chamber"),
    QuartzCuttingKnife("Quartz Cutting Knife"),
    Red("Red"),
    RequiredPower("Required Power: %s"),
    ReturnInventory("Return Inventory"),
    SCSInvalid("SCS Size: Invalid"),
    SCSSize("SCS Size: %sx%sx%s"),
    Scheduled("Scheduled: %s"),
    Security("Security Term"),
    SecurityCardEditor("Biometric Card Editor"),
    SelectAmount("Select Amount"),
    SelectedCraftingCPU("Crafting CPU: %s"),
    SerialNumber("Serial Number: %s"),
    Set("Set"),
    ShowingOf("Showing %d of %d"),
    SkyChest("Sky Stone Chest"),
    // Used in a terminal to indicate that an item is craftable
    SmallFontCraft("Craft"),
    SmithingTablePattern("Smithing Table Patterns"),
    SpatialAnchor("Spatial Anchor"),
    SpatialAnchorAll("Spanning: %d chunks in %d worlds"),
    SpatialAnchorAllLoaded("Loading: %d chunks in %d worlds"),
    SpatialAnchorLoadedChunks("Chunks loaded: %s"),
    SpatialAnchorStatistics("Network Statistics"),
    SpatialAnchorUsedPower("Energy Usage: %s"),
    SpatialCapacity("Capacity: %dx%dx%d"),
    SpatialIOPort("Spatial IO Port"),
    Start("Start"),
    StonecuttingPattern("Stonecutting Patterns"),
    StorageBus("Storage Bus"),
    StorageBusFluids("Fluid Storage Bus"),
    StorageCells("ME Storage Cells"),
    WirelessTerminals("Wireless Terminals"),
    PortableCells("Portable Cells"),
    SearchPlaceholder("Search..."),
    SearchSettingsTitle("Search Settings"),
    SearchSettingsUseInternalSearch("Use AE"),
    SearchSettingsUseExternalSearch("Use %s"),
    SearchSettingsSearchTooltips("Search in tooltips"),
    SearchSettingsRememberSearch("Remember last search"),
    SearchSettingsAutoFocus("Auto-Focus on open"),
    SearchSettingsSyncWithExternal("Sync with %s search"),
    SearchSettingsClearExternal("Clear %s search on open"),
    SearchSettingsReplaceWithExternal("Replace with %s search"),
    SearchTooltip("Search in Name"),
    SearchTooltipIncludingTooltips("Search in Name and Tooltip"),
    SearchTooltipModId("Use @ to search by mod (@ae2)"),
    SearchTooltipItemId("Use * to search by id (*cell)"),
    SearchTooltipTag("Use # to search by tag (#ores)"),
    StorageCellTooltipUpgrades("Upgrades:"),
    Stored("Stored"),
    StoredEnergy("Stored Energy"),
    StoredFluids("Stored Fluids"),
    StoredItems("Stored Items"),
    StoredPower("Stored Power: %s"),
    StoredSize("Stored Size: %dx%dx%d"),
    Stores("Stores"),
    Substitute("Using Substitutions:"),
    TankAmount("Amount: %d"),
    TankCapacity("Capacity: %d"),
    TankBucketCapacity("Can Store up to %d Buckets"),
    Terminal("Terminal"),
    TerminalSettingsTitle("Terminal Settings"),
    TerminalSettingsPinAutoCraftedItems("Pin auto-crafted items to first row"),
    TerminalSettingsNotifyForFinishedJobs("Notify about finished crafting jobs (requires wireless terminal)"),
    TerminalSettingsClearGridOnClose("Automatically clear terminal grid on close (if applicable)"),
    TerminalViewCellsTooltip("View Cells"),
    ToastCraftingJobFinishedTitle("Auto-Crafting Complete"),
    ToastCraftingJobFinishedText("%d %s"),
    ToCraft("To Craft: %s"),
    TransparentFacades("Transparent Facades"),
    TransparentFacadesHint("Controls visibility of facades while the network tool is on your toolbar."),
    Types("Types"),
    Unattached("Unattached"),
    Unformatted("Unformatted"),
    Unlinked("Unlinked"),
    UpgradeToolbelt("Upgrade Toolbelt"),
    VibrationChamber("Vibration Chamber"),
    White("White"),
    Wireless("Wireless Access Point"),
    WirelessRange("Range: %s m"),
    WirelessTerminal("Wireless Term"),
    With("with"),
    Yellow("Yellow"),
    Yes("Yes"),
    inWorldCraftingPresses(
            "Crafting Presses are obtained by breaking a Mysterious Cube. Mysterious Cubes are in the center of meteorites which can be found in around the world. They can be located by using a meteorite compass."),
    inWorldSingularity(
            "To create drop 1 Singularity and 1 Ender Dust and cause an explosion within range of the items.");

    private final String root;

    @Nullable
    private final String englishText;

    private final Component text;

    GuiText(@Nullable String englishText) {
        this.root = "gui.ae2";
        this.englishText = englishText;
        this.text = Component.translatable(getTranslationKey());
    }

    GuiText(@Nullable String englishText, String r) {
        this.root = r;
        this.englishText = englishText;
        this.text = Component.translatable(getTranslationKey());
    }

    @Nullable
    public String getEnglishText() {
        return englishText;
    }

    @Override
    public String getTranslationKey() {
        return this.root + '.' + name();
    }

    public String getLocal() {
        return text.getString();
    }
}
