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


import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.IItems;
import appeng.api.util.AEColoredItemDefinition;
import appeng.bootstrap.FeatureFactory;
import appeng.client.render.crafting.ItemEncodedPatternRendering;
import appeng.core.CreativeTabFacade;
import appeng.core.features.AEFeature;
import appeng.debug.ToolDebugCard;
import appeng.debug.ToolEraser;
import appeng.debug.ToolMeteoritePlacer;
import appeng.debug.ToolReplicatorCard;
import appeng.fluids.items.FluidBasicStorageCell;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.DispenserMatterCannon;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.misc.ItemCrystalSeedRendering;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.misc.ItemPaintBall;
import appeng.items.misc.ItemPaintBallRendering;
import appeng.items.parts.FacadeRendering;
import appeng.items.parts.ItemFacade;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.items.storage.ItemViewCell;
import appeng.items.tools.ToolBiometricCard;
import appeng.items.tools.ToolBiometricCardRendering;
import appeng.items.tools.ToolMemoryCard;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolChargedStaff;
import appeng.items.tools.powered.ToolColorApplicator;
import appeng.items.tools.powered.ToolColorApplicatorRendering;
import appeng.items.tools.powered.ToolEntropyManipulator;
import appeng.items.tools.powered.ToolMatterCannon;
import appeng.items.tools.powered.ToolPortableCell;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.items.tools.quartz.ToolQuartzAxe;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.items.tools.quartz.ToolQuartzHoe;
import appeng.items.tools.quartz.ToolQuartzPickaxe;
import appeng.items.tools.quartz.ToolQuartzSpade;
import appeng.items.tools.quartz.ToolQuartzSword;
import appeng.items.tools.quartz.ToolQuartzWrench;


/**
 * Internal implementation for the API items
 */
public final class ApiItems implements IItems
{
	private final IItemDefinition certusQuartzAxe;
	private final IItemDefinition certusQuartzHoe;
	private final IItemDefinition certusQuartzShovel;
	private final IItemDefinition certusQuartzPick;
	private final IItemDefinition certusQuartzSword;
	private final IItemDefinition certusQuartzWrench;
	private final IItemDefinition certusQuartzKnife;

	private final IItemDefinition netherQuartzAxe;
	private final IItemDefinition netherQuartzHoe;
	private final IItemDefinition netherQuartzShovel;
	private final IItemDefinition netherQuartzPick;
	private final IItemDefinition netherQuartzSword;
	private final IItemDefinition netherQuartzWrench;
	private final IItemDefinition netherQuartzKnife;

	private final IItemDefinition entropyManipulator;
	private final IItemDefinition wirelessTerminal;
	private final IItemDefinition biometricCard;
	private final IItemDefinition chargedStaff;
	private final IItemDefinition massCannon;
	private final IItemDefinition memoryCard;
	private final IItemDefinition networkTool;
	private final IItemDefinition portableCell;

	private final IItemDefinition cellCreative;
	private final IItemDefinition viewCell;

	private final IItemDefinition cell1k;
	private final IItemDefinition cell4k;
	private final IItemDefinition cell16k;
	private final IItemDefinition cell64k;

	private final IItemDefinition fluidCell1k;
	private final IItemDefinition fluidCell4k;
	private final IItemDefinition fluidCell16k;
	private final IItemDefinition fluidCell64k;

	private final IItemDefinition spatialCell2;
	private final IItemDefinition spatialCell16;
	private final IItemDefinition spatialCell128;

	private final IItemDefinition facade;
	private final IItemDefinition crystalSeed;

	// rv1
	private final IItemDefinition encodedPattern;
	private final IItemDefinition colorApplicator;

	private final IItemDefinition paintBall;
	private final AEColoredItemDefinition coloredPaintBall;
	private final AEColoredItemDefinition coloredLumenPaintBall;

	// unsupported dev tools
	private final IItemDefinition toolEraser;
	private final IItemDefinition toolMeteoritePlacer;
	private final IItemDefinition toolDebugCard;
	private final IItemDefinition toolReplicatorCard;

	public ApiItems( FeatureFactory registry )
	{
		FeatureFactory certusTools = registry.features( AEFeature.CERTUS_QUARTZ_TOOLS );
		this.certusQuartzAxe = certusTools.item( "certus_quartz_axe", () -> new ToolQuartzAxe( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_AXE )
				.build();
		this.certusQuartzHoe = certusTools.item( "certus_quartz_hoe", () -> new ToolQuartzHoe( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_HOE )
				.build();
		this.certusQuartzShovel = certusTools.item( "certus_quartz_spade", () -> new ToolQuartzSpade( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_SPADE )
				.build();
		this.certusQuartzPick = certusTools.item( "certus_quartz_pickaxe", () -> new ToolQuartzPickaxe( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_PICKAXE )
				.build();
		this.certusQuartzSword = certusTools.item( "certus_quartz_sword", () -> new ToolQuartzSword( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_SWORD )
				.build();
		this.certusQuartzWrench = certusTools.item( "certus_quartz_wrench", ToolQuartzWrench::new ).addFeatures( AEFeature.QUARTZ_WRENCH ).build();
		this.certusQuartzKnife = certusTools.item( "certus_quartz_cutting_knife", () -> new ToolQuartzCuttingKnife( AEFeature.CERTUS_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_KNIFE )
				.build();

		FeatureFactory netherTools = registry.features( AEFeature.NETHER_QUARTZ_TOOLS );
		this.netherQuartzAxe = netherTools.item( "nether_quartz_axe", () -> new ToolQuartzAxe( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_AXE )
				.build();
		this.netherQuartzHoe = netherTools.item( "nether_quartz_hoe", () -> new ToolQuartzHoe( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_HOE )
				.build();
		this.netherQuartzShovel = netherTools.item( "nether_quartz_spade", () -> new ToolQuartzSpade( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_SPADE )
				.build();
		this.netherQuartzPick = netherTools.item( "nether_quartz_pickaxe", () -> new ToolQuartzPickaxe( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_PICKAXE )
				.build();
		this.netherQuartzSword = netherTools.item( "nether_quartz_sword", () -> new ToolQuartzSword( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_SWORD )
				.build();
		this.netherQuartzWrench = netherTools.item( "nether_quartz_wrench", ToolQuartzWrench::new ).addFeatures( AEFeature.QUARTZ_WRENCH ).build();
		this.netherQuartzKnife = netherTools.item( "nether_quartz_cutting_knife", () -> new ToolQuartzCuttingKnife( AEFeature.NETHER_QUARTZ_TOOLS ) )
				.addFeatures( AEFeature.QUARTZ_KNIFE )
				.build();

		FeatureFactory powerTools = registry.features( AEFeature.POWERED_TOOLS );
		this.entropyManipulator = powerTools.item( "entropy_manipulator", ToolEntropyManipulator::new )
				.addFeatures( AEFeature.ENTROPY_MANIPULATOR )
				.dispenserBehavior( DispenserBlockTool::new )
				.build();
		this.wirelessTerminal = powerTools.item( "wireless_terminal", ToolWirelessTerminal::new ).addFeatures( AEFeature.WIRELESS_ACCESS_TERMINAL ).build();
		this.chargedStaff = powerTools.item( "charged_staff", ToolChargedStaff::new ).addFeatures( AEFeature.CHARGED_STAFF ).build();
		this.massCannon = powerTools.item( "matter_cannon", ToolMatterCannon::new )
				.addFeatures( AEFeature.MATTER_CANNON )
				.dispenserBehavior( DispenserMatterCannon::new )
				.build();
		this.portableCell = powerTools.item( "portable_cell", ToolPortableCell::new ).addFeatures( AEFeature.PORTABLE_CELL, AEFeature.STORAGE_CELLS ).build();
		this.colorApplicator = powerTools.item( "color_applicator", ToolColorApplicator::new )
				.addFeatures( AEFeature.COLOR_APPLICATOR )
				.dispenserBehavior( DispenserBlockTool::new )
				.rendering( new ToolColorApplicatorRendering() )
				.build();

		this.biometricCard = registry.item( "biometric_card", ToolBiometricCard::new )
				.rendering( new ToolBiometricCardRendering() )
				.features( AEFeature.SECURITY )
				.build();
		this.memoryCard = registry.item( "memory_card", ToolMemoryCard::new ).features( AEFeature.MEMORY_CARD ).build();
		this.networkTool = registry.item( "network_tool", ToolNetworkTool::new ).features( AEFeature.NETWORK_TOOL ).build();

		this.cellCreative = registry.item( "creative_storage_cell", ItemCreativeStorageCell::new )
				.features( AEFeature.STORAGE_CELLS, AEFeature.CREATIVE )
				.build();
		this.viewCell = registry.item( "view_cell", ItemViewCell::new ).features( AEFeature.VIEW_CELL ).build();

		FeatureFactory storageCells = registry.features( AEFeature.STORAGE_CELLS );
		this.cell1k = storageCells.item( "storage_cell_1k", () -> new ItemBasicStorageCell( MaterialType.CELL1K_PART, 1 ) ).build();
		this.cell4k = storageCells.item( "storage_cell_4k", () -> new ItemBasicStorageCell( MaterialType.CELL4K_PART, 4 ) ).build();
		this.cell16k = storageCells.item( "storage_cell_16k", () -> new ItemBasicStorageCell( MaterialType.CELL16K_PART, 16 ) ).build();
		this.cell64k = storageCells.item( "storage_cell_64k", () -> new ItemBasicStorageCell( MaterialType.CELL64K_PART, 64 ) ).build();

		this.fluidCell1k = storageCells.item( "fluid_storage_cell_1k", () -> new FluidBasicStorageCell( MaterialType.CELL1K_PART, 1 ) ).build();
		this.fluidCell4k = storageCells.item( "fluid_storage_cell_4k", () -> new FluidBasicStorageCell( MaterialType.CELL4K_PART, 4 ) ).build();
		this.fluidCell16k = storageCells.item( "fluid_storage_cell_16k", () -> new FluidBasicStorageCell( MaterialType.CELL16K_PART, 16 ) ).build();
		this.fluidCell64k = storageCells.item( "fluid_storage_cell_64k", () -> new FluidBasicStorageCell( MaterialType.CELL64K_PART, 64 ) ).build();

		FeatureFactory spatialCells = registry.features( AEFeature.SPATIAL_IO );
		this.spatialCell2 = spatialCells.item( "spatial_storage_cell_2_cubed", () -> new ItemSpatialStorageCell( 2 ) ).build();
		this.spatialCell16 = spatialCells.item( "spatial_storage_cell_16_cubed", () -> new ItemSpatialStorageCell( 16 ) ).build();
		this.spatialCell128 = spatialCells.item( "spatial_storage_cell_128_cubed", () -> new ItemSpatialStorageCell( 128 ) ).build();

		this.facade = registry.item( "facade", ItemFacade::new )
				.features( AEFeature.FACADES )
				.creativeTab( CreativeTabFacade.instance )
				.rendering( new FacadeRendering() )
				.build();
		this.crystalSeed = registry.item( "crystal_seed", ItemCrystalSeed::new )
				.features( AEFeature.CRYSTAL_SEEDS )
				.rendering( new ItemCrystalSeedRendering() )
				.build();

		// rv1
		this.encodedPattern = registry.item( "encoded_pattern", ItemEncodedPattern::new )
				.features( AEFeature.PATTERNS )
				.rendering( new ItemEncodedPatternRendering() )
				.build();

		this.paintBall = registry.item( "paint_ball", ItemPaintBall::new )
				.features( AEFeature.PAINT_BALLS )
				.rendering( new ItemPaintBallRendering() )
				.build();
		this.coloredPaintBall = registry.colored( this.paintBall, 0 );
		this.coloredLumenPaintBall = registry.colored( this.paintBall, 20 );

		FeatureFactory debugTools = registry.features( AEFeature.UNSUPPORTED_DEVELOPER_TOOLS, AEFeature.CREATIVE );
		this.toolEraser = debugTools.item( "debug_eraser", ToolEraser::new ).build();
		this.toolMeteoritePlacer = debugTools.item( "debug_meteorite_placer", ToolMeteoritePlacer::new ).build();
		this.toolDebugCard = debugTools.item( "debug_card", ToolDebugCard::new ).build();
		this.toolReplicatorCard = debugTools.item( "debug_replicator_card", ToolReplicatorCard::new ).build();
	}

	@Override
	public IItemDefinition certusQuartzAxe()
	{
		return this.certusQuartzAxe;
	}

	@Override
	public IItemDefinition certusQuartzHoe()
	{
		return this.certusQuartzHoe;
	}

	@Override
	public IItemDefinition certusQuartzShovel()
	{
		return this.certusQuartzShovel;
	}

	@Override
	public IItemDefinition certusQuartzPick()
	{
		return this.certusQuartzPick;
	}

	@Override
	public IItemDefinition certusQuartzSword()
	{
		return this.certusQuartzSword;
	}

	@Override
	public IItemDefinition certusQuartzWrench()
	{
		return this.certusQuartzWrench;
	}

	@Override
	public IItemDefinition certusQuartzKnife()
	{
		return this.certusQuartzKnife;
	}

	@Override
	public IItemDefinition netherQuartzAxe()
	{
		return this.netherQuartzAxe;
	}

	@Override
	public IItemDefinition netherQuartzHoe()
	{
		return this.netherQuartzHoe;
	}

	@Override
	public IItemDefinition netherQuartzShovel()
	{
		return this.netherQuartzShovel;
	}

	@Override
	public IItemDefinition netherQuartzPick()
	{
		return this.netherQuartzPick;
	}

	@Override
	public IItemDefinition netherQuartzSword()
	{
		return this.netherQuartzSword;
	}

	@Override
	public IItemDefinition netherQuartzWrench()
	{
		return this.netherQuartzWrench;
	}

	@Override
	public IItemDefinition netherQuartzKnife()
	{
		return this.netherQuartzKnife;
	}

	@Override
	public IItemDefinition entropyManipulator()
	{
		return this.entropyManipulator;
	}

	@Override
	public IItemDefinition wirelessTerminal()
	{
		return this.wirelessTerminal;
	}

	@Override
	public IItemDefinition biometricCard()
	{
		return this.biometricCard;
	}

	@Override
	public IItemDefinition chargedStaff()
	{
		return this.chargedStaff;
	}

	@Override
	public IItemDefinition massCannon()
	{
		return this.massCannon;
	}

	@Override
	public IItemDefinition memoryCard()
	{
		return this.memoryCard;
	}

	@Override
	public IItemDefinition networkTool()
	{
		return this.networkTool;
	}

	@Override
	public IItemDefinition portableCell()
	{
		return this.portableCell;
	}

	@Override
	public IItemDefinition cellCreative()
	{
		return this.cellCreative;
	}

	@Override
	public IItemDefinition viewCell()
	{
		return this.viewCell;
	}

	@Override
	public IItemDefinition cell1k()
	{
		return this.cell1k;
	}

	@Override
	public IItemDefinition cell4k()
	{
		return this.cell4k;
	}

	@Override
	public IItemDefinition cell16k()
	{
		return this.cell16k;
	}

	@Override
	public IItemDefinition cell64k()
	{
		return this.cell64k;
	}

	@Override
	public IItemDefinition fluidCell1k()
	{
		return this.fluidCell1k;
	}

	@Override
	public IItemDefinition fluidCell4k()
	{
		return this.fluidCell4k;
	}

	@Override
	public IItemDefinition fluidCell16k()
	{
		return this.fluidCell16k;
	}

	@Override
	public IItemDefinition fluidCell64k()
	{
		return this.fluidCell64k;
	}

	@Override
	public IItemDefinition spatialCell2()
	{
		return this.spatialCell2;
	}

	@Override
	public IItemDefinition spatialCell16()
	{
		return this.spatialCell16;
	}

	@Override
	public IItemDefinition spatialCell128()
	{
		return this.spatialCell128;
	}

	@Override
	public IItemDefinition facade()
	{
		return this.facade;
	}

	@Override
	public IItemDefinition crystalSeed()
	{
		return this.crystalSeed;
	}

	@Override
	public IItemDefinition encodedPattern()
	{
		return this.encodedPattern;
	}

	@Override
	public IItemDefinition colorApplicator()
	{
		return this.colorApplicator;
	}

	@Override
	public AEColoredItemDefinition coloredPaintBall()
	{
		return this.coloredPaintBall;
	}

	@Override
	public AEColoredItemDefinition coloredLumenPaintBall()
	{
		return this.coloredLumenPaintBall;
	}

	public IItemDefinition paintBall()
	{
		return this.paintBall;
	}

	public IItemDefinition toolEraser()
	{
		return this.toolEraser;
	}

	public IItemDefinition toolMeteoritePlacer()
	{
		return this.toolMeteoritePlacer;
	}

	public IItemDefinition toolDebugCard()
	{
		return this.toolDebugCard;
	}

	public IItemDefinition toolReplicatorCard()
	{
		return this.toolReplicatorCard;
	}
}
