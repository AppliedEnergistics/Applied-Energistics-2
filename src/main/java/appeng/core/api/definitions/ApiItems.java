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


import appeng.api.definitions.IItems;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.core.features.AEFeature;
import appeng.debug.ToolDebugCard;
import appeng.debug.ToolEraser;
import appeng.debug.ToolMeteoritePlacer;
import appeng.debug.ToolReplicatorCard;
import appeng.items.materials.MaterialType;
import appeng.items.misc.ItemCrystalSeed;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.misc.ItemPaintBall;
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
	private final AEItemDefinition certusQuartzAxe;
	private final AEItemDefinition certusQuartzHoe;
	private final AEItemDefinition certusQuartzShovel;
	private final AEItemDefinition certusQuartzPick;
	private final AEItemDefinition certusQuartzSword;
	private final AEItemDefinition certusQuartzWrench;
	private final AEItemDefinition certusQuartzKnife;

	private final AEItemDefinition netherQuartzAxe;
	private final AEItemDefinition netherQuartzHoe;
	private final AEItemDefinition netherQuartzShovel;
	private final AEItemDefinition netherQuartzPick;
	private final AEItemDefinition netherQuartzSword;
	private final AEItemDefinition netherQuartzWrench;
	private final AEItemDefinition netherQuartzKnife;

	private final AEItemDefinition entropyManipulator;
	private final AEItemDefinition wirelessTerminal;
	private final AEItemDefinition biometricCard;
	private final AEItemDefinition chargedStaff;
	private final AEItemDefinition massCannon;
	private final AEItemDefinition memoryCard;
	private final AEItemDefinition networkTool;
	private final AEItemDefinition portableCell;

	private final AEItemDefinition cellCreative;
	private final AEItemDefinition viewCell;

	private final AEItemDefinition cell1k;
	private final AEItemDefinition cell4k;
	private final AEItemDefinition cell16k;
	private final AEItemDefinition cell64k;

	private final AEItemDefinition spatialCell2;
	private final AEItemDefinition spatialCell16;
	private final AEItemDefinition spatialCell128;

	private final AEItemDefinition facade;
	private final AEItemDefinition crystalSeed;

	// rv1
	private final AEItemDefinition encodedPattern;
	private final AEItemDefinition colorApplicator;

	private final AEItemDefinition paintBall;
	private final AEColoredItemDefinition coloredPaintBall;
	private final AEColoredItemDefinition coloredLumenPaintBall;

	// unsupported dev tools
	private final AEItemDefinition toolEraser;
	private final AEItemDefinition toolMeteoritePlacer;
	private final AEItemDefinition toolDebugCard;
	private final AEItemDefinition toolReplicatorCard;

	public ApiItems( DefinitionConstructor constructor )
	{
		this.certusQuartzAxe = constructor.registerAndConstructDefinition( new ToolQuartzAxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzHoe = constructor.registerAndConstructDefinition( new ToolQuartzHoe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzShovel = constructor.registerAndConstructDefinition( new ToolQuartzSpade( AEFeature.CertusQuartzTools ) );
		this.certusQuartzPick = constructor.registerAndConstructDefinition( new ToolQuartzPickaxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzSword = constructor.registerAndConstructDefinition( new ToolQuartzSword( AEFeature.CertusQuartzTools ) );
		this.certusQuartzWrench = constructor.registerAndConstructDefinition( new ToolQuartzWrench( AEFeature.CertusQuartzTools ) );
		this.certusQuartzKnife = constructor.registerAndConstructDefinition( new ToolQuartzCuttingKnife( AEFeature.CertusQuartzTools ) );

		this.netherQuartzAxe = constructor.registerAndConstructDefinition( new ToolQuartzAxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzHoe = constructor.registerAndConstructDefinition( new ToolQuartzHoe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzShovel = constructor.registerAndConstructDefinition( new ToolQuartzSpade( AEFeature.NetherQuartzTools ) );
		this.netherQuartzPick = constructor.registerAndConstructDefinition( new ToolQuartzPickaxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzSword = constructor.registerAndConstructDefinition( new ToolQuartzSword( AEFeature.NetherQuartzTools ) );
		this.netherQuartzWrench = constructor.registerAndConstructDefinition( new ToolQuartzWrench( AEFeature.NetherQuartzTools ) );
		this.netherQuartzKnife = constructor.registerAndConstructDefinition( new ToolQuartzCuttingKnife( AEFeature.NetherQuartzTools ) );

		this.entropyManipulator = constructor.registerAndConstructDefinition( new ToolEntropyManipulator() );
		this.wirelessTerminal = constructor.registerAndConstructDefinition( new ToolWirelessTerminal() );
		this.biometricCard = constructor.registerAndConstructDefinition( new ToolBiometricCard() );
		this.chargedStaff = constructor.registerAndConstructDefinition( new ToolChargedStaff() );
		this.massCannon = constructor.registerAndConstructDefinition( new ToolMassCannon() );
		this.memoryCard = constructor.registerAndConstructDefinition( new ToolMemoryCard() );
		this.networkTool = constructor.registerAndConstructDefinition( new ToolNetworkTool() );
		this.portableCell = constructor.registerAndConstructDefinition( new ToolPortableCell() );

		this.cellCreative = constructor.registerAndConstructDefinition( new ItemCreativeStorageCell() );
		this.viewCell = constructor.registerAndConstructDefinition( new ItemViewCell() );

		this.cell1k = constructor.registerAndConstructDefinition( new ItemBasicStorageCell( MaterialType.Cell1kPart, 1 ) );
		this.cell4k = constructor.registerAndConstructDefinition( new ItemBasicStorageCell( MaterialType.Cell4kPart, 4 ) );
		this.cell16k = constructor.registerAndConstructDefinition( new ItemBasicStorageCell( MaterialType.Cell16kPart, 16 ) );
		this.cell64k = constructor.registerAndConstructDefinition( new ItemBasicStorageCell( MaterialType.Cell64kPart, 64 ) );

		this.spatialCell2 = constructor.registerAndConstructDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 2 ) );
		this.spatialCell16 = constructor.registerAndConstructDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 16 ) );
		this.spatialCell128 = constructor.registerAndConstructDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 128 ) );

		this.facade = constructor.registerAndConstructDefinition( new ItemFacade() );
		this.crystalSeed = constructor.registerAndConstructDefinition( new ItemCrystalSeed() );

		// rv1
		this.encodedPattern = constructor.registerAndConstructDefinition( new ItemEncodedPattern() );
		this.colorApplicator = constructor.registerAndConstructDefinition( new ToolColorApplicator() );

		this.paintBall = constructor.registerAndConstructDefinition( new ItemPaintBall() );
		this.coloredPaintBall = constructor.constructColoredDefinition( this.paintBall, 0 );
		this.coloredLumenPaintBall = constructor.constructColoredDefinition( this.paintBall, 20 );

		this.toolEraser = constructor.registerAndConstructDefinition( new ToolEraser() );
		this.toolMeteoritePlacer = constructor.registerAndConstructDefinition( new ToolMeteoritePlacer() );
		this.toolDebugCard = constructor.registerAndConstructDefinition( new ToolDebugCard() );
		this.toolReplicatorCard = constructor.registerAndConstructDefinition( new ToolReplicatorCard() );
	}

	@Override
	public AEItemDefinition certusQuartzAxe()
	{
		return this.certusQuartzAxe;
	}

	@Override
	public AEItemDefinition certusQuartzHoe()
	{
		return this.certusQuartzHoe;
	}

	@Override
	public AEItemDefinition certusQuartzShovel()
	{
		return this.certusQuartzShovel;
	}

	@Override
	public AEItemDefinition certusQuartzPick()
	{
		return this.certusQuartzPick;
	}

	@Override
	public AEItemDefinition certusQuartzSword()
	{
		return this.certusQuartzSword;
	}

	@Override
	public AEItemDefinition certusQuartzWrench()
	{
		return this.certusQuartzWrench;
	}

	@Override
	public AEItemDefinition certusQuartzKnife()
	{
		return this.certusQuartzKnife;
	}

	@Override
	public AEItemDefinition netherQuartzAxe()
	{
		return this.netherQuartzAxe;
	}

	@Override
	public AEItemDefinition netherQuartzHoe()
	{
		return this.netherQuartzHoe;
	}

	@Override
	public AEItemDefinition netherQuartzShovel()
	{
		return this.netherQuartzShovel;
	}

	@Override
	public AEItemDefinition netherQuartzPick()
	{
		return this.netherQuartzPick;
	}

	@Override
	public AEItemDefinition netherQuartzSword()
	{
		return this.netherQuartzSword;
	}

	@Override
	public AEItemDefinition netherQuartzWrench()
	{
		return this.netherQuartzWrench;
	}

	@Override
	public AEItemDefinition netherQuartzKnife()
	{
		return this.netherQuartzKnife;
	}

	@Override
	public AEItemDefinition entropyManipulator()
	{
		return this.entropyManipulator;
	}

	@Override
	public AEItemDefinition wirelessTerminal()
	{
		return this.wirelessTerminal;
	}

	@Override
	public AEItemDefinition biometricCard()
	{
		return this.biometricCard;
	}

	@Override
	public AEItemDefinition chargedStaff()
	{
		return this.memoryCard;
	}

	@Override
	public AEItemDefinition massCannon()
	{
		return this.massCannon;
	}

	@Override
	public AEItemDefinition memoryCard()
	{
		return this.memoryCard;
	}

	@Override
	public AEItemDefinition networkTool()
	{
		return this.networkTool;
	}

	@Override
	public AEItemDefinition portableCell()
	{
		return this.portableCell;
	}

	@Override
	public AEItemDefinition cellCreative()
	{
		return this.cellCreative;
	}

	@Override
	public AEItemDefinition viewCell()
	{
		return this.viewCell;
	}

	@Override
	public AEItemDefinition cell1k()
	{
		return this.cell1k;
	}

	@Override
	public AEItemDefinition cell4k()
	{
		return this.cell4k;
	}

	@Override
	public AEItemDefinition cell16k()
	{
		return this.cell16k;
	}

	@Override
	public AEItemDefinition cell64k()
	{
		return this.cell64k;
	}

	@Override
	public AEItemDefinition spatialCell2()
	{
		return this.spatialCell2;
	}

	@Override
	public AEItemDefinition spatialCell16()
	{
		return this.spatialCell16;
	}

	@Override
	public AEItemDefinition spatialCell128()
	{
		return this.spatialCell128;
	}

	@Override
	public AEItemDefinition facade()
	{
		return this.facade;
	}

	@Override
	public AEItemDefinition crystalSeed()
	{
		return this.crystalSeed;
	}

	@Override
	public AEItemDefinition encodedPattern()
	{
		return this.encodedPattern;
	}

	@Override
	public AEItemDefinition colorApplicator()
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

	public AEItemDefinition paintBall()
	{
		return this.paintBall;
	}

	public AEItemDefinition toolEraser()
	{
		return this.toolEraser;
	}

	public AEItemDefinition toolMeteoritePlacer()
	{
		return this.toolMeteoritePlacer;
	}

	public AEItemDefinition toolDebugCard()
	{
		return this.toolDebugCard;
	}

	public AEItemDefinition toolReplicatorCard()
	{
		return this.toolReplicatorCard;
	}
}
