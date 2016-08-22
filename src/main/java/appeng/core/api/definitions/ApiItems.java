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
import appeng.core.CreativeTabFacade;
import appeng.core.features.AEFeature;
import appeng.debug.ToolDebugCard;
import appeng.debug.ToolEraser;
import appeng.debug.ToolMeteoritePlacer;
import appeng.debug.ToolReplicatorCard;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.misc.ItemCrystalSeedRendering;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.misc.ItemPaintBall;
import appeng.items.misc.ItemPaintBallRendering;
import appeng.items.parts.ItemFacade;
import appeng.items.storage.ItemBasicStorageCell;
import appeng.items.storage.ItemCreativeStorageCell;
import appeng.items.storage.ItemSpatialStorageCell;
import appeng.items.storage.ItemViewCell;
import appeng.items.tools.ToolBiometricCard;
import appeng.items.tools.ToolMemoryCard;
import appeng.items.tools.ToolNetworkTool;
import appeng.items.tools.powered.ToolChargedStaff;
import appeng.items.tools.powered.ToolColorApplicator;
import appeng.items.tools.powered.ToolEntropyManipulator;
import appeng.items.tools.powered.ToolMassCannon;
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
		FeatureFactory certusTools = registry.features( AEFeature.CertusQuartzTools );
		this.certusQuartzAxe = certusTools.item( "certus_quartz_axe", () -> new ToolQuartzAxe( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzAxe ).build();
		this.certusQuartzHoe = certusTools.item( "certus_quartz_hoe", () -> new ToolQuartzHoe( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzHoe ).build();
		this.certusQuartzShovel = certusTools.item( "certus_quartz_spade", () -> new ToolQuartzSpade( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzSpade ).build();
		this.certusQuartzPick = certusTools.item( "certus_quartz_pickaxe", () -> new ToolQuartzPickaxe( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzPickaxe ).build();
		this.certusQuartzSword = certusTools.item( "certus_quartz_sword", () -> new ToolQuartzSword( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzSword ).build();
		this.certusQuartzWrench = certusTools.item( "certus_quartz_wrench", ToolQuartzWrench::new ).addFeatures( AEFeature.QuartzWrench ).build();
		this.certusQuartzKnife = certusTools.item( "certus_quartz_cutting_knife", () -> new ToolQuartzCuttingKnife( AEFeature.CertusQuartzTools ) ).addFeatures( AEFeature.QuartzKnife ).build();

		FeatureFactory netherTools = registry.features( AEFeature.NetherQuartzTools );
		this.netherQuartzAxe = netherTools.item( "nether_quartz_axe", () -> new ToolQuartzAxe( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzAxe ).build();
		this.netherQuartzHoe = netherTools.item( "nether_quartz_hoe", () -> new ToolQuartzHoe( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzHoe ).build();
		this.netherQuartzShovel = netherTools.item( "nether_quartz_spade", () -> new ToolQuartzSpade( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzSpade ).build();
		this.netherQuartzPick = netherTools.item( "nether_quartz_pickaxe", () -> new ToolQuartzPickaxe( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzPickaxe ).build();
		this.netherQuartzSword = netherTools.item( "nether_quartz_sword", () -> new ToolQuartzSword( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzSword ).build();
		this.netherQuartzWrench = netherTools.item( "nether_quartz_wrench", ToolQuartzWrench::new ).addFeatures( AEFeature.QuartzWrench ).build();
		this.netherQuartzKnife = netherTools.item( "nether_quartz_cutting_knife", () -> new ToolQuartzCuttingKnife( AEFeature.NetherQuartzTools ) ).addFeatures( AEFeature.QuartzKnife ).build();

		FeatureFactory powerTools = registry.features( AEFeature.PoweredTools );
		this.entropyManipulator = powerTools.item( "entropy_manipulator", ToolEntropyManipulator::new ).addFeatures( AEFeature.EntropyManipulator ).build();
		this.wirelessTerminal = powerTools.item( "wireless_terminal", ToolWirelessTerminal::new ).addFeatures( AEFeature.WirelessAccessTerminal ).build();
		this.chargedStaff = powerTools.item( "charged_staff", ToolChargedStaff::new ).addFeatures( AEFeature.ChargedStaff ).build();
		this.massCannon = powerTools.item( "mass_cannon", ToolMassCannon::new ).addFeatures( AEFeature.MatterCannon ).build();
		this.portableCell = powerTools.item( "portable_cell", ToolPortableCell::new ).addFeatures( AEFeature.PortableCell, AEFeature.StorageCells ).build();
		this.colorApplicator = powerTools.item( "color_applicator", ToolColorApplicator::new ).addFeatures( AEFeature.ColorApplicator ).build();

		this.biometricCard = registry.item( "biometric_card", ToolBiometricCard::new ).features( AEFeature.Security ).build();
		this.memoryCard = registry.item( "memory_card", ToolMemoryCard::new ).build();
		this.networkTool = registry.item( "network_tool", ToolNetworkTool::new ).features( AEFeature.NetworkTool ).build();

		this.cellCreative = registry.item( "creative_storage_cell", ItemCreativeStorageCell::new ).features( AEFeature.StorageCells, AEFeature.Creative ).build();
		this.viewCell = registry.item( "view_cell", ItemViewCell::new ).build();

		FeatureFactory storageCells = registry.features( AEFeature.StorageCells );
		this.cell1k = storageCells.item( "basic_storage_cell_1k", () -> new ItemBasicStorageCell( MaterialType.Cell1kPart, 1 ) ).build();
		this.cell4k = storageCells.item( "basic_storage_cell_4k", () -> new ItemBasicStorageCell( MaterialType.Cell4kPart, 4 ) ).build();
		this.cell16k = storageCells.item( "basic_storage_cell_16k", () -> new ItemBasicStorageCell( MaterialType.Cell16kPart, 16 ) ).build();
		this.cell64k = storageCells.item( "basic_storage_cell_64k", () -> new ItemBasicStorageCell( MaterialType.Cell64kPart, 64 ) ).build();

		FeatureFactory spatialCells = registry.features( AEFeature.SpatialIO );
		this.spatialCell2 = spatialCells.item( "spatial_storage_cell_2_cubed", () -> new ItemSpatialStorageCell( 2 ) ).build();
		this.spatialCell16 = spatialCells.item( "spatial_storage_cell_16_cubed", () -> new ItemSpatialStorageCell( 16 ) ).build();
		this.spatialCell128 = spatialCells.item( "spatial_storage_cell_128_cubed", () -> new ItemSpatialStorageCell( 128 ) ).build();

		this.facade = registry.item( "facade", ItemFacade::new )
				.features( AEFeature.Facades )
				.creativeTab( CreativeTabFacade.instance )
				.build();
		this.crystalSeed = registry.item( "crystal_seed", ItemCrystalSeed::new )
				.rendering( new ItemCrystalSeedRendering() )
				.build();

		// rv1
		this.encodedPattern = registry.item( "encoded_pattern", ItemEncodedPattern::new ).features( AEFeature.Patterns ).build();

		this.paintBall = registry.item( "paint_ball", ItemPaintBall::new )
				.features( AEFeature.PaintBalls )
				.rendering( new ItemPaintBallRendering() )
				.build();
		this.coloredPaintBall = registry.colored( this.paintBall, 0 );
		this.coloredLumenPaintBall = registry.colored( this.paintBall, 20 );

		FeatureFactory debugTools = registry.features( AEFeature.UnsupportedDeveloperTools, AEFeature.Creative );
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
		return this.memoryCard;
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
