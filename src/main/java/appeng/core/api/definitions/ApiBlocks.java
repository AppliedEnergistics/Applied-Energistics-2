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


import net.minecraft.block.BlockDispenser;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
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
import appeng.block.misc.BlockCellWorkbench;
import appeng.block.misc.BlockCharger;
import appeng.block.misc.BlockCondenser;
import appeng.block.misc.BlockInscriber;
import appeng.block.misc.BlockInterface;
import appeng.block.misc.BlockLightDetector;
import appeng.block.misc.BlockPaint;
import appeng.block.misc.BlockQuartzGrowthAccelerator;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.misc.BlockSecurity;
import appeng.block.misc.BlockSkyCompass;
import appeng.block.misc.BlockTinyTNT;
import appeng.block.misc.BlockVibrationChamber;
import appeng.block.networking.BlockCableBus;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockCreativeEnergyCell;
import appeng.block.networking.BlockDenseEnergyCell;
import appeng.block.networking.BlockEnergyAcceptor;
import appeng.block.networking.BlockEnergyCell;
import appeng.block.networking.BlockEnergyCellRendering;
import appeng.block.networking.BlockWireless;
import appeng.block.networking.CableBusColor;
import appeng.block.networking.CableModelCustomizer;
import appeng.block.qnb.BlockQuantumLinkChamber;
import appeng.block.qnb.BlockQuantumRing;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.block.spatial.BlockSpatialIOPort;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.block.storage.BlockSkyChest;
import appeng.block.storage.BlockSkyChest.SkyChestType;
import appeng.block.storage.SkyChestRenderingCustomizer;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.FeatureFactory;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.debug.BlockChunkloader;
import appeng.debug.BlockCubeGenerator;
import appeng.debug.BlockItemGen;
import appeng.debug.BlockPhantomNode;
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
	/*
	 * private final IBlockDefinition skyStoneSlab;
	 * private final IBlockDefinition skyStoneBlockSlab;
	 * private final IBlockDefinition skyStoneBrickSlab;
	 * private final IBlockDefinition skyStoneSmallBrickSlab;
	 * private final IBlockDefinition fluixSlab;
	 * private final IBlockDefinition quartzSlab;
	 * private final IBlockDefinition chiseledQuartzSlab;
	 * private final IBlockDefinition quartzPillarSlab;
	 */

	private final IBlockDefinition itemGen;
	private final IBlockDefinition chunkLoader;
	private final IBlockDefinition phantomNode;
	private final IBlockDefinition cubeGenerator;

	public ApiBlocks( FeatureFactory registry )
	{
		// this.quartzOre = new BlockDefinition( "ore.quartz", new OreQuartz() );
		this.quartzOre = registry.block( "quartz_ore", BlockQuartzOre::new )
				.postInit( ( block, item ) ->
				{
					OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( block ) );
				} )
				.build();
		this.quartzOreCharged = registry.block( "charged_quartz_ore", BlockChargedQuartzOre::new )
				.postInit( ( block, item ) ->
				{
					OreDictionary.registerOre( "oreCertusQuartz", new ItemStack( block ) );
				} )
				.build();
		this.matrixFrame = registry.block( "matrix_frame", BlockMatrixFrame::new ).features( AEFeature.SpatialIO ).build();

		FeatureFactory deco = registry.features( AEFeature.DecorativeQuartzBlocks );
		this.quartzBlock = deco.block( "quartz_block", BlockQuartz::new ).build();
		this.quartzPillar = deco.block( "quartz_pillar", BlockQuartzPillar::new ).build();
		this.chiseledQuartzBlock = deco.block( "chiseled_quartz_block", BlockChiseledQuartz::new ).build();
		this.quartzGlass = deco.block( "quartz_glass", BlockQuartzGlass::new ).build();
		this.quartzVibrantGlass = deco.block( "quartz_vibrant_glass", BlockQuartzLamp::new ).addFeatures( AEFeature.DecorativeLights ).build();
		this.quartzFixture = registry.block( "quartz_fixture", BlockQuartzTorch::new ).features( AEFeature.DecorativeLights ).build();

		this.fluixBlock = deco.block( "fluix_block", BlockFluix::new ).build();
		this.skyStoneBlock = deco.block( "sky_stone_block", () -> new BlockSkyStone( SkystoneType.STONE ) ).build();
		this.smoothSkyStoneBlock = deco.block( "smooth_skystone", () -> new BlockSkyStone( SkystoneType.BLOCK ) ).build();
		this.skyStoneBrick = deco.block( "skystone_brick", () -> new BlockSkyStone( SkystoneType.BRICK ) ).build();
		this.skyStoneSmallBrick = deco.block( "skystone_small_brick", () -> new BlockSkyStone( SkystoneType.SMALL_BRICK ) ).build();

		this.skyStoneChest = registry.block( "skystone_chest", () -> new BlockSkyChest( SkyChestType.STONE ) )
				.features( AEFeature.SkyStoneChests )
				.rendering( new SkyChestRenderingCustomizer( SkyChestType.STONE ) )
				.build();
		this.smoothSkyStoneChest = registry.block( "smooth_skystone_chest", () -> new BlockSkyChest( SkyChestType.BLOCK ) )
				.features( AEFeature.SkyStoneChests )
				.rendering( new SkyChestRenderingCustomizer( SkyChestType.BLOCK ) )
				.build();

		this.skyCompass = registry.block( "sky_compass", BlockSkyCompass::new ).features( AEFeature.MeteoriteCompass ).build();
		this.grindstone = registry.block( "grindstone", BlockGrinder::new ).features( AEFeature.GrindStone ).build();
		this.crank = registry.block( "crank", BlockCrank::new ).features( AEFeature.GrindStone ).build();
		this.inscriber = registry.block( "inscriber", BlockInscriber::new ).features( AEFeature.Inscriber ).build();
		this.wirelessAccessPoint = registry.block( "wireless_access_point", BlockWireless::new ).features( AEFeature.WirelessAccessTerminal ).build();
		this.charger = registry.block( "charger", BlockCharger::new )
				.rendering( new BlockRenderingCustomizer()
				{
					@Override
					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
					{
						rendering.tesr( BlockCharger.createTesr() );
					}
				} )
				.build();
		this.tinyTNT = registry.block( "tiny_tnt", BlockTinyTNT::new ).features( AEFeature.TinyTNT )
				.postInit( ( block, item ) ->
				{
					BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject( item, new DispenserBehaviorTinyTNT() );
				} )
				.build();
		this.securityStation = registry.block( "security_station", BlockSecurity::new ).features( AEFeature.Security ).build();
		this.quantumRing = registry.block( "quantum_ring", BlockQuantumRing::new ).features( AEFeature.QuantumNetworkBridge ).build();
		this.quantumLink = registry.block( "quantum_link", BlockQuantumLinkChamber::new ).features( AEFeature.QuantumNetworkBridge ).build();
		this.spatialPylon = registry.block( "spatial_pylon", BlockSpatialPylon::new ).features( AEFeature.SpatialIO ).build();
		this.spatialIOPort = registry.block( "spatial_ioport", BlockSpatialIOPort::new ).features( AEFeature.SpatialIO ).build();
		this.controller = registry.block( "controller", BlockController::new ).features( AEFeature.Channels ).build();
		this.drive = registry.block( "drive", BlockDrive::new ).features( AEFeature.StorageCells, AEFeature.MEDrive ).build();
		this.chest = registry.block( "chest", BlockChest::new ).features( AEFeature.StorageCells, AEFeature.MEChest ).build();
		this.iface = registry.block( "interface", BlockInterface::new ).build();
		this.cellWorkbench = registry.block( "cell_workbench", BlockCellWorkbench::new ).features( AEFeature.StorageCells ).build();
		this.iOPort = registry.block( "ioport", BlockIOPort::new ).features( AEFeature.StorageCells, AEFeature.IOPort ).build();
		this.condenser = registry.block( "condenser", BlockCondenser::new ).build();
		this.energyAcceptor = registry.block( "energy_acceptor", BlockEnergyAcceptor::new ).build();
		this.vibrationChamber = registry.block( "vibration_chamber", BlockVibrationChamber::new ).features( AEFeature.PowerGen ).build();
		this.quartzGrowthAccelerator = registry.block( "quartz_growth_accelerator", BlockQuartzGrowthAccelerator::new ).build();
		this.energyCell = registry.block( "energy_cell", BlockEnergyCell::new )
				.item( AEBaseItemBlockChargeable::new )
				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "energy_cell" ) ) )
				.build();
		this.energyCellDense = registry.block( "dense_energy_cell", BlockDenseEnergyCell::new )
				.features( AEFeature.DenseEnergyCells )
				.item( AEBaseItemBlockChargeable::new )
				.rendering( new BlockEnergyCellRendering( new ResourceLocation( AppEng.MOD_ID, "dense_energy_cell" ) ) )
				.build();
		this.energyCellCreative = registry.block( "creative_energy_cell", BlockCreativeEnergyCell::new )
				.features( AEFeature.Creative )
				.item( AEBaseItemBlockChargeable::new )
				.build();

		FeatureFactory crafting = registry.features( AEFeature.CraftingCPU );
		this.craftingUnit = crafting.block( "crafting_unit", () -> new BlockCraftingUnit( CraftingUnitType.UNIT ) ).build();
		this.craftingAccelerator = crafting.block( "crafting_accelerator", () -> new BlockCraftingUnit( CraftingUnitType.ACCELERATOR ) ).build();
		this.craftingStorage1k = crafting.block( "crafting_storage_1k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_1K ) )
				.item( ItemCraftingStorage::new )
				.build();
		this.craftingStorage4k = crafting.block( "crafting_storage_4k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_4K ) )
				.item( ItemCraftingStorage::new )
				.build();
		this.craftingStorage16k = crafting.block( "crafting_storage_16k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_16K ) )
				.item( ItemCraftingStorage::new )
				.build();
		this.craftingStorage64k = crafting.block( "crafting_storage_64k", () -> new BlockCraftingStorage( CraftingUnitType.STORAGE_64K ) )
				.item( ItemCraftingStorage::new )
				.build();
		this.craftingMonitor = crafting.block( "crafting_monitor", BlockCraftingMonitor::new ).build();

		this.molecularAssembler = registry.block( "molecular_assembler", BlockMolecularAssembler::new ).features( AEFeature.MolecularAssembler ).build();
		this.lightDetector = registry.block( "light_detector", BlockLightDetector::new ).features( AEFeature.LightDetector ).build();
		this.paint = registry.block( "paint", BlockPaint::new ).features( AEFeature.PaintBalls ).build();

		this.skyStoneStairs = makeStairs( "skystone_stairs", registry, this.skyStoneBlock() );
		this.smoothSkyStoneStairs = makeStairs( "smooth_skystone_stairs", registry, this.smoothSkyStoneBlock() );
		this.skyStoneBrickStairs = makeStairs( "skystone_brick_stairs", registry, this.skyStoneBrick() );
		this.skyStoneSmallBrickStairs = makeStairs( "skystone_small_brick_stairs", registry, this.skyStoneSmallBrick() );
		this.fluixStairs = makeStairs( "fluix_stairs", registry, this.fluixBlock() );
		this.quartzStairs = makeStairs( "quartz_stairs", registry, this.quartzBlock() );
		this.chiseledQuartzStairs = makeStairs( "chiseled_quartz_stairs", registry, this.chiseledQuartzBlock() );
		this.quartzPillarStairs = makeStairs( "quartz_pillar_stairs", registry, this.quartzPillar() );

		this.multiPart = registry.block( "multipart_block", BlockCableBus::new )
				.rendering( new BlockRenderingCustomizer()
				{
					@Override
					@SideOnly( Side.CLIENT )
					public void customize( IBlockRendering rendering, IItemRendering itemRendering )
					{
						rendering.modelCustomizer( new CableModelCustomizer()::customizeModel )
								.blockColor( new CableBusColor() );
					}
				} )
				.build();

		// TODO Re-Add Slabs...
		/*
		 * this.skyStoneSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( skyStone_stone,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "SkyStoneSlabBlock" ) );
		 * this.skyStoneBlockSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( skyStone_block,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "SkyStoneBlockSlabBlock" ) );
		 * this.skyStoneBrickSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( skyStone_brick,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "SkyStoneBrickSlabBlock" ) );
		 * this.skyStoneSmallBrickSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( skyStone_smallbrick,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "SkyStoneSmallBrickSlabBlock" ) );
		 * this.fluixSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( fluixBlock,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "FluixSlabBlock" ) );
		 * this.quartzSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( quartzBlock,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "QuartzSlabBlock" ) );
		 * this.chiseledQuartzSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( chiseledQuartzBlock,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "ChiseledQuartzSlabBlock" ) );
		 * this.quartzPillarSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( quartzPillar,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "QuartzPillarSlabBlock" ) )
		 */

		this.itemGen = registry.block( "debug_item_gen", BlockItemGen::new ).features( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ).build();
		this.chunkLoader = registry.block( "debug_chunk_loader", BlockChunkloader::new ).features( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ).build();
		this.phantomNode = registry.block( "debug_phantom_node", BlockPhantomNode::new ).features( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ).build();
		this.cubeGenerator = registry.block( "debug_cube_gen", BlockCubeGenerator::new ).features( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative ).build();
	}

	private static IBlockDefinition makeStairs( String registryName, FeatureFactory registry, IBlockDefinition block )
	{
		return registry.block( registryName, () -> new BlockStairCommon( block.maybeBlock().get(), block.identifier() ) )
				.features( AEFeature.DecorativeQuartzBlocks )
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

	/*
	 * @Override
	 * public IBlockDefinition skyStoneSlab()
	 * {
	 * return this.skyStoneSlab;
	 * }
	 * @Override
	 * public IBlockDefinition skyStoneBlockSlab()
	 * {
	 * return this.skyStoneBlockSlab;
	 * }
	 * @Override
	 * public IBlockDefinition skyStoneBrickSlab()
	 * {
	 * return this.skyStoneBrickSlab;
	 * }
	 * @Override
	 * public IBlockDefinition skyStoneSmallBrickSlab()
	 * {
	 * return this.skyStoneSmallBrickSlab;
	 * }
	 * @Override
	 * public IBlockDefinition fluixSlab()
	 * {
	 * return this.fluixSlab;
	 * }
	 * @Override
	 * public IBlockDefinition quartzSlab()
	 * {
	 * return this.quartzSlab;
	 * }
	 * @Override
	 * public IBlockDefinition chiseledQuartzSlab()
	 * {
	 * return this.chiseledQuartzSlab;
	 * }
	 * @Override
	 * public IBlockDefinition quartzPillarSlab()
	 * {
	 * return this.quartzPillarSlab;
	 * }
	 */

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
