package appeng.core;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
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
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class AEConfig extends Configuration implements IConfigurableObject, IConfigManagerHost
{

	public static AEConfig instance;

	public static final double TunnelPowerLoss = 0.05;

	public String latestVersion = VERSION;
	public long latestTimeStamp = 0;

	public static final String VERSION = "@version@";
	public static final String CHANNEL = "@aechannel@";

	public final static String PACKET_CHANNEL = "AE";

	public final IConfigManager settings = new ConfigManager( this );
	public final EnumSet<AEFeature> featureFlags = EnumSet.noneOf( AEFeature.class );
	PowerUnits selectedPowerUnit = PowerUnits.AE;

	public int storageBiomeID = -1;
	public int storageProviderID = -1;

	public int formationPlaneEntityLimit = 128;

	public float spawnChargedChance = 0.92f;
	public int quartzOresPerCluster = 4;
	public int quartzOresClusterAmount = 15;
	public int chargedChange = 4;
	public int minMeteoriteDistance = 707;
	public int minMeteoriteDistanceSq = minMeteoriteDistance * minMeteoriteDistance;

	private double WirelessBaseCost = 8;
	private double WirelessCostMultiplier = 1;
	private final double WirelessHighWirelessCount = 64;
	private double WirelessTerminalDrainMultiplier = 1;
	private double teleporterDrainMultiplier = 50;
	private double teleporterDimensionTravelDrain = 75000;

	private double WirelessBaseRange = 16;
	private double WirelessBoosterRangeMultiplier = 1;
	private double WirelessBoosterExp = 1.5;

	public double wireless_getDrainRate(double range)
	{
		return WirelessTerminalDrainMultiplier * range;
	}

	public double teleporter_getDrain(double distance, boolean dimTravel)
	{
		if (dimTravel) {
			if (teleporterDimensionTravelDrain<0) {
				return -1;
			} else {
				return teleporterDimensionTravelDrain;
			}
		} else {
			return teleporterDrainMultiplier * distance;
		}
	}

	public double wireless_getMaxRange(int boosters)
	{
		return WirelessBaseRange + WirelessBoosterRangeMultiplier * Math.pow( boosters, WirelessBoosterExp );
	}

	public double wireless_getPowerDrain(int boosters)
	{
		return WirelessBaseCost + WirelessCostMultiplier * Math.pow( boosters, 1 + boosters / WirelessHighWirelessCount );
	}

	@Override
	public Property get(String category, String key, String defaultValue, String comment, Property.Type type)
	{
		Property prop = super.get( category, key, defaultValue, comment, type );

		if ( prop != null )
		{
			if ( !category.equals( "Client" ) )
				prop.setRequiresMcRestart( true );
		}

		return prop;
	}

	public double spatialPowerExponent = 1.35;
	public double spatialPowerMultiplier = 1250.0;

	public String grinderOres[] = {
			// Vanilla Items
			"Obsidian", "Ender", "EnderPearl", "Coal", "Iron", "Gold", "Charcoal", "NetherQuartz",
			// Common Mod Ores
			"Copper", "Tin", "Silver", "Lead", "Bronze",
			// AE
			"CertusQuartz", "Wheat", "Fluix",
			// Other Mod Ores
			"Brass", "Platinum", "Nickel", "Invar", "Aluminium", "Electrum", "Osmium" };

	public double oreDoublePercentage = 90.0;

	public boolean enableEffects = true;
	public boolean useLargeFonts = false;
	public final int[] craftByStacks = new int[] { 1, 10, 100, 1000 };
	public final int[] priorityByStacks = new int[] { 1, 10, 100, 1000 };
	public final int[] levelByStacks = new int[] { 1, 10, 100, 1000 };

	public int wirelessTerminalBattery = 1600000;
	public int entropyManipulatorBattery = 200000;
	public int matterCannonBattery = 200000;
	public int portableCellBattery = 20000;
	public int colorApplicatorBattery = 20000;
	public int chargedStaffBattery = 8000;

	public boolean disableColoredCableRecipesInNEI = true;

	public boolean updatable = false;
	final private File myPath;

	public double meteoriteClusterChance = 0.1;
	public double meteoriteSpawnChance = 0.3;
	public int[] meteoriteDimensionWhitelist = new int[] { 0 };

	public int craftingCalculationTimePerTick = 5;

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if ( eventArgs.modID.equals( AppEng.modid ) )
		{
			clientSync();
		}
	}

	private void clientSync()
	{
		disableColoredCableRecipesInNEI = get( "Client", "disableColoredCableRecipesInNEI", true ).getBoolean( true );
		enableEffects = get( "Client", "enableEffects", true ).getBoolean( true );
		useLargeFonts = get( "Client", "useTerminalUseLargeFont", false ).getBoolean( false );

		// load buttons..
		for (int btnNum = 0; btnNum < 4; btnNum++)
		{
			Property cmb = get( "Client", "craftAmtButton" + (btnNum + 1), craftByStacks[btnNum] );
			Property pmb = get( "Client", "priorityAmtButton" + (btnNum + 1), priorityByStacks[btnNum] );
			Property lmb = get( "Client", "levelAmtButton" + (btnNum + 1), levelByStacks[btnNum] );

			int buttonCap = (int) (Math.pow( 10, btnNum + 1 ) - 1);

			craftByStacks[btnNum] = Math.abs( cmb.getInt( craftByStacks[btnNum] ) );
			priorityByStacks[btnNum] = Math.abs( pmb.getInt( priorityByStacks[btnNum] ) );
			levelByStacks[btnNum] = Math.abs( pmb.getInt( levelByStacks[btnNum] ) );

			cmb.comment = "Controls buttons on Crafting Screen : Capped at " + buttonCap;
			pmb.comment = "Controls buttons on Priority Screen : Capped at " + buttonCap;
			lmb.comment = "Controls buttons on Level Emitter Screen : Capped at " + buttonCap;

			craftByStacks[btnNum] = Math.min( craftByStacks[btnNum], buttonCap );
			priorityByStacks[btnNum] = Math.min( priorityByStacks[btnNum], buttonCap );
			levelByStacks[btnNum] = Math.min( levelByStacks[btnNum], buttonCap );
		}

		for (Enum e : settings.getSettings())
		{
			String Category = "Client"; // e.getClass().getSimpleName();
			Enum value = settings.getSetting( e );

			Property p = this.get( Category, e.name(), value.name(), getListComment( value ) );

			try
			{
				value = Enum.valueOf( value.getClass(), p.getString() );
			}
			catch (IllegalArgumentException er)
			{
				AELog.info( "Invalid value '" + p.getString() + "' for " + e.name() + " using '" + value.name() + "' instead" );
			}

			settings.putSetting( e, value );
		}

	}

	public boolean disableColoredCableRecipesInNEI()
	{
		return disableColoredCableRecipesInNEI;
	}

	public String getFilePath()
	{
		return myPath.toString();
	}

	public AEConfig(String path) {
		super( new File( path + "AppliedEnergistics2.cfg" ) );
		myPath = new File( path + "AppliedEnergistics2.cfg" );

		FMLCommonHandler.instance().bus().register( this );

		final double DEFAULT_BC_EXCHANGE = 5.0;
		final double DEFAULT_IC2_EXCHANGE = 2.0;
		final double DEFAULT_RTC_EXCHANGE = 1.0 / 11256.0;
		final double DEFAULT_RF_EXCHANGE = 0.5;
		final double DEFAULT_MEKANISM_EXCHANGE = 0.2;

		PowerUnits.MJ.conversionRatio = get( "PowerRatios", "BuildCraft", DEFAULT_BC_EXCHANGE ).getDouble( DEFAULT_BC_EXCHANGE );
		PowerUnits.MK.conversionRatio = get( "PowerRatios", "Mekanism", DEFAULT_MEKANISM_EXCHANGE ).getDouble( DEFAULT_MEKANISM_EXCHANGE );
		PowerUnits.EU.conversionRatio = get( "PowerRatios", "IC2", DEFAULT_IC2_EXCHANGE ).getDouble( DEFAULT_IC2_EXCHANGE );
		PowerUnits.WA.conversionRatio = get( "PowerRatios", "RotaryCraft", DEFAULT_RTC_EXCHANGE ).getDouble( DEFAULT_RTC_EXCHANGE );
		PowerUnits.RF.conversionRatio = get( "PowerRatios", "ThermalExpansion", DEFAULT_RF_EXCHANGE ).getDouble( DEFAULT_RF_EXCHANGE );

		double usageEffective = get( "PowerRatios", "UsageMultiplier", 1.0 ).getDouble( 1.0 );
		PowerMultiplier.CONFIG.multiplier = Math.max( 0.01, usageEffective );

		CondenserOutput.MATTER_BALLS.requiredPower = get( "Condenser", "MatterBalls", 256 ).getInt( 256 );
		CondenserOutput.SINGULARITY.requiredPower = get( "Condenser", "Singularity", 256000 ).getInt( 256000 );

		grinderOres = get( "GrindStone", "grinderOres", grinderOres ).getStringList();
		oreDoublePercentage = get( "GrindStone", "oreDoublePercentage", oreDoublePercentage ).getDouble( oreDoublePercentage );

		settings.registerSetting( Settings.SEARCH_TOOLTIPS, YesNo.YES );
		settings.registerSetting( Settings.TERMINAL_STYLE, TerminalStyle.TALL );
		settings.registerSetting( Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH );

		spawnChargedChance = (float) (1.0 - get( "worldGen", "spawnChargedChance", 1.0 - spawnChargedChance ).getDouble( 1.0 - spawnChargedChance ));
		minMeteoriteDistance = get( "worldGen", "minMeteoriteDistance", minMeteoriteDistance ).getInt( minMeteoriteDistance );
		meteoriteClusterChance = get( "worldGen", "meteoriteClusterChance", meteoriteClusterChance ).getDouble( meteoriteClusterChance );
		meteoriteSpawnChance = get( "worldGen", "meteoriteSpawnChance", meteoriteSpawnChance ).getDouble( meteoriteSpawnChance );
		meteoriteDimensionWhitelist = get ("worldGen", "meteoriteDimensionWhitelist", meteoriteDimensionWhitelist).getIntList();

		quartzOresPerCluster = get( "worldGen", "quartzOresPerCluster", quartzOresPerCluster ).getInt( quartzOresPerCluster );
		quartzOresClusterAmount = get( "worldGen", "quartzOresClusterAmount", quartzOresClusterAmount ).getInt( quartzOresClusterAmount );

		minMeteoriteDistanceSq = minMeteoriteDistance * minMeteoriteDistance;

		addCustomCategoryComment(
				"wireless",
				"Range= WirelessBaseRange + WirelessBoosterRangeMultiplier * Math.pow( boosters, WirelessBoosterExp )\nPowerDrain= WirelessBaseCost + WirelessCostMultiplier * Math.pow( boosters, 1 + boosters / WirelessHighWirelessCount )" );

		WirelessBaseCost = get( "wireless", "WirelessBaseCost", WirelessBaseCost ).getDouble( WirelessBaseCost );
		WirelessCostMultiplier = get( "wireless", "WirelessCostMultiplier", WirelessCostMultiplier ).getDouble( WirelessCostMultiplier );
		WirelessBaseRange = get( "wireless", "WirelessBaseRange", WirelessBaseRange ).getDouble( WirelessBaseRange );
		WirelessBoosterRangeMultiplier = get( "wireless", "WirelessBoosterRangeMultiplier", WirelessBoosterRangeMultiplier ).getDouble(
				WirelessBoosterRangeMultiplier );
		WirelessBoosterExp = get( "wireless", "WirelessBoosterExp", WirelessBoosterExp ).getDouble( WirelessBoosterExp );
		WirelessTerminalDrainMultiplier = get( "wireless", "WirelessTerminalDrainMultiplier", WirelessTerminalDrainMultiplier ).getDouble(
				WirelessTerminalDrainMultiplier );
		teleporterDrainMultiplier = get( "teleporter", "teleporterTerminalDrainMultiplier", teleporterDrainMultiplier ).getDouble(
				teleporterDrainMultiplier );
		teleporterDimensionTravelDrain = get( "teleporter", "teleporterDimensionTravelDrain", teleporterDimensionTravelDrain ).getDouble(
				teleporterDimensionTravelDrain );

		formationPlaneEntityLimit = get( "automation", "formationPlaneEntityLimit", formationPlaneEntityLimit ).getInt( formationPlaneEntityLimit );

		wirelessTerminalBattery = get( "battery", "wirelessTerminal", wirelessTerminalBattery ).getInt( wirelessTerminalBattery );
		chargedStaffBattery = get( "battery", "chargedStaff", chargedStaffBattery ).getInt( chargedStaffBattery );
		entropyManipulatorBattery = get( "battery", "entropyManipulator", entropyManipulatorBattery ).getInt( entropyManipulatorBattery );
		portableCellBattery = get( "battery", "portableCell", portableCellBattery ).getInt( portableCellBattery );
		colorApplicatorBattery = get( "battery", "colorApplicator", colorApplicatorBattery ).getInt( colorApplicatorBattery );
		matterCannonBattery = get( "battery", "matterCannon", matterCannonBattery ).getInt( matterCannonBattery );

		clientSync();

		for (AEFeature feature : AEFeature.values())
		{
			if ( feature.isVisible )
			{
				if ( get( "Features." + feature.category, feature.name(), feature.defaultValue ).getBoolean( feature.defaultValue ) )
					featureFlags.add( feature );
			}
			else
				featureFlags.add( feature );
		}

		ModContainer imb = cpw.mods.fml.common.Loader.instance().getIndexedModList().get( "ImmibisCore" );
		if ( imb != null )
		{
			List<String> version = Arrays.asList( "59.0.0", "59.0.1", "59.0.2" );
			if ( version.contains( imb.getVersion() ) )
				featureFlags.remove( AEFeature.AlphaPass );
		}

		try
		{
			selectedPowerUnit = PowerUnits.valueOf( get( "Client", "PowerUnit", selectedPowerUnit.name(), getListComment( selectedPowerUnit ) ).getString() );
		}
		catch (Throwable t)
		{
			selectedPowerUnit = PowerUnits.AE;
		}

		for (TickRates tr : TickRates.values())
		{
			tr.Load( this );
		}

		if ( isFeatureEnabled( AEFeature.SpatialIO ) )
		{
			storageBiomeID = get( "spatialio", "storageBiomeID", storageBiomeID ).getInt( storageBiomeID );
			storageProviderID = get( "spatialio", "storageProviderID", storageProviderID ).getInt( storageProviderID );
			spatialPowerMultiplier = get( "spatialio", "spatialPowerMultiplier", spatialPowerMultiplier ).getDouble( spatialPowerMultiplier );
			spatialPowerExponent = get( "spatialio", "spatialPowerExponent", spatialPowerExponent ).getDouble( spatialPowerExponent );
		}

		if ( isFeatureEnabled( AEFeature.CraftingCPU ) )
		{
			craftingCalculationTimePerTick = get( "craftingCPU", "craftingCalculationTimePerTick", craftingCalculationTimePerTick ).getInt(
					craftingCalculationTimePerTick );
		}

		if ( isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			try
			{
				latestVersion = get( "VersionChecker", "LatestVersion", "" ).getString();
				latestTimeStamp = Long.parseLong( get( "VersionChecker", "LatestTimeStamp", "" ).getString() );
			}
			catch (NumberFormatException err)
			{
				latestTimeStamp = 0;
			}
		}

		updatable = true;
	}

	public boolean useAEVersion(MaterialType mt)
	{
		if ( isFeatureEnabled( AEFeature.WebsiteRecipes ) )
			return true;

		setCategoryComment(
				"OreCamouflage",
				"AE2 Automatically uses alternative ores present in your instance of MC to blend better with its surroundings, if you prefer you can disable this selectively using these flags; Its important to note, that some if these items even if enabled may not be craftable in game because other items are overriding their recipes." );
		Property p = get( "OreCamouflage", mt.name(), true );
		p.comment = "OreDictionary Names: " + mt.getOreName();

		return !p.getBoolean( true );
	}

	private String getListComment(Enum value)
	{
		String comment = null;

		if ( value != null )
		{
			EnumSet set = EnumSet.allOf( value.getClass() );

			for (Object Oeg : set)
			{
				Enum eg = (Enum) Oeg;
				if ( comment == null )
					comment = "Possible Values: " + eg.name();
				else
					comment += ", " + eg.name();
			}
		}

		return comment;
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum setting, Enum newValue)
	{
		for (Enum e : settings.getSettings())
		{
			if ( e == setting )
			{
				String Category = "Client";
				Property p = this.get( Category, e.name(), settings.getSetting( e ).name(), getListComment( newValue ) );
				p.set( newValue.name() );
			}
		}

		if ( updatable )
			save();
	}

	@Override
	public void save()
	{
		if ( isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			get( "VersionChecker", "LatestVersion", latestVersion ).set( latestVersion );
			get( "VersionChecker", "LatestTimeStamp", "" ).set( Long.toString( latestTimeStamp ) );
		}

		if ( isFeatureEnabled( AEFeature.SpatialIO ) )
		{
			get( "spatialio", "storageBiomeID", storageBiomeID ).set( storageBiomeID );
			get( "spatialio", "storageProviderID", storageProviderID ).set( storageProviderID );
		}

		get( "Client", "PowerUnit", selectedPowerUnit.name(), getListComment( selectedPowerUnit ) ).set( selectedPowerUnit.name() );

		if ( hasChanged() )
			super.save();
	}

	public int getFreeIDSLot(int varID, String Category)
	{
		boolean alreadyUsed = false;
		int min = 0;

		for (Property p : getCategory( Category ).getValues().values())
		{
			int thisInt = p.getInt();

			if ( varID == thisInt )
				alreadyUsed = true;

			min = Math.max( min, thisInt + 1 );
		}

		if ( alreadyUsed )
		{
			if ( min < 16383 )
				min = 16383;

			return min;
		}

		return varID;
	}

	public int getFreeMaterial(int varID)
	{
		return getFreeIDSLot( varID, "materials" );
	}

	public int getFreePart(int varID)
	{
		return getFreeIDSLot( varID, "parts" );
	}

	@Override
	public IConfigManager getConfigManager()
	{
		return settings;
	}

	public boolean isFeatureEnabled(AEFeature f)
	{
		return featureFlags.contains( f );
	}

	public boolean useTerminalUseLargeFont()
	{
		return useLargeFonts;
	}

	public int craftItemsByStackAmounts(int i)
	{
		return craftByStacks[i];
	}

	public int priorityByStacksAmounts(int i)
	{
		return priorityByStacks[i];
	}

	public int levelByStackAmounts(int i)
	{
		return levelByStacks[i];
	}

	public Enum getSetting(String Category, Class<? extends Enum> class1, Enum myDefault)
	{
		String name = class1.getSimpleName();
		Property p = get( Category, name, myDefault.name() );

		try
		{
			return (Enum) class1.getField( p.toString() ).get( class1 );
		}
		catch (Throwable t)
		{
			// :{
		}

		return myDefault;
	}

	public void setSetting(String Category, Enum s)
	{
		String name = s.getClass().getSimpleName();
		get( Category, name, s.name() ).set( s.name() );
		save();
	}

	public PowerUnits selectedPowerUnit()
	{
		return selectedPowerUnit;
	}

	public void nextPowerUnit(boolean backwards)
	{
		selectedPowerUnit = Platform.rotateEnum( selectedPowerUnit, backwards, Settings.POWER_UNITS.getPossibleValues() );
		save();
	}

}
