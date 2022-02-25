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


import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.api.config.CondenserOutput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.TerminalStyle;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.core.features.AEFeature;
import appeng.core.settings.TickRates;
import appeng.items.materials.MaterialType;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;


public final class AEConfig extends Configuration implements IConfigurableObject, IConfigManagerHost
{

	public static final String VERSION = "@version@";
	public static final String CHANNEL = "@aechannel@";
	public static final String PACKET_CHANNEL = "AE";

	// Config instance
	private static AEConfig instance;

	// Default Grindstone ores
	private static final String[] ORES_VANILLA = {
			"Obsidian", "Ender", "EnderPearl", "Coal", "Iron", "Gold",
			"Charcoal", "NetherQuartz"
	};
	private static final String[] ORES_AE = {"CertusQuartz", "Wheat", "Fluix"};
	private static final String[] ORES_COMMON = {
			"Copper", "Tin", "Silver", "Lead", "Bronze"
	};
	private static final String[] ORES_MISC = {
			"Brass", "Platinum", "Nickel", "Invar", "Aluminium", "Electrum",
			"Osmium", "Zinc"
	};

	private String[] nonBlockingItems = {
			"[gregtech]", "gregtech:circuit.integrated",
			"gregtech:shape.mold.plate", "gregtech:shape.mold.gear",
			"gregtech:shape.mold.credit", "gregtech:shape.mold.bottle",
			"gregtech:shape.mold.ingot", "gregtech:shape.mold.ball",
			"gregtech:shape.mold.block", "gregtech:shape.mold.nugget",
			"gregtech:shape.mold.cylinder", "gregtech:shape.mold.anvil",
			"gregtech:shape.mold.name", "gregtech:shape.mold.gear.small",
			"gregtech:shape.mold.rotor", "gregtech:shape.extruder.plate",
			"gregtech:shape.extruder.rod", "gregtech:shape.extruder.bolt",
			"gregtech:shape.extruder.ring", "gregtech:shape.extruder.cell",
			"gregtech:shape.extruder.ingot", "gregtech:shape.extruder.wire",
			"gregtech:shape.extruder.pipe.tiny",
			"gregtech:shape.extruder.pipe.small",
			"gregtech:shape.extruder.pipe.medium",
			"gregtech:shape.extruder.pipe.normal",
			"gregtech:shape.extruder.pipe.large",
			"gregtech:shape.extruder.pipe.huge",
			"gregtech:shape.extruder.block", "gregtech:shape.extruder.sword",
			"gregtech:shape.extruder.pickaxe", "gregtech:shape.extruder.shovel",
			"gregtech:shape.extruder.axe", "gregtech:shape.extruder.hoe",
			"gregtech:shape.extruder.hammer", "gregtech:shape.extruder.file",
			"gregtech:shape.extruder.saw", "gregtech:shape.extruder.gear",
			"gregtech:shape.extruder.bottle", "gregtech:shape.extruder.foil",
			"gregtech:shape.extruder.gear_small",
			"gregtech:shape.extruder.rod_long", "gregtech:shape.extruder.rotor",
			"gregtech:glass_lens.white", "gregtech:glass_lens.orange",
			"gregtech:glass_lens.magenta", "gregtech:glass_lens.light_blue",
			"gregtech:glass_lens.yellow", "gregtech:glass_lens.lime",
			"gregtech:glass_lens.pink", "gregtech:glass_lens.gray",
			"gregtech:glass_lens.light_gray", "gregtech:glass_lens.cyan",
			"gregtech:glass_lens.purple", "gregtech:glass_lens.blue",
			"gregtech:glass_lens.brown", "gregtech:glass_lens.green",
			"gregtech:glass_lens.red", "gregtech:glass_lens.black",
			"contenttweaker:smallgearextrudershape",
			"contenttweaker:creativeportabletankmold"
	};

	// Default Energy Conversion Rates
	private static final double DEFAULT_IC2_EXCHANGE = 2.0;
	private static final double DEFAULT_GTEU_EXCHANGE = 2.0;
	private static final double DEFAULT_RF_EXCHANGE = 0.5;

	private final IConfigManager settings = new ConfigManager( this );

	private final EnumSet<AEFeature> featureFlags = EnumSet.noneOf( AEFeature.class );
	private final File configFile;
	private boolean updatable = false;

	// Misc
	private boolean removeCrashingItemsOnLoad = false;
	private int formationPlaneEntityLimit = 128;
	private boolean enableEffects = true;
	private boolean useLargeFonts = false;
	private boolean useColoredCraftingStatus;
	private boolean disableColoredCableRecipesInJEI = true;
	private int craftingCalculationTimePerTick = 5;
	private PowerUnits selectedPowerUnit = PowerUnits.AE;

	// GUI Buttons
	private final int[] craftByStacks = {1, 10, 100, 1000};
	private final int[] priorityByStacks = {1, 10, 100, 1000};
	private final int[] levelByStacks = {1, 10, 100, 1000};
	private final int[] levelByMillibuckets = {10, 100, 1000, 10000};

	// Spatial IO/Dimension
	private int storageProviderID = -1;
	private int storageDimensionID = -1;
	private double spatialPowerExponent = 1.35;
	private double spatialPowerMultiplier = 1250.0;

	// Grindstone
	private String[] grinderOres = Stream.of( ORES_VANILLA, ORES_AE, ORES_COMMON, ORES_MISC ).flatMap( Stream::of ).toArray( String[]::new );
	private Set<String> grinderBlackList;
	private double oreDoublePercentage = 90.0;

	// Batteries
	private int wirelessTerminalBattery = 1600000;
	private int entropyManipulatorBattery = 200000;
	private int matterCannonBattery = 200000;
	private int portableCellBattery = 20000;
	private int colorApplicatorBattery = 20000;
	private int chargedStaffBattery = 8000;

	// Certus quartz
	private float spawnChargedChance = 0.92f;
	private int quartzOresPerCluster = 4;
	private int quartzOresClusterAmount = 15;
	private int chargedChange = 4;

	// Meteors
	private int minMeteoriteDistance = 707;
	private int minMeteoriteDistanceSq = this.minMeteoriteDistance * this.minMeteoriteDistance;
	private double meteoriteClusterChance = 0.1;
	private int meteoriteMaximumSpawnHeight = 180;
	private int[] meteoriteDimensionWhitelist = {0};

	// Wireless
	private double wirelessBaseCost = 8;
	private double wirelessCostMultiplier = 1;
	private double wirelessTerminalDrainMultiplier = 1;
	private double wirelessBaseRange = 16;
	private double wirelessBoosterRangeMultiplier = 1;
	private double wirelessBoosterExp = 1.5;
	private double wirelessHighWirelessCount = 64;

	// Tunnels
	public static final double TUNNEL_POWER_LOSS = 0.05;

	private AEConfig( final File configFile )
	{
		super( configFile );
		this.configFile = configFile;

		MinecraftForge.EVENT_BUS.register( this );

		PowerUnits.EU.conversionRatio = this.get( "PowerRatios", "IC2", DEFAULT_IC2_EXCHANGE ).getDouble( DEFAULT_IC2_EXCHANGE );
		PowerUnits.RF.conversionRatio = this.get( "PowerRatios", "ForgeEnergy", DEFAULT_RF_EXCHANGE ).getDouble( DEFAULT_RF_EXCHANGE );
		PowerUnits.GTEU.conversionRatio = this.get( "PowerRatios", "GTEU", DEFAULT_GTEU_EXCHANGE ).getDouble( DEFAULT_GTEU_EXCHANGE );

		final double usageEffective = this.get( "PowerRatios", "UsageMultiplier", 1.0 ).getDouble( 1.0 );
		PowerMultiplier.CONFIG.multiplier = Math.max( 0.01, usageEffective );

		CondenserOutput.MATTER_BALLS.requiredPower = this.get( "Condenser", "MatterBalls", 256 ).getInt( 256 );
		CondenserOutput.SINGULARITY.requiredPower = this.get( "Condenser", "Singularity", 256000 ).getInt( 256000 );

		this.removeCrashingItemsOnLoad = this.get( "general", "removeCrashingItemsOnLoad", false, "Will auto-remove items that crash when being loaded from storage. This will destroy those items instead of crashing the game!" ).getBoolean();

		this.setCategoryComment( "BlockingMode", "Map of items to not block when blockingmode is enabled.\n[modid]\nmodid:item:metadata(optional,default:0)\nSupports more than one modid, so you can block different things between, for example, gregtech or enderio" );
		this.nonBlockingItems = this.get( "BlockingMode", "nonBlockingItems", nonBlockingItems, "NonBlockingItems" ).getStringList();

		this.setCategoryComment( "GrindStone", "Creates recipe of the following pattern automatically: '1 oreTYPE => 2 dustTYPE' and '(1 ingotTYPE or 1 crystalTYPE or 1 gemTYPE) => 1 dustTYPE'" );
		this.grinderOres = this.get( "GrindStone", "grinderOres", this.grinderOres, "The list of types to handle. Specify without a prefix like ore or dust." ).getStringList();
		this.grinderBlackList = Sets.newHashSet( this.get( "GrindStone", "blacklist", new String[]{}, "Blacklists the exact oredict name from being handled by any recipe." ).getStringList() );
		this.oreDoublePercentage = this.get( "GrindStone", "oreDoublePercentage", this.oreDoublePercentage, "Chance to actually get an output with stacksize > 1." ).getDouble( this.oreDoublePercentage );

		this.settings.registerSetting( Settings.SEARCH_TOOLTIPS, YesNo.YES );
		this.settings.registerSetting( Settings.TERMINAL_STYLE, TerminalStyle.TALL );
		this.settings.registerSetting( Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH );

		this.spawnChargedChance = (float) ( 1.0 - this.get( "worldGen", "spawnChargedChance", 1.0 - this.spawnChargedChance ).getDouble( 1.0 - this.spawnChargedChance ) );
		this.minMeteoriteDistance = this.get( "worldGen", "minMeteoriteDistance", this.minMeteoriteDistance ).getInt( this.minMeteoriteDistance );
		this.meteoriteClusterChance = this.get( "worldGen", "meteoriteClusterChance", this.meteoriteClusterChance ).getDouble( this.meteoriteClusterChance );
		this.meteoriteMaximumSpawnHeight = this.get( "worldGen", "meteoriteMaximumSpawnHeight", this.meteoriteMaximumSpawnHeight ).getInt( this.meteoriteMaximumSpawnHeight );
		this.meteoriteDimensionWhitelist = this.get( "worldGen", "meteoriteDimensionWhitelist", this.meteoriteDimensionWhitelist ).getIntList();

		this.quartzOresPerCluster = this.get( "worldGen", "quartzOresPerCluster", this.quartzOresPerCluster ).getInt( this.quartzOresPerCluster );
		this.quartzOresClusterAmount = this.get( "worldGen", "quartzOresClusterAmount", this.quartzOresClusterAmount ).getInt( this.quartzOresClusterAmount );

		this.minMeteoriteDistanceSq = this.minMeteoriteDistance * this.minMeteoriteDistance;

		this.addCustomCategoryComment( "wireless", "Range= wirelessBaseRange + wirelessBoosterRangeMultiplier * Math.pow( boosters, wirelessBoosterExp )\nPowerDrain= wirelessBaseCost + wirelessCostMultiplier * Math.pow( boosters, 1 + boosters / wirelessHighWirelessCount )" );

		this.wirelessBaseCost = this.get( "wireless", "wirelessBaseCost", this.wirelessBaseCost ).getDouble( this.wirelessBaseCost );
		this.wirelessCostMultiplier = this.get( "wireless", "wirelessCostMultiplier", this.wirelessCostMultiplier ).getDouble( this.wirelessCostMultiplier );
		this.wirelessBaseRange = this.get( "wireless", "wirelessBaseRange", this.wirelessBaseRange ).getDouble( this.wirelessBaseRange );
		this.wirelessBoosterRangeMultiplier = this.get( "wireless", "wirelessBoosterRangeMultiplier", this.wirelessBoosterRangeMultiplier ).getDouble( this.wirelessBoosterRangeMultiplier );
		this.wirelessBoosterExp = this.get( "wireless", "wirelessBoosterExp", this.wirelessBoosterExp ).getDouble( this.wirelessBoosterExp );
		this.wirelessTerminalDrainMultiplier = this.get( "wireless", "wirelessTerminalDrainMultiplier", this.wirelessTerminalDrainMultiplier ).getDouble( this.wirelessTerminalDrainMultiplier );

		this.formationPlaneEntityLimit = this.get( "automation", "formationPlaneEntityLimit", this.formationPlaneEntityLimit ).getInt( this.formationPlaneEntityLimit );

		this.wirelessTerminalBattery = this.get( "battery", "wirelessTerminal", this.wirelessTerminalBattery ).getInt( this.wirelessTerminalBattery );
		this.chargedStaffBattery = this.get( "battery", "chargedStaff", this.chargedStaffBattery ).getInt( this.chargedStaffBattery );
		this.entropyManipulatorBattery = this.get( "battery", "entropyManipulator", this.entropyManipulatorBattery ).getInt( this.entropyManipulatorBattery );
		this.portableCellBattery = this.get( "battery", "portableCell", this.portableCellBattery ).getInt( this.portableCellBattery );
		this.colorApplicatorBattery = this.get( "battery", "colorApplicator", this.colorApplicatorBattery ).getInt( this.colorApplicatorBattery );
		this.matterCannonBattery = this.get( "battery", "matterCannon", this.matterCannonBattery ).getInt( this.matterCannonBattery );

		this.clientSync();

		this.addCustomCategoryComment( "features", "Warning: Disabling a feature may disable other features depending on it." );
		for( final AEFeature feature : AEFeature.values() )
		{
			if( feature.isVisible() )
			{
				final Property option = this.get( "Features." + feature.category(), feature.key(), feature.isEnabled(), feature.comment() );

				if( option.getBoolean( feature.isEnabled() ) )
				{
					this.featureFlags.add( feature );
				}
			}
			else
			{
				this.featureFlags.add( feature );
			}
		}

		final ModContainer imb = net.minecraftforge.fml.common.Loader.instance().getIndexedModList().get( "ImmibisCore" );
		if( imb != null )
		{
			final List<String> version = Arrays.asList( "59.0.0", "59.0.1", "59.0.2" );
			if( version.contains( imb.getVersion() ) )
			{
				this.featureFlags.remove( AEFeature.ALPHA_PASS );
			}
		}

		try
		{
			this.selectedPowerUnit = PowerUnits.valueOf( this.get( "Client", "PowerUnit", this.selectedPowerUnit.name(), this.getListComment( this.selectedPowerUnit ) ).getString() );
		}
		catch( final Throwable t )
		{
			this.selectedPowerUnit = PowerUnits.AE;
		}

		for( final TickRates tr : TickRates.values() )
		{
			tr.Load( this );
		}

		if( this.isFeatureEnabled( AEFeature.SPATIAL_IO ) )
		{
			this.storageProviderID = this.get( "spatialio", "storageProviderID", this.storageProviderID ).getInt( this.storageProviderID );
			this.storageDimensionID = this.get( "spatialio", "storageDimensionID", this.storageDimensionID ).getInt( this.storageDimensionID );
			this.spatialPowerMultiplier = this.get( "spatialio", "spatialPowerMultiplier", this.spatialPowerMultiplier ).getDouble( this.spatialPowerMultiplier );
			this.spatialPowerExponent = this.get( "spatialio", "spatialPowerExponent", this.spatialPowerExponent ).getDouble( this.spatialPowerExponent );
		}

		if( this.isFeatureEnabled( AEFeature.CRAFTING_CPU ) )
		{
			this.craftingCalculationTimePerTick = this.get( "craftingCPU", "craftingCalculationTimePerTick", this.craftingCalculationTimePerTick ).getInt( this.craftingCalculationTimePerTick );
		}

		this.updatable = true;
	}

	public static void init( final File configFile )
	{
		instance = new AEConfig( configFile );
	}

	public static AEConfig instance()
	{
		return instance;
	}

	private void clientSync()
	{
		this.disableColoredCableRecipesInJEI = this.get( "Client", "disableColoredCableRecipesInJEI", true ).getBoolean( true );
		this.enableEffects = this.get( "Client", "enableEffects", true ).getBoolean( true );
		this.useLargeFonts = this.get( "Client", "useTerminalUseLargeFont", false ).getBoolean( false );
		this.useColoredCraftingStatus = this.get( "Client", "useColoredCraftingStatus", true ).getBoolean( true );

		// load buttons..
		for( int btnNum = 0; btnNum < 4; btnNum++ )
		{
			final Property cmb = this.get( "Client", "craftAmtButton" + ( btnNum + 1 ), this.craftByStacks[btnNum] );
			final Property pmb = this.get( "Client", "priorityAmtButton" + ( btnNum + 1 ), this.priorityByStacks[btnNum] );
			final Property lmb = this.get( "Client", "levelAmtButton" + ( btnNum + 1 ), this.levelByStacks[btnNum] );

			final int buttonCap = (int) ( Math.pow( 10, btnNum + 1 ) - 1 );

			this.craftByStacks[btnNum] = Math.abs( cmb.getInt( this.craftByStacks[btnNum] ) );
			this.priorityByStacks[btnNum] = Math.abs( pmb.getInt( this.priorityByStacks[btnNum] ) );
			this.levelByStacks[btnNum] = Math.abs( pmb.getInt( this.levelByStacks[btnNum] ) );

			cmb.setComment( "Controls buttons on Crafting Screen : Capped at " + buttonCap );
			pmb.setComment( "Controls buttons on Priority Screen : Capped at " + buttonCap );
			lmb.setComment( "Controls buttons on Level Emitter Screen : Capped at " + buttonCap );

			this.craftByStacks[btnNum] = Math.min( this.craftByStacks[btnNum], buttonCap );
			this.priorityByStacks[btnNum] = Math.min( this.priorityByStacks[btnNum], buttonCap );
			this.levelByStacks[btnNum] = Math.min( this.levelByStacks[btnNum], buttonCap );
		}

		for( final Settings e : this.settings.getSettings() )
		{
			final String Category = "Client"; // e.getClass().getSimpleName();
			Enum<?> value = this.settings.getSetting( e );

			final Property p = this.get( Category, e.name(), value.name(), this.getListComment( value ) );

			try
			{
				value = Enum.valueOf( value.getClass(), p.getString() );
			}
			catch( final IllegalArgumentException er )
			{
				AELog.info( "Invalid value '" + p.getString() + "' for " + e.name() + " using '" + value.name() + "' instead" );
			}

			this.settings.putSetting( e, value );
		}
	}

	private String getListComment( final Enum value )
	{
		String comment = null;

		if( value != null )
		{
			final EnumSet set = EnumSet.allOf( value.getClass() );

			for( final Object Oeg : set )
			{
				final Enum eg = (Enum) Oeg;
				if( comment == null )
				{
					comment = "Possible Values: " + eg.name();
				}
				else
				{
					comment += ", " + eg.name();
				}
			}
		}

		return comment;
	}

	public boolean isFeatureEnabled( final AEFeature f )
	{
		return this.featureFlags.contains( f );
	}

	public boolean areFeaturesEnabled( Collection<AEFeature> features )
	{
		return this.featureFlags.containsAll( features );
	}

	public double wireless_getDrainRate( final double range )
	{
		return this.wirelessTerminalDrainMultiplier * range;
	}

	public double wireless_getMaxRange( final int boosters )
	{
		return this.wirelessBaseRange + this.wirelessBoosterRangeMultiplier * Math.pow( boosters, this.wirelessBoosterExp );
	}

	public double wireless_getPowerDrain( final int boosters )
	{
		return this.wirelessBaseCost + this.wirelessCostMultiplier * Math.pow( boosters, 1 + boosters / this.wirelessHighWirelessCount );
	}

	@Override
	public Property get( final String category, final String key, final String defaultValue, final String comment, final Property.Type type )
	{
		final Property prop = super.get( category, key, defaultValue, comment, type );

		if( prop != null )
		{
			if( !category.equals( "Client" ) )
			{
				prop.setRequiresMcRestart( true );
			}
		}

		return prop;
	}

	@Override
	public void save()
	{
		if( this.isFeatureEnabled( AEFeature.SPATIAL_IO ) )
		{
			this.get( "spatialio", "storageProviderID", this.storageProviderID ).set( this.storageProviderID );
			this.get( "spatialio", "storageDimensionID", this.storageDimensionID ).set( this.storageDimensionID );
		}

		this.get( "Client", "PowerUnit", this.selectedPowerUnit.name(), this.getListComment( this.selectedPowerUnit ) ).set( this.selectedPowerUnit.name() );

		if( this.hasChanged() )
		{
			super.save();
		}
	}

	@SubscribeEvent
	public void onConfigChanged( final ConfigChangedEvent.OnConfigChangedEvent eventArgs )
	{
		if( eventArgs.getModID().equals( AppEng.MOD_ID ) )
		{
			this.clientSync();
		}
	}

	public boolean disableColoredCableRecipesInJEI()
	{
		return this.disableColoredCableRecipesInJEI;
	}

	public String getFilePath()
	{
		return this.configFile.toString();
	}

	public boolean useAEVersion( final MaterialType mt )
	{
		if( this.isFeatureEnabled( AEFeature.WEBSITE_RECIPES ) )
		{
			return true;
		}

		this.setCategoryComment( "OreCamouflage", "AE2 Automatically uses alternative ores present in your instance of MC to blend better with its surroundings, if you prefer you can disable this selectively using these flags; Its important to note, that some if these items even if enabled may not be craftable in game because other items are overriding their recipes." );
		final Property p = this.get( "OreCamouflage", mt.name(), true );
		p.setComment( "OreDictionary Names: " + mt.getOreName() );

		return !p.getBoolean( true );
	}

	@Override
	public void updateSetting( final IConfigManager manager, final Enum setting, final Enum newValue )
	{
		for( final Settings e : this.settings.getSettings() )
		{
			if( e == setting )
			{
				final String Category = "Client";
				final Property p = this.get( Category, e.name(), this.settings.getSetting( e ).name(), this.getListComment( newValue ) );
				p.set( newValue.name() );
			}
		}

		if( this.updatable )
		{
			this.save();
		}
	}

	public int getFreeMaterial( final int varID )
	{
		return this.getFreeIDSLot( varID, "materials" );
	}

	public int getFreeIDSLot( final int varID, final String category )
	{
		boolean alreadyUsed = false;
		int min = 0;

		for( final Property p : this.getCategory( category ).getValues().values() )
		{
			final int thisInt = p.getInt();

			if( varID == thisInt )
			{
				alreadyUsed = true;
			}

			min = Math.max( min, thisInt + 1 );
		}

		if( alreadyUsed )
		{
			if( min < 16383 )
			{
				min = 16383;
			}

			return min;
		}

		return varID;
	}

	public int getFreePart( final int varID )
	{
		return this.getFreeIDSLot( varID, "parts" );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return this.settings;
	}

	public boolean useTerminalUseLargeFont()
	{
		return this.useLargeFonts;
	}

	public int craftItemsByStackAmounts( final int i )
	{
		return this.craftByStacks[i];
	}

	public int priorityByStacksAmounts( final int i )
	{
		return this.priorityByStacks[i];
	}

	public int levelByStackAmounts( final int i )
	{
		return this.levelByStacks[i];
	}

	public int levelByMillyBuckets( final int i )
	{
		return this.levelByMillibuckets[i];
	}

	public Enum getSetting( final String category, final Class<? extends Enum> class1, final Enum myDefault )
	{
		final String name = class1.getSimpleName();
		final Property p = this.get( category, name, myDefault.name() );

		try
		{
			return (Enum) class1.getField( p.toString() ).get( class1 );
		}
		catch( final Throwable t )
		{
			// :{
		}

		return myDefault;
	}

	public void setSetting( final String category, final Enum s )
	{
		final String name = s.getClass().getSimpleName();
		this.get( category, name, s.name() ).set( s.name() );
		this.save();
	}

	public PowerUnits selectedPowerUnit()
	{
		return this.selectedPowerUnit;
	}

	public void nextPowerUnit( final boolean backwards )
	{
		this.selectedPowerUnit = Platform.rotateEnum( this.selectedPowerUnit, backwards, Settings.POWER_UNITS.getPossibleValues() );
		this.save();
	}

	// Getters
	public boolean isRemoveCrashingItemsOnLoad()
	{
		return this.removeCrashingItemsOnLoad;
	}

	public int getFormationPlaneEntityLimit()
	{
		return this.formationPlaneEntityLimit;
	}

	public boolean isEnableEffects()
	{
		return this.enableEffects;
	}

	public boolean isUseLargeFonts()
	{
		return this.useLargeFonts;
	}

	public boolean isUseColoredCraftingStatus()
	{
		return this.useColoredCraftingStatus;
	}

	public boolean isDisableColoredCableRecipesInJEI()
	{
		return this.disableColoredCableRecipesInJEI;
	}

	public int getCraftingCalculationTimePerTick()
	{
		return this.craftingCalculationTimePerTick;
	}

	public PowerUnits getSelectedPowerUnit()
	{
		return this.selectedPowerUnit;
	}

	public int[] getCraftByStacks()
	{
		return this.craftByStacks;
	}

	public int[] getPriorityByStacks()
	{
		return this.priorityByStacks;
	}

	public int[] getLevelByStacks()
	{
		return this.levelByStacks;
	}

	public int getStorageProviderID()
	{
		return this.storageProviderID;
	}

	public int getStorageDimensionID()
	{
		return this.storageDimensionID;
	}

	public double getSpatialPowerExponent()
	{
		return this.spatialPowerExponent;
	}

	public double getSpatialPowerMultiplier()
	{
		return this.spatialPowerMultiplier;
	}

	public String[] getGrinderOres()
	{
		return this.grinderOres;
	}

	public Set<String> getGrinderBlackList()
	{
		return this.grinderBlackList;
	}

	public double getOreDoublePercentage()
	{
		return this.oreDoublePercentage;
	}

	public int getWirelessTerminalBattery()
	{
		return this.wirelessTerminalBattery;
	}

	public int getEntropyManipulatorBattery()
	{
		return this.entropyManipulatorBattery;
	}

	public int getMatterCannonBattery()
	{
		return this.matterCannonBattery;
	}

	public int getPortableCellBattery()
	{
		return this.portableCellBattery;
	}

	public int getColorApplicatorBattery()
	{
		return this.colorApplicatorBattery;
	}

	public int getChargedStaffBattery()
	{
		return this.chargedStaffBattery;
	}

	public float getSpawnChargedChance()
	{
		return this.spawnChargedChance;
	}

	public int getQuartzOresPerCluster()
	{
		return this.quartzOresPerCluster;
	}

	public int getQuartzOresClusterAmount()
	{
		return this.quartzOresClusterAmount;
	}

	public String[] getNonBlockingItems()
	{
		return nonBlockingItems;
	}

	public int getChargedChange()
	{
		return this.chargedChange;
	}

	public int getMinMeteoriteDistance()
	{
		return this.minMeteoriteDistance;
	}

	public int getMinMeteoriteDistanceSq()
	{
		return this.minMeteoriteDistanceSq;
	}

	public double getMeteoriteClusterChance()
	{
		return this.meteoriteClusterChance;
	}

	public int getMeteoriteMaximumSpawnHeight()
	{
		return this.meteoriteMaximumSpawnHeight;
	}

	public int[] getMeteoriteDimensionWhitelist()
	{
		return this.meteoriteDimensionWhitelist;
	}

	public double getWirelessBaseCost()
	{
		return this.wirelessBaseCost;
	}

	public double getWirelessCostMultiplier()
	{
		return this.wirelessCostMultiplier;
	}

	public double getWirelessTerminalDrainMultiplier()
	{
		return this.wirelessTerminalDrainMultiplier;
	}

	public double getWirelessBaseRange()
	{
		return this.wirelessBaseRange;
	}

	public double getWirelessBoosterRangeMultiplier()
	{
		return this.wirelessBoosterRangeMultiplier;
	}

	public double getWirelessBoosterExp()
	{
		return this.wirelessBoosterExp;
	}

	public double getWirelessHighWirelessCount()
	{
		return this.wirelessHighWirelessCount;
	}

	// Setters keep visibility as low as possible.

	void setStorageProviderID( int id )
	{
		this.storageProviderID = id;
	}

	void setStorageDimensionID( int id )
	{
		this.storageDimensionID = id;
	}
}
