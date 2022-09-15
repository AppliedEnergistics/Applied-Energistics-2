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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

import javax.annotation.Nullable;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.networking.pathing.ChannelMode;
import appeng.core.config.BooleanOption;
import appeng.core.config.ConfigFileManager;
import appeng.core.config.ConfigSection;
import appeng.core.config.ConfigValidationException;
import appeng.core.config.DoubleOption;
import appeng.core.config.EnumOption;
import appeng.core.config.IntegerOption;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;

public final class AEConfig {

    public static final String CLIENT_CONFIG_PATH = "ae2/client.json";
    public static final String COMMON_CONFIG_PATH = "ae2/common.json";
    public final ClientConfig CLIENT;
    public final ConfigFileManager clientConfigManager;
    public final CommonConfig COMMON;
    public final ConfigFileManager commonConfigManager;

    AEConfig(Path configDir) {
        ConfigSection clientRoot = ConfigSection.createRoot();
        CLIENT = new ClientConfig(clientRoot);

        if (configDir != null) { // Might be null when running from a test...
            clientConfigManager = createConfigFileManager(clientRoot, configDir, CLIENT_CONFIG_PATH);
        } else {
            clientConfigManager = null;
        }

        ConfigSection commonRoot = ConfigSection.createRoot();
        COMMON = new CommonConfig(commonRoot);

        if (configDir != null) {
            commonConfigManager = createConfigFileManager(commonRoot, configDir, COMMON_CONFIG_PATH);
        } else {
            commonConfigManager = null;
        }

        syncClientConfig();
        syncCommonConfig();
    }

    private static ConfigFileManager createConfigFileManager(ConfigSection commonRoot, Path configDir,
            String filename) {
        var configFile = configDir.resolve(filename);
        ConfigFileManager result = new ConfigFileManager(commonRoot, configFile);
        if (!Files.exists(configFile)) {
            result.save(); // Save a default file
        } else {
            try {
                result.load();
            } catch (ConfigValidationException e) {
                AELog.error("Failed to load AE2 Config. Making backup", e);

                // Backup and delete config files to reset them
                makeBackupAndReset(configDir, filename);
            }

            // Re-save immediately to write-out new defaults
            try {
                result.save();
            } catch (Exception e) {
                AELog.warn(e);
            }
        }
        return result;
    }

    // Default Energy Conversion Rates
    private static final double DEFAULT_FE_EXCHANGE = 0.5;

    // Config instance
    private static AEConfig instance;

    public static void load(Path configFolder) {
        if (instance != null) {
            throw new IllegalStateException("Config is already loaded");
        }
        instance = new AEConfig(configFolder);
    }

    private static void makeBackupAndReset(Path configFolder, String configFile) {
        var backupFile = configFolder.resolve(configFile + ".bak");
        var originalFile = configFolder.resolve(configFile);
        try {
            Files.move(originalFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            AELog.warn("Failed to backup config file %s: %s!", originalFile, e);
        }
    }

    // Misc
    private int formationPlaneEntityLimit;
    private boolean enableEffects;
    private boolean useLargeFonts;
    private boolean useColoredCraftingStatus;
    private boolean disableColoredCableRecipesInJEI;
    private boolean isEnableFacadesInJEI;
    private boolean isEnableFacadeRecipesInJEI;
    private int craftingCalculationTimePerTick;
    private boolean craftingSimulatedExtraction;

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

    @Nullable
    private TagKey<Fluid> improvedFluidTagKey;

    private void syncClientConfig() {
        this.disableColoredCableRecipesInJEI = CLIENT.disableColoredCableRecipesInJEI.get();
        this.isEnableFacadesInJEI = CLIENT.enableFacadesInJEI.get();
        this.isEnableFacadeRecipesInJEI = CLIENT.enableFacadeRecipesInJEI.get();
        this.enableEffects = CLIENT.enableEffects.get();
        this.useLargeFonts = CLIENT.useLargeFonts.get();
        this.useColoredCraftingStatus = CLIENT.useColoredCraftingStatus.get();
    }

    private void syncCommonConfig() {
        PowerUnits.RF.conversionRatio = COMMON.powerRatioForgeEnergy.get();
        PowerMultiplier.CONFIG.multiplier = COMMON.powerUsageMultiplier.get();

        CondenserOutput.MATTER_BALLS.requiredPower = COMMON.condenserMatterBallsPower.get();
        CondenserOutput.SINGULARITY.requiredPower = COMMON.condenserSingularityPower.get();

        this.wirelessBaseCost = COMMON.wirelessBaseCost.get();
        this.wirelessCostMultiplier = COMMON.wirelessCostMultiplier.get();
        this.wirelessBaseRange = COMMON.wirelessBaseRange.get();
        this.wirelessBoosterRangeMultiplier = COMMON.wirelessBoosterRangeMultiplier.get();
        this.wirelessBoosterExp = COMMON.wirelessBoosterExp.get();
        this.wirelessHighWirelessCount = COMMON.wirelessHighWirelessCount.get();
        this.wirelessTerminalDrainMultiplier = COMMON.wirelessTerminalDrainMultiplier.get();

        this.formationPlaneEntityLimit = COMMON.formationPlaneEntityLimit.get();

        this.wirelessTerminalBattery = COMMON.wirelessTerminalBattery.get();
        this.chargedStaffBattery = COMMON.chargedStaffBattery.get();
        this.entropyManipulatorBattery = COMMON.entropyManipulatorBattery.get();
        this.portableCellBattery = COMMON.portableCellBattery.get();
        this.colorApplicatorBattery = COMMON.colorApplicatorBattery.get();
        this.matterCannonBattery = COMMON.matterCannonBattery.get();

        for (TickRates tr : TickRates.values()) {
            tr.setMin(COMMON.tickRateMin.get(tr).get());
            tr.setMax(COMMON.tickRateMax.get(tr).get());
        }

        this.spatialPowerMultiplier = COMMON.spatialPowerMultiplier.get();
        this.spatialPowerExponent = COMMON.spatialPowerExponent.get();

        this.craftingCalculationTimePerTick = COMMON.craftingCalculationTimePerTick.get();
        this.craftingSimulatedExtraction = COMMON.craftingSimulatedExtraction.get();

        AELog.setCraftingLogEnabled(COMMON.craftingLog.get());
        AELog.setDebugLogEnabled(COMMON.debugLog.get());
        AELog.setGridLogEnabled(COMMON.gridLog.get());
    }

    public static AEConfig instance() {
        return instance;
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

    public boolean isSearchTooltips() {
        return CLIENT.searchTooltips.get();
    }

    public void setSearchTooltips(boolean enable) {
        CLIENT.searchTooltips.set(enable);
    }

    public boolean isSearchModNameInTooltips() {
        return CLIENT.searchModNameInTooltips.get();
    }

    public void setSearchModNameInTooltips(boolean enable) {
        CLIENT.searchModNameInTooltips.set(enable);
    }

    public boolean isUseExternalSearch() {
        return CLIENT.useExternalSearch.get();
    }

    public void setUseExternalSearch(boolean enable) {
        CLIENT.useExternalSearch.set(enable);
    }

    public boolean isClearExternalSearchOnOpen() {
        return CLIENT.clearExternalSearchOnOpen.get();
    }

    public void setClearExternalSearchOnOpen(boolean enable) {
        CLIENT.clearExternalSearchOnOpen.set(enable);
    }

    public boolean isRememberLastSearch() {
        return CLIENT.rememberLastSearch.get();
    }

    public void setRememberLastSearch(boolean enable) {
        CLIENT.rememberLastSearch.set(enable);
    }

    public boolean isAutoFocusSearch() {
        return CLIENT.autoFocusSearch.get();
    }

    public void setAutoFocusSearch(boolean enable) {
        CLIENT.autoFocusSearch.set(enable);
    }

    public boolean isSyncWithExternalSearch() {
        return CLIENT.syncWithExternalSearch.get();
    }

    public void setSyncWithExternalSearch(boolean enable) {
        CLIENT.syncWithExternalSearch.set(enable);
    }

    public TerminalStyle getTerminalStyle() {
        return CLIENT.terminalStyle.get();
    }

    public void setTerminalStyle(TerminalStyle setting) {
        CLIENT.terminalStyle.set(setting);
    }

    public void save() {
    }

    public PowerUnits getSelectedPowerUnit() {
        return this.CLIENT.selectedPowerUnit.get();
    }

    public void nextPowerUnit(boolean backwards) {
        PowerUnits selectedPowerUnit = EnumCycler.rotateEnum(getSelectedPowerUnit(), backwards,
                Settings.POWER_UNITS.getValues());
        CLIENT.selectedPowerUnit.set(selectedPowerUnit);
    }

    // Getters
    public boolean isBlockEntityFacadesEnabled() {
        return COMMON.allowBlockEntityFacades.get();
    }

    public boolean isDebugToolsEnabled() {
        return COMMON.debugTools.get();
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

    public boolean isDisableColoredCableRecipesInJEI() {
        return this.disableColoredCableRecipesInJEI;
    }

    public boolean isEnableFacadesInJEI() {
        return this.isEnableFacadesInJEI;
    }

    public boolean isEnableFacadeRecipesInJEI() {
        return this.isEnableFacadeRecipesInJEI;
    }

    public int getCraftingCalculationTimePerTick() {
        return this.craftingCalculationTimePerTick;
    }

    public boolean isCraftingSimulatedExtraction() {
        return this.craftingSimulatedExtraction;
    }

    public double getSpatialPowerExponent() {
        return this.spatialPowerExponent;
    }

    public double getSpatialPowerMultiplier() {
        return this.spatialPowerMultiplier;
    }

    public double getChargerChargeRate() {
        return COMMON.chargerChargeRate.get();
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
        return CLIENT.debugGuiOverlays.get();
    }

    public boolean isSpawnPressesInMeteoritesEnabled() {
        return COMMON.spawnPressesInMeteorites.get();
    }

    public boolean isGenerateMeteorites() {
        return COMMON.generateMeteorites.get();
    }

    public boolean isMatterCanonBlockDamageEnabled() {
        return COMMON.matterCannonBlockDamage.get();
    }

    public boolean isTinyTntBlockDamageEnabled() {
        return COMMON.tinyTntBlockDamage.get();
    }

    public boolean isInWorldSingularityEnabled() {
        return COMMON.inWorldSingularity.get();
    }

    public boolean isDisassemblyCraftingEnabled() {
        return COMMON.disassemblyCrafting.get();
    }

    public int getGrowthAcceleratorSpeed() {
        return COMMON.growthAcceleratorSpeed.get();
    }

    public boolean isSecurityAuditLogEnabled() {
        return COMMON.securityAuditLog.get();
    }

    public boolean isBlockUpdateLogEnabled() {
        return COMMON.blockUpdateLog.get();
    }

    public boolean isPacketLogEnabled() {
        return COMMON.packetLog.get();
    }

    public boolean isChunkLoggerTraceEnabled() {
        return COMMON.chunkLoggerTrace.get();
    }

    public boolean serverOpsIgnoreSecurity() {
        return COMMON.serverOpsIgnoreSecurity.get();
    }

    public ChannelMode getChannelMode() {
        return COMMON.channels.get();
    }

    public void setChannelModel(ChannelMode mode) {
        COMMON.channels.set(mode);
    }

    public int getPathfindingStepsPerTick() {
        return COMMON.pathfindingStepsPerTick.get();
    }

    /**
     * @return True if an in-world preview of parts and facade placement should be shown when holding one in hand.
     */
    public boolean isPlacementPreviewEnabled() {
        return CLIENT.showPlacementPreview.get();
    }

    public boolean isPortableCellDisassemblyEnabled() {
        return COMMON.portableCellDisassembly.get();
    }

    // Tooltip settings

    /**
     * Show upgrade inventory in tooltips of storage cells and similar devices.
     */
    public boolean isTooltipShowCellUpgrades() {
        return CLIENT.tooltipShowCellUpgrades.get();
    }

    /**
     * Show part of the content in tooltips of storage cells and similar devices.
     */
    public boolean isTooltipShowCellContent() {
        return CLIENT.tooltipShowCellContent.get();
    }

    /**
     * How much of the content to show in storage cellls and similar devices.
     */
    public int getTooltipMaxCellContentShown() {
        return CLIENT.tooltipMaxCellContentShown.get();
    }

    public boolean isPinAutoCraftedItems() {
        return CLIENT.pinAutoCraftedItems.get();
    }

    public void setPinAutoCraftedItems(boolean enabled) {
        CLIENT.pinAutoCraftedItems.set(enabled);
    }

    public boolean isNotifyForFinishedCraftingJobs() {
        return CLIENT.notifyForFinishedCraftingJobs.get();
    }

    public void setNotifyForFinishedCraftingJobs(boolean enabled) {
        CLIENT.notifyForFinishedCraftingJobs.set(enabled);
    }

    public boolean isClearGridOnClose() {
        return CLIENT.clearGridOnClose.get();
    }

    public void setClearGridOnClose(boolean enabled) {
        CLIENT.clearGridOnClose.set(enabled);
    }

    // Setters keep visibility as low as possible.

    private static class ClientConfig {

        // Misc
        public final BooleanOption enableEffects;
        public final BooleanOption useLargeFonts;
        public final BooleanOption useColoredCraftingStatus;
        public final BooleanOption disableColoredCableRecipesInJEI;
        public final BooleanOption enableFacadesInJEI;
        public final BooleanOption enableFacadeRecipesInJEI;
        public final EnumOption<PowerUnits> selectedPowerUnit;
        public final BooleanOption debugGuiOverlays;
        public final BooleanOption showPlacementPreview;
        public final BooleanOption notifyForFinishedCraftingJobs;

        // Terminal Settings
        public final EnumOption<TerminalStyle> terminalStyle;
        public final BooleanOption pinAutoCraftedItems;
        public final BooleanOption clearGridOnClose;

        // Search Settings
        public final BooleanOption searchTooltips;
        public final BooleanOption searchModNameInTooltips;
        public final BooleanOption useExternalSearch;
        public final BooleanOption clearExternalSearchOnOpen;
        public final BooleanOption syncWithExternalSearch;
        public final BooleanOption rememberLastSearch;
        public final BooleanOption autoFocusSearch;

        // Tooltip settings
        public final BooleanOption tooltipShowCellUpgrades;
        public final BooleanOption tooltipShowCellContent;
        public final IntegerOption tooltipMaxCellContentShown;

        public ClientConfig(ConfigSection root) {
            var client = root.subsection("client");
            this.disableColoredCableRecipesInJEI = client.addBoolean("disableColoredCableRecipesInJEI", true);
            this.enableFacadesInJEI = client.addBoolean("enableFacadesInJEI", true,
                    "Show facades in JEI ingredient list");
            this.enableFacadeRecipesInJEI = client.addBoolean("enableFacadeRecipesInJEI", true,
                    "Show facade recipes in JEI for supported blocks");
            this.enableEffects = client.addBoolean("enableEffects", true);
            this.useLargeFonts = client.addBoolean("useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = client.addBoolean("useColoredCraftingStatus", true);
            this.selectedPowerUnit = client.addEnum("PowerUnit", PowerUnits.AE, "Power unit shown in AE UIs");
            this.debugGuiOverlays = client.addBoolean("showDebugGuiOverlays", false, "Show debugging GUI overlays");
            this.showPlacementPreview = client.addBoolean("showPlacementPreview", true,
                    "Show a preview of part and facade placement");
            this.notifyForFinishedCraftingJobs = client.addBoolean("notifyForFinishedCraftingJobs", true,
                    "Show toast when long-running crafting jobs finish.");

            var terminals = root.subsection("terminals");
            this.terminalStyle = terminals.addEnum("terminalStyle", TerminalStyle.TALL);
            this.pinAutoCraftedItems = terminals.addBoolean("pinAutoCraftedItems", true,
                    "Pin items that the player auto-crafts to the top of the terminal");
            this.clearGridOnClose = client.addBoolean("clearGridOnClose", false,
                    "Automatically clear the crafting/encoding grid when closing the terminal");

            // Search Settings
            var search = root.subsection("search");
            this.searchTooltips = search.addBoolean("searchTooltips", true,
                    "Should tooltips be searched. Performance impact");
            this.searchModNameInTooltips = search.addBoolean("searchModNameInTooltips", false,
                    "Should the mod name be included when searching in tooltips.");
            this.useExternalSearch = search.addBoolean("useExternalSearch", false,
                    "Replaces AEs own search with the search of REI or JEI");
            this.clearExternalSearchOnOpen = search.addBoolean("clearExternalSearchOnOpen", true,
                    "When using useExternalSearch, clears the search when the terminal opens");
            this.syncWithExternalSearch = search.addBoolean("syncWithExternalSearch", true,
                    "When REI/JEI is installed, automatically set the AE or REI/JEI search text when either is changed while the terminal is open");
            this.rememberLastSearch = search.addBoolean("rememberLastSearch", true,
                    "Remembers the last search term and restores it when the terminal opens");
            this.autoFocusSearch = search.addBoolean("autoFocusSearch", false,
                    "Automatically focuses the search field when the terminal opens");

            var tooltips = root.subsection("tooltips");
            this.tooltipShowCellUpgrades = tooltips.addBoolean("showCellUpgrades", true,
                    "Show installed upgrades in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipShowCellContent = tooltips.addBoolean("showCellContent", true,
                    "Show a preview of the content in the tooltips of storage cells, color applicators and matter cannons");
            this.tooltipMaxCellContentShown = tooltips.addInt("maxCellContentShown", 5, 1, 32,
                    "The maximum number of content entries to show in the tooltip of storage cells, color applicators and matter cannons");
        }

    }

    private static class CommonConfig {

        // Misc
        public final IntegerOption formationPlaneEntityLimit;
        public final IntegerOption craftingCalculationTimePerTick;
        public final BooleanOption craftingSimulatedExtraction;
        public final BooleanOption allowBlockEntityFacades;
        public final BooleanOption debugTools;
        public final BooleanOption matterCannonBlockDamage;
        public final BooleanOption tinyTntBlockDamage;
        public final BooleanOption serverOpsIgnoreSecurity;
        public final EnumOption<ChannelMode> channels;
        public final IntegerOption pathfindingStepsPerTick;

        // Crafting
        public final BooleanOption inWorldSingularity;
        public final BooleanOption disassemblyCrafting;
        public final IntegerOption growthAcceleratorSpeed;

        // Spatial IO/Dimension
        public final DoubleOption spatialPowerExponent;
        public final DoubleOption spatialPowerMultiplier;

        // Logging
        public final BooleanOption securityAuditLog;
        public final BooleanOption blockUpdateLog;
        public final BooleanOption packetLog;
        public final BooleanOption craftingLog;
        public final BooleanOption debugLog;
        public final BooleanOption gridLog;
        public final BooleanOption chunkLoggerTrace;

        // Batteries
        public final DoubleOption chargerChargeRate;
        public final IntegerOption wirelessTerminalBattery;
        public final IntegerOption entropyManipulatorBattery;
        public final IntegerOption matterCannonBattery;
        public final IntegerOption portableCellBattery;
        public final IntegerOption colorApplicatorBattery;
        public final IntegerOption chargedStaffBattery;

        public final BooleanOption generateMeteorites;

        // Meteors
        public final BooleanOption spawnPressesInMeteorites;

        // Wireless
        public final DoubleOption wirelessBaseCost;
        public final DoubleOption wirelessCostMultiplier;
        public final DoubleOption wirelessTerminalDrainMultiplier;
        public final DoubleOption wirelessBaseRange;
        public final DoubleOption wirelessBoosterRangeMultiplier;
        public final DoubleOption wirelessBoosterExp;
        public final DoubleOption wirelessHighWirelessCount;

        // Portable Cells
        public final BooleanOption portableCellDisassembly;

        // Power Ratios
        public final DoubleOption powerRatioForgeEnergy;
        public final DoubleOption powerUsageMultiplier;

        // Condenser Power Requirement
        public final IntegerOption condenserMatterBallsPower;
        public final IntegerOption condenserSingularityPower;

        public final Map<TickRates, IntegerOption> tickRateMin = new HashMap<>();
        public final Map<TickRates, IntegerOption> tickRateMax = new HashMap<>();

        public CommonConfig(ConfigSection root) {

            ConfigSection general = root.subsection("general");
            debugTools = general.addBoolean("unsupportedDeveloperTools", false);
            matterCannonBlockDamage = general.addBoolean("matterCannonBlockDamage", true,
                    "Enables the ability of the Matter Cannon to break blocks.");
            tinyTntBlockDamage = general.addBoolean("tinyTntBlockDamage", true,
                    "Enables the ability of Tiny TNT to break blocks.");
            serverOpsIgnoreSecurity = general.addBoolean("serverOpsIgnoreSecurity", true,
                    "Server operators are not restricted by ME security terminal settings.");
            channels = general.addEnum("channels", ChannelMode.DEFAULT,
                    "Changes the channel capacity that cables provide in AE2.");
            pathfindingStepsPerTick = general.addInt("pathfindingStepsPerTick", 4,
                    1, 1024,
                    "The number of pathfinding steps that are taken per tick and per grid that is booting. Lower numbers will mean booting takes longer, but less work is done per tick.");

            ConfigSection automation = root.subsection("automation");
            formationPlaneEntityLimit = automation.addInt("formationPlaneEntityLimit", 128);

            ConfigSection facades = root.subsection("facades");
            allowBlockEntityFacades = facades.addBoolean("allowBlockEntities", false,
                    "Unsupported: Allows whitelisting block entities as facades. Could work, have render issues, or corrupt your world. USE AT YOUR OWN RISK.");

            ConfigSection craftingCPU = root.subsection("craftingCPU");
            this.craftingCalculationTimePerTick = craftingCPU.addInt("craftingCalculationTimePerTick", 5);
            this.craftingSimulatedExtraction = craftingCPU.addBoolean("craftingSimulatedExtraction", false,
                    "When true: simulate extraction of all the network's contents when starting a crafting job calculation. When false: use the cached available content list (same as terminals). Enabling might work a bit better, but it will significantly reduce performance.");

            var crafting = root.subsection("crafting");
            inWorldSingularity = crafting.addBoolean("inWorldSingularity", true,
                    "Enable the in-world crafting of singularities.");
            disassemblyCrafting = crafting.addBoolean("disassemblyCrafting", true,
                    "Enable shift-clicking with the crafting units in hand to disassemble them.");
            growthAcceleratorSpeed = crafting.addInt("growthAccelerator", 10, 1, 100,
                    "Number of ticks between two crystal growth accelerator ticks");

            ConfigSection spatialio = root.subsection("spatialio");
            this.spatialPowerMultiplier = spatialio.addDouble("spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = spatialio.addDouble("spatialPowerExponent", 1.35);

            var logging = root.subsection("logging");
            securityAuditLog = logging.addBoolean("securityAuditLog", false);
            blockUpdateLog = logging.addBoolean("blockUpdateLog", false);
            packetLog = logging.addBoolean("packetLog", false);
            craftingLog = logging.addBoolean("craftingLog", false);
            debugLog = logging.addBoolean("debugLog", false);
            gridLog = logging.addBoolean("gridLog", false);
            chunkLoggerTrace = logging.addBoolean("chunkLoggerTrace", false,
                    "Enable stack trace logging for the chunk loading debug command");

            ConfigSection battery = root.subsection("battery");
            this.chargerChargeRate = battery.addDouble("chargerChargeRate", 1,
                    0.1, 10,
                    "The chargers charging rate factor, which is applied to the charged items charge rate. 2 means it charges everything twice as fast. 0.5 half as fast.");
            this.wirelessTerminalBattery = battery.addInt("wirelessTerminal", 1600000);
            this.chargedStaffBattery = battery.addInt("chargedStaff", 8000);
            this.entropyManipulatorBattery = battery.addInt("entropyManipulator", 200000);
            this.portableCellBattery = battery.addInt("portableCell", 20000);
            this.colorApplicatorBattery = battery.addInt("colorApplicator", 20000);
            this.matterCannonBattery = battery.addInt("matterCannon", 200000);

            ConfigSection worldGen = root.subsection("worldGen");

            this.spawnPressesInMeteorites = worldGen.addBoolean("spawnPressesInMeteorites", true);

            this.generateMeteorites = worldGen.addBoolean("generateMeteorites", true);

            ConfigSection wireless = root.subsection("wireless");
            this.wirelessBaseCost = wireless.addDouble("wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = wireless.addDouble("wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = wireless.addDouble("wirelessBaseRange", 16.0);
            this.wirelessBoosterRangeMultiplier = wireless.addDouble("wirelessBoosterRangeMultiplier", 1.0);
            this.wirelessBoosterExp = wireless.addDouble("wirelessBoosterExp", 1.5);
            this.wirelessHighWirelessCount = wireless.addDouble("wirelessHighWirelessCount", 64.0);
            this.wirelessTerminalDrainMultiplier = wireless.addDouble("wirelessTerminalDrainMultiplier", 1.0);

            ConfigSection portableCells = root.subsection("PortableCells");
            portableCellDisassembly = portableCells.addBoolean("allowDisassembly", true,
                    "Allow disassembly of portable cells into the recipe ingredients using shift+right-click");

            ConfigSection PowerRatios = root.subsection("PowerRatios");
            powerRatioForgeEnergy = PowerRatios.addDouble("ForgeEnergy", DEFAULT_FE_EXCHANGE);
            powerUsageMultiplier = PowerRatios.addDouble("UsageMultiplier", 1.0, 0.01, Double.MAX_VALUE);

            ConfigSection Condenser = root.subsection("Condenser");
            condenserMatterBallsPower = Condenser.addInt("MatterBalls", 256);
            condenserSingularityPower = Condenser.addInt("Singularity", 256000);

            ConfigSection tickrates = root.subsection("tickRates",
                    " Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested.");
            for (TickRates tickRate : TickRates.values()) {
                tickRateMin.put(tickRate, tickrates.addInt(tickRate.name() + "Min", tickRate.getDefaultMin()));
                tickRateMax.put(tickRate, tickrates.addInt(tickRate.name() + "Max", tickRate.getDefaultMax()));
            }
        }

    }

}
