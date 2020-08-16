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
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.features.AEFeature;
import appeng.client.gui.NumberEntryType;
import appeng.core.config.BooleanOption;
import appeng.core.config.ConfigFileManager;
import appeng.core.config.ConfigSection;
import appeng.core.config.DoubleOption;
import appeng.core.config.EnumOption;
import appeng.core.config.IntegerOption;
import appeng.core.config.StringListOption;
import appeng.core.settings.TickRates;
import appeng.util.EnumCycler;
import net.minecraft.world.dimension.DimensionType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

public final class AEConfig {

    public final ClientConfig clientConfig;
    public final ConfigFileManager clientConfigManager;
    public final CommonConfig commonConfig;
    public final ConfigFileManager commonConfigManager;

    AEConfig(File configDir) {
        ConfigSection clientRoot = ConfigSection.createRoot();
        clientConfig = new ClientConfig(clientRoot);
        clientConfigManager = createConfigFileManager(clientRoot, configDir, "appliedenergistics2/client.json");

        ConfigSection commonRoot = ConfigSection.createRoot();
        commonConfig = new CommonConfig(commonRoot);
        commonConfigManager = createConfigFileManager(commonRoot, configDir, "appliedenergistics2/common.json");

        syncClientConfig();
        syncCommonConfig();
    }

    private static ConfigFileManager createConfigFileManager(ConfigSection commonRoot, File configDir,
            String filename) {
        File configFile = new File(configDir, filename);
        ConfigFileManager result = new ConfigFileManager(commonRoot, configFile);
        if (!configFile.exists()) {
            result.save(); // Save a default file
        } else {
            result.load();
        }
        return result;
    }

    // Default Energy Conversion Rates
    private static final double DEFAULT_IC2_EXCHANGE = 2.0;
    private static final double DEFAULT_TR_EXCHANGE = 2.0;

    public static final String VERSION = "@version@";
    public static final String CHANNEL = "@aechannel@";

    // Config instance
    private static AEConfig instance;

    public static void load(File configFolder) {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new AEConfig(configFolder);
    }

    private final EnumSet<AEFeature> featureFlags = EnumSet.noneOf(AEFeature.class);

    // Misc
    private boolean removeCrashingItemsOnLoad;
    private int formationPlaneEntityLimit;
    private boolean enableEffects;
    private boolean useLargeFonts;
    private boolean useColoredCraftingStatus;
    private boolean disableColoredCableRecipesInJEI;
    private int craftingCalculationTimePerTick;

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

    // Meteors
    private int meteoriteMaximumSpawnHeight;
    private Set<String> meteoriteDimensionWhitelist;

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

    private void syncClientConfig() {
        this.disableColoredCableRecipesInJEI = clientConfig.disableColoredCableRecipesInJEI.get();
        this.enableEffects = clientConfig.enableEffects.get();
        this.useLargeFonts = clientConfig.useLargeFonts.get();
        this.useColoredCraftingStatus = clientConfig.useColoredCraftingStatus.get();

        for (int btnNum = 0; btnNum < 4; btnNum++) {
            this.craftByStacks[btnNum] = clientConfig.craftByStacks.get(btnNum).get();
            this.priorityByStacks[btnNum] = clientConfig.priorityByStacks.get(btnNum).get();
            this.levelByStacks[btnNum] = clientConfig.levelByStacks.get(btnNum).get();
        }
    }

    private void syncCommonConfig() {
        PowerUnits.EU.conversionRatio = commonConfig.powerRatioIc2.get();
        PowerUnits.TR.conversionRatio = commonConfig.powerRatioTechReborn.get();
        PowerMultiplier.CONFIG.multiplier = commonConfig.powerUsageMultiplier.get();

        CondenserOutput.MATTER_BALLS.requiredPower = commonConfig.condenserMatterBallsPower.get();
        CondenserOutput.SINGULARITY.requiredPower = commonConfig.condenserSingularityPower.get();

        this.oreDoublePercentage = (float) commonConfig.oreDoublePercentage.get();

        this.meteoriteMaximumSpawnHeight = commonConfig.meteoriteMaximumSpawnHeight.get();
        this.meteoriteDimensionWhitelist = new HashSet<>(commonConfig.meteoriteDimensionWhitelist.get());

        this.wirelessBaseCost = commonConfig.wirelessBaseCost.get();
        this.wirelessCostMultiplier = commonConfig.wirelessCostMultiplier.get();
        this.wirelessBaseRange = commonConfig.wirelessBaseRange.get();
        this.wirelessBoosterRangeMultiplier = commonConfig.wirelessBoosterRangeMultiplier.get();
        this.wirelessBoosterExp = commonConfig.wirelessBoosterExp.get();
        this.wirelessHighWirelessCount = commonConfig.wirelessHighWirelessCount.get();
        this.wirelessTerminalDrainMultiplier = commonConfig.wirelessTerminalDrainMultiplier.get();

        this.formationPlaneEntityLimit = commonConfig.formationPlaneEntityLimit.get();

        this.wirelessTerminalBattery = commonConfig.wirelessTerminalBattery.get();
        this.chargedStaffBattery = commonConfig.chargedStaffBattery.get();
        this.entropyManipulatorBattery = commonConfig.entropyManipulatorBattery.get();
        this.portableCellBattery = commonConfig.portableCellBattery.get();
        this.colorApplicatorBattery = commonConfig.colorApplicatorBattery.get();
        this.matterCannonBattery = commonConfig.matterCannonBattery.get();

        this.featureFlags.clear();

        for (final AEFeature feature : AEFeature.values()) {
            if (feature.isVisible() && feature.isConfig()) {
                if (commonConfig.enabledFeatures.containsKey(feature)
                        && commonConfig.enabledFeatures.get(feature).get()) {
                    this.featureFlags.add(feature);
                }
            } else {
                this.featureFlags.add(feature);
            }
        }

        for (final TickRates tr : TickRates.values()) {
            tr.setMin(commonConfig.tickRateMin.get(tr).get());
            tr.setMax(commonConfig.tickRateMin.get(tr).get());
        }

        this.spatialPowerMultiplier = commonConfig.spatialPowerMultiplier.get();
        this.spatialPowerExponent = commonConfig.spatialPowerExponent.get();
        this.spatialBlockTags = commonConfig.spatialBlockTags.get();

        this.craftingCalculationTimePerTick = commonConfig.craftingCalculationTimePerTick.get();

        this.removeCrashingItemsOnLoad = commonConfig.removeCrashingItemsOnLoad.get();
    }

    public static AEConfig instance() {
        return instance;
    }

    public boolean isFeatureEnabled(final AEFeature f) {
        return (!f.isConfig() && f.isEnabled()) || this.featureFlags.contains(f);
    }

    public boolean areFeaturesEnabled(Collection<AEFeature> features) {
        return this.featureFlags.containsAll(features);
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
        return clientConfig.searchTooltips.get();
    }

    public TerminalStyle getTerminalStyle() {
        return clientConfig.terminalStyle.get();
    }

    public void setTerminalStyle(TerminalStyle setting) {
        clientConfig.terminalStyle.set(setting);
    }

    public SearchBoxMode getTerminalSearchMode() {
        return clientConfig.terminalSearchMode.get();
    }

    public void setTerminalSearchMode(SearchBoxMode setting) {
        clientConfig.terminalSearchMode.set(setting);
    }

    public void save() {
    }

    /**
     * Returns an array with the quantity-steps for the +/- buttons in number entry
     * dialogs of the given type. Guaranteed to have 4 entries.
     */
    public int[] getNumberEntrySteps(NumberEntryType type) {
        switch (type) {
            case CRAFT_ITEM_COUNT:
                return craftByStacks;
            case PRIORITY:
                return priorityByStacks;
            case LEVEL_ITEM_COUNT:
                return levelByStacks;
            case LEVEL_FLUID_VOLUME:
                return levelByMillibuckets;
            default:
                throw new IllegalArgumentException("Unknown number entry: " + type);
        }
    }

    public PowerUnits getSelectedPowerUnit() {
        return this.clientConfig.selectedPowerUnit.get();
    }

    @SuppressWarnings("unchecked")
    public void nextPowerUnit(final boolean backwards) {
        PowerUnits selectedPowerUnit = EnumCycler.rotateEnum(getSelectedPowerUnit(), backwards,
                (EnumSet<PowerUnits>) Settings.POWER_UNITS.getPossibleValues());
        clientConfig.selectedPowerUnit.set(selectedPowerUnit);
    }

    // Getters
    public boolean isRemoveCrashingItemsOnLoad() {
        return this.removeCrashingItemsOnLoad;
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

    public double getPowerTransactionLimitTechReborn() {
        return commonConfig.powerTransactionLimitTechReborn.get();
    }

    public float getSpawnChargedChance() {
        return (float) commonConfig.spawnChargedChance.get();
    }

    public int getQuartzOresPerCluster() {
        return commonConfig.quartzOresPerCluster.get();
    }

    public int getQuartzOresClusterAmount() {
        return commonConfig.quartzOresClusterAmount.get();
    }

    public int getMeteoriteMaximumSpawnHeight() {
        return this.meteoriteMaximumSpawnHeight;
    }

    public Set<String> getMeteoriteDimensionWhitelist() {
        return this.meteoriteDimensionWhitelist;
    }

    // Setters keep visibility as low as possible.

    private static class ClientConfig {

        // Misc
        public final BooleanOption enableEffects;
        public final BooleanOption useLargeFonts;
        public final BooleanOption useColoredCraftingStatus;
        public final BooleanOption disableColoredCableRecipesInJEI;
        public final EnumOption<PowerUnits> selectedPowerUnit;

        // GUI Buttons
        private static final int[] BTN_BY_STACK_DEFAULTS = { 1, 10, 100, 1000 };
        public final List<IntegerOption> craftByStacks;
        public final List<IntegerOption> priorityByStacks;
        public final List<IntegerOption> levelByStacks;

        // Terminal Settings
        public final EnumOption<YesNo> searchTooltips;
        public final EnumOption<TerminalStyle> terminalStyle;
        public final EnumOption<SearchBoxMode> terminalSearchMode;

        public ClientConfig(ConfigSection root) {
            ConfigSection client = root.subsection("client");
            this.disableColoredCableRecipesInJEI = client.addBoolean("disableColoredCableRecipesInJEI", true);
            this.enableEffects = client.addBoolean("enableEffects", true);
            this.useLargeFonts = client.addBoolean("useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = client.addBoolean("useColoredCraftingStatus", true);
            this.selectedPowerUnit = client.addEnum("PowerUnit", PowerUnits.AE, "Power unit shown in AE UIs");

            this.craftByStacks = new ArrayList<>(4);
            this.priorityByStacks = new ArrayList<>(4);
            this.levelByStacks = new ArrayList<>(4);
            // load buttons..
            for (int btnNum = 0; btnNum < 4; btnNum++) {
                int defaultValue = BTN_BY_STACK_DEFAULTS[btnNum];
                final int buttonCap = (int) (Math.pow(10, btnNum + 1) - 1);

                this.craftByStacks.add(client.addInt("craftByStacks" + btnNum, defaultValue, 1, buttonCap,
                        "Controls buttons on Crafting Screen"));
                this.priorityByStacks.add(client.addInt("priorityByStacks" + btnNum, defaultValue, 1, buttonCap,
                        "Controls buttons on Priority Screen"));
                this.levelByStacks.add(client.addInt("levelByStacks" + btnNum, defaultValue, 1, buttonCap,
                        "Controls buttons on Level Emitter Screen"));
            }

            ConfigSection terminals = root.subsection("terminals");
            this.searchTooltips = terminals.addEnum("searchTooltips", YesNo.YES,
                    "Should tooltips be searched. Performance impact");
            this.terminalStyle = terminals.addEnum("terminalStyle", TerminalStyle.TALL);
            this.terminalSearchMode = terminals.addEnum("terminalSearchMode", SearchBoxMode.AUTOSEARCH);
        }

    }

    private static class CommonConfig {

        // Feature toggles
        public final Map<AEFeature, BooleanOption> enabledFeatures = new EnumMap<>(AEFeature.class);

        // Misc
        public final BooleanOption removeCrashingItemsOnLoad;
        public final IntegerOption formationPlaneEntityLimit;
        public final IntegerOption craftingCalculationTimePerTick;

        // Spatial IO/Dimension
        public final DoubleOption spatialPowerExponent;
        public final DoubleOption spatialPowerMultiplier;
        public final BooleanOption spatialBlockTags;

        // Grindstone
        public final DoubleOption oreDoublePercentage;

        // Batteries
        public final IntegerOption wirelessTerminalBattery;
        public final IntegerOption entropyManipulatorBattery;
        public final IntegerOption matterCannonBattery;
        public final IntegerOption portableCellBattery;
        public final IntegerOption colorApplicatorBattery;
        public final IntegerOption chargedStaffBattery;

        // Certus quartz
        public final DoubleOption spawnChargedChance;
        public final IntegerOption quartzOresPerCluster;
        public final IntegerOption quartzOresClusterAmount;

        // Meteors
        public final IntegerOption meteoriteMaximumSpawnHeight;
        public final StringListOption meteoriteDimensionWhitelist;

        // Wireless
        public final DoubleOption wirelessBaseCost;
        public final DoubleOption wirelessCostMultiplier;
        public final DoubleOption wirelessTerminalDrainMultiplier;
        public final DoubleOption wirelessBaseRange;
        public final DoubleOption wirelessBoosterRangeMultiplier;
        public final DoubleOption wirelessBoosterExp;
        public final DoubleOption wirelessHighWirelessCount;

        // Power Ratios
        public final DoubleOption powerRatioIc2;
        public final DoubleOption powerRatioTechReborn;
        public final DoubleOption powerUsageMultiplier;

        // How much TR energy can be transfered at most in one operation
        public final DoubleOption powerTransactionLimitTechReborn;

        // Condenser Power Requirement
        public final IntegerOption condenserMatterBallsPower;
        public final IntegerOption condenserSingularityPower;

        public final Map<TickRates, IntegerOption> tickRateMin = new HashMap<>();
        public final Map<TickRates, IntegerOption> tickRateMax = new HashMap<>();

        public CommonConfig(ConfigSection root) {

            // Feature switches
            ConfigSection features = root.subsection("features",
                    "Warning: Disabling a feature may disable other features depending on it.");

            // We need to group by feature category
            Map<String, List<AEFeature>> groupedFeatures = Arrays.stream(AEFeature.values())
                    .filter(AEFeature::isVisible) // Only provide config settings for visible features
                    .collect(Collectors.groupingBy(AEFeature::category));

            for (final String category : groupedFeatures.keySet()) {
                List<AEFeature> featuresInGroup = groupedFeatures.get(category);

                ConfigSection categorySection = features.subsection(category);
                for (AEFeature feature : featuresInGroup) {
                    if (feature.isConfig()) {
                        enabledFeatures.put(feature,
                                categorySection.addBoolean(feature.key(), feature.isEnabled(), feature.comment()));
                    }
                }
            }

            ConfigSection general = root.subsection("general");
            removeCrashingItemsOnLoad = general.addBoolean("removeCrashingItemsOnLoad", false,
                    "Will auto-remove items that crash when being loaded from storage. This will destroy those items instead of crashing the game!");

            ConfigSection automation = root.subsection("automation");
            formationPlaneEntityLimit = automation.addInt("formationPlaneEntityLimit", 128);

            ConfigSection craftingCPU = root.subsection("craftingCPU");
            this.craftingCalculationTimePerTick = craftingCPU.addInt("craftingCalculationTimePerTick", 5);

            ConfigSection spatialio = root.subsection("spatialio");
            this.spatialPowerMultiplier = spatialio.addDouble("spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = spatialio.addDouble("spatialPowerExponent", 1.35);
            this.spatialBlockTags = spatialio.addBoolean("spatialBlockTags", false,
                    "BE CAREFUL, CAN CORRUPT YOUR WORLD! Will use #spatial/whitelist as whitelist.");

            ConfigSection grindStone = root.subsection("GrindStone");
            this.oreDoublePercentage = grindStone.addDouble("oreDoublePercentage", 90.0, 0.0, 100.0,
                    "Chance to actually get an output with stacksize > 1.");

            ConfigSection battery = root.subsection("battery");
            this.wirelessTerminalBattery = battery.addInt("wirelessTerminal", 1600000);
            this.chargedStaffBattery = battery.addInt("chargedStaff", 8000);
            this.entropyManipulatorBattery = battery.addInt("entropyManipulator", 200000);
            this.portableCellBattery = battery.addInt("portableCell", 20000);
            this.colorApplicatorBattery = battery.addInt("colorApplicator", 20000);
            this.matterCannonBattery = battery.addInt("matterCannon", 200000);

            ConfigSection worldGen = root.subsection("worldGen");

            this.spawnChargedChance = worldGen.addDouble("spawnChargedChance", 0.08, 0.0, 1.0);
            this.meteoriteMaximumSpawnHeight = worldGen.addInt("meteoriteMaximumSpawnHeight", 180);
            List<String> defaultDimensionWhitelist = new ArrayList<>();
            defaultDimensionWhitelist.add(DimensionType.OVERWORLD_REGISTRY_KEY.getValue().toString());
            this.meteoriteDimensionWhitelist = worldGen.addStringList("meteoriteDimensionWhitelist",
                    defaultDimensionWhitelist);

            this.quartzOresPerCluster = worldGen.addInt("quartzOresPerCluster", 4);
            this.quartzOresClusterAmount = worldGen.addInt("quartzOresClusterAmount", 20);

            ConfigSection wireless = root.subsection("wireless");
            this.wirelessBaseCost = wireless.addDouble("wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = wireless.addDouble("wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = wireless.addDouble("wirelessBaseRange", 16.0);
            this.wirelessBoosterRangeMultiplier = wireless.addDouble("wirelessBoosterRangeMultiplier", 1.0);
            this.wirelessBoosterExp = wireless.addDouble("wirelessBoosterExp", 1.5);
            this.wirelessHighWirelessCount = wireless.addDouble("wirelessHighWirelessCount", 64.0);
            this.wirelessTerminalDrainMultiplier = wireless.addDouble("wirelessTerminalDrainMultiplier", 1.0);

            ConfigSection PowerRatios = root.subsection("PowerRatios");
            powerRatioIc2 = PowerRatios.addDouble("IC2", DEFAULT_IC2_EXCHANGE);
            powerRatioTechReborn = PowerRatios.addDouble("TechReborn", DEFAULT_TR_EXCHANGE);
            powerUsageMultiplier = PowerRatios.addDouble("UsageMultiplier", 1.0, 0.01, Double.MAX_VALUE);

            ConfigSection integration = root.subsection("Integration");
            powerTransactionLimitTechReborn = integration.addDouble("MaxTechRebornEnergyPerTransaction", 10000.0, 0.1, 1000000.0,
                    "The maximum amount of TechReborn energy units that can be transfered per operation.");

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
