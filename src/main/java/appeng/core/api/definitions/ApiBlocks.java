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


import net.minecraft.block.Block;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.ITileDefinition;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingStorage;
import appeng.block.crafting.BlockCraftingUnit;
import appeng.block.crafting.BlockCraftingUnit.CraftingUnitType;
import appeng.block.crafting.BlockMolecularAssembler;
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
import appeng.block.networking.BlockWireless;
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


/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks
{
	private final IBlockDefinition quartzOre;
	private final IBlockDefinition quartzOreCharged;
	private final IBlockDefinition matrixFrame;
	private final IBlockDefinition quartz;
	private final IBlockDefinition quartzPillar;
	private final IBlockDefinition quartzChiseled;
	private final IBlockDefinition quartzGlass;
	private final IBlockDefinition quartzVibrantGlass;
	private final IBlockDefinition quartzTorch;
	private final IBlockDefinition fluix;
	private final IBlockDefinition skyStone_stone;
	private final IBlockDefinition skyStone_block;
	private final IBlockDefinition skyStone_brick;
	private final IBlockDefinition skyStone_smallbrick;
	private final IBlockDefinition skyChest;
	private final IBlockDefinition skyChestBlock;
	private final IBlockDefinition skyCompass;
	private final ITileDefinition grindStone;
	private final ITileDefinition crankHandle;
	private final ITileDefinition inscriber;
	private final ITileDefinition wireless;
	private final ITileDefinition charger;
	private final IBlockDefinition tinyTNT;
	private final ITileDefinition security;
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
	private final IBlockDefinition skyStoneStair;
	private final IBlockDefinition skyStoneBlockStair;
	private final IBlockDefinition skyStoneBrickStair;
	private final IBlockDefinition skyStoneSmallBrickStair;
	private final IBlockDefinition fluixStair;
	private final IBlockDefinition quartzStair;
	private final IBlockDefinition chiseledQuartzStair;
	private final IBlockDefinition quartzPillarStair;
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

	public ApiBlocks( final DefinitionConstructor constructor )
	{
		// this.quartzOre = new BlockDefinition( "ore.quartz", new OreQuartz() );
		this.quartzOre = constructor.registerBlockDefinition( new BlockQuartzOre() );
		this.quartzOreCharged = constructor.registerBlockDefinition( new BlockChargedQuartzOre() );
		this.matrixFrame = constructor.registerBlockDefinition( new BlockMatrixFrame() );
		this.quartz = constructor.registerBlockDefinition( new BlockQuartz() );
		this.quartzPillar = constructor.registerBlockDefinition( new BlockQuartzPillar() );
		this.quartzChiseled = constructor.registerBlockDefinition( new BlockChiseledQuartz() );
		this.quartzGlass = constructor.registerBlockDefinition( new BlockQuartzGlass() );
		this.quartzVibrantGlass = constructor.registerBlockDefinition( new BlockQuartzLamp() );
		this.quartzTorch = constructor.registerBlockDefinition( new BlockQuartzTorch() );
		this.fluix = constructor.registerBlockDefinition( new BlockFluix() );
		this.skyStone_stone = constructor.registerBlockDefinition( new BlockSkyStone( SkystoneType.STONE ) );
		this.skyStone_block = constructor.registerBlockDefinition( new BlockSkyStone( SkystoneType.BLOCK ) );
		this.skyStone_brick = constructor.registerBlockDefinition( new BlockSkyStone( SkystoneType.BRICK ) );
		this.skyStone_smallbrick = constructor.registerBlockDefinition( new BlockSkyStone( SkystoneType.SMALL_BRICK ) );
		this.skyChest = constructor.registerBlockDefinition( new BlockSkyChest( SkyChestType.STONE ) );
		this.skyChestBlock = constructor.registerBlockDefinition( new BlockSkyChest( SkyChestType.BLOCK ) );
		this.skyCompass = constructor.registerBlockDefinition( new BlockSkyCompass() );
		this.grindStone = constructor.registerTileDefinition( new BlockGrinder() );
		this.crankHandle = constructor.registerTileDefinition( new BlockCrank() );
		this.inscriber = constructor.registerTileDefinition( new BlockInscriber() );
		this.wireless = constructor.registerTileDefinition( new BlockWireless() );
		this.charger = constructor.registerTileDefinition( new BlockCharger() );
		this.tinyTNT = constructor.registerBlockDefinition( new BlockTinyTNT() );
		this.security = constructor.registerTileDefinition( new BlockSecurity() );
		this.quantumRing = constructor.registerTileDefinition( new BlockQuantumRing() );
		this.quantumLink = constructor.registerTileDefinition( new BlockQuantumLinkChamber() );
		this.spatialPylon = constructor.registerTileDefinition( new BlockSpatialPylon() );
		this.spatialIOPort = constructor.registerTileDefinition( new BlockSpatialIOPort() );
		this.multiPart = constructor.registerTileDefinition( new BlockCableBus() );
		this.controller = constructor.registerTileDefinition( new BlockController() );
		this.drive = constructor.registerTileDefinition( new BlockDrive() );
		this.chest = constructor.registerTileDefinition( new BlockChest() );
		this.iface = constructor.registerTileDefinition( new BlockInterface() );
		this.cellWorkbench = constructor.registerTileDefinition( new BlockCellWorkbench() );
		this.iOPort = constructor.registerTileDefinition( new BlockIOPort() );
		this.condenser = constructor.registerTileDefinition( new BlockCondenser() );
		this.energyAcceptor = constructor.registerTileDefinition( new BlockEnergyAcceptor() );
		this.vibrationChamber = constructor.registerTileDefinition( new BlockVibrationChamber() );
		this.quartzGrowthAccelerator = constructor.registerTileDefinition( new BlockQuartzGrowthAccelerator() );
		this.energyCell = constructor.registerTileDefinition( new BlockEnergyCell() );
		this.energyCellDense = constructor.registerTileDefinition( new BlockDenseEnergyCell() );
		this.energyCellCreative = constructor.registerTileDefinition( new BlockCreativeEnergyCell() );
		this.craftingUnit = constructor.registerTileDefinition( new BlockCraftingUnit( CraftingUnitType.UNIT ) );
		this.craftingAccelerator = constructor.registerTileDefinition( new BlockCraftingUnit( CraftingUnitType.ACCELERATOR ) );
		this.craftingStorage1k = constructor.registerTileDefinition( new BlockCraftingStorage( CraftingUnitType.STORAGE_1K ) );
		this.craftingStorage4k = constructor.registerTileDefinition( new BlockCraftingStorage( CraftingUnitType.STORAGE_4K ) );
		this.craftingStorage16k = constructor.registerTileDefinition( new BlockCraftingStorage( CraftingUnitType.STORAGE_16K ) );
		this.craftingStorage64k = constructor.registerTileDefinition( new BlockCraftingStorage( CraftingUnitType.STORAGE_64K ) );
		this.craftingMonitor = constructor.registerTileDefinition( new BlockCraftingMonitor() );
		this.molecularAssembler = constructor.registerTileDefinition( new BlockMolecularAssembler() );
		this.lightDetector = constructor.registerTileDefinition( new BlockLightDetector() );
		this.paint = constructor.registerTileDefinition( new BlockPaint() );

		this.skyStoneStair = this.makeStairs( constructor, this.skyStone_stone, "skystone.stone" );
		this.skyStoneBlockStair = this.makeStairs( constructor, this.skyStone_block, "skystone.block" );
		this.skyStoneBrickStair = this.makeStairs( constructor, this.skyStone_brick, "skystone.brick" );
		this.skyStoneSmallBrickStair = this.makeStairs( constructor, this.skyStone_smallbrick, "skystone.brick.small" );
		this.fluixStair = this.makeStairs( constructor, this.fluix, "fluix" );
		this.quartzStair = this.makeStairs( constructor, this.quartz, "quartz.certus" );
		this.chiseledQuartzStair = this.makeStairs( constructor, this.quartzChiseled, "quartz.certus.chiseled" );
		this.quartzPillarStair = this.makeStairs( constructor, this.quartzPillar, "quartz.certus.pillar" );

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
		 * this.chiseledQuartzSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( chiseledQuartz,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "ChiseledQuartzSlabBlock" ) );;
		 * this.quartzPillarSlab = constructor.registerBlockDefinition( new AEBaseSlabBlock( quartzPillar,
		 * EnumSet.of(AEFeature.DecorativeQuartzBlocks), false, "QuartzPillarSlabBlock" ) )
		 */

		this.itemGen = constructor.registerBlockDefinition( new BlockItemGen() );
		this.chunkLoader = constructor.registerBlockDefinition( new BlockChunkloader() );
		this.phantomNode = constructor.registerBlockDefinition( new BlockPhantomNode() );
		this.cubeGenerator = constructor.registerBlockDefinition( new BlockCubeGenerator() );
	}

	private IBlockDefinition makeStairs( final DefinitionConstructor constructor, final IBlockDefinition definition, final String name )
	{
		for( final Block block : definition.maybeBlock().asSet() )
		{
			return constructor.registerBlockDefinition( new BlockStairCommon( block, name ) );
		}

		return null;
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
	public IBlockDefinition quartz()
	{
		return this.quartz;
	}

	@Override
	public IBlockDefinition quartzPillar()
	{
		return this.quartzPillar;
	}

	@Override
	public IBlockDefinition quartzChiseled()
	{
		return this.quartzChiseled;
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
	public IBlockDefinition quartzTorch()
	{
		return this.quartzTorch;
	}

	@Override
	public IBlockDefinition fluix()
	{
		return this.fluix;
	}

	@Override
	public IBlockDefinition skyStone()
	{
		return this.skyStone_stone;
	}

	@Override
	public IBlockDefinition skyStoneBlock()
	{
		return this.skyStone_block;
	}

	@Override
	public IBlockDefinition skyStoneBrick()
	{
		return this.skyStone_brick;
	}

	@Override
	public IBlockDefinition skyStoneSmallBrick()
	{
		return this.skyStone_smallbrick;
	}

	@Override
	public IBlockDefinition skyChest()
	{
		return this.skyChest;
	}

	@Override
	public IBlockDefinition skyChestBlock()
	{
		return this.skyChestBlock;
	}

	@Override
	public IBlockDefinition skyCompass()
	{
		return this.skyCompass;
	}

	@Override
	public IBlockDefinition skyStoneStair()
	{
		return this.skyStoneStair;
	}

	@Override
	public IBlockDefinition skyStoneBlockStair()
	{
		return this.skyStoneBlockStair;
	}

	@Override
	public IBlockDefinition skyStoneBrickStair()
	{
		return this.skyStoneBrickStair;
	}

	@Override
	public IBlockDefinition skyStoneSmallBrickStair()
	{
		return this.skyStoneSmallBrickStair;
	}

	@Override
	public IBlockDefinition fluixStair()
	{
		return this.fluixStair;
	}

	@Override
	public IBlockDefinition quartzStair()
	{
		return this.quartzStair;
	}

	@Override
	public IBlockDefinition chiseledQuartzStair()
	{
		return this.chiseledQuartzStair;
	}

	@Override
	public IBlockDefinition quartzPillarStair()
	{
		return this.quartzPillarStair;
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
	public ITileDefinition grindStone()
	{
		return this.grindStone;
	}

	@Override
	public ITileDefinition crankHandle()
	{
		return this.crankHandle;
	}

	@Override
	public ITileDefinition inscriber()
	{
		return this.inscriber;
	}

	@Override
	public ITileDefinition wireless()
	{
		return this.wireless;
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
	public ITileDefinition security()
	{
		return this.security;
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
