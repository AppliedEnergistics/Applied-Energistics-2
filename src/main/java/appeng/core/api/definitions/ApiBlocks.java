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


import appeng.core.AELog;
import appeng.debug.TileChunkLoader;
import appeng.debug.TileCubeGenerator;
import appeng.debug.TileItemGen;
import appeng.debug.TilePhantomNode;
import appeng.tile.crafting.TileCraftingMonitorTile;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.crafting.TileMolecularAssembler;
import appeng.tile.grindstone.TileCrank;
import appeng.tile.grindstone.TileGrinder;
import appeng.tile.misc.TileCellWorkbench;
import appeng.tile.misc.TileCharger;
import appeng.tile.misc.TileCondenser;
import appeng.tile.misc.TileInscriber;
import appeng.tile.misc.TileInterface;
import appeng.tile.misc.TileLightDetector;
import appeng.tile.misc.TilePaint;
import appeng.tile.misc.TileQuartzGrowthAccelerator;
import appeng.tile.misc.TileSecurityStation;
import appeng.tile.misc.TileSkyCompass;
import appeng.tile.misc.TileVibrationChamber;
import appeng.tile.networking.TileCableBus;
import appeng.tile.networking.TileController;
import appeng.tile.networking.TileCreativeEnergyCell;
import appeng.tile.networking.TileDenseEnergyCell;
import appeng.tile.networking.TileEnergyAcceptor;
import appeng.tile.networking.TileEnergyCell;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.tile.spatial.TileSpatialIOPort;
import appeng.tile.spatial.TileSpatialPylon;
import appeng.tile.storage.TileChest;
import appeng.tile.storage.TileDrive;
import appeng.tile.storage.TileIOPort;
import appeng.tile.storage.TileSkyChest;
import com.google.common.base.Verify;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.block.AEBaseItemBlockChargeable;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingStorage;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.block.crafting.BlockCraftingUnit.CraftingUnitType;
import appeng.block.crafting.BlockMolecularAssembler;
import appeng.block.crafting.ItemCraftingStorage;
import appeng.block.grindstone.BlockCrank;
import appeng.block.grindstone.BlockGrinder;
import appeng.block.grindstone.CrankRendering;
import appeng.block.misc.BlockCellWorkbench;
import appeng.block.misc.BlockCharger;
import appeng.block.misc.BlockCondenser;
import appeng.block.misc.BlockInscriber;
import appeng.block.misc.BlockInterface;
import appeng.block.misc.BlockLightDetector;
import appeng.block.misc.BlockQuartzFixture;
import appeng.block.misc.BlockQuartzGrowthAccelerator;
import appeng.block.misc.BlockSecurityStation;
import appeng.block.misc.BlockSkyCompass;
import appeng.block.misc.BlockTinyTNT;
import appeng.block.misc.BlockVibrationChamber;
import appeng.block.misc.InscriberRendering;
import appeng.block.misc.SecurityStationRendering;
import appeng.block.misc.SkyCompassRendering;
import appeng.block.networking.BlockCableBus;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockCreativeEnergyCell;
import appeng.block.networking.BlockDenseEnergyCell;
import appeng.block.networking.BlockEnergyAcceptor;
import appeng.block.networking.BlockEnergyCell;
import appeng.block.networking.BlockEnergyCellRendering;
import appeng.block.networking.BlockWireless;
import appeng.block.networking.CableBusRendering;
import appeng.block.networking.ControllerRendering;
import appeng.block.networking.WirelessRendering;
import appeng.block.paint.BlockPaint;
import appeng.block.paint.PaintRendering;
import appeng.block.qnb.BlockQuantumLinkChamber;
import appeng.block.qnb.BlockQuantumRing;
import appeng.block.qnb.QuantumBridgeRendering;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.block.spatial.BlockSpatialIOPort;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.block.storage.BlockSkyChest;
import appeng.block.storage.BlockSkyChest.SkyChestType;
import appeng.block.storage.ChestRendering;
import appeng.block.storage.DriveRendering;
import appeng.block.storage.SkyChestRenderingCustomizer;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.client.render.crafting.CraftingCubeRendering;
import appeng.client.render.model.GlassModel;
import appeng.client.render.spatial.SpatialPylonRendering;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.core.features.BlockDefinition;
import appeng.core.features.registries.PartModels;
import appeng.debug.BlockChunkloader;
import appeng.debug.BlockCubeGenerator;
import appeng.debug.BlockItemGen;
import appeng.debug.BlockPhantomNode;
import appeng.decorative.slab.BlockSlabCommon;
import appeng.decorative.solid.BlockChargedQuartzOre;
import appeng.decorative.solid.BlockChiseledQuartz;
import appeng.decorative.solid.BlockFluix;
import appeng.decorative.solid.BlockQuartz;
import appeng.decorative.solid.BlockQuartzGlass;
import appeng.decorative.solid.BlockQuartzLamp;
import appeng.decorative.solid.BlockQuartzOre;
import appeng.decorative.solid.BlockQuartzPillar;
import appeng.decorative.solid.BlockSkyStone;
import appeng.decorative.solid.BlockSkyStone.SkystoneType;
import appeng.decorative.stair.BlockStairCommon;
import appeng.hooks.DispenserBehaviorTinyTNT;

import java.util.ArrayList;
import java.util.List;


/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks
{
	private final IBlockDefinition quartzOre;
	private final IBlockDefinition quartzOreCharged;
	private final IBlockDefinition matrixFrame;
	private final IBlockDefinition quartzBlock;
	private final IBlockDefinition quartzPillar;
	private final IBlockDefinition chiseledQuartzBlock;
	private final IBlockDefinition quartzGlass;
	private final IBlockDefinition quartzVibrantGlass;
	private final IBlockDefinition quartzFixture;
	private final IBlockDefinition fluixBlock;
	private final IBlockDefinition skyStoneBlock;
	private final IBlockDefinition smoothSkyStoneBlock;
	private final IBlockDefinition skyStoneBrick;
	private final IBlockDefinition skyStoneSmallBrick;
	private final IBlockDefinition skyStoneChest;
	private final IBlockDefinition smoothSkyStoneChest;
	private final IBlockDefinition skyCompass;
	private final ITileDefinition grindstone;
	private final ITileDefinition crank;
	private final ITileDefinition inscriber;
	private final ITileDefinition wirelessAccessPoint;
	private final ITileDefinition charger;
	private final IBlockDefinition tinyTNT;
	private final ITileDefinition securityStation;
	private final ITileDefinition quantumRing;
	private final ITileDefinition quantumLink;
	private final ITileDefinition spatialPylon;
	private final ITileDefinition spatialIOPort;
	private final ITileDefinition multiPart;
	private final ITileDefinition controller;
	private final ITileDefinition drive;
	private final ITileDefinition chest;
	private final ITileDefinition iface;
	private final ITileDefinition cellWorkbench;
	private final ITileDefinition iOPort;
	private final ITileDefinition condenser;
	private final ITileDefinition energyAcceptor;
	private final ITileDefinition vibrationChamber;
	private final ITileDefinition quartzGrowthAccelerator;
	private final ITileDefinition energyCell;
	private final ITileDefinition energyCellDense;
	private final ITileDefinition energyCellCreative;
	private final ITileDefinition craftingUnit;
	private final ITileDefinition craftingAccelerator;
	private final ITileDefinition craftingStorage1k;
	private final ITileDefinition craftingStorage4k;
	private final ITileDefinition craftingStorage16k;
	private final ITileDefinition craftingStorage64k;
	private final ITileDefinition craftingMonitor;
	private final ITileDefinition molecularAssembler;
	private final ITileDefinition lightDetector;
	private final ITileDefinition paint;
	private final IBlockDefinition skyStoneStairs;
	private final IBlockDefinition smoothSkyStoneStairs;
	private final IBlockDefinition skyStoneBrickStairs;
	private final IBlockDefinition skyStoneSmallBrickStairs;
	private final IBlockDefinition fluixStairs;
	private final IBlockDefinition quartzStairs;
	private final IBlockDefinition chiseledQuartzStairs;
	private final IBlockDefinition quartzPillarStairs;

	private final IBlockDefinition skyStoneSlab;
	private final IBlockDefinition smoothSkyStoneSlab;
	private final IBlockDefinition skyStoneBrickSlab;
	private final IBlockDefinition skyStoneSmallBrickSlab;
	private final IBlockDefinition fluixSlab;
	private final IBlockDefinition quartzSlab;
	private final IBlockDefinition chiseledQuartzSlab;
	private final IBlockDefinition quartzPillarSlab;

	private final IBlockDefinition itemGen;
	private final IBlockDefinition chunkLoader;
	private final IBlockDefinition phantomNode;
	private final IBlockDefinition cubeGenerator;

//	private List<Class<? extends TileEntity>> registeredClasses = new ArrayList<Class<? extends TileEntity>>();

	private void registerTileEntity( Class<? extends TileEntity> teClass, String name )
	{
//		if(this.registeredClasses.contains( teClass ) )
//		{
//			AELog.debug( "Already registered " + teClass.getName() + " to Forge!" );
//			return;
//		}
//
//		registeredClasses.add(teClass);

		GameRegistry.registerTileEntity( teClass, AppEng.MOD_ID.toLowerCase() + ":" + name);

	}

	public ApiBlocks( FeatureFactory registry, PartModels partModels )
	{
		// this.quartzOre = new BlockDefinition( "ore.quartz", new OreQuartz() );
		this.quartzOre = registry.block( "quartz_ore", BlockQuartzOre::new )
				.features( AEFeature.CERTUS_ORE )
				.postInit( ( block, item ) ->
				{
					OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( block ) );
				} )
				.build();
		this.quartzOreCharged = registry.block( "charged_quartz_ore", BlockChargedQuartzOre::new )
				.features( AEFeature.CERTUS_ORE, AEFeature.CHARGED_CERTUS_ORE )
				.postInit( ( block, item ) ->
				{
					OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( block ) );
				} )
				.build();
		this.matrixFrame = registry.block( "matrix_frame", BlockMatrixFrame::new ).features( AEFeature.SPATIAL_IO ).build();

		FeatureFactory deco = registry.features( AEFeature.DECORATIVE_QUARTZ_BLOCKS );
		this.quartzBlock = deco.block( "quartz_block", BlockQuartz::new ).build();
		this.quartzPillar = deco.block( "quartz_pillar", BlockQuartzPillar::new ).build();
		this.chiseledQuartzBlock = deco.block( "chiseled_quartz_block", BlockChiseledQuartz::new ).build();
		this.quartzGlass = deco.block( "quartz_glass", BlockQuartzGlass::new )
				.useCustomItemModel()
				.rendering( new BlockRenderingCustomizer()
				{
					@Override
					@SideOnly( Side.CLIENT )
					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
					{
						rendering.builtInModel( "models/block/builtin/quartz_glass", new GlassModel() );
					}
				} )
				.build();
		this.quartzVibrantGlass = deco.block( "quartz_vibrant_glass", BlockQuartzLamp::new )
				.addFeatures( AEFeature.DECORATIVE_LIGHTS )
				.useCustomItemModel()
				.build();
		this.quartzFixture = registry.block( "quartz_fixture", BlockQuartzFixture::new )
				.features( AEFeature.DECORATIVE_LIGHTS )
				.useCustomItemModel()
				.build();

		this.fluixBlock = deco.block( "fluix_block", BlockFluix::new ).build();
		this.skyStoneBlock = deco.block( "sky_stone_block", () -> new BlockSkyStone( SkystoneType.STONE ) ).build();
		this.smoothSkyStoneBlock = deco.block( "smooth_sky_stone_block", () -> new BlockSkyStone( SkystoneType.BLOCK ) ).build();
		this.skyStoneBrick = deco.block( "sky_stone_brick", () -> new BlockSkyStone( SkystoneType.BRICK ) ).build();
		this.skyStoneSmallBrick = deco.block( "sky_stone_small_brick", () -> new BlockSkyStone( SkystoneType.SMALL_BRICK ) ).build();

		this.skyStoneChest = registry.block( "sky_stone_chest", () -> new BlockSkyChest( SkyChestType.STONE ) )
				.features( AEFeature.SKY_STONE_CHESTS )
				.tileEntity( TileSkyChest.class )
				.rendering( new SkyChestRenderingCustomizer( SkyChestType.STONE ) )
				.build();
		this.smoothSkyStoneChest = registry.block( "smooth_sky_stone_chest", () -> new BlockSkyChest( SkyChestType.BLOCK ) )
				.features( AEFeature.SKY_STONE_CHESTS )
				.tileEntity( TileSkyChest.class )
				.rendering( new SkyChestRenderingCustomizer( SkyChestType.BLOCK ) )
				.build();

		this.skyCompass = registry.block( "sky_compass", BlockSkyCompass::new )
				.features( AEFeature.METEORITE_COMPASS )
				.tileEntity( TileSkyCompass.class )
				.rendering( new SkyCompassRendering() )
				.build();
		this.grindstone = registry.block( "grindstone", BlockGrinder::new ).features( AEFeature.GRIND_STONE ).tileEntity( TileGrinder.class ).build();
		this.crank = registry.block( "crank", BlockCrank::new )
				.features( AEFeature.GRIND_STONE )
				.tileEntity( TileCrank.class )
				.rendering( new CrankRendering() )
				.build();
		this.inscriber = registry.block( "inscriber", BlockInscriber::new )
				.features( AEFeature.INSCRIBER )
				.tileEntity( TileInscriber.class )
				.rendering( new InscriberRendering() )
				.build();
		this.wirelessAccessPoint = registry.block( "wireless_access_point", BlockWireless::new )
				.features( AEFeature.WIRELESS_ACCESS_TERMINAL )
				.tileEntity( TileWireless.class )
				.rendering( new WirelessRendering() )
				.build();
		this.charger = registry.block( "charger", BlockCharger::new )
				.features( AEFeature.CHARGER )
				.tileEntity( TileCharger.class )
				.rendering( new BlockRenderingCustomizer()
				{
					@Override
					@SideOnly( Side.CLIENT )
					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
					{
						rendering.tesr( BlockCharger.createTesr() );
					}
				} )
				.build();
		this.tinyTNT = registry.block( "tiny_tnt", BlockTinyTNT::new )
				.features( AEFeature.TINY_TNT )
				.postInit( ( block, item ) ->
				{
					BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject( item, new DispenserBehaviorTinyTNT() );
				} )
				.build();
		this.securityStation = registry.block( "security_station", BlockSecurityStation::new )
				.features( AEFeature.SECURITY )
				.tileEntity( TileSecurityStation.class )
				.rendering( new SecurityStationRendering() )
				.build();
		this.quantumRing = registry.block( "quantum_ring", BlockQuantumRing::new )
				.features( AEFeature.QUANTUM_NETWORK_BRIDGE )
				.tileEntity( TileQuantumBridge.class )
				.rendering( new QuantumBridgeRendering() )
				.build();
		this.quantumLink = registry.block( "quantum_link", BlockQuantumLinkChamber::new )
				.features( AEFeature.QUANTUM_NETWORK_BRIDGE )
				.tileEntity( TileQuantumBridge.class )
				.rendering( new QuantumBridgeRendering() )
				.build();
		this.spatialPylon = registry.block( "spatial_pylon", BlockSpatialPylon::new )
				.features( AEFeature.SPATIAL_IO )
				.tileEntity( TileSpatialPylon.class )
				.useCustomItemModel()
				.rendering( new SpatialPylonRendering() )
				.build();
		this.spatialIOPort = registry.block( "spatial_io_port", BlockSpatialIOPort::new ).features( AEFeature.SPATIAL_IO ).tileEntity( TileSpatialIOPort.class ).build();
		this.controller = registry.block( "controller", BlockController::new )
				.features( AEFeature.CHANNELS )
				.tileEntity( TileController.class )
				.useCustomItemModel()
				.rendering( new ControllerRendering() )
				.build();
		this.drive = registry.block( "drive", BlockDrive::new )
				.features( AEFeature.STORAGE_CELLS, AEFeature.ME_DRIVE )
				.tileEntity( TileDrive.class )
				.useCustomItemModel()
				.rendering( new DriveRendering() )
				.build();
		this.chest = registry.block( "chest", BlockChest::new )
				.features( AEFeature.STORAGE_CELLS, AEFeature.ME_CHEST )
				.tileEntity( TileChest.class )
				.useCustomItemModel()
				.rendering( new ChestRendering() )
				.build();
		this.iface = registry.block( "interface", BlockInterface::new ).features( AEFeature.INTERFACE ).tileEntity( TileInterface.class ).build();
		this.cellWorkbench = registry.block( "cell_workbench", BlockCellWorkbench::new ).features( AEFeature.STORAGE_CELLS ).tileEntity( TileCellWorkbench.class ).build();
		this.iOPort = registry.block( "io_port", BlockIOPort::new ).features( AEFeature.STORAGE_CELLS, AEFeature.IO_PORT ).tileEntity( TileIOPort.class ).build();
		this.condenser = registry.block( "condenser", BlockCondenser::new ).features( AEFeature.CONDENSER ).tileEntity( TileCondenser.class ).build();
		this.energyAcceptor = registry.block( "energy_acceptor", BlockEnergyAcceptor::new ).features( AEFeature.ENERGY_ACCEPTOR ).tileEntity( TileEnergyAcceptor.class ).build();
		this.vibrationChamber = registry.block( "vibration_chamber", BlockVibrationChamber::new ).features( AEFeature.POWER_GEN ).tileEntity( TileVibrationChamber.class ).build();
		this.quartzGrowthAccelerator = registry.block( "quartz_growth_accelerator", BlockQuartzGrowthAccelerator::new )
				.tileEntity( TileQuartzGrowthAccelerator.class )
				.features( AEFeature.CRYSTAL_GROWTH_ACCELERATOR )
				.build();
		this.energyCell = registry.block( "energy_cell", BlockEnergyCell::new )
				.features( AEFeature.ENERGY_CELLS )
				.item( AEBaseItemBlockChargeable::new )
				.tileEntity( TileEnergyCell.class )
				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "energy_cell" ) ) )
				.build();
		this.energyCellDense = registry.block( "dense_energy_cell", BlockDenseEnergyCell::new )
				.features( AEFeature.ENERGY_CELLS, AEFeature.DENSE_ENERGY_CELLS )
				.item( AEBaseItemBlockChargeable::new )
				.tileEntity( TileDenseEnergyCell.class )
				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "dense_energy_cell" ) ) )
				.build();
		this.energyCellCreative = registry.block( "creative_energy_cell", BlockCreativeEnergyCell::new )
				.features( AEFeature.CREATIVE )
				.item( AEBaseItemBlockChargeable::new )
				.tileEntity( TileCreativeEnergyCell.class )
				.build();

		FeatureFactory crafting = registry.features( AEFeature.CRAFTING_CPU );
		this.craftingUnit = crafting.block( "crafting_unit", () -> new BlockCraftingUnit( CraftingUnitType.UNIT ) )
				.rendering( new CraftingCubeRendering( "crafting_unit", CraftingUnitType.UNIT ) )
				.tileEntity( TileCraftingTile.class )
				.useCustomItemModel()
				.build();
		this.craftingAccelerator = crafting.block( "crafting_accelerator", () -> new BlockCraftingUnit( CraftingUnitType.ACCELERATOR ) )
				.rendering( new CraftingCubeRendering( "crafting_accelerator", CraftingUnitType.ACCELERATOR ) )
				.tileEntity( TileCraftingTile.class )
				.useCustomItemModel()
				.build();
		this.craftingStorage1k = crafting.block( "crafting_storage_1k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_1K ) )
				.item( ItemCraftingStorage::new )
				.tileEntity( TileCraftingStorageTile.class )
				.rendering( new CraftingCubeRendering( "crafting_storage_1k", CraftingUnitType.STORAGE_1K ) )
				.useCustomItemModel()
				.build();
		this.craftingStorage4k = crafting.block( "crafting_storage_4k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_4K ) )
				.item( ItemCraftingStorage::new )
				.tileEntity( TileCraftingStorageTile.class )
				.rendering( new CraftingCubeRendering( "crafting_storage_4k", CraftingUnitType.STORAGE_4K ) )
				.useCustomItemModel()
				.build();
		this.craftingStorage16k = crafting.block( "crafting_storage_16k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_16K ) )
				.item( ItemCraftingStorage::new )
				.tileEntity( TileCraftingStorageTile.class )
				.rendering( new CraftingCubeRendering( "crafting_storage_16k", CraftingUnitType.STORAGE_16K ) )
				.useCustomItemModel()
				.build();
		this.craftingStorage64k = crafting.block( "crafting_storage_64k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_64K ) )
				.item( ItemCraftingStorage::new )
				.tileEntity( TileCraftingStorageTile.class )
				.rendering( new CraftingCubeRendering( "crafting_storage_64k", CraftingUnitType.STORAGE_64K ) )
				.useCustomItemModel()
				.build();
		this.craftingMonitor = crafting.block( "crafting_monitor", BlockCraftingMonitor::new )
				.tileEntity( TileCraftingMonitorTile.class )
				.rendering( new CraftingCubeRendering( "crafting_monitor", CraftingUnitType.MONITOR ) )
				.useCustomItemModel()
				.build();

		this.molecularAssembler = registry.block( "molecular_assembler", BlockMolecularAssembler::new ).features( AEFeature.MOLECULAR_ASSEMBLER ).tileEntity( TileMolecularAssembler.class ).build();
		this.lightDetector = registry.block( "light_detector", BlockLightDetector::new )
				.features( AEFeature.LIGHT_DETECTOR )
				.tileEntity( TileLightDetector.class )
				.useCustomItemModel()
				.build();
		this.paint = registry.block( "paint", BlockPaint::new )
				.features( AEFeature.PAINT_BALLS )
				.tileEntity( TilePaint.class )
				.rendering( new PaintRendering() )
				.build();

		this.skyStoneStairs = makeStairs( "sky_stone_stairs", registry, this.skyStoneBlock() );
		this.smoothSkyStoneStairs = makeStairs( "smooth_sky_stone_stairs", registry, this.smoothSkyStoneBlock() );
		this.skyStoneBrickStairs = makeStairs( "sky_stone_brick_stairs", registry, this.skyStoneBrick() );
		this.skyStoneSmallBrickStairs = makeStairs( "sky_stone_small_brick_stairs", registry, this.skyStoneSmallBrick() );
		this.fluixStairs = makeStairs( "fluix_stairs", registry, this.fluixBlock() );
		this.quartzStairs = makeStairs( "quartz_stairs", registry, this.quartzBlock() );
		this.chiseledQuartzStairs = makeStairs( "chiseled_quartz_stairs", registry, this.chiseledQuartzBlock() );
		this.quartzPillarStairs = makeStairs( "quartz_pillar_stairs", registry, this.quartzPillar() );

		this.multiPart = registry.block( "cable_bus", BlockCableBus::new )
				.rendering( new CableBusRendering( partModels ) )
				// (handled in BlockCableBus.java and its setupTile())
				// .tileEntity( TileCableBus.class )
				.postInit( ( block, item ) ->
				{
					( (BlockCableBus) block ).setupTile();
				} )
				.build();

		this.skyStoneSlab = makeSlab( "sky_stone_slab", "sky_stone_double_slab", registry, this.skyStoneBlock() );
		this.smoothSkyStoneSlab = makeSlab( "smooth_sky_stone_slab", "smooth_sky_stone_double_slab", registry, this.smoothSkyStoneBlock() );
		this.skyStoneBrickSlab = makeSlab( "sky_stone_brick_slab", "sky_stone_brick_double_slab", registry, this.skyStoneBrick() );
		this.skyStoneSmallBrickSlab = makeSlab( "sky_stone_small_brick_slab", "sky_stone_small_brick_double_slab", registry, this.skyStoneSmallBrick() );
		this.fluixSlab = makeSlab( "fluix_slab", "fluix_double_slab", registry, this.fluixBlock() );
		this.quartzSlab = makeSlab( "quartz_slab", "quartz_double_slab", registry, this.quartzBlock() );
		this.chiseledQuartzSlab = makeSlab( "chiseled_quartz_slab", "chiseled_quartz_double_slab", registry, this.chiseledQuartzBlock() );
		this.quartzPillarSlab = makeSlab( "quartz_pillar_slab", "quartz_pillar_double_slab", registry, this.quartzPillar() );

		this.itemGen = registry.block( "debug_item_gen", BlockItemGen::new )
				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
				.tileEntity( TileItemGen.class )
				.useCustomItemModel()
				.build();
		this.chunkLoader = registry.block( "debug_chunk_loader", BlockChunkloader::new )
				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
				.tileEntity( TileChunkLoader.class )
				.useCustomItemModel()
				.build();
		this.phantomNode = registry.block( "debug_phantom_node", BlockPhantomNode::new )
				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
				.tileEntity( TilePhantomNode.class )
				.useCustomItemModel()
				.build();
		this.cubeGenerator = registry.block( "debug_cube_gen", BlockCubeGenerator::new )
				.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE )
				.tileEntity( TileCubeGenerator.class )
				.useCustomItemModel()
				.build();


		this.registerTileEntity( TileSkyChest.class, "sky_stone_chest" );
		this.registerTileEntity( TileSkyCompass.class, "sky_compass" );
		this.registerTileEntity( TileGrinder.class, "grindstone" );
		this.registerTileEntity( TileCrank.class, "crank" );
		this.registerTileEntity( TileInscriber.class, "inscriber" );
		this.registerTileEntity( TileWireless.class, "wireless_access_point" );
		this.registerTileEntity( TileCharger.class, "charger" );
		this.registerTileEntity( TileSecurityStation.class, "security_station" );
		this.registerTileEntity( TileQuantumBridge.class, "quantum_ring" );
		this.registerTileEntity( TileSpatialPylon.class, "spatial_pylon" );
		this.registerTileEntity( TileSpatialIOPort.class, "spatial_io_port" );
		this.registerTileEntity( TileController.class, "controller" );
		this.registerTileEntity( TileDrive.class, "drive" );
		this.registerTileEntity( TileChest.class, "chest" );
		this.registerTileEntity( TileInterface.class, "interface" );
		this.registerTileEntity( TileCellWorkbench.class, "cell_workbench" );
		this.registerTileEntity( TileIOPort.class, "io_port" );
		this.registerTileEntity( TileCondenser.class, "condenser" );
		this.registerTileEntity( TileEnergyAcceptor.class, "energy_acceptor" );
		this.registerTileEntity( TileVibrationChamber.class, "vibration_chamber" );
		this.registerTileEntity( TileQuartzGrowthAccelerator.class, "quartz_growth_accelerator" );
		this.registerTileEntity( TileEnergyCell.class, "energy_cell" );
		this.registerTileEntity( TileDenseEnergyCell.class, "dense_energy_cell" );
		this.registerTileEntity( TileCreativeEnergyCell.class, "creative_energy_cell" );
		this.registerTileEntity( TileCraftingTile.class, "crafting_unit" );
		this.registerTileEntity( TileCraftingStorageTile.class, "crafting_storage" );
		this.registerTileEntity( TileCraftingMonitorTile.class, "crafting_monitor" );
		this.registerTileEntity( TileMolecularAssembler.class, "molecular_assembler" );
		this.registerTileEntity( TileLightDetector.class, "light_detector" );
		this.registerTileEntity( TilePaint.class, "paint" );
		// This is handled by TileCableBus.java
		// this.registerTileEntity( TileCableBus.class, "cable_bus" );
		this.registerTileEntity( TileItemGen.class, "debug_item_gen" );
		this.registerTileEntity( TileChunkLoader.class, "debug_chunk_loader" );
		this.registerTileEntity( TilePhantomNode.class, "debug_phantom_node" );
		this.registerTileEntity( TileCubeGenerator.class, "debug_cube_gen" );
	}

	private static IBlockDefinition makeSlab( String slabId, String doubleSlabId, FeatureFactory registry, IBlockDefinition blockDef )
	{
		if( !blockDef.maybeBlock().isPresent() )
		{
			return new BlockDefinition( slabId, null, null );
		}

		Block block = blockDef.maybeBlock().get();

		IBlockDefinition slabDef = registry.block( slabId, () -> new BlockSlabCommon.Half( block ) )
				.features( AEFeature.DECORATIVE_QUARTZ_BLOCKS )
				.disableItem()
				.build();

		if( !slabDef.maybeBlock().isPresent() )
		{
			return new BlockDefinition( slabId, null, null );
		}

		BlockSlab slabBlock = (BlockSlab) slabDef.maybeBlock().get();

		// Reigster the double slab variant as well
		IBlockDefinition doubleSlabDef = registry.block( doubleSlabId, () -> new BlockSlabCommon.Double( slabBlock, block ) )
				.features( AEFeature.DECORATIVE_QUARTZ_BLOCKS )
				.disableItem()
				.build();

		Verify.verify( doubleSlabDef.maybeBlock().isPresent() );

		BlockSlab doubleSlabBlock = (BlockSlab) doubleSlabDef.maybeBlock().get();

		// Make the slab item
		IItemDefinition itemDef = registry.item( slabId, () -> new ItemSlab( slabBlock, slabBlock, doubleSlabBlock ) )
				.features( AEFeature.DECORATIVE_QUARTZ_BLOCKS )
				.build();

		Verify.verify( itemDef.maybeItem().isPresent() );

		// Return a new composite block definition that combines the single slab block with the slab item
		return new BlockDefinition( slabId, slabBlock, (ItemBlock) itemDef.maybeItem().get() );
	}

	private static IBlockDefinition makeStairs( String registryName, FeatureFactory registry, IBlockDefinition block )
	{
		return registry.block( registryName, () -> new BlockStairCommon( block.maybeBlock().get(), block.identifier() ) )
				.features( AEFeature.DECORATIVE_QUARTZ_BLOCKS )
				.rendering( new BlockRenderingCustomizer()
				{
					@Override
					@SideOnly( Side.CLIENT )
					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
					{
						ModelResourceLocation model = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, registryName ), "facing=east,half=bottom,shape=straight" );
						itemRendering.model( model );
					}
				} )
				.build();
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
}
