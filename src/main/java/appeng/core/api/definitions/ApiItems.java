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
import appeng.items.tools.powered.*;
import appeng.items.tools.quartz.*;


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

	public ApiItems( final DefinitionConstructor constructor )
	{
		this.certusQuartzAxe = constructor.registerItemDefinition( new ToolQuartzAxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzHoe = constructor.registerItemDefinition( new ToolQuartzHoe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzShovel = constructor.registerItemDefinition( new ToolQuartzSpade( AEFeature.CertusQuartzTools ) );
		this.certusQuartzPick = constructor.registerItemDefinition( new ToolQuartzPickaxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzSword = constructor.registerItemDefinition( new ToolQuartzSword( AEFeature.CertusQuartzTools ) );
		this.certusQuartzWrench = constructor.registerItemDefinition( new ToolQuartzWrench( AEFeature.CertusQuartzTools ) );
		this.certusQuartzKnife = constructor.registerItemDefinition( new ToolQuartzCuttingKnife( AEFeature.CertusQuartzTools ) );

		this.netherQuartzAxe = constructor.registerItemDefinition( new ToolQuartzAxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzHoe = constructor.registerItemDefinition( new ToolQuartzHoe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzShovel = constructor.registerItemDefinition( new ToolQuartzSpade( AEFeature.NetherQuartzTools ) );
		this.netherQuartzPick = constructor.registerItemDefinition( new ToolQuartzPickaxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzSword = constructor.registerItemDefinition( new ToolQuartzSword( AEFeature.NetherQuartzTools ) );
		this.netherQuartzWrench = constructor.registerItemDefinition( new ToolQuartzWrench( AEFeature.NetherQuartzTools ) );
		this.netherQuartzKnife = constructor.registerItemDefinition( new ToolQuartzCuttingKnife( AEFeature.NetherQuartzTools ) );

		this.entropyManipulator = constructor.registerItemDefinition( new ToolEntropyManipulator() );
		this.wirelessTerminal = constructor.registerItemDefinition( new ToolWirelessTerminal() );
		this.biometricCard = constructor.registerItemDefinition( new ToolBiometricCard() );
		this.chargedStaff = constructor.registerItemDefinition( new ToolChargedStaff() );
		this.massCannon = constructor.registerItemDefinition( new ToolMassCannon() );
		this.memoryCard = constructor.registerItemDefinition( new ToolMemoryCard() );
		this.networkTool = constructor.registerItemDefinition( new ToolNetworkTool() );
		this.portableCell = constructor.registerItemDefinition( new ToolPortableCell() );

		this.cellCreative = constructor.registerItemDefinition( new ItemCreativeStorageCell() );
		this.viewCell = constructor.registerItemDefinition( new ItemViewCell() );

		this.cell1k = constructor.registerItemDefinition( new ItemBasicStorageCell( MaterialType.Cell1kPart, 1 ) );
		this.cell4k = constructor.registerItemDefinition( new ItemBasicStorageCell( MaterialType.Cell4kPart, 4 ) );
		this.cell16k = constructor.registerItemDefinition( new ItemBasicStorageCell( MaterialType.Cell16kPart, 16 ) );
		this.cell64k = constructor.registerItemDefinition( new ItemBasicStorageCell( MaterialType.Cell64kPart, 64 ) );

		this.spatialCell2 = constructor.registerItemDefinition( new ItemSpatialStorageCell( 2 ) );
		this.spatialCell16 = constructor.registerItemDefinition( new ItemSpatialStorageCell( 16 ) );
		this.spatialCell128 = constructor.registerItemDefinition( new ItemSpatialStorageCell( 128 ) );

		this.facade = constructor.registerItemDefinition( new ItemFacade() );
		this.crystalSeed = constructor.registerItemDefinition( new ItemCrystalSeed() );

		// rv1
		this.encodedPattern = constructor.registerItemDefinition( new ItemEncodedPattern() );
		this.colorApplicator = constructor.registerItemDefinition( new ToolColorApplicator() );

		this.paintBall = constructor.registerItemDefinition( new ItemPaintBall() );
		this.coloredPaintBall = constructor.constructColoredDefinition( this.paintBall, 0 );
		this.coloredLumenPaintBall = constructor.constructColoredDefinition( this.paintBall, 20 );

		this.toolEraser = constructor.registerItemDefinition( new ToolEraser() );
		this.toolMeteoritePlacer = constructor.registerItemDefinition( new ToolMeteoritePlacer() );
		this.toolDebugCard = constructor.registerItemDefinition( new ToolDebugCard() );
		this.toolReplicatorCard = constructor.registerItemDefinition( new ToolReplicatorCard() );
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
