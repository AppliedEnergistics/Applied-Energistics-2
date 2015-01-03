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


import java.util.Set;

import com.google.common.collect.ImmutableSet;

import appeng.api.definitions.IBlocks;
import appeng.api.util.AEItemDefinition;
import appeng.api.util.IOrientableBlock;
import appeng.block.crafting.BlockCraftingMonitor;
import appeng.block.crafting.BlockCraftingStorage;
import appeng.block.crafting.BlockCraftingUnit;
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
import appeng.block.solids.BlockFluix;
import appeng.block.solids.BlockQuartz;
import appeng.block.solids.BlockQuartzChiseled;
import appeng.block.solids.BlockQuartzGlass;
import appeng.block.solids.BlockQuartzLamp;
import appeng.block.solids.BlockQuartzPillar;
import appeng.block.solids.BlockSkyStone;
import appeng.block.solids.OreQuartz;
import appeng.block.solids.OreQuartzCharged;
import appeng.block.spatial.BlockMatrixFrame;
import appeng.block.spatial.BlockSpatialIOPort;
import appeng.block.spatial.BlockSpatialPylon;
import appeng.block.stair.ChiseledQuartzStairBlock;
import appeng.block.stair.FluixStairBlock;
import appeng.block.stair.QuartzPillarStairBlock;
import appeng.block.stair.QuartzStairBlock;
import appeng.block.stair.SkyStoneBlockStairBlock;
import appeng.block.stair.SkyStoneBrickStairBlock;
import appeng.block.stair.SkyStoneSmallBrickStairBlock;
import appeng.block.stair.SkyStoneStairBlock;
import appeng.block.storage.BlockChest;
import appeng.block.storage.BlockDrive;
import appeng.block.storage.BlockIOPort;
import appeng.block.storage.BlockSkyChest;
import appeng.core.features.WrappedDamageItemDefinition;
import appeng.debug.BlockChunkloader;
import appeng.debug.BlockCubeGenerator;
import appeng.debug.BlockItemGen;
import appeng.debug.BlockPhantomNode;


/**
 * Internal implementation for the API blocks
 */
public final class ApiBlocks implements IBlocks
{
	private final AEItemDefinition quartzOre;
	private final AEItemDefinition quartzOreCharged;
	private final AEItemDefinition matrixFrame;
	private final AEItemDefinition quartz;
	private final AEItemDefinition quartzPillar;
	private final AEItemDefinition quartzChiseled;
	private final AEItemDefinition quartzGlass;
	private final AEItemDefinition quartzVibrantGlass;
	private final AEItemDefinition quartzTorch;
	private final AEItemDefinition fluix;
	private final AEItemDefinition skyStone;
	private final AEItemDefinition skyChest;
	private final AEItemDefinition skyCompass;
	private final AEItemDefinition grindStone;
	private final AEItemDefinition crankHandle;
	private final AEItemDefinition inscriber;
	private final AEItemDefinition wireless;
	private final AEItemDefinition charger;
	private final AEItemDefinition tinyTNT;
	private final AEItemDefinition security;
	private final AEItemDefinition quantumRing;
	private final AEItemDefinition quantumLink;
	private final AEItemDefinition spatialPylon;
	private final AEItemDefinition spatialIOPort;
	private final AEItemDefinition multiPart;
	private final AEItemDefinition controller;
	private final AEItemDefinition drive;
	private final AEItemDefinition chest;
	private final AEItemDefinition iface;
	private final AEItemDefinition cellWorkbench;
	private final AEItemDefinition iOPort;
	private final AEItemDefinition condenser;
	private final AEItemDefinition energyAcceptor;
	private final AEItemDefinition vibrationChamber;
	private final AEItemDefinition quartzGrowthAccelerator;
	private final AEItemDefinition energyCell;
	private final AEItemDefinition energyCellDense;
	private final AEItemDefinition energyCellCreative;
	private final AEItemDefinition craftingUnit;
	private final AEItemDefinition craftingAccelerator;
	private final AEItemDefinition craftingStorage1k;
	private final AEItemDefinition craftingStorage4k;
	private final AEItemDefinition craftingStorage16k;
	private final AEItemDefinition craftingStorage64k;
	private final AEItemDefinition craftingMonitor;
	private final AEItemDefinition molecularAssembler;
	private final AEItemDefinition lightDetector;
	private final AEItemDefinition paint;
	private final AEItemDefinition skyStoneStair;
	private final AEItemDefinition skyStoneBlockStair;
	private final AEItemDefinition skyStoneBrickStair;
	private final AEItemDefinition skyStoneSmallBrickStair;
	private final AEItemDefinition fluixStair;
	private final AEItemDefinition quartzStair;
	private final AEItemDefinition chiseledQuartzStair;
	private final AEItemDefinition quartzPillarStair;

	private final AEItemDefinition itemGen;
	private final AEItemDefinition chunkLoader;
	private final AEItemDefinition phantomNode;
	private final AEItemDefinition cubeGenerator;

	private final Set<IOrientableBlock> orientables;

	public ApiBlocks( DefinitionConstructor constructor )
	{
		final BlockLightDetector lightDetector = new BlockLightDetector();
		final BlockQuartzPillar quartzPillar = new BlockQuartzPillar();
		final BlockSkyStone skyStone = new BlockSkyStone();
		final BlockQuartzGrowthAccelerator cga = new BlockQuartzGrowthAccelerator();
		final BlockQuartzTorch quartzTorch = new BlockQuartzTorch();

		this.orientables = ImmutableSet.<IOrientableBlock>of( lightDetector, quartzPillar, skyStone, cga, quartzTorch );

		this.quartzOre = constructor.registerAndConstructDefinition( new OreQuartz() );
		this.quartzOreCharged = constructor.registerAndConstructDefinition( new OreQuartzCharged() );
		this.matrixFrame = constructor.registerAndConstructDefinition( new BlockMatrixFrame() );
		this.quartz = constructor.registerAndConstructDefinition( new BlockQuartz() );
		this.quartzPillar = constructor.registerAndConstructDefinition( quartzPillar );
		this.quartzChiseled = constructor.registerAndConstructDefinition( new BlockQuartzChiseled() );
		this.quartzGlass = constructor.registerAndConstructDefinition( new BlockQuartzGlass() );
		this.quartzVibrantGlass = constructor.registerAndConstructDefinition( new BlockQuartzLamp() );
		this.quartzTorch = constructor.registerAndConstructDefinition( quartzTorch );
		this.fluix = constructor.registerAndConstructDefinition( new BlockFluix() );
		this.skyStone = constructor.registerAndConstructDefinition( skyStone );
		this.skyChest = constructor.registerAndConstructDefinition( new BlockSkyChest() );
		this.skyCompass = constructor.registerAndConstructDefinition( new BlockSkyCompass() );
		this.grindStone = constructor.registerAndConstructDefinition( new BlockGrinder() );
		this.crankHandle = constructor.registerAndConstructDefinition( new BlockCrank() );
		this.inscriber = constructor.registerAndConstructDefinition( new BlockInscriber() );
		this.wireless = constructor.registerAndConstructDefinition( new BlockWireless() );
		this.charger = constructor.registerAndConstructDefinition( new BlockCharger() );
		this.tinyTNT = constructor.registerAndConstructDefinition( new BlockTinyTNT() );
		this.security = constructor.registerAndConstructDefinition( new BlockSecurity() );
		this.quantumRing = constructor.registerAndConstructDefinition( new BlockQuantumRing() );
		this.quantumLink = constructor.registerAndConstructDefinition( new BlockQuantumLinkChamber() );
		this.spatialPylon = constructor.registerAndConstructDefinition( new BlockSpatialPylon() );
		this.spatialIOPort = constructor.registerAndConstructDefinition( new BlockSpatialIOPort() );
		this.multiPart = constructor.registerAndConstructDefinition( new BlockCableBus() );
		this.controller = constructor.registerAndConstructDefinition( new BlockController() );
		this.drive = constructor.registerAndConstructDefinition( new BlockDrive() );
		this.chest = constructor.registerAndConstructDefinition( new BlockChest() );
		this.iface = constructor.registerAndConstructDefinition( new BlockInterface() );
		this.cellWorkbench = constructor.registerAndConstructDefinition( new BlockCellWorkbench() );
		this.iOPort = constructor.registerAndConstructDefinition( new BlockIOPort() );
		this.condenser = constructor.registerAndConstructDefinition( new BlockCondenser() );
		this.energyAcceptor = constructor.registerAndConstructDefinition( new BlockEnergyAcceptor() );
		this.vibrationChamber = constructor.registerAndConstructDefinition( new BlockVibrationChamber() );
		this.quartzGrowthAccelerator = constructor.registerAndConstructDefinition( cga );
		this.energyCell = constructor.registerAndConstructDefinition( new BlockEnergyCell() );
		this.energyCellDense = constructor.registerAndConstructDefinition( new BlockDenseEnergyCell() );
		this.energyCellCreative = constructor.registerAndConstructDefinition( new BlockCreativeEnergyCell() );
		this.craftingUnit = constructor.registerAndConstructDefinition( new BlockCraftingUnit() );
		this.craftingAccelerator = new WrappedDamageItemDefinition( this.craftingUnit, 1 );
		this.craftingStorage1k = constructor.registerAndConstructDefinition( new BlockCraftingStorage() );
		this.craftingStorage4k = new WrappedDamageItemDefinition( this.craftingStorage1k, 1 );
		this.craftingStorage16k = new WrappedDamageItemDefinition( this.craftingStorage1k, 2 );
		this.craftingStorage64k = new WrappedDamageItemDefinition( this.craftingStorage1k, 3 );
		this.craftingMonitor = constructor.registerAndConstructDefinition( new BlockCraftingMonitor() );
		this.molecularAssembler = constructor.registerAndConstructDefinition( new BlockMolecularAssembler() );
		this.lightDetector = constructor.registerAndConstructDefinition( lightDetector );
		this.paint = constructor.registerAndConstructDefinition( new BlockPaint() );

		this.skyStoneStair = constructor.registerAndConstructDefinition( new SkyStoneStairBlock( this.skyStone.block(), 0 ) );
		this.skyStoneBlockStair = constructor.registerAndConstructDefinition( new SkyStoneBlockStairBlock( this.skyStone.block(), 1 ) );
		this.skyStoneBrickStair = constructor.registerAndConstructDefinition( new SkyStoneBrickStairBlock( this.skyStone.block(), 2 ) );
		this.skyStoneSmallBrickStair = constructor.registerAndConstructDefinition( new SkyStoneSmallBrickStairBlock( this.skyStone.block(), 3 ) );

		this.fluixStair = constructor.registerAndConstructDefinition( new FluixStairBlock( this.fluix.block() ) );

		this.quartzStair = constructor.registerAndConstructDefinition( new QuartzStairBlock( this.quartz.block() ) );

		this.chiseledQuartzStair = constructor.registerAndConstructDefinition( new ChiseledQuartzStairBlock( this.quartzChiseled.block() ) );

		this.quartzPillarStair = constructor.registerAndConstructDefinition( new QuartzPillarStairBlock( this.quartzPillar.block() ) );

		this.itemGen = constructor.registerAndConstructDefinition( new BlockItemGen() );
		this.chunkLoader = constructor.registerAndConstructDefinition( new BlockChunkloader() );
		this.phantomNode = constructor.registerAndConstructDefinition( new BlockPhantomNode() );
		this.cubeGenerator = constructor.registerAndConstructDefinition( new BlockCubeGenerator() );
	}

	@Override
	public AEItemDefinition quartzOre()
	{
		return this.quartzOre;
	}

	@Override
	public AEItemDefinition quartzOreCharged()
	{
		return this.quartzOreCharged;
	}

	@Override
	public AEItemDefinition matrixFrame()
	{
		return this.matrixFrame;
	}

	@Override
	public AEItemDefinition quartz()
	{
		return this.quartz;
	}

	@Override
	public AEItemDefinition quartzPillar()
	{
		return this.quartzPillar;
	}

	@Override
	public AEItemDefinition quartzChiseled()
	{
		return this.quartzChiseled;
	}

	@Override
	public AEItemDefinition quartzGlass()
	{
		return this.quartzGlass;
	}

	@Override
	public AEItemDefinition quartzVibrantGlass()
	{
		return this.quartzVibrantGlass;
	}

	@Override
	public AEItemDefinition quartzTorch()
	{
		return this.quartzTorch;
	}

	@Override
	public AEItemDefinition fluix()
	{
		return this.fluix;
	}

	@Override
	public AEItemDefinition skyStone()
	{
		return this.skyStone;
	}

	@Override
	public AEItemDefinition skyChest()
	{
		return this.skyChest;
	}

	@Override
	public AEItemDefinition skyCompass()
	{
		return this.skyCompass;
	}

	@Override
	public AEItemDefinition skyStoneStair()
	{
		return this.skyStoneStair;
	}

	@Override
	public AEItemDefinition skyStoneBlockStair()
	{
		return this.skyStoneBlockStair;
	}

	@Override
	public AEItemDefinition skyStoneBrickStair()
	{
		return this.skyStoneBrickStair;
	}

	@Override
	public AEItemDefinition skyStoneSmallBrickStair()
	{
		return this.skyStoneSmallBrickStair;
	}

	@Override
	public AEItemDefinition fluixStair()
	{
		return this.fluixStair;
	}

	@Override
	public AEItemDefinition quartzStair()
	{
		return this.quartzStair;
	}

	@Override
	public AEItemDefinition chiseledQuartzStair()
	{
		return this.chiseledQuartzStair;
	}

	@Override
	public AEItemDefinition quartzPillarStair()
	{
		return this.quartzPillarStair;
	}

	@Override
	public AEItemDefinition grindStone()
	{
		return this.grindStone;
	}

	@Override
	public AEItemDefinition crankHandle()
	{
		return this.crankHandle;
	}

	@Override
	public AEItemDefinition inscriber()
	{
		return this.inscriber;
	}

	@Override
	public AEItemDefinition wireless()
	{
		return this.wireless;
	}

	@Override
	public AEItemDefinition charger()
	{
		return this.charger;
	}

	@Override
	public AEItemDefinition tinyTNT()
	{
		return this.tinyTNT;
	}

	@Override
	public AEItemDefinition security()
	{
		return this.security;
	}

	@Override
	public AEItemDefinition quantumRing()
	{
		return this.quantumRing;
	}

	@Override
	public AEItemDefinition quantumLink()
	{
		return this.quantumLink;
	}

	@Override
	public AEItemDefinition spatialPylon()
	{
		return this.spatialPylon;
	}

	@Override
	public AEItemDefinition spatialIOPort()
	{
		return this.spatialIOPort;
	}

	@Override
	public AEItemDefinition multiPart()
	{
		return this.multiPart;
	}

	@Override
	public AEItemDefinition controller()
	{
		return this.controller;
	}

	@Override
	public AEItemDefinition drive()
	{
		return this.drive;
	}

	@Override
	public AEItemDefinition chest()
	{
		return this.chest;
	}

	@Override
	public AEItemDefinition iface()
	{
		return this.iface;
	}

	@Override
	public AEItemDefinition cellWorkbench()
	{
		return this.cellWorkbench;
	}

	@Override
	public AEItemDefinition iOPort()
	{
		return this.iOPort;
	}

	@Override
	public AEItemDefinition condenser()
	{
		return this.condenser;
	}

	@Override
	public AEItemDefinition energyAcceptor()
	{
		return this.energyAcceptor;
	}

	@Override
	public AEItemDefinition vibrationChamber()
	{
		return this.vibrationChamber;
	}

	@Override
	public AEItemDefinition quartzGrowthAccelerator()
	{
		return this.quartzGrowthAccelerator;
	}

	@Override
	public AEItemDefinition energyCell()
	{
		return this.energyCell;
	}

	@Override
	public AEItemDefinition energyCellDense()
	{
		return this.energyCellDense;
	}

	@Override
	public AEItemDefinition energyCellCreative()
	{
		return this.energyCellCreative;
	}

	@Override
	public AEItemDefinition craftingUnit()
	{
		return this.craftingUnit;
	}

	@Override
	public AEItemDefinition craftingAccelerator()
	{
		return this.craftingAccelerator;
	}

	@Override
	public AEItemDefinition craftingStorage1k()
	{
		return this.craftingStorage1k;
	}

	@Override
	public AEItemDefinition craftingStorage4k()
	{
		return this.craftingStorage4k;
	}

	@Override
	public AEItemDefinition craftingStorage16k()
	{
		return this.craftingStorage16k;
	}

	@Override
	public AEItemDefinition craftingStorage64k()
	{
		return this.craftingStorage64k;
	}

	@Override
	public AEItemDefinition craftingMonitor()
	{
		return this.craftingMonitor;
	}

	@Override
	public AEItemDefinition molecularAssembler()
	{
		return this.molecularAssembler;
	}

	@Override
	public AEItemDefinition lightDetector()
	{
		return this.lightDetector;
	}

	@Override
	public AEItemDefinition paint()
	{
		return this.paint;
	}

	public AEItemDefinition chunkLoader()
	{
		return this.chunkLoader;
	}

	public AEItemDefinition itemGen()
	{
		return this.itemGen;
	}

	public AEItemDefinition phantomNode()
	{
		return this.phantomNode;
	}

	public AEItemDefinition cubeGenerator()
	{
		return this.cubeGenerator;
	}

	public Set<IOrientableBlock> orientables()
	{
		return this.orientables;
	}
}
