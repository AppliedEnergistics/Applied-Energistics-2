package appeng.core.features;

public enum AEFeature
{
	Core(null), // stuff that has no reason for ever being turned off, or that
				// is just flat out required by tons of
				// important stuff.

	CertusQuartzWorldGen("World"), MeteoriteWorldGen("World"),

	DecorativeLights("World"), DecorativeQuartzBlocks("World"), SkyStoneChests("World"), SpawnPressesInMeteorites("World"),

	GrindStone("World"), Flour("World"), Inscriber("World"),

	ChestLoot("World"), VillagerTrading("World"),

	TinyTNT("World"),

	PoweredTools("ToolsClassifications"),

	CertusQuartzTools("ToolsClassifications"),

	NetherQuartzTools("ToolsClassifications"),

	QuartzHoe("Tools"), QuartzSpade("Tools"), QuartzSword("Tools"), QuartzPickaxe("Tools"), QuartzAxe("Tools"), QuartzKnife("Tools"), QuartzWrench("Tools"),

	ChargedStaff("Tools"), EntropyManipulator("Tools"), MatterCannon("Tools"), WirelessAccessTerminal("Tools"), ColorApplicator("Tools"),

	CraftingCPU("CraftingFeatures"), PowerGen("NetworkFeatures"), Security("NetworkFeatures"),

	// Crafting("NetworkFeatures"), MolecularAssembler("NetworkFeatures"),

	SpatialIO("NetworkFeatures"), QuantumNetworkBridge("NetworkFeatures"),

	LevelEmiter("NetworkBuses"), CraftingTerminal("NetworkBuses"), StorageMonitor("NetworkBuses"), P2PTunnel("NetworkBuses"), FormationPlane("NetworkBuses"), AnnihilationPlane(
			"NetworkBuses"), ImportBus("NetworkBuses"), ExportBus("NetworkBuses"), StorageBus("NetworkBuses"), PartConversionMonitor("NetworkBuses"),

	StorageCells("Storage"), PortableCell("PortableCell"), MEChest("Storage"), MEDrive("Storage"), IOPort("Storage"),

	NetworkTool("NetworkTool"),

	DenseEnergyCells("HigherCapacity"), DenseCables("HigherCapacity"),

	P2PTunnelRF("P2PTunnels"), P2PTunnelME("P2PTunnels"), P2PTunnelItems("P2PTunnels"), P2PTunnelRedstone("P2PTunnels"), P2PTunnelEU("P2PTunnels"), P2PTunnelMJ(
			"P2PTunnels"), P2PTunnelLiquids("P2PTunnels"), P2PTunnelLight("P2PTunnels"),

	MassCannonBlockDamage("BlockFeatures"), TinyTNTBlockDamage("BlockFeatures"), Facades("Facades"),

	VersionChecker("Services"), UnsupportedDeveloperTools("Misc", false), Creative("Misc"),

	GrinderLogging("Misc", false), Logging("Misc"), IntegrationLogging("Misc", false), CustomRecipes("Crafting", false), WebsiteRecipes("Misc", false),

	enableFacadeCrafting("Crafting"), inWorldSingularity("Crafting"), inWorldFluix("Crafting"), inWorldPurification("Crafting"), UpdateLogging("Misc", false),

	AlphaPass("Rendering"), PaintBalls("Tools"), PacketLogging("Misc", false), CraftingLog("Misc", false), InterfaceTerminal("Crafting"), LightDetector("Misc"),

	enableDisassemblyCrafting("Crafting"), MolecularAssembler("CraftingFeatures"), MeteoriteCompass("Tools"), Patterns("CraftingFeatures"),

	ChunkLoggerTrace("Commands", false);

	String Category;
	boolean visible = true;
	boolean defValue = true;

	private AEFeature(String cat) {
		Category = cat;
		visible = !this.name().equals( "Core" );
	}

	private AEFeature(String cat, boolean defv) {
		this( cat );
		defValue = defv;
	}

	public String getCategory()
	{
		return Category;
	}

	public Boolean defaultVaue()
	{
		return defValue;
	}

	public Boolean isVisible()
	{
		return visible;
	}

}
