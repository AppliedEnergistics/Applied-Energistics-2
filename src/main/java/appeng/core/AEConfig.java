/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.networking.pathing.ChannelMode;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;
import appeng.util.Platform;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public final class AEConfig {

    private final StartupConfig startup = new StartupConfig();
    private final ClientConfig client = new ClientConfig();
    private final CommonConfig common = new CommonConfig();

    // Default Energy Conversion Rates
    private static final double DEFAULT_FE_EXCHANGE = 0.5;

    private static AEConfig instance;

    private AEConfig(ModContainer container) {
        container.registerConfig(ModConfig.Type.STARTUP, startup.spec);
        container.registerConfig(ModConfig.Type.CLIENT, client.spec);
        container.registerConfig(ModConfig.Type.COMMON, common.spec);
    }

    public static void register(ModContainer container) {
        if (!container.getModId().equals(AppEng.MOD_ID)) {
            throw new IllegalArgumentException();
        }
        instance = new AEConfig(container);
    }

    // Misc
    private int formationPlaneEntityLimit;
    private boolean enableEffects;
    private boolean useLargeFonts;
    private boolean useColoredCraftingStatus;
    private boolean disableColoredCableRecipesInRecipeViewer;
    private boolean enableFacadesInRecipeViewer;
    private boolean enableFacadeRecipesInRecipeViewer;
    private int craftingCalculationTimePerTick;
    private boolean craftingSimulatedExtraction;
    private boolean spatialAnchorEnablesRandomTicks;

    // Spatial IO/Dimension
    private double spatialPowerExponent;
    private double spatialPowerMultiplier;

    // Batteries
    private int wirelessTerminalBattery;
    private int entropyManipulatorBattery;
    private int matterCannonBattery;
    private int portableCellBattery;
    private int colorApplicatorBattery;
    private int chargedStaffBattery;

    // Wireless
    private double wirelessBaseCost;
    private double wirelessCostMultiplier;
    private double wirelessTerminalDrainMultiplier;
    private double wirelessBaseRange;
    private double wirelessBoosterRangeMultiplier;
    private double wirelessBoosterExp;
    private double wirelessHighWirelessCount;

    // Tunnels
    public static final double TUNNEL_POWER_LOSS = 0.05;

    public static AEConfig instance() {
        return instance;
    }

    private void syncClientConfig() {
        this.disableColoredCableRecipesInRecipeViewer = client.disableColoredCableRecipesInRecipeViewer.get();
        this.enableFacadesInRecipeViewer = client.enableFacadesInRecipeViewer.get();
        this.enableFacadeRecipesInRecipeViewer = client.enableFacadeRecipesInRecipeViewer.get();
        this.enableEffects = client.enableEffects.get();
        this.useLargeFonts = client.useLargeFonts.get();
        this.useColoredCraftingStatus = client.useColoredCraftingStatus.get();
    }

    private void syncCommonConfig() {
        PowerUnits.FE.conversionRatio = common.powerRatioForgeEnergy.get();
        PowerMultiplier.CONFIG.multiplier = common.powerUsageMultiplier.get();

        CondenserOutput.MATTER_BALLS.requiredPower = common.condenserMatterBallsPower.get();
        CondenserOutput.SINGULARITY.requiredPower = common.condenserSingularityPower.get();

        this.wirelessBaseCost = common.wirelessBaseCost.get();
        this.wirelessCostMultiplier = common.wirelessCostMultiplier.get();
        this.wirelessBaseRange = common.wirelessBaseRange.get();
        this.wirelessBoosterRangeMultiplier = common.wirelessBoosterRangeMultiplier.get();
        this.wirelessBoosterExp = common.wirelessBoosterExp.get();
        this.wirelessHighWirelessCount = common.wirelessHighWirelessCount.get();
        this.wirelessTerminalDrainMultiplier = common.wirelessTerminalDrainMultiplier.get();

        this.formationPlaneEntityLimit = common.formationPlaneEntityLimit.get();

        this.wirelessTerminalBattery = common.wirelessTerminalBattery.get();
        this.chargedStaffBattery = common.chargedStaffBattery.get();
        this.entropyManipulatorBattery = common.entropyManipulatorBattery.get();
        this.portableCellBattery = common.portableCellBattery.get();
        this.colorApplicatorBattery = common.colorApplicatorBattery.get();
        this.matterCannonBattery = common.matterCannonBattery.get();

        for (TickRates tr : TickRates.values()) {
            tr.setMin(common.tickRateMin.get(tr).get());
            tr.setMax(common.tickRateMax.get(tr).get());
        }

        this.spatialPowerMultiplier = common.spatialPowerMultiplier.get();
        this.spatialPowerExponent = common.spatialPowerExponent.get();

        this.craftingCalculationTimePerTick = common.craftingCalculationTimePerTick.get();
        this.craftingSimulatedExtraction = common.craftingSimulatedExtraction.get();
        this.spatialAnchorEnablesRandomTicks = common.spatialAnchorEnableRandomTicks.get();

        AELog.setCraftingLogEnabled(common.craftingLog.get());
        AELog.setDebugLogEnabled(common.debugLog.get());
        AELog.setGridLogEnabled(common.gridLog.get());
    }

    public double wireless_getDrainRate(double range) {
        return this.wirelessTerminalDrainMultiplier * range;
    }

    public double wireless_getMaxRange(int boosters) {
        return this.wirelessBaseRange
               + this.wirelessBoosterRangeMultiplier * Math.pow(boosters, this.wirelessBoosterExp);
    }

    public double wireless_getPowerDrain(int boosters) {
        return this.wirelessBaseCost
               + this.wirelessCostMultiplier * Math.pow(boosters, 1 + boosters / this.wirelessHighWirelessCount);
    }

    public boolean isSearchModNameInTooltips() {
        return client.searchModNameInTooltips.get();
    }

    public void setSearchModNameInTooltips(boolean enable) {
        client.searchModNameInTooltips.set(enable);
    }

    public boolean isUseExternalSearch() {
        return client.useExternalSearch.get();
    }

    public void setUseExternalSearch(boolean enable) {
        client.useExternalSearch.set(enable);
    }

    public boolean isClearExternalSearchOnOpen() {
        return client.clearExternalSearchOnOpen.get();
    }

    public void setClearExternalSearchOnOpen(boolean enable) {
        client.clearExternalSearchOnOpen.set(enable);
    }

    public boolean isRememberLastSearch() {
        return client.rememberLastSearch.get();
    }

    public void setRememberLastSearch(boolean enable) {
        client.rememberLastSearch.set(enable);
    }

    public boolean isAutoFocusSearch() {
        return client.autoFocusSearch.get();
    }

    public void setAutoFocusSearch(boolean enable) {
        client.autoFocusSearch.set(enable);
    }

    public boolean isSyncWithExternalSearch() {
        return client.syncWithExternalSearch.get();
    }

    public void setSyncWithExternalSearch(boolean enable) {
        client.syncWithExternalSearch.set(enable);
    }

    public TerminalStyle getTerminalStyle() {
        return client.terminalStyle.get();
    }

    public void setTerminalStyle(TerminalStyle setting) {
        client.terminalStyle.set(setting);
    }

    public boolean isGuideHotkeyEnabled() {
        return startup.enableGuideHotkey.get();
    }

    public double getGridEnergyStoragePerNode() {
        return common.gridEnergyStoragePerNode.get();
    }

    public double getCrystalResonanceGeneratorRate() {
        return common.crystalResonanceGeneratorRate.get();
    }

    public void reload() {
        syncClientConfig();
        syncCommonConfig();
    }

    public PowerUnits getSelectedPowerUnit() {
        return this.client.selectedPowerUnit.get();
    }

    public void nextPowerUnit(boolean backwards) {
        PowerUnits selectedPowerUnit = EnumCycler.rotateEnum(getSelectedPowerUnit(), backwards,
                Settings.POWER_UNITS.getValues());
        client.selectedPowerUnit.set(selectedPowerUnit);
    }

    // Getters
    public boolean isBlockEntityFacadesEnabled() {
        return common.allowBlockEntityFacades.get();
    }

    public boolean isDebugToolsEnabled() {
        return common.debugTools.get();
    }

    public int getFormationPlaneEntityLimit() {
        return this.formationPlaneEntityLimit;
    }

    public boolean isEnableEffects() {
        return this.enableEffects;
    }

    public boolean isUseLargeFonts() {
        return this.useLargeFonts;
    }

    public boolean isUseColoredCraftingStatus() {
        return this.useColoredCraftingStatus;
    }

    public boolean isDisableColoredCableRecipesInRecipeViewer() {
        return this.disableColoredCableRecipesInRecipeViewer;
    }

    public boolean isEnableFacadesInRecipeViewer() {
        return this.enableFacadesInRecipeViewer;
    }

    public boolean isEnableFacadeRecipesInRecipeViewer() {
        return this.enableFacadeRecipesInRecipeViewer;
    }

    public int getCraftingCalculationTimePerTick() {
        return this.craftingCalculationTimePerTick;
    }

    public boolean isCraftingSimulatedExtraction() {
        return this.craftingSimulatedExtraction;
    }

    public boolean isSpatialAnchorEnablesRandomTicks() {
        return this.spatialAnchorEnablesRandomTicks;
    }

    public double getSpatialPowerExponent() {
        return this.spatialPowerExponent;
    }

    public double getSpatialPowerMultiplier() {
        return this.spatialPowerMultiplier;
    }

    public double getChargerChargeRate() {
        return common.chargerChargeRate.get();
    }

    public DoubleSupplier getWirelessTerminalBattery() {
        return () -> this.wirelessTerminalBattery;
    }

    public DoubleSupplier getEntropyManipulatorBattery() {
        return () -> this.entropyManipulatorBattery;
    }

    public DoubleSupplier getMatterCannonBattery() {
        return () -> this.matterCannonBattery;
    }

    public DoubleSupplier getPortableCellBattery() {
        return () -> this.portableCellBattery;
    }

    public DoubleSupplier getColorApplicatorBattery() {
        return () -> this.colorApplicatorBattery;
    }

    public DoubleSupplier getChargedStaffBattery() {
        return () -> this.chargedStaffBattery;
    }

    public boolean isShowDebugGuiOverlays() {
        return client.debugGuiOverlays.get();
    }

    public void setShowDebugGuiOverlays(boolean enable) {
        client.debugGuiOverlays.set(enable);
    }

    public boolean isSpawnPressesInMeteoritesEnabled() {
        return common.spawnPressesInMeteorites.get();
    }

    public boolean isSpawnFlawlessOnlyEnabled() {
        return common.spawnFlawlessOnly.get();
    }

    public boolean isMatterCanonBlockDamageEnabled() {
        return common.matterCannonBlockDamage.get();
    }

    public boolean isTinyTntBlockDamageEnabled() {
        return common.tinyTntBlockDamage.get();
    }

    public boolean isDisassemblyCraftingEnabled() {
        return common.disassemblyCrafting.get();
    }

    public int getGrowthAcceleratorSpeed() {
        return common.growthAcceleratorSpeed.get();
    }

    public boolean isAnnihilationPlaneSkyDustGenerationEnabled() {
        return common.annihilationPlaneSkyDustGeneration.get();
    }

    public boolean isBlockUpdateLogEnabled() {
        return common.blockUpdateLog.get();
    }

    public boolean isChunkLoggerTraceEnabled() {
        return common.chunkLoggerTrace.get();
    }

    public ChannelMode getChannelMode() {
        return common.channels.get();
    }

    public void setChannelModel(ChannelMode mode) {
        common.channels.set(mode);
    }

    public int getPathfindingStepsPerTick() {
        return common.pathfindingStepsPerTick.get();
    }

    /**
     * @return True if an in-world preview of parts and facade placement should be shown when holding one in hand.
     */
    public boolean isPlacementPreviewEnabled() {
        return client.showPlacementPreview.get();
    }

    public boolean isPortableCellDisassemblyEnabled() {
        return common.portableCellDisassembly.get();
    }

    // Tooltip settings

    /**
     * Show upgrade inventory in tooltips of storage cells and similar devices.
     */
    public boolean isTooltipShowCellUpgrades() {
        return client.tooltipShowCellUpgrades.get();
    }

    /**
     * Show part of the content in tooltips of storage cells and similar devices.
     */
    public boolean isTooltipShowCellContent() {
        return client.tooltipShowCellContent.get();
    }

    /**
     * How much of the content to show in storage cellls and similar devices.
     */
    public int getTooltipMaxCellContentShown() {
        return client.tooltipMaxCellContentShown.get();
    }

    public boolean isPinAutoCraftedItems() {
        return client.pinAutoCraftedItems.get();
    }

    public void setPinAutoCraftedItems(boolean enabled) {
        client.pinAutoCraftedItems.set(enabled);
    }

    public boolean isNotifyForFinishedCraftingJobs() {
        return client.notifyForFinishedCraftingJobs.get();
    }

    public void setNotifyForFinishedCraftingJobs(boolean enabled) {
        client.notifyForFinishedCraftingJobs.set(enabled);
    }

    public boolean isClearGridOnClose() {
        return client.clearGridOnClose.get();
    }

    public void setClearGridOnClose(boolean enabled) {
        client.clearGridOnClose.set(enabled);
    }

    public double getVibrationChamberBaseEnergyPerFuelTick() {
        return common.vibrationChamberBaseEnergyPerFuelTick.get();
    }

    public int getVibrationChamberMinEnergyPerGameTick() {
        return common.vibrationChamberMinEnergyPerTick.get();
    }

    public int getVibrationChamberMaxEnergyPerGameTick() {
        return common.vibrationChamberMaxEnergyPerTick.get();
    }

    public int getTerminalMargin() {
        return client.terminalMargin.get();
    }

    public void save() {
        common.spec.save();
        client.spec.save();
    }

    private static class StartupConfig {
        private final ModConfigSpec spec;

        public final BooleanValue enableGuideHotkey;

        public StartupConfig() {
            var builder = new ModConfigSpec.Builder();
            this.enableGuideHotkey = define(builder, "enableGuideHotkey", true,
                    "Enables the 'hold key to show guide' functionality in tooltips");
            this.spec = builder.build();
        }
    }

    private static class ClientConfig {
        private final ModConfigSpec spec;

        // Misc
        public final BooleanValue enableEffects;
        public final BooleanValue useLargeFonts;
        public final BooleanValue useColoredCraftingStatus;
        public final BooleanValue disableColoredCableRecipesInRecipeViewer;
        public final BooleanValue enableFacadesInRecipeViewer;
        public final BooleanValue enableFacadeRecipesInRecipeViewer;
        public final EnumValue<PowerUnits> selectedPowerUnit;
        public final BooleanValue debugGuiOverlays;
        public final BooleanValue showPlacementPreview;
        public final BooleanValue notifyForFinishedCraftingJobs;

        // Terminal Settings
        public final EnumValue<TerminalStyle> terminalStyle;
        public final BooleanValue pinAutoCraftedItems;
        public final BooleanValue clearGridOnClose;
        public final IntValue terminalMargin;

        // Search Settings
        public final BooleanValue searchModNameInTooltips;
        public final BooleanValue useExternalSearch;
        public final BooleanValue clearExternalSearchOnOpen;
        public final BooleanValue syncWithExternalSearch;
        public final BooleanValue rememberLastSearch;
        public final BooleanValue autoFocusSearch;

        // Tooltip settings
        public final BooleanValue tooltipShowCellUpgrades;
        public final BooleanValue tooltipShowCellContent;
        public final IntValue tooltipMaxCellContentShown;

        public ClientConfig() {
            var builder = new ModConfigSpec.Builder();

            builder.push("client");
            this.disableColoredCableRecipesInRecipeViewer = define(builder, "disableColoredCableRecipesInJEI", true);
            this.enableFacadesInRecipeViewer = define(builder, "enableFacadesInRecipeViewer", false,
                    "Show facades in REI/JEI/EMI item list");
            this.enableFacadeRecipesInRecipeViewer = define(builder, "enableFacadeRecipesInRecipeViewer", true,
                    "Show facade recipes in REI/JEI/EMI for supported blocks");
            this.enableEffects = define(builder, "enableEffects", true);
            this.useLargeFonts = define(builder, "useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = define(builder, "useColoredCraftingStatus", true);
            this.selectedPowerUnit = defineEnum(builder, "PowerUnit", PowerUnits.AE, "Power unit shown in AE UIs");
            this.debugGuiOverlays = define(builder, "showDebugGuiOverlays", false, "Show debugging GUI overlays");
            this.showPlacementPreview = define(builder, "showPlacementPreview", true,
                    "Show a preview of part and facade placement");
            this.notifyForFinishedCraftingJobs = define(builder, "notifyForFinishedCraftingJobs", true,
                    "Show toast when long-running crafting jobs finish.");
            builder.pop();

            var terminals = builder.push("terminals");
            this.terminalStyle = defineEnum(terminals, "terminalStyle", TerminalStyle.SMALL);
            this.pinAutoCraftedItems = define(builder, "pinAutoCraftedItems", true,
                    "Pin items that the player auto-crafts to the top of the terminal");
            this.clearGridOnClose = define(builder, "clearGridOnClose", false,
                    "Automatically clear the crafting/encoding grid when closing the terminal");
            this.terminalMargin = define(builder, "terminalMargin", 25,
                    "The vertical margin to apply when sizing terminals. Used to make room for centered item mod search bars");
            builder.pop();

            // Search Settings
            builder.push("search");
            this.searchModNameInTooltips = define(builder, "searchModNameInTooltips", false,
                    "Should the mod name be included when searching in tooltips.");
            this.useExternalSearch = define(builder, "useExternalSearch", false,
                    "Replaces AEs own search with the search of REI or JEI");
            this.clearExternalSearchOnOpen = define(builder, "clearExternalSearchOnOpen", true,
                    "When using useExternalSearch, clears the search when the terminal opens");
            this.syncWithExternalSearch = define(builder, "syncWithExternalSearch", true,
                    "When REI/JEI is installed, automatically set the AE or REI/JEI search text when either is changed while the terminal is open");
            this.rememberLastSearch = define(builder, "rememberLastSearch", true,
                    "Remembers the last search term and restores it when the terminal opens");
            this.autoFocusSearch = define(builder, "autoFocusSearch", false,
                    "Automatically focuses the search field when the terminal opens");
            builder.pop();

            builder.push("tooltips");
            this.tooltipShowCellUpgrades = define(builder, "showCellUpgrades", true,
                    "Show installed upgrades in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipShowCellContent = define(builder, "showCellContent", true,
                    "Show a preview of the content in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipMaxCellContentShown = define(builder, "maxCellContentShown", 5, 1, 32,
                    "The maximum number of content entries to show in the tooltip of storage cells, color applicators and matter cannons");
            builder.pop();

            this.spec = builder.build();
        }

    }

    private static class CommonConfig {
        private final ModConfigSpec spec;

        // Misc
        public final IntValue formationPlaneEntityLimit;
        public final IntValue craftingCalculationTimePerTick;
        public final BooleanValue craftingSimulatedExtraction;
        public final BooleanValue allowBlockEntityFacades;
        public final BooleanValue debugTools;
        public final BooleanValue matterCannonBlockDamage;
        public final BooleanValue tinyTntBlockDamage;
        public final EnumValue<ChannelMode> channels;
        public final IntValue pathfindingStepsPerTick;
        public final BooleanValue spatialAnchorEnableRandomTicks;

        public final BooleanValue disassemblyCrafting;
        public final IntValue growthAcceleratorSpeed;
        public final BooleanValue annihilationPlaneSkyDustGeneration;

        // Spatial IO/Dimension
        public final DoubleValue spatialPowerExponent;
        public final DoubleValue spatialPowerMultiplier;

        // Logging
        public final BooleanValue blockUpdateLog;
        public final BooleanValue craftingLog;
        public final BooleanValue debugLog;
        public final BooleanValue gridLog;
        public final BooleanValue chunkLoggerTrace;

        // Batteries
        public final DoubleValue chargerChargeRate;
        public final IntValue wirelessTerminalBattery;
        public final IntValue entropyManipulatorBattery;
        public final IntValue matterCannonBattery;
        public final IntValue portableCellBattery;
        public final IntValue colorApplicatorBattery;
        public final IntValue chargedStaffBattery;

        // Meteors
        public final BooleanValue spawnPressesInMeteorites;
        public final BooleanValue spawnFlawlessOnly;

        // Wireless
        public final DoubleValue wirelessBaseCost;
        public final DoubleValue wirelessCostMultiplier;
        public final DoubleValue wirelessTerminalDrainMultiplier;
        public final DoubleValue wirelessBaseRange;
        public final DoubleValue wirelessBoosterRangeMultiplier;
        public final DoubleValue wirelessBoosterExp;
        public final DoubleValue wirelessHighWirelessCount;

        // Portable Cells
        public final BooleanValue portableCellDisassembly;

        // Power Ratios
        public final DoubleValue powerRatioForgeEnergy;
        public final DoubleValue powerUsageMultiplier;
        public final DoubleValue gridEnergyStoragePerNode;
        public final DoubleValue crystalResonanceGeneratorRate;

        // Vibration Chamber
        public final DoubleValue vibrationChamberBaseEnergyPerFuelTick;
        public final IntValue vibrationChamberMinEnergyPerTick;
        public final IntValue vibrationChamberMaxEnergyPerTick;

        // Condenser Power Requirement
        public final IntValue condenserMatterBallsPower;
        public final IntValue condenserSingularityPower;

        public final Map<TickRates, IntValue> tickRateMin = new HashMap<>();
        public final Map<TickRates, IntValue> tickRateMax = new HashMap<>();

        public CommonConfig() {
            var builder = new ModConfigSpec.Builder();

            builder.push("builder");
            debugTools = define(builder, "unsupportedDeveloperTools", Platform.isDevelopmentEnvironment());
            matterCannonBlockDamage = define(builder, "matterCannonBlockDamage", true,
                    "Enables the ability of the Matter Cannon to break blocks.");
            tinyTntBlockDamage = define(builder, "tinyTntBlockDamage", true,
                    "Enables the ability of Tiny TNT to break blocks.");
            channels = defineEnum(builder, "channels", ChannelMode.DEFAULT,
                    "Changes the channel capacity that cables provide in AE2.");
            pathfindingStepsPerTick = define(builder, "pathfindingStepsPerTick", 4,
                    1, 1024,
                    "The number of pathfinding steps that are taken per tick and per grid that is booting. Lower numbers will mean booting takes longer, but less work is done per tick.");
            spatialAnchorEnableRandomTicks = define(builder, "spatialAnchorEnableRandomTicks", true,
                    "Whether Spatial Anchors should force random chunk ticks and entity spawning.");
            builder.pop();

            builder.push("automation");
            formationPlaneEntityLimit = define(builder, "formationPlaneEntityLimit", 128);
            builder.pop();

            builder.push("facades");
            allowBlockEntityFacades = define(builder, "allowBlockEntities", false,
                    "Unsupported: Allows whitelisting block entities as facades. Could work, have render issues, or corrupt your world. USE AT YOUR OWN RISK.");
            builder.pop();

            builder.push("craftingCPU");
            this.craftingCalculationTimePerTick = define(builder, "craftingCalculationTimePerTick", 5);
            this.craftingSimulatedExtraction = define(builder, "craftingSimulatedExtraction", false,
                    "When true: simulate extraction of all the network's contents when starting a crafting job calculation. When false: use the cached available content list (same as terminals). Enabling might work a bit better, but it will significantly reduce performance.");
            builder.pop();

            builder.push("crafting");
            disassemblyCrafting = define(builder, "disassemblyCrafting", true,
                    "Enable shift-clicking with the crafting units in hand to disassemble them.");
            growthAcceleratorSpeed = define(builder, "growthAccelerator", 10, 1, 100,
                    "Number of ticks between two crystal growth accelerator ticks");
            annihilationPlaneSkyDustGeneration = define(builder, "annihilationPlaneSkyDustGeneration", true,
                    "If enabled, an annihilation placed face up at the maximum world height will generate sky stone passively.");
            builder.pop();

            builder.push("spatialio");
            this.spatialPowerMultiplier = define(builder, "spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = define(builder, "spatialPowerExponent", 1.35);
            builder.pop();

            builder.push("logging");
            blockUpdateLog = define(builder, "blockUpdateLog", false);
            craftingLog = define(builder, "craftingLog", false);
            debugLog = define(builder, "debugLog", false);
            gridLog = define(builder, "gridLog", false);
            chunkLoggerTrace = define(builder, "chunkLoggerTrace", false,
                    "Enable stack trace logging for the chunk loading debug command");
            builder.pop();

            builder.push("battery");
            this.chargerChargeRate = define(builder, "chargerChargeRate", 1.0,
                    0.1, 10.0,
                    "The chargers charging rate factor, which is applied to the charged items charge rate. 2 means it charges everything twice as fast. 0.5 half as fast.");
            this.wirelessTerminalBattery = define(builder, "wirelessTerminal", 1600000);
            this.chargedStaffBattery = define(builder, "chargedStaff", 8000);
            this.entropyManipulatorBattery = define(builder, "entropyManipulator", 200000);
            this.portableCellBattery = define(builder, "portableCell", 20000);
            this.colorApplicatorBattery = define(builder, "colorApplicator", 20000);
            this.matterCannonBattery = define(builder, "matterCannon", 200000);
            builder.pop();

            builder.push("worldGen");
            this.spawnPressesInMeteorites = define(builder, "spawnPressesInMeteorites", true);
            this.spawnFlawlessOnly = define(builder, "spawnFlawlessOnly", false);
            builder.pop();

            builder.push("wireless");
            this.wirelessBaseCost = define(builder, "wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = define(builder, "wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = define(builder, "wirelessBaseRange", 16.0);
            this.wirelessBoosterRangeMultiplier = define(builder, "wirelessBoosterRangeMultiplier", 1.0);
            this.wirelessBoosterExp = define(builder, "wirelessBoosterExp", 1.5);
            this.wirelessHighWirelessCount = define(builder, "wirelessHighWirelessCount", 64.0);
            this.wirelessTerminalDrainMultiplier = define(builder, "wirelessTerminalDrainMultiplier", 1.0);
            builder.pop();

            builder.push("PortableCells");
            portableCellDisassembly = define(builder, "allowDisassembly", true,
                    "Allow disassembly of portable cells into the recipe ingredients using shift+right-click");
            builder.pop();

            builder.push("PowerRatios");
            powerRatioForgeEnergy = define(builder, "ForgeEnergy", DEFAULT_FE_EXCHANGE);
            powerUsageMultiplier = define(builder, "UsageMultiplier", 1.0, 0.01, Double.MAX_VALUE);
            gridEnergyStoragePerNode = define(builder, "GridEnergyStoragePerNode", 25.0, 1.0, 1000000.0,
                    "How much energy can the internal grid buffer storage per node attached to the grid.");
            crystalResonanceGeneratorRate = define(builder, "CrystalResonanceGeneratorRate", 20.0, 0.0, 1000000.0,
                    "How much energy a crystal resonance generator generates per tick.");
            builder.pop();

            builder.push("Condenser");
            condenserMatterBallsPower = define(builder, "MatterBalls", 256);
            condenserSingularityPower = define(builder, "Singularity", 256000);
            builder.pop();

            builder.comment(" Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested.");
            builder.push("tickRates");
            for (TickRates tickRate : TickRates.values()) {
                tickRateMin.put(tickRate, define(builder, tickRate.name() + "Min", tickRate.getDefaultMin()));
                tickRateMax.put(tickRate, define(builder, tickRate.name() + "Max", tickRate.getDefaultMax()));
            }
            builder.pop();

            builder.comment("Settings for the Vibration Chamber");
            builder.push("vibrationChamber");
            vibrationChamberBaseEnergyPerFuelTick = define(builder, "baseEnergyPerFuelTick", 5.0, 0.1, 1000.0,
                    "AE energy produced per fuel burn tick (reminder: coal = 1600, block of coal = 16000, lava bucket = 20000 burn ticks)");
            vibrationChamberMinEnergyPerTick = define(builder, "minEnergyPerGameTick", 4, 0, 1000,
                    "Minimum amount of AE/t the vibration chamber can slow down to when energy is being wasted.");
            vibrationChamberMaxEnergyPerTick = define(builder, "baseMaxEnergyPerGameTick", 40, 1, 1000,
                    "Maximum amount of AE/t the vibration chamber can speed up to when generated energy is being fully consumed.");
            builder.pop();

            spec = builder.build();
        }

    }

    private static BooleanValue define(ModConfigSpec.Builder builder, String name, boolean defaultValue, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue);
    }

    private static BooleanValue define(ModConfigSpec.Builder builder, String name, boolean defaultValue) {
        return builder.define(name, defaultValue);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue);
    }

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue) {
        return define(builder, name, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue);
    }

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue, min, max);
    }

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min, double max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue) {
        return define(builder, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static <T extends Enum<T>> EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name, T defaultValue) {
        return builder.defineEnum(name, defaultValue);
    }

    private static <T extends Enum<T>> EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name, T defaultValue, String comment) {
        builder.comment(comment);
        return defineEnum(builder, name, defaultValue);
    }

}
