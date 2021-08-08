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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.client.gui.NumberEntryType;
import appeng.core.registries.WorldGenRegistry;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AEConfig {

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    // Default Energy Conversion Rates
    private static final double DEFAULT_RF_EXCHANGE = 0.5;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();

        final Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder()
                .configure(CommonConfig::new);
        COMMON_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    // Config instance
    private static final AEConfig instance = new AEConfig();

    // Misc
    private boolean removeCrashingItemsOnLoad;
    private int formationPlaneEntityLimit;
    private boolean enableEffects;
    private boolean useLargeFonts;
    private boolean useColoredCraftingStatus;
    private int craftingCalculationTimePerTick;
    private PowerUnits selectedPowerUnit = PowerUnits.AE;

    // GUI Buttons
    private final int[] craftByStacks = new int[4];
    private final int[] priorityByStacks = new int[4];
    private final int[] levelByStacks = new int[4];
    private final int[] levelByMillibuckets = { 10, 100, 1000, 10000 };

    // Spatial IO/Dimension
    private double spatialPowerExponent;
    private double spatialPowerMultiplier;
    private boolean spatialBlockTags;

    // Grindstone
    private float oreDoublePercentage;

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

    // FIXME: this is shit, move this concern out of the config class
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            instance.syncClientConfig();
        } else if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            instance.syncCommonConfig();
        }
    }

    private void syncClientConfig() {
        this.enableEffects = CLIENT.enableEffects.get();
        this.useLargeFonts = CLIENT.useLargeFonts.get();
        this.useColoredCraftingStatus = CLIENT.useColoredCraftingStatus.get();
        this.selectedPowerUnit = CLIENT.selectedPowerUnit.get();

        // load buttons..
        for (int btnNum = 0; btnNum < 4; btnNum++) {
            this.craftByStacks[btnNum] = CLIENT.craftByStacks.get(btnNum).get();
            this.priorityByStacks[btnNum] = CLIENT.priorityByStacks.get(btnNum).get();
            this.levelByStacks[btnNum] = CLIENT.levelByStacks.get(btnNum).get();
        }
    }

    private void syncCommonConfig() {
        PowerUnits.RF.conversionRatio = COMMON.powerRatioForgeEnergy.get();
        PowerMultiplier.CONFIG.multiplier = COMMON.powerUsageMultiplier.get();

        CondenserOutput.MATTER_BALLS.requiredPower = COMMON.condenserMatterBallsPower.get();
        CondenserOutput.SINGULARITY.requiredPower = COMMON.condenserSingularityPower.get();

        this.oreDoublePercentage = COMMON.oreDoublePercentage.get().floatValue();

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

        for (final TickRates tr : TickRates.values()) {
            tr.setMin(COMMON.tickRateMin.get(tr).get());
            tr.setMax(COMMON.tickRateMin.get(tr).get());
        }

        this.spatialPowerMultiplier = COMMON.spatialPowerMultiplier.get();
        this.spatialPowerExponent = COMMON.spatialPowerExponent.get();
        this.spatialBlockTags = COMMON.spatialBlockTags.get();

        this.craftingCalculationTimePerTick = COMMON.craftingCalculationTimePerTick.get();

        this.removeCrashingItemsOnLoad = COMMON.removeCrashingItemsOnLoad.get();

        WorldGenRegistry.INSTANCE.setConfigBlacklists(
                COMMON.quartzOresBiomeBlacklist.get().stream().map(ResourceLocation::new)
                        .collect(Collectors.toList()),
                COMMON.meteoriteBiomeBlacklist.get().stream().map(ResourceLocation::new)
                        .collect(Collectors.toList()));
    }

    public static AEConfig instance() {
        return instance;
    }

    public double wireless_getDrainRate(final double range) {
        return this.wirelessTerminalDrainMultiplier * range;
    }

    public double wireless_getMaxRange(final int boosters) {
        return this.wirelessBaseRange
                + this.wirelessBoosterRangeMultiplier * Math.pow(boosters, this.wirelessBoosterExp);
    }

    public double wireless_getPowerDrain(final int boosters) {
        return this.wirelessBaseCost
                + this.wirelessCostMultiplier * Math.pow(boosters, 1 + boosters / this.wirelessHighWirelessCount);
    }

    public YesNo getSearchTooltips() {
        return CLIENT.searchTooltips.get();
    }

    public TerminalStyle getTerminalStyle() {
        return CLIENT.terminalStyle.get();
    }

    public void setTerminalStyle(TerminalStyle setting) {
        CLIENT.terminalStyle.set(setting);
    }

    public SearchBoxMode getTerminalSearchMode() {
        return CLIENT.terminalSearchMode.get();
    }

    public void setTerminalSearchMode(SearchBoxMode setting) {
        CLIENT.terminalSearchMode.set(setting);
    }

    public void save() {
        if (CLIENT_SPEC.isLoaded()) {
            CLIENT.selectedPowerUnit.set(this.selectedPowerUnit);
            CLIENT_SPEC.save();
        }

        if (COMMON_SPEC.isLoaded()) {
            COMMON_SPEC.save();
        }
    }

    /**
     * Returns an array with the quantity-steps for the +/- buttons in number entry dialogs of the given type.
     * Guaranteed to have 4 entries.
     */
    public int[] getNumberEntrySteps(NumberEntryType type) {
        return switch (type) {
            case CRAFT_ITEM_COUNT -> craftByStacks;
            case PRIORITY -> priorityByStacks;
            case LEVEL_ITEM_COUNT -> levelByStacks;
            case LEVEL_FLUID_VOLUME -> levelByMillibuckets;
            default -> throw new IllegalArgumentException("Unknown number entry: " + type);
        };
    }

    public PowerUnits getSelectedPowerUnit() {
        return this.selectedPowerUnit;
    }

    @SuppressWarnings("unchecked")
    public void nextPowerUnit(final boolean backwards) {
        this.selectedPowerUnit = EnumCycler.rotateEnum(this.selectedPowerUnit, backwards,
                (EnumSet<PowerUnits>) Settings.POWER_UNITS.getPossibleValues());
        this.save();
    }

    // Getters
    public boolean isRemoveCrashingItemsOnLoad() {
        return this.removeCrashingItemsOnLoad;
    }

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
        return CLIENT.disableColoredCableRecipesInJEI.get();
    }

    public boolean isShowFacadesInJEIEnabled() {
        return CLIENT.showFacadesInJEI.get();
    }

    public int getCraftingCalculationTimePerTick() {
        return this.craftingCalculationTimePerTick;
    }

    public double getSpatialPowerExponent() {
        return this.spatialPowerExponent;
    }

    public double getSpatialPowerMultiplier() {
        return this.spatialPowerMultiplier;
    }

    public boolean getSpatialBlockTags() {
        return this.spatialBlockTags;
    }

    public float getOreDoublePercentage() {
        return this.oreDoublePercentage;
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

    public float getSpawnChargedChance() {
        return COMMON.spawnChargedChance.get().floatValue();
    }

    public int getQuartzOresPerCluster() {
        return COMMON.quartzOresPerCluster.get();
    }

    public int getQuartzOresClusterAmount() {
        return COMMON.quartzOresClusterAmount.get();
    }

    @Nullable
    public String getImprovedFluidTag() {
        return Strings.emptyToNull(COMMON.improvedFluidTag.get());
    }

    public float getImprovedFluidMultiplier() {
        return COMMON.improvedFluidMultiplier.get().floatValue();
    }

    public boolean isShowDebugGuiOverlays() {
        return CLIENT.debugGuiOverlays.get();
    }

    public boolean isGenerateMeteorites() {
        return COMMON.generateMeteorites.get();
    }

    public boolean isSpawnPressesInMeteoritesEnabled() {
        return COMMON.spawnPressesInMeteorites.get();
    }

    public boolean isGenerateQuartzOre() {
        return COMMON.generateQuartzOre.get();
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

    public boolean isInWorldFluixEnabled() {
        return COMMON.inWorldFluix.get();
    }

    public boolean isInWorldPurificationEnabled() {
        return COMMON.inWorldPurification.get();
    }

    public boolean isDisassemblyCraftingEnabled() {
        return COMMON.disassemblyCrafting.get();
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

    public boolean isCraftingLogEnabled() {
        return COMMON.craftingLog.get();
    }

    public boolean isDebugLogEnabled() {
        return COMMON.debugLog.get();
    }

    public boolean isChunkLoggerTraceEnabled() {
        return COMMON.chunkLoggerTrace.get();
    }

    // Setters keep visibility as low as possible.

    private static class ClientConfig {

        // Misc
        public final BooleanValue enableEffects;
        public final BooleanValue useLargeFonts;
        public final BooleanValue useColoredCraftingStatus;
        public final BooleanValue disableColoredCableRecipesInJEI;
        public final BooleanValue showFacadesInJEI;
        public final EnumValue<PowerUnits> selectedPowerUnit;
        public final BooleanValue debugGuiOverlays;

        // GUI Buttons
        private static final int[] BTN_BY_STACK_DEFAULTS = { 1, 10, 100, 1000 };
        public final List<ConfigValue<Integer>> craftByStacks;
        public final List<ConfigValue<Integer>> priorityByStacks;
        public final List<ConfigValue<Integer>> levelByStacks;

        // Terminal Settings
        public final EnumValue<YesNo> searchTooltips;
        public final EnumValue<TerminalStyle> terminalStyle;
        public final EnumValue<SearchBoxMode> terminalSearchMode;

        public ClientConfig(ForgeConfigSpec.Builder builder) {
            builder.push("client");
            this.disableColoredCableRecipesInJEI = builder.comment("TODO").define("disableColoredCableRecipesInJEI",
                    true);
            this.showFacadesInJEI = builder.define("showFacadesInJEI", true);
            this.enableEffects = builder.comment("TODO").define("enableEffects", true);
            this.useLargeFonts = builder.comment("TODO").define("useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = builder.comment("TODO").define("useColoredCraftingStatus", true);
            this.selectedPowerUnit = builder.comment("Power unit shown in AE UIs").defineEnum("PowerUnit",
                    PowerUnits.AE, PowerUnits.values());
            this.debugGuiOverlays = builder.comment("Show debugging GUI overlays")
                    .define("showDebugGuiOverlays", false);

            this.craftByStacks = new ArrayList<>(4);
            this.priorityByStacks = new ArrayList<>(4);
            this.levelByStacks = new ArrayList<>(4);
            // load buttons..
            for (int btnNum = 0; btnNum < 4; btnNum++) {
                int defaultValue = BTN_BY_STACK_DEFAULTS[btnNum];
                final int buttonCap = (int) (Math.pow(10, btnNum + 1) - 1);

                this.craftByStacks.add(builder.comment("Controls buttons on Crafting Screen")
                        .defineInRange("craftByStacks" + btnNum, defaultValue, 1, buttonCap));
                this.priorityByStacks.add(builder.comment("Controls buttons on Priority Screen")
                        .defineInRange("priorityByStacks" + btnNum, defaultValue, 1, buttonCap));
                this.levelByStacks.add(builder.comment("Controls buttons on Level Emitter Screen")
                        .defineInRange("levelByStacks" + btnNum, defaultValue, 1, buttonCap));
            }

            builder.pop();

            builder.push("terminals");
            this.searchTooltips = builder.comment("Should tooltips be searched. Performance impact")
                    .defineEnum("searchTooltips", YesNo.YES, YesNo.values());
            this.terminalStyle = builder.defineEnum("terminalStyle", TerminalStyle.TALL, TerminalStyle.values());
            this.terminalSearchMode = builder.defineEnum("terminalSearchMode", SearchBoxMode.AUTOSEARCH,
                    SearchBoxMode.values());
            builder.pop();
        }

    }

    private static class CommonConfig {

        // Misc
        public final BooleanValue removeCrashingItemsOnLoad;
        public final ConfigValue<Integer> formationPlaneEntityLimit;
        public final ConfigValue<Integer> craftingCalculationTimePerTick;
        public final BooleanValue allowBlockEntityFacades;
        public final BooleanValue debugTools;
        public final BooleanValue matterCannonBlockDamage;
        public final BooleanValue tinyTntBlockDamage;

        // Crafting
        public final BooleanValue inWorldSingularity;
        public final BooleanValue inWorldFluix;
        public final BooleanValue inWorldPurification;
        public final BooleanValue disassemblyCrafting;

        // Spatial IO/Dimension
        public final ConfigValue<Double> spatialPowerExponent;
        public final ConfigValue<Double> spatialPowerMultiplier;
        public final ConfigValue<Boolean> spatialBlockTags;

        // Logging
        public final BooleanValue securityAuditLog;
        public final BooleanValue blockUpdateLog;
        public final BooleanValue packetLog;
        public final BooleanValue craftingLog;
        public final BooleanValue debugLog;
        public final BooleanValue chunkLoggerTrace;

        // Grindstone
        public final DoubleValue oreDoublePercentage;

        // Batteries
        public final ConfigValue<Integer> wirelessTerminalBattery;
        public final ConfigValue<Integer> entropyManipulatorBattery;
        public final ConfigValue<Integer> matterCannonBattery;
        public final ConfigValue<Integer> portableCellBattery;
        public final ConfigValue<Integer> colorApplicatorBattery;
        public final ConfigValue<Integer> chargedStaffBattery;

        // Certus quartz
        public final DoubleValue spawnChargedChance;
        public final ConfigValue<Integer> quartzOresPerCluster;
        public final ConfigValue<Integer> quartzOresClusterAmount;
        public final BooleanValue generateQuartzOre;
        public final ConfigValue<List<? extends String>> quartzOresBiomeBlacklist;

        // Meteors
        public final BooleanValue generateMeteorites;
        public final BooleanValue spawnPressesInMeteorites;
        public final ConfigValue<List<? extends String>> meteoriteBiomeBlacklist;

        // Wireless
        public final ConfigValue<Double> wirelessBaseCost;
        public final ConfigValue<Double> wirelessCostMultiplier;
        public final ConfigValue<Double> wirelessTerminalDrainMultiplier;
        public final ConfigValue<Double> wirelessBaseRange;
        public final ConfigValue<Double> wirelessBoosterRangeMultiplier;
        public final ConfigValue<Double> wirelessBoosterExp;
        public final ConfigValue<Double> wirelessHighWirelessCount;

        // Power Ratios
        public final ConfigValue<Double> powerRatioForgeEnergy;
        public final DoubleValue powerUsageMultiplier;

        // Condenser Power Requirement
        public final ConfigValue<Integer> condenserMatterBallsPower;
        public final ConfigValue<Integer> condenserSingularityPower;

        // In-World Purification
        // Settings for improved speed depending on fluid the crystal is in
        public final ConfigValue<String> improvedFluidTag;
        public final ConfigValue<Double> improvedFluidMultiplier;

        public final Map<TickRates, ConfigValue<Integer>> tickRateMin = new HashMap<>();
        public final Map<TickRates, ConfigValue<Integer>> tickRateMax = new HashMap<>();

        public CommonConfig(ForgeConfigSpec.Builder builder) {

            builder.push("general");
            removeCrashingItemsOnLoad = builder.comment(
                    "Will auto-remove items that crash when being loaded from storage. This will destroy those items instead of crashing the game!")
                    .define("removeCrashingItemsOnLoad", false);
            debugTools = builder.define("unsupportedDeveloperTools", false);
            matterCannonBlockDamage = builder
                    .comment("Enables the ability of the Matter Cannon to break blocks.")
                    .define("matterCannonBlockDamage", true);
            tinyTntBlockDamage = builder
                    .comment("Enables the ability of Tiny TNT to break blocks.")
                    .define("tinyTntBlockDamage", true);
            builder.pop();

            builder.push("automation");
            formationPlaneEntityLimit = builder.comment("TODO").define("formationPlaneEntityLimit", 128);
            builder.pop();

            builder.push("facades");
            allowBlockEntityFacades = builder.comment(
                    "Unsupported: Allows whitelisting block entities as facades. Could work, have render issues, or corrupt your world. USE AT YOUR OWN RISK.")
                    .define("allowBlockEntities", false);
            builder.pop();

            builder.push("craftingCPU");

            this.craftingCalculationTimePerTick = builder.define("craftingCalculationTimePerTick", 5);

            builder.pop();

            builder.push("crafting");
            inWorldSingularity = builder.comment("Enable the in-world crafting of singularities.")
                    .define("inWorldSingularity", true);
            inWorldFluix = builder.comment("Enable the in-world crafting of fluix crystals.").define("inWorldFluix",
                    true);
            inWorldPurification = builder.comment("Enable the in-world crafting of pure crystals.")
                    .define("inWorldPurification", true);
            disassemblyCrafting = builder
                    .comment("Enable the recipes for disassembling storage cells and crafting units.")
                    .define("disassemblyCrafting", true);
            builder.pop();

            builder.push("spatialio");
            this.spatialPowerMultiplier = builder.define("spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = builder.define("spatialPowerExponent", 1.35);
            this.spatialBlockTags = builder
                    .comment("BE CAREFUL, CAN CORRUPT YOUR WORLD! Will use #spatial/whitelist as whitelist.")
                    .define("spatialBlockTags", false);
            builder.pop();

            builder.push("logging");
            securityAuditLog = builder.define("securityAuditLog", false);
            blockUpdateLog = builder.define("blockUpdateLog", false);
            packetLog = builder.define("packetLog", false);
            craftingLog = builder.define("craftingLog", false);
            debugLog = builder.define("debugLog", false);
            chunkLoggerTrace = builder.comment("Enable stack trace logging for the chunk loading debug command")
                    .define("chunkLoggerTrace", false);
            builder.pop();

            builder.push("GrindStone");
            this.oreDoublePercentage = builder.comment("Chance to actually get an output with stacksize > 1.")
                    .defineInRange("oreDoublePercentage", 90.0, 0.0, 100.0);
            builder.pop();

            builder.push("battery");
            this.wirelessTerminalBattery = builder.define("wirelessTerminal", 1600000);
            this.chargedStaffBattery = builder.define("chargedStaff", 8000);
            this.entropyManipulatorBattery = builder.define("entropyManipulator", 200000);
            this.portableCellBattery = builder.define("portableCell", 20000);
            this.colorApplicatorBattery = builder.define("colorApplicator", 20000);
            this.matterCannonBattery = builder.define("matterCannon", 200000);
            builder.pop();

            builder.push("worldGen");

            this.spawnChargedChance = builder.defineInRange("spawnChargedChance", 0.08, 0.0, 1.0);
            this.generateMeteorites = builder.define("generateMeteorites", true);
            this.meteoriteBiomeBlacklist = builder.defineList("meteoriteBiomeBlacklist",
                    Collections.emptyList(),
                    obj -> obj instanceof String && ResourceLocation.isValidResourceLocation((String) obj));
            this.spawnPressesInMeteorites = builder.define("spawnPressesInMeteorites", true);

            this.generateQuartzOre = builder.define("generateQuartzOre", true);
            this.quartzOresPerCluster = builder.define("quartzOresPerCluster", 4);
            this.quartzOresClusterAmount = builder.define("quartzOresClusterAmount", 20);
            this.quartzOresBiomeBlacklist = builder.defineList("quartzOresBiomeBlacklist",
                    Collections.emptyList(),
                    obj -> obj instanceof String && ResourceLocation.isValidResourceLocation((String) obj));

            builder.pop();

            builder.push("wireless");
            this.wirelessBaseCost = builder.define("wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = builder.define("wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = builder.define("wirelessBaseRange", 16.0);
            this.wirelessBoosterRangeMultiplier = builder.define("wirelessBoosterRangeMultiplier", 1.0);
            this.wirelessBoosterExp = builder.define("wirelessBoosterExp", 1.5);
            this.wirelessHighWirelessCount = builder.define("wirelessHighWirelessCount", 64.0);
            this.wirelessTerminalDrainMultiplier = builder.define("wirelessTerminalDrainMultiplier", 1.0);
            builder.pop();

            builder.push("PowerRatios");
            powerRatioForgeEnergy = builder.define("ForgeEnergy", DEFAULT_RF_EXCHANGE);
            powerUsageMultiplier = builder.defineInRange("UsageMultiplier", 1.0, 0.01, Double.MAX_VALUE);
            builder.pop();

            builder.push("Condenser");
            condenserMatterBallsPower = builder.define("MatterBalls", 256);
            condenserSingularityPower = builder.define("Singularity", 256000);
            builder.pop();

            builder.comment(
                    " Min / Max Tickrates for dynamic ticking, most of these components also use sleeping, to prevent constant ticking, adjust with care, non standard rates are not supported or tested.")
                    .push("tickRates");
            for (TickRates tickRate : TickRates.values()) {
                tickRateMin.put(tickRate, builder.define(tickRate.name() + "Min", tickRate.getDefaultMin()));
                tickRateMax.put(tickRate, builder.define(tickRate.name() + "Max", tickRate.getDefaultMax()));
            }
            builder.pop();

            builder.comment("Settings for in-world purification of crystals.").push("inWorldPurification");

            improvedFluidTag = builder.comment(
                    "A fluid tag that identifies fluids that improve crystal purification speed. Does not affect purification with water/lava.")
                    .define("improvedFluidTag", "");
            improvedFluidMultiplier = builder
                    .comment("The speed multiplier to use when the crystals are submerged in the improved fluid.")
                    .defineInRange("improvedFluidMultiplier", 2.0, 1.0, 10.0);

            builder.pop();
        }

    }

}
