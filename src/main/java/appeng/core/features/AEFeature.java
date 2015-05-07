/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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
	Core( null ), // stuff that has no reason for ever being turned off, or that
	// is just flat out required by tons of
	// important stuff.

	CertusQuartzWorldGen( "World" ), MeteoriteWorldGen( "World" ),

	DecorativeLights( "World" ), DecorativeQuartzBlocks( "World" ), SkyStoneChests( "World" ), SpawnPressesInMeteorites( "World" ),

	GrindStone( "World" ), Flour( "World" ), Inscriber( "World" ),

	ChestLoot( "World" ), VillagerTrading( "World" ),

	TinyTNT( "World" ),

	PoweredTools( "ToolsClassifications" ),

	CertusQuartzTools( "ToolsClassifications" ),

	NetherQuartzTools( "ToolsClassifications" ),

	QuartzHoe( "Tools" ), QuartzSpade( "Tools" ), QuartzSword( "Tools" ), QuartzPickaxe( "Tools" ), QuartzAxe( "Tools" ), QuartzKnife( "Tools" ), QuartzWrench( "Tools" ),

	ChargedStaff( "Tools" ), EntropyManipulator( "Tools" ), MatterCannon( "Tools" ), WirelessAccessTerminal( "Tools" ), ColorApplicator( "Tools" ),

	CraftingCPU( "CraftingFeatures" ), PowerGen( "NetworkFeatures" ), Security( "NetworkFeatures" ),

	SpatialIO( "NetworkFeatures" ), QuantumNetworkBridge( "NetworkFeatures" ), Channels( "NetworkFeatures" ),

	LevelEmitter( "NetworkBuses" ), CraftingTerminal( "NetworkBuses" ), StorageMonitor( "NetworkBuses" ), P2PTunnel( "NetworkBuses" ), FormationPlane( "NetworkBuses" ), AnnihilationPlane( "NetworkBuses" ), ImportBus( "NetworkBuses" ), ExportBus( "NetworkBuses" ), StorageBus( "NetworkBuses" ), PartConversionMonitor( "NetworkBuses" ),

	StorageCells( "Storage" ), PortableCell( "PortableCell" ), MEChest( "Storage" ), MEDrive( "Storage" ), IOPort( "Storage" ),

	NetworkTool( "NetworkTool" ),

	DenseEnergyCells( "HigherCapacity" ), DenseCables( "HigherCapacity" ),

	P2PTunnelRF( "P2PTunnels" ), P2PTunnelME( "P2PTunnels" ), P2PTunnelItems( "P2PTunnels" ), P2PTunnelRedstone( "P2PTunnels" ), P2PTunnelEU( "P2PTunnels" ), P2PTunnelLiquids( "P2PTunnels" ), P2PTunnelLight( "P2PTunnels" ), P2PTunnelOpenComputers( "P2PTunnels" ),

	MassCannonBlockDamage( "BlockFeatures" ), TinyTNTBlockDamage( "BlockFeatures" ), Facades( "Facades" ),

	UnsupportedDeveloperTools( "Misc", false ), Creative( "Misc" ),

	GrinderLogging( "Misc", false ), Logging( "Misc" ), IntegrationLogging( "Misc", false ), CustomRecipes( "Crafting", false ), WebsiteRecipes( "Misc", false ),

	enableFacadeCrafting( "Crafting" ), inWorldSingularity( "Crafting" ), inWorldFluix( "Crafting" ), inWorldPurification( "Crafting" ), UpdateLogging( "Misc", false ),

	AlphaPass( "Rendering" ), PaintBalls( "Tools" ), PacketLogging( "Misc", false ), CraftingLog( "Misc", false ), InterfaceTerminal( "Crafting" ), LightDetector( "Misc" ),

	enableDisassemblyCrafting( "Crafting" ), MolecularAssembler( "CraftingFeatures" ), MeteoriteCompass( "Tools" ), Patterns( "CraftingFeatures" ),

	ChunkLoggerTrace( "Commands", false ), LogSecurityAudits( "Misc", false ), Achievements( "Misc" );

	public final String category;
	public final boolean isVisible;
	public final boolean defaultValue;

	AEFeature( String cat )
	{
		this(cat, true);
	}

	AEFeature( String cat, boolean defaultValue )
	{
		this.category = cat;
		this.isVisible = !this.name().equals( "Core" );
		this.defaultValue = defaultValue;
	}
}
