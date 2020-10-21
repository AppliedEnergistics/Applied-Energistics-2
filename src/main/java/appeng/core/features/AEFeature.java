/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.core.features;


public enum AEFeature
{
	// stuff that has no reason for ever being turned off, or that
	// is just flat out required by tons of
	// important stuff.
	CORE( "Core", null )
	{
		@Override
		public boolean isVisible()
		{
			return false;
		}
	},

	CERTUS_QUARTZ_WORLD_GEN( "CertusQuartzWorldGen", Constants.CATEGORY_WORLD ),
	METEORITE_WORLD_GEN( "MeteoriteWorldGen", Constants.CATEGORY_WORLD ),
	DECORATIVE_LIGHTS( "DecorativeLights", Constants.CATEGORY_WORLD ),
	DECORATIVE_BLOCKS( "DecorativeBlocks", Constants.CATEGORY_WORLD, "Blocks that are not used in any essential recipes, also slabs and stairs." ),
	SKY_STONE_CHESTS( "SkyStoneChests", Constants.CATEGORY_WORLD ),
	SPAWN_PRESSES_IN_METEORITES( "SpawnPressesInMeteorites", Constants.CATEGORY_WORLD ),
	FLOUR( "Flour", Constants.CATEGORY_WORLD ),
	CHEST_LOOT( "ChestLoot", Constants.CATEGORY_WORLD ),
	VILLAGER_TRADING( "VillagerTrading", Constants.CATEGORY_WORLD ),
	TINY_TNT( "TinyTNT", Constants.CATEGORY_WORLD ),
	CERTUS_ORE( "CertusOre", Constants.CATEGORY_WORLD ),
	CHARGED_CERTUS_ORE( "ChargedCertusOre", Constants.CATEGORY_WORLD ),

	GRIND_STONE( "GrindStone", Constants.CATEGORY_MACHINES ),
	INSCRIBER( "Inscriber", Constants.CATEGORY_MACHINES ),
	CHARGER( "Charger", Constants.CATEGORY_MACHINES ),
	CRYSTAL_GROWTH_ACCELERATOR( "CrystalGrowthAccelerator", Constants.CATEGORY_MACHINES ),
	POWER_GEN( "VibrationChamber", Constants.CATEGORY_MACHINES ),

	POWERED_TOOLS( "PoweredTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),
	CERTUS_QUARTZ_TOOLS( "CertusQuartzTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),
	NETHER_QUARTZ_TOOLS( "NetherQuartzTools", Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),

	QUARTZ_HOE( "QuartzHoe", Constants.CATEGORY_TOOLS ),
	QUARTZ_SPADE( "QuartzSpade", Constants.CATEGORY_TOOLS ),
	QUARTZ_SWORD( "QuartzSword", Constants.CATEGORY_TOOLS ),
	QUARTZ_PICKAXE( "QuartzPickaxe", Constants.CATEGORY_TOOLS ),
	QUARTZ_AXE( "QuartzAxe", Constants.CATEGORY_TOOLS ),
	QUARTZ_KNIFE( "QuartzKnife", Constants.CATEGORY_TOOLS ),
	QUARTZ_WRENCH( "QuartzWrench", Constants.CATEGORY_TOOLS ),
	CHARGED_STAFF( "ChargedStaff", Constants.CATEGORY_TOOLS ),
	ENTROPY_MANIPULATOR( "EntropyManipulator", Constants.CATEGORY_TOOLS ),
	MATTER_CANNON( "MatterCannon", Constants.CATEGORY_TOOLS ),
	WIRELESS_ACCESS_TERMINAL( "WirelessAccessTerminal", Constants.CATEGORY_TOOLS ),
	COLOR_APPLICATOR( "ColorApplicator", Constants.CATEGORY_TOOLS ),
	METEORITE_COMPASS( "MeteoriteCompass", Constants.CATEGORY_TOOLS ),

	SECURITY( "Security", Constants.CATEGORY_NETWORK_FEATURES ),
	SPATIAL_IO( "SpatialIO", Constants.CATEGORY_NETWORK_FEATURES ),
	QUANTUM_NETWORK_BRIDGE( "QuantumNetworkBridge", Constants.CATEGORY_NETWORK_FEATURES ),
	CHANNELS( "Channels", Constants.CATEGORY_NETWORK_FEATURES ),

	INTERFACE( "Interface", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_INTERFACE( "FluidInterface", Constants.CATEGORY_NETWORK_BUSES ),
	LEVEL_EMITTER( "LevelEmitter", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_LEVEL_EMITTER( "FluidLevelEmitter", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_TERMINAL( "FluidTerminal", Constants.CATEGORY_NETWORK_BUSES ),
	CRAFTING_TERMINAL( "CraftingTerminal", Constants.CATEGORY_NETWORK_BUSES ),
	TERMINAL( "Terminal", Constants.CATEGORY_NETWORK_BUSES ),
	STORAGE_MONITOR( "StorageMonitor", Constants.CATEGORY_NETWORK_BUSES ),
	P2P_TUNNEL( "P2PTunnel", Constants.CATEGORY_NETWORK_BUSES ),
	FORMATION_PLANE( "FormationPlane", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_FORMATION_PLANE( "FluidFormationPlane", Constants.CATEGORY_NETWORK_BUSES ),
	ANNIHILATION_PLANE( "AnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES ),
	IDENTITY_ANNIHILATION_PLANE( "IdentityAnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_ANNIHILATION_PLANE( "FluidAnnihilationPlane", Constants.CATEGORY_NETWORK_BUSES ),
	IMPORT_BUS( "ImportBus", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_IMPORT_BUS( "FluidImportBus", Constants.CATEGORY_NETWORK_BUSES ),
	EXPORT_BUS( "ExportBus", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_EXPORT_BUS( "FluidExportBus", Constants.CATEGORY_NETWORK_BUSES ),
	STORAGE_BUS( "StorageBus", Constants.CATEGORY_NETWORK_BUSES ),
	FLUID_STORAGE_BUS( "FluidStorageBus", Constants.CATEGORY_NETWORK_BUSES ),
	PART_CONVERSION_MONITOR( "PartConversionMonitor", Constants.CATEGORY_NETWORK_BUSES ),
	TOGGLE_BUS( "ToggleBus", Constants.CATEGORY_NETWORK_BUSES ),
	PANELS( "Panels", Constants.CATEGORY_NETWORK_BUSES ),
	QUARTZ_FIBER( "QuartzFiber", Constants.CATEGORY_NETWORK_BUSES ),
	CABLE_ANCHOR( "CableAnchor", Constants.CATEGORY_NETWORK_BUSES ),

	PORTABLE_CELL( "PortableCell", Constants.CATEGORY_PORTABLE_CELL ),

	STORAGE_CELLS( "StorageCells", Constants.CATEGORY_STORAGE ),
	ME_CHEST( "MEChest", Constants.CATEGORY_STORAGE ),
	ME_DRIVE( "MEDrive", Constants.CATEGORY_STORAGE ),
	IO_PORT( "IOPort", Constants.CATEGORY_STORAGE ),
	CONDENSER( "Condenser", Constants.CATEGORY_STORAGE ),

	NETWORK_TOOL( "NetworkTool", Constants.CATEGORY_NETWORK_TOOL ),
	MEMORY_CARD( "MemoryCard", Constants.CATEGORY_NETWORK_TOOL ),

	GLASS_CABLES( "GlassCables", Constants.CATEGORY_CABLES ),
	COVERED_CABLES( "CoveredCables", Constants.CATEGORY_CABLES ),
	SMART_CABLES( "SmartCables", Constants.CATEGORY_CABLES ),
	DENSE_CABLES( "DenseCables", Constants.CATEGORY_CABLES ),

	ENERGY_CELLS( "EnergyCells", Constants.CATEGORY_ENERGY ),
	ENERGY_ACCEPTOR( "EnergyAcceptor", Constants.CATEGORY_ENERGY ),
	DENSE_ENERGY_CELLS( "DenseEnergyCells", Constants.CATEGORY_ENERGY ),

	P2P_TUNNEL_ME( "P2PTunnelME", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_ITEMS( "P2PTunnelItems", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_REDSTONE( "P2PTunnelRedstone", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_EU( "P2PTunnelEU", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_FE( "P2PTunnelFE", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_FLUIDS( "P2PTunnelFluids", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_LIGHT( "P2PTunnelLight", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_OPEN_COMPUTERS( "P2PTunnelOpenComputers", Constants.CATEGORY_P2P_TUNNELS ),
	P2P_TUNNEL_PRESSURE( "P2PTunnelPressure", Constants.CATEGORY_P2P_TUNNELS ),

	MASS_CANNON_BLOCK_DAMAGE( "MassCannonBlockDamage", Constants.CATEGORY_BLOCK_FEATURES ),
	TINY_TNT_BLOCK_DAMAGE( "TinyTNTBlockDamage", Constants.CATEGORY_BLOCK_FEATURES ),

	FACADES( "Facades", Constants.CATEGORY_FACADES ),

	UNSUPPORTED_DEVELOPER_TOOLS( "UnsupportedDeveloperTools", Constants.CATEGORY_MISC, false ),
	CREATIVE( "Creative", Constants.CATEGORY_MISC ),
	GRINDER_LOGGING( "GrinderLogging", Constants.CATEGORY_MISC, false ),
	LOGGING( "Logging", Constants.CATEGORY_MISC ),
	INTEGRATION_LOGGING( "IntegrationLogging", Constants.CATEGORY_MISC, false ),
	WEBSITE_RECIPES( "WebsiteRecipes", Constants.CATEGORY_MISC, false ),
	LOG_SECURITY_AUDITS( "LogSecurityAudits", Constants.CATEGORY_MISC, false ),
	ACHIEVEMENTS( "Achievements", Constants.CATEGORY_MISC ),
	UPDATE_LOGGING( "UpdateLogging", Constants.CATEGORY_MISC, false ),
	PACKET_LOGGING( "PacketLogging", Constants.CATEGORY_MISC, false ),
	CRAFTING_LOG( "CraftingLog", Constants.CATEGORY_MISC, false ),
	LIGHT_DETECTOR( "LightDetector", Constants.CATEGORY_MISC ),
	DEBUG_LOGGING( "DebugLogging", Constants.CATEGORY_MISC, false ),

	ENABLE_FACADE_CRAFTING( "EnableFacadeCrafting", Constants.CATEGORY_CRAFTING ),
	IN_WORLD_SINGULARITY( "InWorldSingularity", Constants.CATEGORY_CRAFTING ),
	IN_WORLD_FLUIX( "InWorldFluix", Constants.CATEGORY_CRAFTING ),
	IN_WORLD_PURIFICATION( "InWorldPurification", Constants.CATEGORY_CRAFTING ),
	INTERFACE_TERMINAL( "InterfaceTerminal", Constants.CATEGORY_CRAFTING ),
	ENABLE_DISASSEMBLY_CRAFTING( "EnableDisassemblyCrafting", Constants.CATEGORY_CRAFTING ),

	ALPHA_PASS( "AlphaPass", Constants.CATEGORY_RENDERING ),
	PAINT_BALLS( "PaintBalls", Constants.CATEGORY_TOOLS ),

	MOLECULAR_ASSEMBLER( "MolecularAssembler", Constants.CATEGORY_CRAFTING_FEATURES ),
	PATTERNS( "Patterns", Constants.CATEGORY_CRAFTING_FEATURES ),
	CRAFTING_CPU( "CraftingCPU", Constants.CATEGORY_CRAFTING_FEATURES ),
	CRAFTING_MANAGER_FALLBACK( "CraftingManagerFallback", Constants.CATEGORY_CRAFTING_FEATURES, "Use CraftingManager to find an alternative recipe, after a pattern rejected an ingredient. Should be enabled to avoid issues, but can have a minor performance impact." ),
	INSANE_BLOCKING_MODE( "InsaneBlockingMode", Constants.CATEGORY_CRAFTING_FEATURES, "Use the default AE2 blocking mode that doesn't work on any machines" ),

	BASIC_CARDS( "BasicCards", Constants.CATEGORY_UPGRADES ),
	ADVANCED_CARDS( "AdvancedCards", Constants.CATEGORY_UPGRADES ),
	VIEW_CELL( "ViewCell", Constants.CATEGORY_UPGRADES ),

	CRYSTAL_SEEDS( "CrystalSeeds", Constants.CATEGORY_MATERIALS ),
	PURE_CRYSTALS( "PureCrystals", Constants.CATEGORY_MATERIALS ),
	CERTUS( "Certus", Constants.CATEGORY_MATERIALS ),
	FLUIX( "Fluix", Constants.CATEGORY_MATERIALS ),
	SILICON( "Silicon", Constants.CATEGORY_MATERIALS ),
	DUSTS( "Dusts", Constants.CATEGORY_MATERIALS ),
	NUGGETS( "Nuggets", Constants.CATEGORY_MATERIALS ),
	QUARTZ_GLASS( "QuartzGlass", Constants.CATEGORY_MATERIALS ),
	SKY_STONE( "SkyStone", Constants.CATEGORY_MATERIALS ),

	PROCESSORS( "Processors", Constants.CATEGORY_COMPONENTS ),
	PRINTED_CIRCUITS( "PrintedCircuits", Constants.CATEGORY_COMPONENTS ),
	PRESSES( "Presses", Constants.CATEGORY_COMPONENTS ),
	MATTER_BALL( "MatterBall", Constants.CATEGORY_COMPONENTS ),
	CORES( "Cores", Constants.CATEGORY_COMPONENTS ),

	CHUNK_LOGGER_TRACE( "ChunkLoggerTrace", Constants.CATEGORY_COMMANDS, false );

	private final String key;
	private final String category;
	private final boolean enabled;
	private final String comment;

	AEFeature( final String key, final String cat )
	{
		this( key, cat, true );
	}

	AEFeature( final String key, final String cat, final String comment )
	{
		this( key, cat, true, comment );
	}

	AEFeature( final String key, final String cat, final boolean enabled )
	{
		this( key, cat, enabled, null );
	}

	AEFeature( final String key, final String cat, final boolean enabled, final String comment )
	{
		this.key = key;
		this.category = cat;
		this.enabled = enabled;
		this.comment = comment;
	}

	/**
	 * override to set visibility
	 *
	 * @return default true
	 */
	public boolean isVisible()
	{
		return true;
	}

	public String key()
	{
		return this.key;
	}

	public String category()
	{
		return this.category;
	}

	public boolean isEnabled()
	{
		return this.enabled;
	}

	public String comment()
	{
		return this.comment;
	}

	private enum Constants
	{
		;

		private static final String CATEGORY_MISC = "Misc";
		private static final String CATEGORY_CRAFTING = "Crafting";
		private static final String CATEGORY_WORLD = "World";
		private static final String CATEGORY_MACHINES = "Machines";
		private static final String CATEGORY_TOOLS = "Tools";
		private static final String CATEGORY_TOOLS_CLASSIFICATIONS = "ToolsClassifications";
		private static final String CATEGORY_NETWORK_BUSES = "NetworkBuses";
		private static final String CATEGORY_P2P_TUNNELS = "P2PTunnels";
		private static final String CATEGORY_BLOCK_FEATURES = "BlockFeatures";
		private static final String CATEGORY_CRAFTING_FEATURES = "CraftingFeatures";
		private static final String CATEGORY_STORAGE = "Storage";
		private static final String CATEGORY_CABLES = "Cables";
		private static final String CATEGORY_NETWORK_FEATURES = "NetworkFeatures";
		private static final String CATEGORY_COMMANDS = "Commands";
		private static final String CATEGORY_RENDERING = "Rendering";
		private static final String CATEGORY_FACADES = "Facades";
		private static final String CATEGORY_NETWORK_TOOL = "NetworkTool";
		private static final String CATEGORY_PORTABLE_CELL = "PortableCell";
		private static final String CATEGORY_ENERGY = "Energy";
		private static final String CATEGORY_UPGRADES = "Upgrades";
		private static final String CATEGORY_MATERIALS = "Materials";
		private static final String CATEGORY_COMPONENTS = "CraftingComponents";
	}
}
