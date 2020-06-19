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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.features.AEFeature;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.settings.TickRates;
import appeng.util.ConfigManager;
import appeng.util.EnumCycler;
import appeng.util.IConfigManagerHost;

@Mod.EventBusSubscriber(modid = AppEng.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AEConfig implements IConfigurableObject, IConfigManagerHost {

    public static final ClientConfig CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // Default Grindstone ores
    private static final String[] ORES_VANILLA = { "Obsidian", "Ender", "EnderPearl", "Coal", "Iron", "Gold",
            "Charcoal", "NetherQuartz" };
    private static final String[] ORES_AE = { "CertusQuartz", "Wheat", "Fluix" };
    private static final String[] ORES_COMMON = { "Copper", "Tin", "Silver", "Lead", "Bronze" };
    private static final String[] ORES_MISC = { "Brass", "Platinum", "Nickel", "Invar", "Aluminium", "Electrum",
            "Osmium", "Zinc" };

    // Default Energy Conversion Rates
    private static final double DEFAULT_IC2_EXCHANGE = 2.0;
    private static final double DEFAULT_RF_EXCHANGE = 0.5;

    static {
        final Pair<ClientConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        CLIENT_SPEC = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    public static final String VERSION = "@version@";
    public static final String CHANNEL = "@aechannel@";
    public static final String PACKET_CHANNEL = "AE";

    // Config instance
    private static final AEConfig instance = new AEConfig();

    private final IConfigManager settings = new ConfigManager(this);

    private final EnumSet<AEFeature> featureFlags = EnumSet.noneOf(AEFeature.class);
    private boolean updatable = false;

    // Misc
    private boolean removeCrashingItemsOnLoad;
    private int formationPlaneEntityLimit;
    private boolean enableEffects;
    private boolean useLargeFonts;
    private boolean useColoredCraftingStatus;
    private boolean disableColoredCableRecipesInJEI;
    private int craftingCalculationTimePerTick;
    private PowerUnits selectedPowerUnit;

    // GUI Buttons
    private int[] craftByStacks = new int[4];
    private int[] priorityByStacks = new int[4];
    private int[] levelByStacks = new int[4];
    private final int[] levelByMillibuckets = { 10, 100, 1000, 10000 };

    // Spatial IO/Dimension
    private String storageProviderID;
    private String storageDimensionID;
    private double spatialPowerExponent;
    private double spatialPowerMultiplier;

    // Grindstone
    private List<String> grinderOres;
    private Set<String> grinderBlackList;
    private float oreDoublePercentage;

    // Batteries
    private int wirelessTerminalBattery;
    private int entropyManipulatorBattery;
    private int matterCannonBattery;
    private int portableCellBattery;
    private int colorApplicatorBattery;
    private int chargedStaffBattery;

    // Certus quartz
    private float spawnChargedChance;
    private int quartzOresPerCluster;
    private int quartzOresClusterAmount;

    // Meteors
    private int minMeteoriteDistance;
    private int minMeteoriteDistanceSq;
    private double meteoriteClusterChance;
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

    // FIXME: this is shit, move this concern out of the config class
    @SubscribeEvent
    public static void onModConfigEvent(final ModConfig.ModConfigEvent configEvent) {
        if (configEvent.getConfig().getSpec() == CLIENT_SPEC) {
            instance.syncConfig(CLIENT);
        }
    }

    private void syncConfig(ClientConfig config) {

        PowerUnits.EU.conversionRatio = config.powerRatioIc2.get();
        PowerUnits.RF.conversionRatio = config.powerRatioForgeEnergy.get();
        PowerMultiplier.CONFIG.multiplier = config.powerUsageMultiplier.get();

        CondenserOutput.MATTER_BALLS.requiredPower = config.condenserMatterBallsPower.get();
        CondenserOutput.SINGULARITY.requiredPower = config.condenserSingularityPower.get();

        this.grinderOres = new ArrayList<>(config.grinderOres.get());
        this.grinderBlackList = new HashSet<>(config.grinderBlackList.get());
        this.oreDoublePercentage = config.oreDoublePercentage.get().floatValue();

        // FIXME: why is this here exactly???
        this.settings.registerSetting(Settings.SEARCH_TOOLTIPS, YesNo.YES);
        this.settings.registerSetting(Settings.TERMINAL_STYLE, TerminalStyle.TALL);
        this.settings.registerSetting(Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH);

        this.spawnChargedChance = (float) (1.0 - config.spawnChargedChance.get());
        this.minMeteoriteDistance = config.minMeteoriteDistance.get();
        this.minMeteoriteDistanceSq = this.minMeteoriteDistance * this.minMeteoriteDistance;
        this.meteoriteClusterChance = config.meteoriteClusterChance.get();
        this.meteoriteMaximumSpawnHeight = config.meteoriteMaximumSpawnHeight.get();
        this.meteoriteDimensionWhitelist = new HashSet<>(config.meteoriteDimensionWhitelist.get());

        this.quartzOresPerCluster = config.quartzOresPerCluster.get();
        this.quartzOresClusterAmount = config.quartzOresPerCluster.get();

        this.wirelessBaseCost = config.wirelessBaseCost.get();
        this.wirelessCostMultiplier = config.wirelessCostMultiplier.get();
        this.wirelessBaseRange = config.wirelessBaseRange.get();
        this.wirelessBoosterRangeMultiplier = config.wirelessBoosterRangeMultiplier.get();
        this.wirelessBoosterExp = config.wirelessBoosterExp.get();
        this.wirelessTerminalDrainMultiplier = config.wirelessTerminalDrainMultiplier.get();

        this.formationPlaneEntityLimit = config.formationPlaneEntityLimit.get();

        this.wirelessTerminalBattery = config.wirelessTerminalBattery.get();
        this.chargedStaffBattery = config.chargedStaffBattery.get();
        this.entropyManipulatorBattery = config.entropyManipulatorBattery.get();
        this.portableCellBattery = config.portableCellBattery.get();
        this.colorApplicatorBattery = config.colorApplicatorBattery.get();
        this.matterCannonBattery = config.matterCannonBattery.get();

        this.clientSync(config);

        this.featureFlags.clear();
        for (final AEFeature feature : AEFeature.values()) {
            if (feature.isVisible()) {
                if (config.enabledFeatures.get(feature).get()) {
                    this.featureFlags.add(feature);
                }
            } else {
                this.featureFlags.add(feature);
            }
        }

// FIXME			final ModContainer imb = net.minecraftforge.fml.common.Loader.instance().getIndexedModList().get( "ImmibisCore" );
// FIXME			if( imb != null )
// FIXME			{
// FIXME				final List<String> version = Arrays.asList( "59.0.0", "59.0.1", "59.0.2" );
// FIXME				if( version.contains( imb.getVersion() ) )
// FIXME				{
// FIXME					this.featureFlags.remove( AEFeature.ALPHA_PASS );
// FIXME				}
// FIXME			}

        for (final TickRates tr : TickRates.values()) {
            tr.setMin(config.tickRateMin.get(tr).get());
            tr.setMax(config.tickRateMin.get(tr).get());
        }

        this.storageProviderID = Strings.emptyToNull(config.storageProviderID.get());
        this.storageDimensionID = Strings.emptyToNull(config.storageDimensionID.get());
        this.spatialPowerMultiplier = config.spatialPowerMultiplier.get();
        this.spatialPowerExponent = config.spatialPowerExponent.get();

        this.craftingCalculationTimePerTick = config.craftingCalculationTimePerTick.get();

        this.updatable = true;
    }

    public static AEConfig instance() {
        return instance;
    }

    private void clientSync(ClientConfig config) {
        this.disableColoredCableRecipesInJEI = config.disableColoredCableRecipesInJEI.get();
        this.enableEffects = config.enableEffects.get();
        this.useLargeFonts = config.useLargeFonts.get();
        this.useColoredCraftingStatus = config.useColoredCraftingStatus.get();
        this.selectedPowerUnit = config.selectedPowerUnit.get();

        // load buttons..
        for (int btnNum = 0; btnNum < 4; btnNum++) {
            this.craftByStacks[btnNum] = config.craftByStacks.get(btnNum).get();
            this.priorityByStacks[btnNum] = config.priorityByStacks.get(btnNum).get();
            this.levelByStacks[btnNum] = config.levelByStacks.get(btnNum).get();
        }

        // FIXME for( final Settings e : this.settings.getSettings() )
        // FIXME {
        // FIXME final String Category = "Client"; // e.getClass().getSimpleName();
        // FIXME Enum<?> value = this.settings.getSetting( e );
// FIXME
        // FIXME final Property p = this.get( Category, e.name(), value.name(),
        // this.getListComment( value ) );
// FIXME
        // FIXME try
        // FIXME {
        // FIXME value = Enum.valueOf( value.getClass(), p.getString() );
        // FIXME }
        // FIXME catch( final IllegalArgumentException er )
        // FIXME {
        // FIXME AELog.info( "Invalid value '" + p.getString() + "' for " + e.name() + "
        // using '" + value.name() + "' instead" );
        // FIXME }
// FIXME
        // FIXME this.settings.putSetting( e, value );
        // FIXME }
    }

    private String getListComment(final Enum value) {
        String comment = null;

        if (value != null) {
            final EnumSet set = EnumSet.allOf(value.getClass());

            for (final Object Oeg : set) {
                final Enum eg = (Enum) Oeg;
                if (comment == null) {
                    comment = "Possible Values: " + eg.name();
                } else {
                    comment += ", " + eg.name();
                }
            }
        }

        return comment;
    }

    public boolean isFeatureEnabled(final AEFeature f) {
        return this.featureFlags.contains(f);
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

// FIXME	@Override
// FIXME	public Property get( final String category, final String key, final String defaultValue, final String comment, final Property.Type type )
// FIXME	{
// FIXME		final Property prop = super.get( category, key, defaultValue, comment, type );
// FIXME
// FIXME		if( prop != null )
// FIXME		{
// FIXME			if( !category.equals( "Client" ) )
// FIXME			{
// FIXME				prop.setRequiresMcRestart( true );
// FIXME			}
// FIXME		}
// FIXME
// FIXME		return prop;
// FIXME	}

    public void save() {
        if (this.isFeatureEnabled(AEFeature.SPATIAL_IO)) {
            CLIENT.storageProviderID.set(Strings.nullToEmpty(this.storageProviderID));
            CLIENT.storageDimensionID.set(Strings.nullToEmpty(this.storageDimensionID));
        }

        CLIENT.selectedPowerUnit.set(this.selectedPowerUnit);

        CLIENT_SPEC.save();
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Settings setting, final Enum<?> newValue) {
        // FIXME for( final Settings e : this.settings.getSettings() )
        // FIXME {
        // FIXME if( e == setting )
        // FIXME {
        // FIXME final String Category = "Client";
        // FIXME final Property p = this.get( Category, e.name(),
        // this.settings.getSetting( e ).name(), this.getListComment( newValue ) );
        // FIXME p.set( newValue.name() );
        // FIXME }
        // FIXME }
// FIXME
        // FIXME if( this.updatable )
        // FIXME {
        // FIXME this.save();
        // FIXME }
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.settings;
    }

    public boolean useTerminalUseLargeFont() {
        return this.useLargeFonts;
    }

    public int craftItemsByStackAmounts(final int i) {
        return this.craftByStacks[i];
    }

    public int priorityByStacksAmounts(final int i) {
        return this.priorityByStacks[i];
    }

    public int levelByStackAmounts(final int i) {
        return this.levelByStacks[i];
    }

    public int levelByMillyBuckets(final int i) {
        return this.levelByMillibuckets[i];
    }

// FIXME	public Enum getSetting( final String category, final Class<? extends Enum> class1, final Enum myDefault )
// FIXME	{
// FIXME		final String name = class1.getSimpleName();
// FIXME		final Property p = this.get( category, name, myDefault.name() );
// FIXME
// FIXME		try
// FIXME		{
// FIXME			return (Enum) class1.getField( p.toString() ).get( class1 );
// FIXME		}
// FIXME		catch( final Throwable t )
// FIXME		{
// FIXME			// :{
// FIXME		}
// FIXME
// FIXME		return myDefault;
// FIXME	}

// FIXME	public void setSetting( final String category, final Enum s )
// FIXME	{
// FIXME		final String name = s.getClass().getSimpleName();
// FIXME		this.get( category, name, s.name() ).set( s.name() );
// FIXME		this.save();
// FIXME	}

    public PowerUnits selectedPowerUnit() {
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

    public PowerUnits getSelectedPowerUnit() {
        return this.selectedPowerUnit;
    }

    public int[] getCraftByStacks() {
        return this.craftByStacks;
    }

    public int[] getPriorityByStacks() {
        return this.priorityByStacks;
    }

    public int[] getLevelByStacks() {
        return this.levelByStacks;
    }

    public String getStorageProviderID() {
        return this.storageProviderID;
    }

    public String getStorageDimensionID() {
        return this.storageDimensionID;
    }

    public double getSpatialPowerExponent() {
        return this.spatialPowerExponent;
    }

    public double getSpatialPowerMultiplier() {
        return this.spatialPowerMultiplier;
    }

    public List<String> getGrinderOres() {
        return this.grinderOres;
    }

    public Set<String> getGrinderBlackList() {
        return this.grinderBlackList;
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
        return this.spawnChargedChance;
    }

    public int getQuartzOresPerCluster() {
        return this.quartzOresPerCluster;
    }

    public int getQuartzOresClusterAmount() {
        return this.quartzOresClusterAmount;
    }

    public int getMinMeteoriteDistance() {
        return this.minMeteoriteDistance;
    }

    public int getMinMeteoriteDistanceSq() {
        return this.minMeteoriteDistanceSq;
    }

    public double getMeteoriteClusterChance() {
        return this.meteoriteClusterChance;
    }

    public int getMeteoriteMaximumSpawnHeight() {
        return this.meteoriteMaximumSpawnHeight;
    }

    public Set<String> getMeteoriteDimensionWhitelist() {
        return this.meteoriteDimensionWhitelist;
    }

    public double getWirelessBaseCost() {
        return this.wirelessBaseCost;
    }

    public double getWirelessCostMultiplier() {
        return this.wirelessCostMultiplier;
    }

    public double getWirelessTerminalDrainMultiplier() {
        return this.wirelessTerminalDrainMultiplier;
    }

    public double getWirelessBaseRange() {
        return this.wirelessBaseRange;
    }

    public double getWirelessBoosterRangeMultiplier() {
        return this.wirelessBoosterRangeMultiplier;
    }

    public double getWirelessBoosterExp() {
        return this.wirelessBoosterExp;
    }

    public double getWirelessHighWirelessCount() {
        return this.wirelessHighWirelessCount;
    }

    // Setters keep visibility as low as possible.

    void setStorageProviderID(String id) {
        this.storageProviderID = id;
    }

    void setStorageDimensionID(String id) {
        this.storageDimensionID = id;
    }

    private static class ClientConfig {

        // Feature toggles
        public final Map<AEFeature, BooleanValue> enabledFeatures = new EnumMap<>(AEFeature.class);

        // Misc
        public final BooleanValue removeCrashingItemsOnLoad;
        public final ConfigValue<Integer> formationPlaneEntityLimit;
        public final BooleanValue enableEffects;
        public final BooleanValue useLargeFonts;
        public final BooleanValue useColoredCraftingStatus;
        public final BooleanValue disableColoredCableRecipesInJEI;
        public final ConfigValue<Integer> craftingCalculationTimePerTick;
        public final EnumValue<PowerUnits> selectedPowerUnit;

        // GUI Buttons
        private static final int[] BTN_BY_STACK_DEFAULTS = { 1, 10, 100, 1000 };
        public final List<ConfigValue<Integer>> craftByStacks;
        public final List<ConfigValue<Integer>> priorityByStacks;
        public final List<ConfigValue<Integer>> levelByStacks;

        // Spatial IO/Dimension
        public final ConfigValue<String> storageProviderID;
        public final ConfigValue<String> storageDimensionID;
        public final ConfigValue<Double> spatialPowerExponent;
        public final ConfigValue<Double> spatialPowerMultiplier;

        // Grindstone
        public final ConfigValue<List<? extends String>> grinderOres;
        public final ConfigValue<List<? extends String>> grinderBlackList;
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

        // Meteors
        public final ConfigValue<Integer> minMeteoriteDistance;
        public final ConfigValue<Double> meteoriteClusterChance;
        public final ConfigValue<Integer> meteoriteMaximumSpawnHeight;
        public final ConfigValue<List<? extends String>> meteoriteDimensionWhitelist;

        // Wireless
        public final ConfigValue<Double> wirelessBaseCost;
        public final ConfigValue<Double> wirelessCostMultiplier;
        public final ConfigValue<Double> wirelessTerminalDrainMultiplier;
        public final ConfigValue<Double> wirelessBaseRange;
        public final ConfigValue<Double> wirelessBoosterRangeMultiplier;
        public final ConfigValue<Double> wirelessBoosterExp;

        // Power Ratios
        public final ConfigValue<Double> powerRatioIc2;
        public final ConfigValue<Double> powerRatioForgeEnergy;
        public final DoubleValue powerUsageMultiplier;

        // Condenser Power Requirement
        public final ConfigValue<Integer> condenserMatterBallsPower;
        public final ConfigValue<Integer> condenserSingularityPower;

        public final Map<TickRates, ConfigValue<Integer>> tickRateMin = new HashMap<>();
        public final Map<TickRates, ConfigValue<Integer>> tickRateMax = new HashMap<>();

        public ClientConfig(ForgeConfigSpec.Builder builder) {

            // Feature switches
            builder.comment("Warning: Disabling a feature may disable other features depending on it.")
                    .push("features");

            // We need to group by feature category
            Map<String, List<AEFeature>> groupedFeatures = Arrays.stream(AEFeature.values())
                    .filter(AEFeature::isVisible) // Only provide config settings for visible features
                    .collect(Collectors.groupingBy(AEFeature::category));

            for (final String category : groupedFeatures.keySet()) {
                List<AEFeature> featuresInGroup = groupedFeatures.get(category);

                builder.push(category);
                for (AEFeature feature : featuresInGroup) {
                    enabledFeatures.put(feature, builder.comment(Strings.nullToEmpty(feature.comment()))
                            .define(feature.key(), feature.isEnabled()));
                }
                builder.pop();
            }

            builder.pop();

            builder.push("general");
            removeCrashingItemsOnLoad = builder.comment(
                    "Will auto-remove items that crash when being loaded from storage. This will destroy those items instead of crashing the game!")
                    .define("removeCrashingItemsOnLoad", false);
            builder.pop();

            builder.push("automation");
            formationPlaneEntityLimit = builder.comment("TODO").define("formationPlaneEntityLimit", 128);
            builder.pop();

            builder.push("client");
            this.disableColoredCableRecipesInJEI = builder.comment("TODO").define("disableColoredCableRecipesInJEI",
                    true);
            this.enableEffects = builder.comment("TODO").define("enableEffects", true);
            this.useLargeFonts = builder.comment("TODO").define("useTerminalUseLargeFont", false);
            this.useColoredCraftingStatus = builder.comment("TODO").define("useColoredCraftingStatus", true);
            this.selectedPowerUnit = builder.comment("Power unit shown in AE UIs").defineEnum("PowerUnit",
                    PowerUnits.AE, PowerUnits.values());

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

            builder.push("craftingCPU");

            this.craftingCalculationTimePerTick = builder.define("craftingCalculationTimePerTick", 5);

            builder.pop();

            builder.push("spatialio");
            this.storageProviderID = builder.define("storageProviderID", "");
            this.storageDimensionID = builder.define("storageDimensionID", "");
            this.spatialPowerMultiplier = builder.define("spatialPowerMultiplier", 1250.0);
            this.spatialPowerExponent = builder.define("spatialPowerExponent", 1.35);
            builder.pop();

            builder.comment(
                    "Creates recipe of the following pattern automatically: '1 oreTYPE => 2 dustTYPE' and '(1 ingotTYPE or 1 crystalTYPE or 1 gemTYPE) => 1 dustTYPE'")
                    .push("GrindStone");

            List<String> defaultGrinderOres = Stream.of(ORES_VANILLA, ORES_AE, ORES_COMMON, ORES_MISC)
                    .flatMap(Arrays::stream).collect(Collectors.toList());
            this.grinderOres = builder
                    .comment("The list of types to handle. Specify without a prefix like ore or dust.")
                    .defineList("grinderOres", defaultGrinderOres, obj -> true); // FIXME: tag validation, is that even
                                                                                 // possible???
            this.grinderBlackList = builder
                    .comment("Blacklists the exact oredict name from being handled by any recipe.")
                    .defineList("blacklist", Collections.emptyList(), obj -> true); // FIXME: tag validation, is that
                                                                                    // even possible???
            this.oreDoublePercentage = builder.comment("Chance to actually get an output with stacksize > 1.")
                    .defineInRange("oreDoublePercentage", 90.0, 0.0, 100.0);
            builder.pop();

            builder.push("battery");
            this.wirelessTerminalBattery = builder.define("wirelessTerminal", 1600000);
            this.chargedStaffBattery = builder.define("chargedStaff", 200000);
            this.entropyManipulatorBattery = builder.define("entropyManipulator", 200000);
            this.portableCellBattery = builder.define("portableCell", 20000);
            this.colorApplicatorBattery = builder.define("colorApplicator", 20000);
            this.matterCannonBattery = builder.define("matterCannon", 8000);
            builder.pop();

            builder.push("worldGen");

            this.spawnChargedChance = builder.defineInRange("spawnChargedChance", 0.08, 0.0, 1.0);
            this.minMeteoriteDistance = builder.define("minMeteoriteDistance", 707);
            this.meteoriteClusterChance = builder.define("meteoriteClusterChance", 0.1);
            this.meteoriteMaximumSpawnHeight = builder.define("meteoriteMaximumSpawnHeight", 180);
            List<String> defaultDimensionWhitelist = new ArrayList<>();
            defaultDimensionWhitelist.add(DimensionType.getKey(DimensionType.OVERWORLD).toString());
            this.meteoriteDimensionWhitelist = builder.defineList("meteoriteDimensionWhitelist",
                    defaultDimensionWhitelist, obj -> true);

            this.quartzOresPerCluster = builder.define("quartzOresPerCluster", 4);
            this.quartzOresClusterAmount = builder.define("quartzOresClusterAmount", 15);

            builder.pop();

            builder.push("wireless");
            this.wirelessBaseCost = builder.define("wirelessBaseCost", 8.0);
            this.wirelessCostMultiplier = builder.define("wirelessCostMultiplier", 1.0);
            this.wirelessBaseRange = builder.define("wirelessBaseRange", 1.0);
            this.wirelessBoosterRangeMultiplier = builder.define("wirelessBoosterRangeMultiplier", 16.0);
            this.wirelessBoosterExp = builder.define("wirelessBoosterExp", 1.0);
            this.wirelessTerminalDrainMultiplier = builder.define("wirelessTerminalDrainMultiplier", 1.5);
            builder.pop();

            builder.push("PowerRatios");
            powerRatioIc2 = builder.define("IC2", DEFAULT_IC2_EXCHANGE);
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
        }

    }

}
