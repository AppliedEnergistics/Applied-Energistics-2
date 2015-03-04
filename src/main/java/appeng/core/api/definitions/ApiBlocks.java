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
import javax.annotation.Nullable;

import net.minecraft.block.Block;

import com.google.common.base.Function;
import com.google.common.base.Optional;
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
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
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
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	private final Optional<AEItemDefinition> quartzOre;
	private final Optional<AEItemDefinition> quartzOreCharged;
	private final Optional<AEItemDefinition> matrixFrame;
	private final Optional<AEItemDefinition> quartz;
	private final Optional<AEItemDefinition> quartzPillar;
	private final Optional<AEItemDefinition> quartzChiseled;
	private final Optional<AEItemDefinition> quartzGlass;
	private final Optional<AEItemDefinition> quartzVibrantGlass;
	private final Optional<AEItemDefinition> quartzTorch;
	private final Optional<AEItemDefinition> fluix;
	private final Optional<AEItemDefinition> skyStone;
	private final Optional<AEItemDefinition> skyChest;
	private final Optional<AEItemDefinition> skyCompass;
	private final Optional<AEItemDefinition> grindStone;
	private final Optional<AEItemDefinition> crankHandle;
	private final Optional<AEItemDefinition> inscriber;
	private final Optional<AEItemDefinition> wireless;
	private final Optional<AEItemDefinition> charger;
	private final Optional<AEItemDefinition> tinyTNT;
	private final Optional<AEItemDefinition> security;
	private final Optional<AEItemDefinition> quantumRing;
	private final Optional<AEItemDefinition> quantumLink;
	private final Optional<AEItemDefinition> spatialPylon;
	private final Optional<AEItemDefinition> spatialIOPort;
	private final Optional<AEItemDefinition> multiPart;
	private final Optional<AEItemDefinition> controller;
	private final Optional<AEItemDefinition> drive;
	private final Optional<AEItemDefinition> chest;
	private final Optional<AEItemDefinition> iface;
	private final Optional<AEItemDefinition> cellWorkbench;
	private final Optional<AEItemDefinition> iOPort;
	private final Optional<AEItemDefinition> condenser;
	private final Optional<AEItemDefinition> energyAcceptor;
	private final Optional<AEItemDefinition> vibrationChamber;
	private final Optional<AEItemDefinition> quartzGrowthAccelerator;
	private final Optional<AEItemDefinition> energyCell;
	private final Optional<AEItemDefinition> energyCellDense;
	private final Optional<AEItemDefinition> energyCellCreative;
	private final Optional<AEItemDefinition> craftingUnit;
	private final Optional<AEItemDefinition> craftingAccelerator;
	private final Optional<AEItemDefinition> craftingStorage1k;
	private final Optional<AEItemDefinition> craftingStorage4k;
	private final Optional<AEItemDefinition> craftingStorage16k;
	private final Optional<AEItemDefinition> craftingStorage64k;
	private final Optional<AEItemDefinition> craftingMonitor;
	private final Optional<AEItemDefinition> molecularAssembler;
	private final Optional<AEItemDefinition> lightDetector;
	private final Optional<AEItemDefinition> paint;
	private final Optional<AEItemDefinition> skyStoneStair;
	private final Optional<AEItemDefinition> skyStoneBlockStair;
	private final Optional<AEItemDefinition> skyStoneBrickStair;
	private final Optional<AEItemDefinition> skyStoneSmallBrickStair;
	private final Optional<AEItemDefinition> fluixStair;
	private final Optional<AEItemDefinition> quartzStair;
	private final Optional<AEItemDefinition> chiseledQuartzStair;
	private final Optional<AEItemDefinition> quartzPillarStair;

	private final Optional<AEItemDefinition> itemGen;
	private final Optional<AEItemDefinition> chunkLoader;
	private final Optional<AEItemDefinition> phantomNode;
	private final Optional<AEItemDefinition> cubeGenerator;

	private final Set<IOrientableBlock> orientables;

	public ApiBlocks( FeatureRegistry features, FeatureHandlerRegistry handlers )
	{
		this.features = features;
		this.handlers = handlers;

		final BlockLightDetector lightDetector = new BlockLightDetector();
		final BlockQuartzPillar quartzPillar = new BlockQuartzPillar();
		final BlockSkyStone skyStone = new BlockSkyStone();
		final BlockQuartzGrowthAccelerator cga = new BlockQuartzGrowthAccelerator();
		final BlockQuartzTorch quartzTorch = new BlockQuartzTorch();

		this.orientables = ImmutableSet.<IOrientableBlock>of( lightDetector, quartzPillar, skyStone, cga, quartzTorch );

		this.quartzOre = this.getMaybeDefinition( new OreQuartz() );
		this.quartzOreCharged = this.getMaybeDefinition( new OreQuartzCharged() );
		this.matrixFrame = this.getMaybeDefinition( new BlockMatrixFrame() );
		this.quartz = this.getMaybeDefinition( new BlockQuartz() );
		this.quartzPillar = this.getMaybeDefinition( quartzPillar );
		this.quartzChiseled = this.getMaybeDefinition( new BlockQuartzChiseled() );
		this.quartzGlass = this.getMaybeDefinition( new BlockQuartzGlass() );
		this.quartzVibrantGlass = this.getMaybeDefinition( new BlockQuartzLamp() );
		this.quartzTorch = this.getMaybeDefinition( quartzTorch );
		this.fluix = this.getMaybeDefinition( new BlockFluix() );
		this.skyStone = this.getMaybeDefinition( skyStone );
		this.skyChest = this.getMaybeDefinition( new BlockSkyChest() );
		this.skyCompass = this.getMaybeDefinition( new BlockSkyCompass() );
		this.grindStone = this.getMaybeDefinition( new BlockGrinder() );
		this.crankHandle = this.getMaybeDefinition( new BlockCrank() );
		this.inscriber = this.getMaybeDefinition( new BlockInscriber() );
		this.wireless = this.getMaybeDefinition( new BlockWireless() );
		this.charger = this.getMaybeDefinition( new BlockCharger() );
		this.tinyTNT = this.getMaybeDefinition( new BlockTinyTNT() );
		this.security = this.getMaybeDefinition( new BlockSecurity() );
		this.quantumRing = this.getMaybeDefinition( new BlockQuantumRing() );
		this.quantumLink = this.getMaybeDefinition( new BlockQuantumLinkChamber() );
		this.spatialPylon = this.getMaybeDefinition( new BlockSpatialPylon() );
		this.spatialIOPort = this.getMaybeDefinition( new BlockSpatialIOPort() );
		this.multiPart = this.getMaybeDefinition( new BlockCableBus() );
		this.controller = this.getMaybeDefinition( new BlockController() );
		this.drive = this.getMaybeDefinition( new BlockDrive() );
		this.chest = this.getMaybeDefinition( new BlockChest() );
		this.iface = this.getMaybeDefinition( new BlockInterface() );
		this.cellWorkbench = this.getMaybeDefinition( new BlockCellWorkbench() );
		this.iOPort = this.getMaybeDefinition( new BlockIOPort() );
		this.condenser = this.getMaybeDefinition( new BlockCondenser() );
		this.energyAcceptor = this.getMaybeDefinition( new BlockEnergyAcceptor() );
		this.vibrationChamber = this.getMaybeDefinition( new BlockVibrationChamber() );
		this.quartzGrowthAccelerator = this.getMaybeDefinition( cga );
		this.energyCell = this.getMaybeDefinition( new BlockEnergyCell() );
		this.energyCellDense = this.getMaybeDefinition( new BlockDenseEnergyCell() );
		this.energyCellCreative = this.getMaybeDefinition( new BlockCreativeEnergyCell() );
		this.craftingUnit = this.getMaybeDefinition( new BlockCraftingUnit() );
		this.craftingAccelerator = this.craftingUnit.transform( new WrappedItemDamageDefinitionTransformation( 1 ) );
		this.craftingStorage1k = this.getMaybeDefinition( new BlockCraftingStorage() );
		this.craftingStorage4k = this.craftingStorage1k.transform( new WrappedItemDamageDefinitionTransformation( 1 ) );
		this.craftingStorage16k = this.craftingStorage1k.transform( new WrappedItemDamageDefinitionTransformation( 2 ) );
		this.craftingStorage64k = this.craftingStorage1k.transform( new WrappedItemDamageDefinitionTransformation( 3 ) );
		this.craftingMonitor = this.getMaybeDefinition( new BlockCraftingMonitor() );
		this.molecularAssembler = this.getMaybeDefinition( new BlockMolecularAssembler() );
		this.lightDetector = this.getMaybeDefinition( lightDetector );
		this.paint = this.getMaybeDefinition( new BlockPaint() );

		if ( this.skyStone.isPresent() )
		{
			final AEItemDefinition definition = this.skyStone.get();
			final Block blockID = definition.block();

			this.skyStoneStair = this.getMaybeDefinition( new SkyStoneStairBlock( blockID, 0 ) );
			this.skyStoneBlockStair = this.getMaybeDefinition( new SkyStoneBlockStairBlock( blockID, 1 ) );
			this.skyStoneBrickStair = this.getMaybeDefinition( new SkyStoneBrickStairBlock( blockID, 2 ) );
			this.skyStoneSmallBrickStair = this.getMaybeDefinition( new SkyStoneSmallBrickStairBlock( blockID, 3 ) );
		}
		else
		{
			this.skyStoneStair = Optional.absent();
			this.skyStoneBlockStair = Optional.absent();
			this.skyStoneBrickStair = Optional.absent();
			this.skyStoneSmallBrickStair = Optional.absent();
		}

		if ( this.fluix.isPresent() )
		{
			final AEItemDefinition definition = this.fluix.get();
			final Block blockID = definition.block();

			this.fluixStair = this.getMaybeDefinition( new FluixStairBlock( blockID ) );
		}
		else
		{
			this.fluixStair = Optional.absent();
		}

		if ( this.quartz.isPresent() )
		{
			final AEItemDefinition definition = this.quartz.get();
			final Block blockID = definition.block();

			this.quartzStair = this.getMaybeDefinition( new QuartzStairBlock( blockID ) );
		}
		else
		{
			this.quartzStair = Optional.absent();
		}

		if ( this.quartzChiseled.isPresent() )
		{
			final AEItemDefinition definition = this.quartzChiseled.get();
			final Block blockID = definition.block();

			this.chiseledQuartzStair = this.getMaybeDefinition( new ChiseledQuartzStairBlock( blockID ) );
		}
		else
		{
			this.chiseledQuartzStair = Optional.absent();
		}

		if ( this.quartzPillar.isPresent() )
		{
			final AEItemDefinition definition = this.quartzPillar.get();
			final Block blockID = definition.block();

			this.quartzPillarStair = this.getMaybeDefinition( new QuartzPillarStairBlock( blockID ) );
		}
		else
		{
			this.quartzPillarStair = Optional.absent();
		}

		this.itemGen = this.getMaybeDefinition( new BlockItemGen() );
		this.chunkLoader = this.getMaybeDefinition( new BlockChunkloader() );
		this.phantomNode = this.getMaybeDefinition( new BlockPhantomNode() );
		this.cubeGenerator = this.getMaybeDefinition( new BlockCubeGenerator() );
	}

	private Optional<AEItemDefinition> getMaybeDefinition( IAEFeature feature )
	{
		final IFeatureHandler handler = feature.handler();

		if ( handler.isFeatureAvailable() )
		{
			this.handlers.addFeatureHandler( handler );
			this.features.addFeature( feature );

			return Optional.of( handler.getDefinition() );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public Optional<AEItemDefinition> quartzOre()
	{
		return this.quartzOre;
	}

	@Override
	public Optional<AEItemDefinition> quartzOreCharged()
	{
		return this.quartzOreCharged;
	}

	@Override
	public Optional<AEItemDefinition> matrixFrame()
	{
		return this.matrixFrame;
	}

	@Override
	public Optional<AEItemDefinition> quartz()
	{
		return this.quartz;
	}

	@Override
	public Optional<AEItemDefinition> quartzPillar()
	{
		return this.quartzPillar;
	}

	@Override
	public Optional<AEItemDefinition> quartzChiseled()
	{
		return this.quartzChiseled;
	}

	@Override
	public Optional<AEItemDefinition> quartzGlass()
	{
		return this.quartzGlass;
	}

	@Override
	public Optional<AEItemDefinition> quartzVibrantGlass()
	{
		return this.quartzVibrantGlass;
	}

	@Override
	public Optional<AEItemDefinition> quartzTorch()
	{
		return this.quartzTorch;
	}

	@Override
	public Optional<AEItemDefinition> fluix()
	{
		return this.fluix;
	}

	@Override
	public Optional<AEItemDefinition> skyStone()
	{
		return this.skyStone;
	}

	@Override
	public Optional<AEItemDefinition> skyChest()
	{
		return this.skyChest;
	}

	@Override
	public Optional<AEItemDefinition> skyCompass()
	{
		return this.skyCompass;
	}

	@Override
	public Optional<AEItemDefinition> skyStoneStair()
	{
		return this.skyStoneStair;
	}

	@Override
	public Optional<AEItemDefinition> skyStoneBlockStair()
	{
		return this.skyStoneBlockStair;
	}

	@Override
	public Optional<AEItemDefinition> skyStoneBrickStair()
	{
		return this.skyStoneBrickStair;
	}

	@Override
	public Optional<AEItemDefinition> skyStoneSmallBrickStair()
	{
		return this.skyStoneSmallBrickStair;
	}

	@Override
	public Optional<AEItemDefinition> fluixStair()
	{
		return this.fluixStair;
	}

	@Override
	public Optional<AEItemDefinition> quartzStair()
	{
		return this.quartzStair;
	}

	@Override
	public Optional<AEItemDefinition> chiseledQuartzStair()
	{
		return this.chiseledQuartzStair;
	}

	@Override
	public Optional<AEItemDefinition> quartzPillarStair()
	{
		return this.quartzPillarStair;
	}

	@Override
	public Optional<AEItemDefinition> grindStone()
	{
		return this.grindStone;
	}

	@Override
	public Optional<AEItemDefinition> crankHandle()
	{
		return this.crankHandle;
	}

	@Override
	public Optional<AEItemDefinition> inscriber()
	{
		return this.inscriber;
	}

	@Override
	public Optional<AEItemDefinition> wireless()
	{
		return this.wireless;
	}

	@Override
	public Optional<AEItemDefinition> charger()
	{
		return this.charger;
	}

	@Override
	public Optional<AEItemDefinition> tinyTNT()
	{
		return this.tinyTNT;
	}

	@Override
	public Optional<AEItemDefinition> security()
	{
		return this.security;
	}

	@Override
	public Optional<AEItemDefinition> quantumRing()
	{
		return this.quantumRing;
	}

	@Override
	public Optional<AEItemDefinition> quantumLink()
	{
		return this.quantumLink;
	}

	@Override
	public Optional<AEItemDefinition> spatialPylon()
	{
		return this.spatialPylon;
	}

	@Override
	public Optional<AEItemDefinition> spatialIOPort()
	{
		return this.spatialIOPort;
	}

	@Override
	public Optional<AEItemDefinition> multiPart()
	{
		return this.multiPart;
	}

	@Override
	public Optional<AEItemDefinition> controller()
	{
		return this.controller;
	}

	@Override
	public Optional<AEItemDefinition> drive()
	{
		return this.drive;
	}

	@Override
	public Optional<AEItemDefinition> chest()
	{
		return this.chest;
	}

	@Override
	public Optional<AEItemDefinition> iface()
	{
		return this.iface;
	}

	@Override
	public Optional<AEItemDefinition> cellWorkbench()
	{
		return this.cellWorkbench;
	}

	@Override
	public Optional<AEItemDefinition> iOPort()
	{
		return this.iOPort;
	}

	@Override
	public Optional<AEItemDefinition> condenser()
	{
		return this.condenser;
	}

	@Override
	public Optional<AEItemDefinition> energyAcceptor()
	{
		return this.energyAcceptor;
	}

	@Override
	public Optional<AEItemDefinition> vibrationChamber()
	{
		return this.vibrationChamber;
	}

	@Override
	public Optional<AEItemDefinition> quartzGrowthAccelerator()
	{
		return this.quartzGrowthAccelerator;
	}

	@Override
	public Optional<AEItemDefinition> energyCell()
	{
		return this.energyCell;
	}

	@Override
	public Optional<AEItemDefinition> energyCellDense()
	{
		return this.energyCellDense;
	}

	@Override
	public Optional<AEItemDefinition> energyCellCreative()
	{
		return this.energyCellCreative;
	}

	@Override
	public Optional<AEItemDefinition> craftingUnit()
	{
		return this.craftingUnit;
	}

	@Override
	public Optional<AEItemDefinition> craftingAccelerator()
	{
		return this.craftingAccelerator;
	}

	@Override
	public Optional<AEItemDefinition> craftingStorage1k()
	{
		return this.craftingStorage1k;
	}

	@Override
	public Optional<AEItemDefinition> craftingStorage4k()
	{
		return this.craftingStorage4k;
	}

	@Override
	public Optional<AEItemDefinition> craftingStorage16k()
	{
		return this.craftingStorage16k;
	}

	@Override
	public Optional<AEItemDefinition> craftingStorage64k()
	{
		return this.craftingStorage64k;
	}

	@Override
	public Optional<AEItemDefinition> craftingMonitor()
	{
		return this.craftingMonitor;
	}

	@Override
	public Optional<AEItemDefinition> molecularAssembler()
	{
		return this.molecularAssembler;
	}

	@Override
	public Optional<AEItemDefinition> lightDetector()
	{
		return this.lightDetector;
	}

	@Override
	public Optional<AEItemDefinition> paint()
	{
		return this.paint;
	}

	public Optional<AEItemDefinition> chunkLoader()
	{
		return this.chunkLoader;
	}

	public Optional<AEItemDefinition> itemGen()
	{
		return this.itemGen;
	}

	public Optional<AEItemDefinition> phantomNode()
	{
		return this.phantomNode;
	}

	public Optional<AEItemDefinition> cubeGenerator()
	{
		return this.cubeGenerator;
	}

	public Set<IOrientableBlock> orientables()
	{
		return this.orientables;
	}

	private static class WrappedItemDamageDefinitionTransformation implements Function<AEItemDefinition, AEItemDefinition>
	{
		private final int itemDamage;

		public WrappedItemDamageDefinitionTransformation( int itemDamage )
		{
			this.itemDamage = itemDamage;
		}

		@Nullable
		@Override
		public AEItemDefinition apply( AEItemDefinition input )
		{
			return new WrappedDamageItemDefinition( input, this.itemDamage );
		}
	}
}
