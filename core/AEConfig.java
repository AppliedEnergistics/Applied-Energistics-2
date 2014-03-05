package appeng.core;

import java.io.File;
import java.util.EnumSet;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import appeng.api.config.CondenserOuput;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.config.SearchBoxMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigureableObject;
import appeng.core.features.AEFeature;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;

public class AEConfig extends Configuration implements IConfigureableObject, IConfigManagerHost
{

	public static AEConfig instance;

	public static float TunnelPowerLoss = 0.05f;

	public String latestVersion = VERSION;
	public long latestTimeStamp = 0;

	public static final String VERSION = "@version@";
	public static final String CHANNEL = "@aechannel@";

	public final static String PACKET_CHANNEL = "AE";

	public IConfigManager settings = new ConfigManager( this );
	public EnumSet<AEFeature> featureFlags = EnumSet.noneOf( AEFeature.class );

	public int storageBiomeID = -1;
	public int storageProviderID = -1;

	public int oresPerCluster = 4;

	private double WirelessBaseCost = 8;
	private double WirelessCostMultiplier = 1;
	private double WirelessHighWirelessCount = 64;
	private double WirelessTerminalDrainMultiplier = 1;

	private double WirelessBaseRange = 16;
	private double WirelessBoosterRangeMultiplier = 1;
	private double WirelessBoosterExp = 1.5;

	public double wireless_getDrainRate(double range)
	{
		return WirelessTerminalDrainMultiplier * range;
	}

	public double wireless_getMaxRange(int boosters)
	{
		return WirelessBaseRange + WirelessBoosterRangeMultiplier * Math.pow( boosters, WirelessBoosterExp );
	}

	public double wireless_getPowerDrain(int boosters)
	{
		return WirelessBaseCost + WirelessCostMultiplier * Math.pow( boosters, 1 + boosters / WirelessHighWirelessCount );
	}

	public double spatialPowerScaler = 1.5;
	public double spatialPowerMultiplier = 1500.0;

	public String grinderOres[] = {
			// Vanilla Items
			"Obsidian", "Ender", "Coal", "Iron", "Gold", "Charcoal", "NetherQuartz",
			// Common Mod Ores
			"Copper", "Tin", "Silver", "Lead", "Bronze",
			// AE
			"CertusQuartz", "Wheat", "Fluix",
			// Other Mod Ores
			"Brass", "Platinum", "Nickel", "Invar", "Aluminium", "Electrum" };

	public double oreDoublePercentage = 90.0;

	public boolean enableEffects = true;

	public int wireless_battery = 1600000;
	public int manipulator_battery = 200000;
	public int mattercannon_battery = 200000;
	public int portablecell_battery = 20000;
	public int staff_battery = 8000;

	public AEConfig(String path) {
		super( new File( path + "AppliedEnergistics2.cfg" ) );

		final double DEFAULT_BC_EXCHANGE = 5.0;
		// final double DEFAULT_UE_EXCHANGE = 5.0;
		final double DEFAULT_IC2_EXCHANGE = 2.0;
		final double DEFAULT_RTC_EXCHANGE = 1.0 / 11256.0;
		final double DEFAULT_RF_EXCHANGE = 0.5;

		PowerUnits.MJ.conversionRatio = get( "PowerRatios", "BuildCraft", DEFAULT_BC_EXCHANGE ).getDouble( DEFAULT_BC_EXCHANGE );
		// PowerUnits.KJ.conversionRatio = get( "PowerRatios",
		// "UniversalElectricity", DEFAULT_UE_EXCHANGE ).getDouble(
		// DEFAULT_UE_EXCHANGE );
		PowerUnits.EU.conversionRatio = get( "PowerRatios", "IC2", DEFAULT_IC2_EXCHANGE ).getDouble( DEFAULT_IC2_EXCHANGE );
		PowerUnits.WA.conversionRatio = get( "PowerRatios", "RotaryCraft", DEFAULT_RTC_EXCHANGE ).getDouble( DEFAULT_RTC_EXCHANGE );
		PowerUnits.RF.conversionRatio = get( "PowerRatios", "ThermalExpansion", DEFAULT_RF_EXCHANGE ).getDouble( DEFAULT_RF_EXCHANGE );

		double usageEffective = get( "PowerRatios", "UsageMultiplier", 1.0 ).getDouble( 1.0 );
		PowerMultiplier.CONFIG.multiplier = Math.max( 0.01, usageEffective );

		CondenserOuput.MATTER_BALLS.requiredPower = get( "Condenser", "MatterBalls", 256 ).getInt( 256 );
		CondenserOuput.SINGULARITY.requiredPower = get( "Condenser", "Singularity", 256000 ).getInt( 256000 );

		grinderOres = get( "GrindStone", "grinderOres", grinderOres ).getStringList();
		oreDoublePercentage = get( "GrindStone", "oreDoublePercentage", oreDoublePercentage ).getDouble( oreDoublePercentage );
		enableEffects = get( "Client", "enableEffects", true ).getBoolean( true );

		// settings.registerSetting( Settings.SEARCH_MODS, YesNo.YES );
		settings.registerSetting( Settings.SEARCH_TOOLTIPS, YesNo.YES );
		settings.registerSetting( Settings.SEARCH_MODE, SearchBoxMode.AUTOSEARCH );
		// settings.registerSetting( Settings.SORT_BY, SortOrder.NAME );
		// settings.registerSetting( Settings.SORT_DIRECTION, SortDir.ASCENDING );

		WirelessBaseCost = get( "wireless", "WirelessBaseCost", WirelessBaseCost ).getDouble( WirelessBaseCost );
		WirelessCostMultiplier = get( "wireless", "WirelessCostMultiplier", WirelessCostMultiplier ).getDouble( WirelessCostMultiplier );
		WirelessBaseRange = get( "wireless", "WirelessBaseRange", WirelessBaseRange ).getDouble( WirelessBaseRange );
		WirelessBoosterRangeMultiplier = get( "wireless", "WirelessBoosterRangeMultiplier", WirelessBoosterRangeMultiplier ).getDouble(
				WirelessBoosterRangeMultiplier );
		WirelessBoosterExp = get( "wireless", "WirelessBoosterExp", WirelessBoosterExp ).getDouble( WirelessBoosterExp );
		WirelessTerminalDrainMultiplier = get( "wireless", "WirelessTerminalDrainMultiplier", WirelessTerminalDrainMultiplier ).getDouble(
				WirelessTerminalDrainMultiplier );

		wireless_battery = get( "battery", "wireless", wireless_battery ).getInt( wireless_battery );
		staff_battery = get( "battery", "staff", staff_battery ).getInt( staff_battery );
		manipulator_battery = get( "battery", "manipulator", manipulator_battery ).getInt( manipulator_battery );
		portablecell_battery = get( "battery", "portablecell", portablecell_battery ).getInt( portablecell_battery );
		mattercannon_battery = get( "battery", "mattercannon", mattercannon_battery ).getInt( mattercannon_battery );

		for (AEFeature feature : AEFeature.values())
		{
			if ( feature.isVisible() )
			{
				if ( get( "Features." + feature.getCategory(), feature.name(), feature.defaultVaue() ).getBoolean( feature.defaultVaue() ) )
					featureFlags.add( feature );
			}
			else
				featureFlags.add( feature );
		}

		for (Enum e : settings.getSettings())
		{
			String Category = e.getClass().getSimpleName();
			this.get( Category, e.name(), settings.getSetting( e ).name() );
		}

		if ( isFeatureEnabled( AEFeature.SpatialIO ) )
		{
			storageBiomeID = get( "spatialio", "storageBiomeID", storageBiomeID ).getInt( storageBiomeID );
			storageProviderID = get( "spatialio", "storageProviderID", storageProviderID ).getInt( storageProviderID );
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
	}

	@Override
	public void updateSetting(IConfigManager manager, Enum setting, Enum newValue)
	{
		for (Enum e : settings.getSettings())
		{
			String Category = e.getClass().getSimpleName();
			this.get( Category, e.name(), settings.getSetting( e ).name() );
		}

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

		if ( hasChanged() )
			super.save();
	}

	public int getFreeMaterial()
	{
		int min = 0;
		for (Property p : getCategory( "materials" ).getValues().values())
			min = Math.max( min, p.getInt() + 1 );
		return min;
	}

	public int getFreePart()
	{
		int min = 0;
		for (Property p : getCategory( "parts" ).getValues().values())
			min = Math.max( min, p.getInt() + 1 );
		return min;
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

	public int getBlockID(Class c, String subname)
	{
		return 0;
		// return getBlock( AEFeatureHandler.getName( c, subname ), blkBaseNumber++ ).getInt();
	}

	public int getItemID(Class c, String subname)
	{
		return 0;
		// return getItem( AEFeatureHandler.getName( c, subname ), blkItemNumber++ ).getInt();
	}

	public boolean useTerminalUseLargeFont()
	{
		return false;
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
}
