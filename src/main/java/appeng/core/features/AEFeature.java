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

package appeng.core.features;


public enum AEFeature
{
	// stuff that has no reason for ever being turned off, or that
	// is just flat out required by tons of
	// important stuff.
	Core( null )
			{
				@Override
				public boolean isVisible()
				{
					return false;
				}
			},

	CertusQuartzWorldGen( Constants.CATEGORY_WORLD ),
	MeteoriteWorldGen( Constants.CATEGORY_WORLD ),
	DecorativeLights( Constants.CATEGORY_WORLD ),
	DecorativeQuartzBlocks( Constants.CATEGORY_WORLD ),
	SkyStoneChests( Constants.CATEGORY_WORLD ),
	SpawnPressesInMeteorites( Constants.CATEGORY_WORLD ),
	GrindStone( Constants.CATEGORY_WORLD ),
	Flour( Constants.CATEGORY_WORLD ),
	Inscriber( Constants.CATEGORY_WORLD ),
	ChestLoot( Constants.CATEGORY_WORLD ),
	VillagerTrading( Constants.CATEGORY_WORLD ),
	TinyTNT( Constants.CATEGORY_WORLD ),

	PoweredTools( Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),
	CertusQuartzTools( Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),
	NetherQuartzTools( Constants.CATEGORY_TOOLS_CLASSIFICATIONS ),

	QuartzHoe( Constants.CATEGORY_TOOLS ),
	QuartzSpade( Constants.CATEGORY_TOOLS ),
	QuartzSword( Constants.CATEGORY_TOOLS ),
	QuartzPickaxe( Constants.CATEGORY_TOOLS ),
	QuartzAxe( Constants.CATEGORY_TOOLS ),
	QuartzKnife( Constants.CATEGORY_TOOLS ),
	QuartzWrench( Constants.CATEGORY_TOOLS ),
	ChargedStaff( Constants.CATEGORY_TOOLS ),
	EntropyManipulator( Constants.CATEGORY_TOOLS ),
	MatterCannon( Constants.CATEGORY_TOOLS ),
	WirelessAccessTerminal( Constants.CATEGORY_TOOLS ),
	ColorApplicator( Constants.CATEGORY_TOOLS ),
	MeteoriteCompass( Constants.CATEGORY_TOOLS ),

	PowerGen( Constants.CATEGORY_NETWORK_FEATURES ),
	Security( Constants.CATEGORY_NETWORK_FEATURES ),
	SpatialIO( Constants.CATEGORY_NETWORK_FEATURES ),
	QuantumNetworkBridge( Constants.CATEGORY_NETWORK_FEATURES ),
	Channels( Constants.CATEGORY_NETWORK_FEATURES ),

	LevelEmitter( Constants.CATEGORY_NETWORK_BUSES ),
	CraftingTerminal( Constants.CATEGORY_NETWORK_BUSES ),
	StorageMonitor( Constants.CATEGORY_NETWORK_BUSES ),
	P2PTunnel( Constants.CATEGORY_NETWORK_BUSES ),
	FormationPlane( Constants.CATEGORY_NETWORK_BUSES ),
	AnnihilationPlane( Constants.CATEGORY_NETWORK_BUSES ),
	IdentityAnnihilationPlane( Constants.CATEGORY_NETWORK_BUSES ),
	ImportBus( Constants.CATEGORY_NETWORK_BUSES ),
	ExportBus( Constants.CATEGORY_NETWORK_BUSES ),
	StorageBus( Constants.CATEGORY_NETWORK_BUSES ),
	PartConversionMonitor( Constants.CATEGORY_NETWORK_BUSES ),

	PortableCell( Constants.CATEGORY_PORTABLE_CELL ),

	StorageCells( Constants.CATEGORY_STORAGE ),
	MEChest( Constants.CATEGORY_STORAGE ),
	MEDrive( Constants.CATEGORY_STORAGE ),
	IOPort( Constants.CATEGORY_STORAGE ),

	NetworkTool( Constants.CATEGORY_NETWORK_TOOL ),

	DenseEnergyCells( Constants.CATEGORY_HIGHER_CAPACITY ),
	DenseCables( Constants.CATEGORY_HIGHER_CAPACITY ),

	P2PTunnelRF( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelME( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelItems( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelRedstone( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelEU( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelLiquids( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelLight( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelOpenComputers( Constants.CATEGORY_P2P_TUNNELS ),
	P2PTunnelPressure( Constants.CATEGORY_P2P_TUNNELS ),

	MassCannonBlockDamage( Constants.CATEGORY_BLOCK_FEATURES ),
	TinyTNTBlockDamage( Constants.CATEGORY_BLOCK_FEATURES ),

	Facades( Constants.CATEGORY_FACADES ),

	UnsupportedDeveloperTools( Constants.CATEGORY_MISC, false ),
	Creative( Constants.CATEGORY_MISC ),
	GrinderLogging( Constants.CATEGORY_MISC, false ),
	Logging( Constants.CATEGORY_MISC ),
	IntegrationLogging( Constants.CATEGORY_MISC, false ),
	WebsiteRecipes( Constants.CATEGORY_MISC, false ),
	LogSecurityAudits( Constants.CATEGORY_MISC, false ),
	Achievements( Constants.CATEGORY_MISC ),
	UpdateLogging( Constants.CATEGORY_MISC, false ),
	PacketLogging( Constants.CATEGORY_MISC, false ),
	CraftingLog( Constants.CATEGORY_MISC, false ),
	LightDetector( Constants.CATEGORY_MISC ),
	DebugLogging( Constants.CATEGORY_MISC, false ),

	EnableFacadeCrafting( Constants.CATEGORY_CRAFTING ),
	InWorldSingularity( Constants.CATEGORY_CRAFTING ),
	InWorldFluix( Constants.CATEGORY_CRAFTING ),
	InWorldPurification( Constants.CATEGORY_CRAFTING ),
	InterfaceTerminal( Constants.CATEGORY_CRAFTING ),
	EnableDisassemblyCrafting( Constants.CATEGORY_CRAFTING ),

	AlphaPass( Constants.CATEGORY_RENDERING ), PaintBalls( Constants.CATEGORY_TOOLS ),

	MolecularAssembler( Constants.CATEGORY_CRAFTING_FEATURES ),
	Patterns( Constants.CATEGORY_CRAFTING_FEATURES ),
	CraftingCPU( Constants.CATEGORY_CRAFTING_FEATURES ),

	ChunkLoggerTrace( Constants.CATEGORY_COMMANDS, false );

	public final String category;
	public final boolean defaultValue;

	AEFeature( final String cat )
	{
		this( cat, true );
	}

	AEFeature( final String cat, final boolean defaultValue )
	{
		this.category = cat;
		this.defaultValue = defaultValue;
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

	private enum Constants
	{
		;

		private static final String CATEGORY_MISC = "Misc";
		private static final String CATEGORY_CRAFTING = "Crafting";
		private static final String CATEGORY_WORLD = "World";
		private static final String CATEGORY_TOOLS = "Tools";
		private static final String CATEGORY_TOOLS_CLASSIFICATIONS = "ToolsClassifications";
		private static final String CATEGORY_NETWORK_BUSES = "NetworkBuses";
		private static final String CATEGORY_P2P_TUNNELS = "P2PTunnels";
		private static final String CATEGORY_BLOCK_FEATURES = "BlockFeatures";
		private static final String CATEGORY_CRAFTING_FEATURES = "CraftingFeatures";
		private static final String CATEGORY_STORAGE = "Storage";
		private static final String CATEGORY_HIGHER_CAPACITY = "HigherCapacity";
		private static final String CATEGORY_NETWORK_FEATURES = "NetworkFeatures";
		private static final String CATEGORY_COMMANDS = "Commands";
		private static final String CATEGORY_RENDERING = "Rendering";
		private static final String CATEGORY_FACADES = "Facades";
		private static final String CATEGORY_NETWORK_TOOL = "NetworkTool";
		private static final String CATEGORY_PORTABLE_CELL = "PortableCell";
	}
}
