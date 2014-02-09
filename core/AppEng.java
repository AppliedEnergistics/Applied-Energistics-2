package appeng.core;

import appeng.core.crash.CrashEnhancement;
import appeng.core.crash.CrashInfo;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.helpers.TickHandler;
import appeng.integration.IntegrationRegistry;
import appeng.integration.IntegrationSide;
import appeng.server.AECommand;
import appeng.services.Profiler;
import appeng.services.VersionChecker;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = AppEng.modid, name = AppEng.name, version = AEConfig.VERSION, dependencies = AppEng.dependencies)
public class AppEng
{

	public final static String modid = "appliedenergistics2";
	public final static String name = "Applied Energistics 2";

	public static AppEng instance;

	public final static String dependencies =

	// a few mods, AE should load after, probably.
	// required-after:AppliedEnergistics2API|all;
	"after:gregtech_addon;after:Mekanism;after:IC2;after:ThermalExpansion;after:BuildCraft|Core;" +

	// depend on version of forge used for build.
			"required-after:Forge@[" // require forge.
			+ net.minecraftforge.common.ForgeVersion.majorVersion + "." // majorVersion
			+ net.minecraftforge.common.ForgeVersion.minorVersion + "." // minorVersion
			+ net.minecraftforge.common.ForgeVersion.revisionVersion + "." // revisionVersion
			+ net.minecraftforge.common.ForgeVersion.buildVersion + ",)"; // buildVersion

	public AppEng() {

		instance = this;

		for (CrashInfo ci : CrashInfo.values())
			FMLCommonHandler.instance().registerCrashCallable( new CrashEnhancement( ci ) );
	}

	private IntegrationRegistry integrationModules = new IntegrationRegistry( new Object[] {

	/**
	 * Display Name, ModID ClassPostFix
	 */
	IntegrationSide.BOTH, "Industrial Craft 2", "IC2", "IC2", // IC2
			IntegrationSide.BOTH, "Railcraft", "Railcraft", "RC", // RC
			IntegrationSide.BOTH, "Thermal Expansion", "ThermalExpansion", "TE", // TE
			IntegrationSide.BOTH, "Mystcraft", "Mystcraft", "Mystcraft", // MC
			IntegrationSide.BOTH, "BuildCraft", "BuildCraft|Silicon", "BC", // BC
			IntegrationSide.BOTH, "BuildCraft Power", null, "MJ", // BC
			IntegrationSide.BOTH, "Greg Tech", "gregtech_addon", "GT", // GT
			IntegrationSide.BOTH, "Universal Electricity", null, "UE", // UE
			IntegrationSide.BOTH, "Logistics Pipes", "LogisticsPipes|Main", "LP", // LP
			// IntegrationSide.CLIENT, "Inventory Tweaks", "", "InvTweaks",
			IntegrationSide.BOTH, "Mine Factory Reloaded", "MineFactoryReloaded", "MFR", // MFR
			IntegrationSide.BOTH, "Better Storage", "betterstorage", "BS", // BS
			IntegrationSide.BOTH, "Factorization", "factorization", "FZ", // FZ
			IntegrationSide.BOTH, "Forestry", "Forestry", "Forestry", // Forestry
			IntegrationSide.BOTH, "Mekanism", "Mekanism", "Mekanism", // MeK
			IntegrationSide.CLIENT, "Not Enought Items", "NotEnoughItems", "NEI", // Not
																					// Eneough
																					// Items
			IntegrationSide.BOTH, "Forge MultiPart", "McMultipart", "FMP" } );

	public boolean isIntegrationEnabled(String Name)
	{
		return integrationModules.isEnabled( Name );
	}

	public Object getIntegration(String Name)
	{
		return integrationModules.getInstance( Name );
	}

	private void startService(String serviceName, Thread thread)
	{
		thread.setName( serviceName );
		thread.setPriority( Thread.MIN_PRIORITY );
		thread.start();
	}

	@EventHandler
	void PreInit(FMLPreInitializationEvent event)
	{
		AEConfig.instance = new AEConfig( event.getModConfigurationDirectory() );
		FacadeConfig.instance = new FacadeConfig( event.getModConfigurationDirectory() );

		AELog.info( "Starting ( PreInit )" );

		if ( Platform.isClient() )
		{
			CreativeTab.init();
			CreativeTabFacade.init();
			CommonHelper.proxy.init();
		}

		Registration.instance.PreInit( event );

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.Profiler ) )
		{
			AELog.info( "Starting Profiler" );
			startService( "AE2 Profiler", (new Thread( Profiler.instance = new Profiler() )) );
		}

		if ( AEConfig.instance.isFeatureEnabled( AEFeature.VersionChecker ) )
		{
			AELog.info( "Starting VersionChecker" );
			startService( "AE2 VersionChecker", new Thread( VersionChecker.instance = new VersionChecker() ) );
		}

		AELog.info( "PreInit ( end )" );
	}

	@EventHandler
	void Init(FMLInitializationEvent event)
	{
		AELog.info( "Init" );

		Registration.instance.Init( event );
		integrationModules.init();

		AELog.info( "Init ( end )" );
	}

	@EventHandler
	void PostInit(FMLPostInitializationEvent event)
	{
		AELog.info( "PostInit" );

		Registration.instance.PostInit( event );
		integrationModules.postinit();

		AEConfig.instance.save();

		NetworkRegistry.INSTANCE.registerGuiHandler( this, GuiBridge.GUI_Handler );
		NetworkHandler.instance = new NetworkHandler( "AE2" );

		AELog.info( "PostInit ( end )" );
	}

	@EventHandler
	public void serverStopping(FMLServerStoppingEvent event)
	{
		WorldSettings.getInstance().shutdown();
		TickHandler.instance.shutdown();
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent evt)
	{
		WorldSettings.getInstance().init();
		evt.registerServerCommand( new AECommand( evt.getServer() ) );
	}

}
