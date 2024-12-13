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

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.networking.pathing.ChannelMode;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;
import appeng.util.Platform;

public final class AEConfig {

    private final ClientConfig client = new ClientConfig();
    private final CommonConfig common = new CommonConfig();

    // Default Energy Conversion Rates
    private static final double DEFAULT_FE_EXCHANGE = 0.5;

    private static AEConfig instance;

    private AEConfig(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, client.spec);
        container.registerConfig(ModConfig.Type.COMMON, common.spec);
        container.getEventBus().addListener((ModConfigEvent.Loading evt) -> {
            if (evt.getConfig().getSpec() == common.spec) {
                common.sync();
            }
        });
        container.getEventBus().addListener((ModConfigEvent.Reloading evt) -> {
            if (evt.getConfig().getSpec() == common.spec) {
                common.sync();
            }
        });
    }

    public static void register(ModContainer container) {
        if (!container.getModId().equals(AppEng.MOD_ID)) {
            throw new IllegalArgumentException();
        }
        instance = new AEConfig(container);
    }

    public static AEConfig instance() {
        return instance;
    }

    // Tunnels
    public double getP2PTunnelEnergyTax() {
        return common.p2pTunnelEnergyTax.get();
    }

    public double getP2PTunnelTransportTax() {
        return common.p2pTunnelTransportTax.get();
    }

    public double wireless_getDrainRate(double range) {
        return common.wirelessTerminalDrainMultiplier.get() * range;
    }

    public double wireless_getMaxRange(int boosters) {
        return common.wirelessBaseRange.get()
                + common.wirelessBoosterRangeMultiplier.get() * Math.pow(boosters, common.wirelessBoosterExp.get());
    }

    public double wireless_getPowerDrain(int boosters) {
        return common.wirelessBaseCost.get()
                + common.wirelessCostMultiplier.get()
                        * Math.pow(boosters, 1 + boosters / common.wirelessHighWirelessCount.get());
    }

    public boolean isSearchModNameInTooltips() {
        return client.searchModNameInTooltips.get();
    }

    public void setSearchModNameInTooltips(boolean enable) {
        if (enable != client.searchModNameInTooltips.getAsBoolean()) {
            client.searchModNameInTooltips.set(enable);
            client.spec.save();
        }
    }

    public boolean isUseExternalSearch() {
        return client.useExternalSearch.get();
    }

    public void setUseExternalSearch(boolean enable) {
        if (enable != client.useExternalSearch.getAsBoolean()) {
            client.useExternalSearch.set(enable);
            client.spec.save();
        }
    }

    public boolean isClearExternalSearchOnOpen() {
        return client.clearExternalSearchOnOpen.get();
    }

    public void setClearExternalSearchOnOpen(boolean enable) {
        if (enable != client.clearExternalSearchOnOpen.getAsBoolean()) {
            client.clearExternalSearchOnOpen.set(enable);
            client.spec.save();
        }
    }

    public boolean isRememberLastSearch() {
        return client.rememberLastSearch.get();
    }

    public void setRememberLastSearch(boolean enable) {
        if (enable != client.rememberLastSearch.getAsBoolean()) {
            client.rememberLastSearch.set(enable);
            client.spec.save();
        }
    }

    public boolean isAutoFocusSearch() {
        return client.autoFocusSearch.get();
    }

    public void setAutoFocusSearch(boolean enable) {
        if (enable != client.autoFocusSearch.getAsBoolean()) {
            client.autoFocusSearch.set(enable);
            client.spec.save();
        }
    }

    public boolean isSyncWithExternalSearch() {
        return client.syncWithExternalSearch.get();
    }

    public void setSyncWithExternalSearch(boolean enable) {
        if (enable != client.syncWithExternalSearch.getAsBoolean()) {
            client.syncWithExternalSearch.set(enable);
            client.spec.save();
        }
    }

    public TerminalStyle getTerminalStyle() {
        return client.terminalStyle.get();
    }

    public void setTerminalStyle(TerminalStyle setting) {
        if (setting != client.terminalStyle.get()) {
            client.terminalStyle.set(setting);
            client.spec.save();
        }
    }

    public double getGridEnergyStoragePerNode() {
        return common.gridEnergyStoragePerNode.get();
    }

    public double getCrystalResonanceGeneratorRate() {
        return common.crystalResonanceGeneratorRate.get();
    }

    public PowerUnit getSelectedEnergyUnit() {
        return this.client.selectedPowerUnit.get();
    }

    public void nextEnergyUnit(boolean backwards) {
        var selected = EnumCycler.rotateEnum(getSelectedEnergyUnit(), backwards,
                Settings.POWER_UNITS.getValues());
        client.selectedPowerUnit.set(selected);
        client.spec.save();
    }

    // Getters
    public boolean isDebugToolsEnabled() {
        return common.debugTools.get();
    }

    public int getFormationPlaneEntityLimit() {
        return common.formationPlaneEntityLimit.get();
    }

    public boolean isEnableEffects() {
        return client.enableEffects.getAsBoolean();
    }

    public boolean isUseLargeFonts() {
        return client.useLargeFonts.getAsBoolean();
    }

    public boolean isUseColoredCraftingStatus() {
        return client.useColoredCraftingStatus.getAsBoolean();
    }

    public boolean isDisableColoredCableRecipesInRecipeViewer() {
        return client.disableColoredCableRecipesInRecipeViewer.getAsBoolean();
    }

    public boolean isEnableFacadesInRecipeViewer() {
        return client.enableFacadesInRecipeViewer.getAsBoolean();
    }

    public boolean isEnableFacadeRecipesInRecipeViewer() {
        return client.enableFacadeRecipesInRecipeViewer.getAsBoolean();
    }

    public boolean isExposeNetworkInventoryToEmi() {
        return client.exposeNetworkInventoryToEmi.getAsBoolean();
    }

    public int getCraftingCalculationTimePerTick() {
        return common.craftingCalculationTimePerTick.get();
    }

    public boolean isSpatialAnchorEnablesRandomTicks() {
        return common.spatialAnchorEnableRandomTicks.get();
    }

    public double getSpatialPowerExponent() {
        return common.spatialPowerExponent.get();
    }

    public double getSpatialPowerMultiplier() {
        return common.spatialPowerMultiplier.get();
    }

    public double getChargerChargeRate() {
        return common.chargerChargeRate.get();
    }

    public DoubleSupplier getWirelessTerminalBattery() {
        return common.wirelessTerminalBattery::get;
    }

    public DoubleSupplier getEntropyManipulatorBattery() {
        return common.entropyManipulatorBattery::get;
    }

    public DoubleSupplier getMatterCannonBattery() {
        return common.matterCannonBattery::get;
    }

    public DoubleSupplier getPortableCellBattery() {
        return common.portableCellBattery::get;
    }

    public DoubleSupplier getColorApplicatorBattery() {
        return common.colorApplicatorBattery::get;
    }

    public DoubleSupplier getChargedStaffBattery() {
        return common.chargedStaffBattery::get;
    }

    public boolean isShowDebugGuiOverlays() {
        return client.debugGuiOverlays.get();
    }

    public void setShowDebugGuiOverlays(boolean enable) {
        if (enable != client.debugGuiOverlays.getAsBoolean()) {
            client.debugGuiOverlays.set(enable);
            client.spec.save();
        }
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
        if (mode != common.channels.get()) {
            common.channels.set(mode);
            client.spec.save();
        }
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
        if (enabled != client.pinAutoCraftedItems.getAsBoolean()) {
            client.pinAutoCraftedItems.set(enabled);
            client.spec.save();
        }
    }

    public boolean isNotifyForFinishedCraftingJobs() {
        return client.notifyForFinishedCraftingJobs.get();
    }

    public void setNotifyForFinishedCraftingJobs(boolean enabled) {
        if (enabled != client.notifyForFinishedCraftingJobs.getAsBoolean()) {
            client.notifyForFinishedCraftingJobs.set(enabled);
            client.spec.save();
        }
    }

    public boolean isClearGridOnClose() {
        return client.clearGridOnClose.get();
    }

    public void setClearGridOnClose(boolean enabled) {
        if (enabled != client.clearGridOnClose.getAsBoolean()) {
            client.clearGridOnClose.set(enabled);
            client.spec.save();
        }
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

    private static class ClientConfig {
        private final ModConfigSpec spec;

        // Misc
        public final BooleanValue enableEffects;
        public final BooleanValue useLargeFonts;
        public final BooleanValue useColoredCraftingStatus;
        public final BooleanValue disableColoredCableRecipesInRecipeViewer;
        public final BooleanValue enableFacadesInRecipeViewer;
        public final BooleanValue enableFacadeRecipesInRecipeViewer;
        public final BooleanValue exposeNetworkInventoryToEmi;
        public final EnumValue<PowerUnit> selectedPowerUnit;
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

            builder.push("recipeViewers");
            this.disableColoredCableRecipesInRecipeViewer = define(builder, "disableColoredCableRecipesInRecipeViewer",
                    true);
            this.enableFacadesInRecipeViewer = define(builder, "enableFacadesInRecipeViewer", false,
                    "Show facades in REI/JEI/EMI item list");
            this.enableFacadeRecipesInRecipeViewer = define(builder, "enableFacadeRecipesInRecipeViewer", true,
                    "Show facade recipes in REI/JEI/EMI for supported blocks");
            this.exposeNetworkInventoryToEmi = define(builder, "provideNetworkInventoryToEmi", false,
                    "Expose the full network inventory to EMI, which might cause performance problems.");
            builder.pop();

            builder.push("client");
            this.enableEffects = define(builder, "enableEffects", true);
            this.useLargeFonts = define(builder, "useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = define(builder, "useColoredCraftingStatus", true);
            this.selectedPowerUnit = defineEnum(builder, "powerUnit", PowerUnit.AE, "Unit of power shown in AE UIs");
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
        public final BooleanValue debugTools;
        public final BooleanValue matterCannonBlockDamage;
        public final BooleanValue tinyTntBlockDamage;
        public final EnumValue<ChannelMode> channels;
        public final IntValue pathfindingStepsPerTick;
        public final BooleanValue spatialAnchorEnableRandomTicks;

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

        // Power Ratios
        public final DoubleValue powerRatioForgeEnergy;
        public final DoubleValue powerUsageMultiplier;
        public final DoubleValue gridEnergyStoragePerNode;
        public final DoubleValue crystalResonanceGeneratorRate;
        public final DoubleValue p2pTunnelEnergyTax;
        public final DoubleValue p2pTunnelTransportTax;

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

            builder.push("general");
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

            builder.push("craftingCPU");
            this.craftingCalculationTimePerTick = define(builder, "craftingCalculationTimePerTick", 5);
            builder.pop();

            builder.push("crafting");
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

            builder.push("powerRatios");
            powerRatioForgeEnergy = define(builder, "forgeEnergy", DEFAULT_FE_EXCHANGE);
            powerUsageMultiplier = define(builder, "usageMultiplier", 1.0, 0.01, Double.MAX_VALUE);
            gridEnergyStoragePerNode = define(builder, "gridEnergyStoragePerNode", 25.0, 1.0, 1000000.0,
                    "How much energy can the internal grid buffer storage per node attached to the grid.");
            crystalResonanceGeneratorRate = define(builder, "crystalResonanceGeneratorRate", 20.0, 0.0, 1000000.0,
                    "How much energy a crystal resonance generator generates per tick.");
            p2pTunnelEnergyTax = define(builder, "p2pTunnelEnergyTax", 0.025, 0.0, 1.0,
                    "The cost to transport energy through an energy P2P tunnel expressed as a factor of the transported energy.");
            p2pTunnelTransportTax = define(builder, "p2pTunnelTransportTax", 0.025, 0.0, 1.0,
                    "The cost to transport items/fluids/etc. through P2P tunnels, expressed in AE energy per equivalent I/O bus operation for the transported object type (i.e. items=per 1 item, fluids=per 125mb).");
            builder.pop();

            builder.push("condenser");
            condenserMatterBallsPower = define(builder, "matterBalls", 256);
            condenserSingularityPower = define(builder, "singularity", 256000);
            builder.pop();

            builder.comment(
                    " Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested.");
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

        public void sync() {
            PowerUnit.FE.conversionRatio = powerRatioForgeEnergy.get();
            PowerMultiplier.CONFIG.multiplier = powerUsageMultiplier.get();

            CondenserOutput.MATTER_BALLS.requiredPower = condenserMatterBallsPower.get();
            CondenserOutput.SINGULARITY.requiredPower = condenserSingularityPower.get();

            for (TickRates tr : TickRates.values()) {
                tr.setMin(tickRateMin.get(tr).get());
                tr.setMax(tickRateMax.get(tr).get());
            }

            AELog.setCraftingLogEnabled(craftingLog.get());
            AELog.setDebugLogEnabled(debugLog.get());
            AELog.setGridLogEnabled(gridLog.get());
        }
    }

    private static BooleanValue define(ModConfigSpec.Builder builder, String name, boolean defaultValue,
            String comment) {
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

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min,
            double max, String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue, min, max);
    }

    private static DoubleValue define(ModConfigSpec.Builder builder, String name, double defaultValue, double min,
            double max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max,
            String comment) {
        builder.comment(comment);
        return define(builder, name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue, int min, int max) {
        return builder.defineInRange(name, defaultValue, min, max);
    }

    private static IntValue define(ModConfigSpec.Builder builder, String name, int defaultValue) {
        return define(builder, name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    private static <T extends Enum<T>> EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name,
            T defaultValue) {
        return builder.defineEnum(name, defaultValue);
    }

    private static <T extends Enum<T>> EnumValue<T> defineEnum(ModConfigSpec.Builder builder, String name,
            T defaultValue, String comment) {
        builder.comment(comment);
        return defineEnum(builder, name, defaultValue);
    }

}
