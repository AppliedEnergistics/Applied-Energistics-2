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

package appeng.core.api.definitions;


import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.ITileDefinition;
import appeng.block.misc.BlockQuartzFixture;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.block.storage.BlockSkyChest;
import appeng.bootstrap.*;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.features.AEFeature;
import appeng.core.features.registries.PartModels;
import appeng.decorative.AEDecorativeBlock;
import appeng.decorative.solid.*;
import appeng.tile.storage.TileSkyChest;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static appeng.decorative.solid.BlockSkyStone.SkystoneType;


/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks
{
	// FIXME: Make everything final again when porting is done
	private  IBlockDefinition quartzOre;
	private  IBlockDefinition quartzOreCharged;
	private  IBlockDefinition matrixFrame;
	private  IBlockDefinition quartzBlock;
	private  IBlockDefinition quartzPillar;
	private  IBlockDefinition chiseledQuartzBlock;
	private  IBlockDefinition quartzGlass;
	private  IBlockDefinition quartzVibrantGlass;
	private  IBlockDefinition quartzFixture;
	private  IBlockDefinition fluixBlock;
	private  IBlockDefinition skyStoneBlock;
	private  IBlockDefinition smoothSkyStoneBlock;
	private  IBlockDefinition skyStoneBrick;
	private  IBlockDefinition skyStoneSmallBrick;
	private  IBlockDefinition skyStoneChest;
	private  IBlockDefinition smoothSkyStoneChest;
	private  IBlockDefinition skyCompass;
	private  ITileDefinition grindstone;
	private  ITileDefinition crank;
	private  ITileDefinition inscriber;
	private  ITileDefinition wirelessAccessPoint;
	private  ITileDefinition charger;
	private  IBlockDefinition tinyTNT;
	private  ITileDefinition securityStation;
	private  ITileDefinition quantumRing;
	private  ITileDefinition quantumLink;
	private  ITileDefinition spatialPylon;
	private  ITileDefinition spatialIOPort;
	private  ITileDefinition multiPart;
	private  ITileDefinition controller;
	private  ITileDefinition drive;
	private  ITileDefinition chest;
	private  ITileDefinition iface;
	private  ITileDefinition fluidIface;
	private  ITileDefinition cellWorkbench;
	private  ITileDefinition iOPort;
	private  ITileDefinition condenser;
	private  ITileDefinition energyAcceptor;
	private  ITileDefinition vibrationChamber;
	private  ITileDefinition quartzGrowthAccelerator;
	private  ITileDefinition energyCell;
	private  ITileDefinition energyCellDense;
	private  ITileDefinition energyCellCreative;
	private  ITileDefinition craftingUnit;
	private  ITileDefinition craftingAccelerator;
	private  ITileDefinition craftingStorage1k;
	private  ITileDefinition craftingStorage4k;
	private  ITileDefinition craftingStorage16k;
	private  ITileDefinition craftingStorage64k;
	private  ITileDefinition craftingMonitor;
	private  ITileDefinition molecularAssembler;
	private  ITileDefinition lightDetector;
	private  ITileDefinition paint;
	private  IBlockDefinition skyStoneStairs;
	private  IBlockDefinition smoothSkyStoneStairs;
	private  IBlockDefinition skyStoneBrickStairs;
	private  IBlockDefinition skyStoneSmallBrickStairs;
	private  IBlockDefinition fluixStairs;
	private  IBlockDefinition quartzStairs;
	private  IBlockDefinition chiseledQuartzStairs;
	private  IBlockDefinition quartzPillarStairs;

	private  IBlockDefinition skyStoneSlab;
	private  IBlockDefinition smoothSkyStoneSlab;
	private  IBlockDefinition skyStoneBrickSlab;
	private  IBlockDefinition skyStoneSmallBrickSlab;
	private  IBlockDefinition fluixSlab;
	private  IBlockDefinition quartzSlab;
	private  IBlockDefinition chiseledQuartzSlab;
	private  IBlockDefinition quartzPillarSlab;

	private  IBlockDefinition itemGen;
	private  IBlockDefinition chunkLoader;
	private  IBlockDefinition phantomNode;
	private  IBlockDefinition cubeGenerator;
	private  IBlockDefinition energyGenerator;

	private static final Block.Properties QUARTZ_PROPERTIES = Block.Properties.create(Material.ROCK)
			.hardnessAndResistance(3, 5);

	private static final Block.Properties SKYSTONE_PROPERTIES = Block.Properties.create( Material.ROCK )
			.hardnessAndResistance( 50, 150 );

	public ApiBlocks( FeatureFactory registry, PartModels partModels )
	{
		this.quartzOre = registry.block( "quartz_ore", () -> new BlockQuartzOre(QUARTZ_PROPERTIES))
				.features( AEFeature.CERTUS_ORE )
				.build();
		this.quartzOreCharged = registry.block( "charged_quartz_ore", () -> new BlockChargedQuartzOre(QUARTZ_PROPERTIES) )
				.features( AEFeature.CERTUS_ORE, AEFeature.CHARGED_CERTUS_ORE )
				.build();
		this.matrixFrame = registry.block( "matrix_frame", BlockMatrixFrame::new )
				.features( AEFeature.SPATIAL_IO )
				.build();

		FeatureFactory deco = registry.features( AEFeature.DECORATIVE_BLOCKS );
		this.quartzBlock = deco.block( "quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES) ).build();
		this.quartzPillar = deco.block( "quartz_pillar", () -> new BlockQuartzPillar(QUARTZ_PROPERTIES) ).build();
		this.chiseledQuartzBlock = deco.block( "chiseled_quartz_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES) ).build();

		this.quartzGlass = registry.features( AEFeature.QUARTZ_GLASS )
				.block( "quartz_glass", () -> new BlockQuartzGlass(Block.Properties.create(Material.GLASS).hardnessAndResistance(2.2F).sound(SoundType.GLASS).notSolid()) )
				.rendering(new BlockRenderingCustomizer() {
					@Override
					@OnlyIn(Dist.CLIENT)
					public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
						rendering.renderType(RenderType.getCutout());
					}
				})
				.build();
		this.quartzVibrantGlass = deco.block( "quartz_vibrant_glass", () -> new BlockQuartzLamp(Block.Properties.create(Material.GLASS).hardnessAndResistance(2.2F).sound(SoundType.GLASS).lightValue(15).notSolid()) )
				.addFeatures( AEFeature.DECORATIVE_LIGHTS, AEFeature.QUARTZ_GLASS )
				.rendering(new BlockRenderingCustomizer() {
					@Override
					@OnlyIn(Dist.CLIENT)
					public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
						rendering.renderType(RenderType.getCutout());
					}
				})
				.build();

		this.quartzFixture = registry.block( "quartz_fixture", BlockQuartzFixture::new )
				.features( AEFeature.DECORATIVE_LIGHTS )
				.rendering(new BlockRenderingCustomizer() {
					@Override
					@OnlyIn(Dist.CLIENT)
					public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
						rendering.renderType(RenderType.getCutout());
					}
				})
				.build();

		this.fluixBlock = registry.features( AEFeature.FLUIX ).block( "fluix_block", () -> new AEDecorativeBlock(QUARTZ_PROPERTIES) ).build();

		this.skyStoneBlock = registry.features( AEFeature.SKY_STONE )
				.block( "sky_stone_block", () -> new BlockSkyStone( SkystoneType.STONE, Block.Properties.create(Material.ROCK)
						.hardnessAndResistance(50, 150)
						.harvestLevel(3) ) )
				.build();

		this.smoothSkyStoneBlock = registry.features( AEFeature.SKY_STONE )
				.block( "smooth_sky_stone_block", () -> new BlockSkyStone( SkystoneType.BLOCK, SKYSTONE_PROPERTIES ) )
				.build();
		this.skyStoneBrick = deco.block( "sky_stone_brick", () -> new BlockSkyStone( SkystoneType.BRICK, SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
		this.skyStoneSmallBrick = deco.block( "sky_stone_small_brick", () -> new BlockSkyStone( SkystoneType.SMALL_BRICK, SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();

		Block.Properties skyStoneChestProps = Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(50, 150)
				.notSolid();

		TileEntityDefinition skyChestTile = registry.tileEntity("sky_chest", TileSkyChest.class, TileSkyChest::new)
				.rendering(new TileEntityRenderingCustomizer<TileSkyChest>() {
					@Override
					@OnlyIn(Dist.CLIENT)
					public void customize(TileEntityRendering<TileSkyChest> rendering) {
						rendering.tileEntityRenderer(SkyChestTESR::new);
					}
				})
				.build();
		this.skyStoneChest = registry.block( "sky_stone_chest", () -> new BlockSkyChest( BlockSkyChest.SkyChestType.STONE, skyStoneChestProps ) )
				.features( AEFeature.SKY_STONE, AEFeature.SKY_STONE_CHESTS )
				.tileEntity( skyChestTile )
				// .rendering( new SkyChestRenderingCustomizer( BlockSkyChest.SkyChestType.STONE ) )
				.build();
		this.smoothSkyStoneChest = registry.block( "smooth_sky_stone_chest", () -> new BlockSkyChest( BlockSkyChest.SkyChestType.BLOCK, skyStoneChestProps ) )
				.features( AEFeature.SKY_STONE, AEFeature.SKY_STONE_CHESTS )
				.tileEntity( skyChestTile )
				// .rendering( new SkyChestRenderingCustomizer( BlockSkyChest.SkyChestType.BLOCK ) )
				.build();

//		this.skyCompass = registry.block( "sky_compass", BlockSkyCompass::new )
//				.features( AEFeature.METEORITE_COMPASS )
//				.tileEntity( new TileEntityDefinition( TileSkyCompass.class ) )
//				.rendering( new SkyCompassRendering() )
//				.build();
//		this.grindstone = registry.block( "grindstone", BlockGrinder::new )
//				.features( AEFeature.GRIND_STONE )
//				.tileEntity( new TileEntityDefinition( TileGrinder.class ) )
//				.build();
//		this.crank = registry.block( "crank", BlockCrank::new )
//				.features( AEFeature.GRIND_STONE )
//				.tileEntity( new TileEntityDefinition( TileCrank.class ) )
//				.rendering( new CrankRendering() )
//				.useCustomItemModel()
//				.build();
//		this.inscriber = registry.block( "inscriber", BlockInscriber::new )
//				.features( AEFeature.INSCRIBER )
//				.tileEntity( new TileEntityDefinition( TileInscriber.class ) )
//				.rendering( new InscriberRendering() )
//				.build();
//		this.wirelessAccessPoint = registry.block( "wireless_access_point", BlockWireless::new )
//				.features( AEFeature.WIRELESS_ACCESS_TERMINAL )
//				.tileEntity( new TileEntityDefinition( TileWireless.class ) )
//				.rendering( new WirelessRendering() )
//				.build();
//		this.charger = registry.block( "charger", BlockCharger::new )
//				.features( AEFeature.CHARGER )
//				.tileEntity( new TileEntityDefinition( TileCharger.class ) )
//				.rendering( new BlockRenderingCustomizer()
//				{
//					@Override
//					@OnlyIn( Dist.CLIENT )
//					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
//					{
//						rendering.tesr( BlockCharger.createTesr() );
//					}
//				} )
//				.build();
//		this.tinyTNT = registry.block( "tiny_tnt", BlockTinyTNT::new )
//				.features( AEFeature.TINY_TNT )
//	FIXME			.bootstrap( ( block, item ) -> (IPreInitComponent) side -> DispenserBlock.registerDispenseBehavior( item,
//						new DispenserBehaviorTinyTNT() ) )
//				.bootstrap( ( block, item ) -> (IEntityRegistrationComponent) r ->
//				{
//					r.register( EntityEntryBuilder.create()
//							.entity( EntityTinyTNTPrimed.class )
//							.id( new ResourceLocation( "appliedenergistics2", EntityTinyTNTPrimed.class.getName() ),
//									EntityIds.get( EntityTinyTNTPrimed.class ) )
//							.name( "EntityTinyTNTPrimed" )
//							.tracker( 16, 4, true )
//							.build() );
//				} )
//				.build();
//		this.securityStation = registry.block( "security_station", BlockSecurityStation::new )
//				.features( AEFeature.SECURITY )
//				.tileEntity( new TileEntityDefinition( TileSecurityStation.class ) )
//				.rendering( new SecurityStationRendering() )
//				.build();
//		this.quantumRing = registry.block( "quantum_ring", BlockQuantumRing::new )
//				.features( AEFeature.QUANTUM_NETWORK_BRIDGE )
//				.tileEntity( new TileEntityDefinition( TileQuantumBridge.class, "quantum_ring" ) )
//				.rendering( new QuantumBridgeRendering() )
//				.build();
//		this.quantumLink = registry.block( "quantum_link", BlockQuantumLinkChamber::new )
//				.features( AEFeature.QUANTUM_NETWORK_BRIDGE )
//				.tileEntity( new TileEntityDefinition( TileQuantumBridge.class, "quantum_ring" ) )
//				.rendering( new QuantumBridgeRendering() )
//				.build();
//		this.spatialPylon = registry.block( "spatial_pylon", BlockSpatialPylon::new )
//				.features( AEFeature.SPATIAL_IO )
//				.tileEntity( new TileEntityDefinition( TileSpatialPylon.class ) )
//				.useCustomItemModel()
//				.rendering( new SpatialPylonRendering() )
//				.build();
//		this.spatialIOPort = registry.block( "spatial_io_port", BlockSpatialIOPort::new )
//				.features( AEFeature.SPATIAL_IO )
//				.tileEntity( new TileEntityDefinition( TileSpatialIOPort.class ) )
//				.build();
//		this.controller = registry.block( "controller", BlockController::new )
//				.features( AEFeature.CHANNELS )
//				.tileEntity( new TileEntityDefinition( TileController.class ) )
//				.useCustomItemModel()
//				.rendering( new ControllerRendering() )
//				.build();
//		this.drive = registry.block( "drive", BlockDrive::new )
//				.features( AEFeature.STORAGE_CELLS, AEFeature.ME_DRIVE )
//				.tileEntity( new TileEntityDefinition( TileDrive.class ) )
//				.useCustomItemModel()
//				.rendering( new DriveRendering() )
//				.build();
//		this.chest = registry.block( "chest", BlockChest::new )
//				.features( AEFeature.STORAGE_CELLS, AEFeature.ME_CHEST )
//				.tileEntity( new TileEntityDefinition( TileChest.class ) )
//				.useCustomItemModel()
//				.rendering( new ChestRendering() )
//				.build();
//		this.iface = registry.block( "interface", BlockInterface::new )
//				.features( AEFeature.INTERFACE )
//				.tileEntity( new TileEntityDefinition( TileInterface.class ) )
//				.build();
//		this.fluidIface = registry.block( "fluid_interface", BlockFluidInterface::new )
//				.features( AEFeature.FLUID_INTERFACE )
//				.tileEntity( new TileEntityDefinition( TileFluidInterface.class ) )
//				.build();
//		this.cellWorkbench = registry.block( "cell_workbench", BlockCellWorkbench::new )
//				.features( AEFeature.STORAGE_CELLS )
//				.tileEntity( new TileEntityDefinition( TileCellWorkbench.class ) )
//				.build();
//		this.iOPort = registry.block( "io_port", BlockIOPort::new )
//				.features( AEFeature.STORAGE_CELLS, AEFeature.IO_PORT )
//				.tileEntity( new TileEntityDefinition( TileIOPort.class ) )
//				.build();
//		this.condenser = registry.block( "condenser", BlockCondenser::new )
//				.features( AEFeature.CONDENSER )
//				.tileEntity( new TileEntityDefinition( TileCondenser.class ) )
//				.build();
//		this.energyAcceptor = registry.block( "energy_acceptor", BlockEnergyAcceptor::new )
//				.features( AEFeature.ENERGY_ACCEPTOR )
//				.tileEntity( new TileEntityDefinition( TileEnergyAcceptor.class ) )
//				.build();
//		this.vibrationChamber = registry.block( "vibration_chamber", BlockVibrationChamber::new )
//				.features( AEFeature.POWER_GEN )
//				.tileEntity( new TileEntityDefinition( TileVibrationChamber.class ) )
//				.build();
//		this.quartzGrowthAccelerator = registry.block( "quartz_growth_accelerator", BlockQuartzGrowthAccelerator::new )
//				.tileEntity( new TileEntityDefinition( TileQuartzGrowthAccelerator.class ) )
//				.features( AEFeature.CRYSTAL_GROWTH_ACCELERATOR )
//				.build();
//		this.energyCell = registry.block( "energy_cell", BlockEnergyCell::new )
//				.features( AEFeature.ENERGY_CELLS )
//				.item( AEBaseBlockItemChargeable::new )
//				.tileEntity( new TileEntityDefinition( TileEnergyCell.class ) )
//				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "energy_cell" ) ) )
//				.build();
//		this.energyCellDense = registry.block( "dense_energy_cell", BlockDenseEnergyCell::new )
//				.features( AEFeature.ENERGY_CELLS, AEFeature.DENSE_ENERGY_CELLS )
//				.item( AEBaseBlockItemChargeable::new )
//				.tileEntity( new TileEntityDefinition( TileDenseEnergyCell.class ) )
//				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "dense_energy_cell" ) ) )
//				.build();
//		this.energyCellCreative = registry.block( "creative_energy_cell", BlockCreativeEnergyCell::new )
//				.features( AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TileCreativeEnergyCell.class ) )
//				.build();
//
//		FeatureFactory crafting = registry.features( AEFeature.CRAFTING_CPU );
//		this.craftingUnit = crafting.block( "crafting_unit", () -> new BlockCraftingUnit( CraftingUnitType.UNIT ) )
//				.rendering( new CraftingCubeRendering( "crafting_unit", CraftingUnitType.UNIT ) )
//				.tileEntity( new TileEntityDefinition( TileCraftingTile.class, "crafting_unit" ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingAccelerator = crafting.block( "crafting_accelerator", () -> new BlockCraftingUnit( CraftingUnitType.ACCELERATOR ) )
//				.rendering( new CraftingCubeRendering( "crafting_accelerator", CraftingUnitType.ACCELERATOR ) )
//				.tileEntity( new TileEntityDefinition( TileCraftingTile.class, "crafting_unit" ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingStorage1k = crafting.block( "crafting_storage_1k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_1K ) )
//				.item( ItemCraftingStorage::new )
//				.tileEntity( new TileEntityDefinition( TileCraftingStorageTile.class, "crafting_storage" ) )
//				.rendering( new CraftingCubeRendering( "crafting_storage_1k", CraftingUnitType.STORAGE_1K ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingStorage4k = crafting.block( "crafting_storage_4k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_4K ) )
//				.item( ItemCraftingStorage::new )
//				.tileEntity( new TileEntityDefinition( TileCraftingStorageTile.class, "crafting_storage" ) )
//				.rendering( new CraftingCubeRendering( "crafting_storage_4k", CraftingUnitType.STORAGE_4K ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingStorage16k = crafting.block( "crafting_storage_16k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_16K ) )
//				.item( ItemCraftingStorage::new )
//				.tileEntity( new TileEntityDefinition( TileCraftingStorageTile.class, "crafting_storage" ) )
//				.rendering( new CraftingCubeRendering( "crafting_storage_16k", CraftingUnitType.STORAGE_16K ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingStorage64k = crafting.block( "crafting_storage_64k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_64K ) )
//				.item( ItemCraftingStorage::new )
//				.tileEntity( new TileEntityDefinition( TileCraftingStorageTile.class, "crafting_storage" ) )
//				.rendering( new CraftingCubeRendering( "crafting_storage_64k", CraftingUnitType.STORAGE_64K ) )
//				.useCustomItemModel()
//				.build();
//		this.craftingMonitor = crafting.block( "crafting_monitor", BlockCraftingMonitor::new )
//				.tileEntity( new TileEntityDefinition( TileCraftingMonitorTile.class ) )
//				.rendering( new CraftingCubeRendering( "crafting_monitor", CraftingUnitType.MONITOR ) )
//				.useCustomItemModel()
//				.build();
//
//		this.molecularAssembler = registry.block( "molecular_assembler", BlockMolecularAssembler::new )
//				.features( AEFeature.MOLECULAR_ASSEMBLER )
//				.tileEntity( new TileEntityDefinition( TileMolecularAssembler.class ) )
//				.build();
//		this.lightDetector = registry.block( "light_detector", BlockLightDetector::new )
//				.features( AEFeature.LIGHT_DETECTOR )
//				.tileEntity( new TileEntityDefinition( TileLightDetector.class ) )
//				.useCustomItemModel()
//				.build();
//		this.paint = registry.block( "paint", BlockPaint::new )
//				.features( AEFeature.PAINT_BALLS )
//				.tileEntity( new TileEntityDefinition( TilePaint.class ) )
//				.rendering( new PaintRendering() )
//				.build();
//
  		this.skyStoneStairs = deco.block( "sky_stone_stairs", () -> new StairsBlock(this.skyStoneBlock().block()::getDefaultState, SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.smoothSkyStoneStairs = deco.block( "smooth_sky_stone_stairs", () -> new StairsBlock(this.smoothSkyStoneBlock().block()::getDefaultState, SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.skyStoneBrickStairs = deco.block( "sky_stone_brick_stairs", () -> new StairsBlock(this.skyStoneBrick().block()::getDefaultState, SKYSTONE_PROPERTIES) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.skyStoneSmallBrickStairs = deco.block( "sky_stone_small_brick_stairs", () -> new StairsBlock(this.skyStoneSmallBrick().block()::getDefaultState, SKYSTONE_PROPERTIES) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();

  		this.fluixStairs = deco.block( "fluix_stairs", () -> new StairsBlock(this.fluixBlock().block()::getDefaultState, QUARTZ_PROPERTIES) )
				.addFeatures( AEFeature.FLUIX )
				.build();
  		this.quartzStairs = deco.block( "quartz_stairs", () -> new StairsBlock(this.quartzBlock().block()::getDefaultState, QUARTZ_PROPERTIES) )
				.addFeatures( AEFeature.CERTUS )
				.build();
  		this.chiseledQuartzStairs = deco.block( "chiseled_quartz_stairs", () -> new StairsBlock(this.chiseledQuartzBlock().block()::getDefaultState, QUARTZ_PROPERTIES) )
				.addFeatures( AEFeature.CERTUS )
				.build();
  		this.quartzPillarStairs = deco.block( "quartz_pillar_stairs", () -> new StairsBlock(this.quartzPillar().block()::getDefaultState, QUARTZ_PROPERTIES) )
				.addFeatures( AEFeature.CERTUS )
				.build();
//
//		this.multiPart = registry.block( "cable_bus", BlockCableBus::new )
//				.rendering( new CableBusRendering( partModels ) )
//				// (handled in BlockCableBus.java and its setupTile())
//				// .tileEntity( TileCableBus.class )
//				// TODO: why the custom registration?
//				.bootstrap( ( block, item ) -> (IPostInitComponent) side -> ( (BlockCableBus) block ).setupTile() )
//				.build();

		this.skyStoneSlab = deco.block( "sky_stone_slab", () -> new SlabBlock( SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.smoothSkyStoneSlab = deco.block( "smooth_sky_stone_slab", () -> new SlabBlock( SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.skyStoneBrickSlab = deco.block( "sky_stone_brick_slab", () -> new SlabBlock( SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();
  		this.skyStoneSmallBrickSlab = deco.block( "sky_stone_small_brick_slab", () -> new SlabBlock( SKYSTONE_PROPERTIES ) )
				.addFeatures( AEFeature.SKY_STONE )
				.build();

		this.fluixSlab = deco.block( "fluix_slab", () -> new SlabBlock( QUARTZ_PROPERTIES ) )
				.addFeatures( AEFeature.FLUIX )
				.build();
  		this.quartzSlab = deco.block( "quartz_slab", () -> new SlabBlock( QUARTZ_PROPERTIES ) )
				.addFeatures( AEFeature.CERTUS )
				.build();
  		this.chiseledQuartzSlab = deco.block( "chiseled_quartz_slab", () -> new SlabBlock( QUARTZ_PROPERTIES ) )
				.addFeatures( AEFeature.CERTUS )
				.build();
  		this.quartzPillarSlab = deco.block( "quartz_pillar_slab", () -> new SlabBlock( QUARTZ_PROPERTIES ) )
				.addFeatures( AEFeature.CERTUS )
				.build();

//		this.itemGen = registry.block( "debug_item_gen", BlockItemGen::new )
//				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TileItemGen.class ) )
//				.useCustomItemModel()
//				.build();
//		this.chunkLoader = registry.block( "debug_chunk_loader", BlockChunkloader::new )
//				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TileChunkLoader.class ) )
//				.useCustomItemModel()
//				.build();
//		this.phantomNode = registry.block( "debug_phantom_node", BlockPhantomNode::new )
//				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TilePhantomNode.class ) )
//				.useCustomItemModel()
//				.build();
//		this.cubeGenerator = registry.block( "debug_cube_gen", BlockCubeGenerator::new )
//				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TileCubeGenerator.class ) )
//				.useCustomItemModel()
//				.build();
//		this.energyGenerator = registry.block( "debug_energy_gen", BlockEnergyGenerator::new )
//				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
//				.tileEntity( new TileEntityDefinition( TileEnergyGenerator.class ) )
//				.useCustomItemModel()
//				.build();
	}

	private Block createQuartzOreBlock() {
		return new BlockQuartzOre(Block.Properties.create(Material.ROCK).hardnessAndResistance(3, 5));
	}

	@Override
	public IBlockDefinition quartzOre()
	{
		return this.quartzOre;
	}

	@Override
	public IBlockDefinition quartzOreCharged()
	{
		return this.quartzOreCharged;
	}

	@Override
	public IBlockDefinition matrixFrame()
	{
		return this.matrixFrame;
	}

	@Override
	public IBlockDefinition quartzBlock()
	{
		return this.quartzBlock;
	}

	@Override
	public IBlockDefinition quartzPillar()
	{
		return this.quartzPillar;
	}

	@Override
	public IBlockDefinition chiseledQuartzBlock()
	{
		return this.chiseledQuartzBlock;
	}

	@Override
	public IBlockDefinition quartzGlass()
	{
		return this.quartzGlass;
	}

	@Override
	public IBlockDefinition quartzVibrantGlass()
	{
		return this.quartzVibrantGlass;
	}

	@Override
	public IBlockDefinition quartzFixture()
	{
		return this.quartzFixture;
	}

	@Override
	public IBlockDefinition fluixBlock()
	{
		return this.fluixBlock;
	}

	@Override
	public IBlockDefinition skyStoneBlock()
	{
		return this.skyStoneBlock;
	}

	@Override
	public IBlockDefinition smoothSkyStoneBlock()
	{
		return this.smoothSkyStoneBlock;
	}

	@Override
	public IBlockDefinition skyStoneBrick()
	{
		return this.skyStoneBrick;
	}

	@Override
	public IBlockDefinition skyStoneSmallBrick()
	{
		return this.skyStoneSmallBrick;
	}

	@Override
	public IBlockDefinition skyStoneChest()
	{
		return this.skyStoneChest;
	}

	@Override
	public IBlockDefinition smoothSkyStoneChest()
	{
		return this.smoothSkyStoneChest;
	}

	@Override
	public IBlockDefinition skyCompass()
	{
		return this.skyCompass;
	}

	@Override
	public IBlockDefinition skyStoneStairs()
	{
		return this.skyStoneStairs;
	}

	@Override
	public IBlockDefinition smoothSkyStoneStairs()
	{
		return this.smoothSkyStoneStairs;
	}

	@Override
	public IBlockDefinition skyStoneBrickStairs()
	{
		return this.skyStoneBrickStairs;
	}

	@Override
	public IBlockDefinition skyStoneSmallBrickStairs()
	{
		return this.skyStoneSmallBrickStairs;
	}

	@Override
	public IBlockDefinition fluixStairs()
	{
		return this.fluixStairs;
	}

	@Override
	public IBlockDefinition quartzStairs()
	{
		return this.quartzStairs;
	}

	@Override
	public IBlockDefinition chiseledQuartzStairs()
	{
		return this.chiseledQuartzStairs;
	}

	@Override
	public IBlockDefinition quartzPillarStairs()
	{
		return this.quartzPillarStairs;
	}

	@Override
	public IBlockDefinition skyStoneSlab()
	{
		return this.skyStoneSlab;
	}

	@Override
	public IBlockDefinition smoothSkyStoneSlab()
	{
		return this.smoothSkyStoneSlab;
	}

	@Override
	public IBlockDefinition skyStoneBrickSlab()
	{
		return this.skyStoneBrickSlab;
	}

	@Override
	public IBlockDefinition skyStoneSmallBrickSlab()
	{
		return this.skyStoneSmallBrickSlab;
	}

	@Override
	public IBlockDefinition fluixSlab()
	{
		return this.fluixSlab;
	}

	@Override
	public IBlockDefinition quartzSlab()
	{
		return this.quartzSlab;
	}

	@Override
	public IBlockDefinition chiseledQuartzSlab()
	{
		return this.chiseledQuartzSlab;
	}

	@Override
	public IBlockDefinition quartzPillarSlab()
	{
		return this.quartzPillarSlab;
	}

	@Override
	public ITileDefinition grindstone()
	{
		return this.grindstone;
	}

	@Override
	public ITileDefinition crank()
	{
		return this.crank;
	}

	@Override
	public ITileDefinition inscriber()
	{
		return this.inscriber;
	}

	@Override
	public ITileDefinition wirelessAccessPoint()
	{
		return this.wirelessAccessPoint;
	}

	@Override
	public ITileDefinition charger()
	{
		return this.charger;
	}

	@Override
	public IBlockDefinition tinyTNT()
	{
		return this.tinyTNT;
	}

	@Override
	public ITileDefinition securityStation()
	{
		return this.securityStation;
	}

	@Override
	public ITileDefinition quantumRing()
	{
		return this.quantumRing;
	}

	@Override
	public ITileDefinition quantumLink()
	{
		return this.quantumLink;
	}

	@Override
	public ITileDefinition spatialPylon()
	{
		return this.spatialPylon;
	}

	@Override
	public ITileDefinition spatialIOPort()
	{
		return this.spatialIOPort;
	}

	@Override
	public ITileDefinition multiPart()
	{
		return this.multiPart;
	}

	@Override
	public ITileDefinition controller()
	{
		return this.controller;
	}

	@Override
	public ITileDefinition drive()
	{
		return this.drive;
	}

	@Override
	public ITileDefinition chest()
	{
		return this.chest;
	}

	@Override
	public ITileDefinition iface()
	{
		return this.iface;
	}

	@Override
	public ITileDefinition fluidIface()
	{
		return this.fluidIface;
	}

	@Override
	public ITileDefinition cellWorkbench()
	{
		return this.cellWorkbench;
	}

	@Override
	public ITileDefinition iOPort()
	{
		return this.iOPort;
	}

	@Override
	public ITileDefinition condenser()
	{
		return this.condenser;
	}

	@Override
	public ITileDefinition energyAcceptor()
	{
		return this.energyAcceptor;
	}

	@Override
	public ITileDefinition vibrationChamber()
	{
		return this.vibrationChamber;
	}

	@Override
	public ITileDefinition quartzGrowthAccelerator()
	{
		return this.quartzGrowthAccelerator;
	}

	@Override
	public ITileDefinition energyCell()
	{
		return this.energyCell;
	}

	@Override
	public ITileDefinition energyCellDense()
	{
		return this.energyCellDense;
	}

	@Override
	public ITileDefinition energyCellCreative()
	{
		return this.energyCellCreative;
	}

	@Override
	public ITileDefinition craftingUnit()
	{
		return this.craftingUnit;
	}

	@Override
	public ITileDefinition craftingAccelerator()
	{
		return this.craftingAccelerator;
	}

	@Override
	public ITileDefinition craftingStorage1k()
	{
		return this.craftingStorage1k;
	}

	@Override
	public ITileDefinition craftingStorage4k()
	{
		return this.craftingStorage4k;
	}

	@Override
	public ITileDefinition craftingStorage16k()
	{
		return this.craftingStorage16k;
	}

	@Override
	public ITileDefinition craftingStorage64k()
	{
		return this.craftingStorage64k;
	}

	@Override
	public ITileDefinition craftingMonitor()
	{
		return this.craftingMonitor;
	}

	@Override
	public ITileDefinition molecularAssembler()
	{
		return this.molecularAssembler;
	}

	@Override
	public ITileDefinition lightDetector()
	{
		return this.lightDetector;
	}

	@Override
	public ITileDefinition paint()
	{
		return this.paint;
	}

	public IBlockDefinition chunkLoader()
	{
		return this.chunkLoader;
	}

	public IBlockDefinition itemGen()
	{
		return this.itemGen;
	}

	public IBlockDefinition phantomNode()
	{
		return this.phantomNode;
	}

	public IBlockDefinition cubeGenerator()
	{
		return this.cubeGenerator;
	}

	public IBlockDefinition energyGenerator()
	{
		return this.energyGenerator;
	}
}
