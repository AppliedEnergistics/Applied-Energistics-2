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


import javax.annotation.Nullable;

import net.minecraft.item.Item;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import appeng.api.definitions.IItems;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.AEFeature;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.ItemStackSrc;
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
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	private final Optional<AEItemDefinition> certusQuartzAxe;
	private final Optional<AEItemDefinition> certusQuartzHoe;
	private final Optional<AEItemDefinition> certusQuartzShovel;
	private final Optional<AEItemDefinition> certusQuartzPick;
	private final Optional<AEItemDefinition> certusQuartzSword;
	private final Optional<AEItemDefinition> certusQuartzWrench;
	private final Optional<AEItemDefinition> certusQuartzKnife;

	private final Optional<AEItemDefinition> netherQuartzAxe;
	private final Optional<AEItemDefinition> netherQuartzHoe;
	private final Optional<AEItemDefinition> netherQuartzShovel;
	private final Optional<AEItemDefinition> netherQuartzPick;
	private final Optional<AEItemDefinition> netherQuartzSword;
	private final Optional<AEItemDefinition> netherQuartzWrench;
	private final Optional<AEItemDefinition> netherQuartzKnife;

	private final Optional<AEItemDefinition> entropyManipulator;
	private final Optional<AEItemDefinition> wirelessTerminal;
	private final Optional<AEItemDefinition> biometricCard;
	private final Optional<AEItemDefinition> chargedStaff;
	private final Optional<AEItemDefinition> massCannon;
	private final Optional<AEItemDefinition> memoryCard;
	private final Optional<AEItemDefinition> networkTool;
	private final Optional<AEItemDefinition> portableCell;

	private final Optional<AEItemDefinition> cellCreative;
	private final Optional<AEItemDefinition> viewCell;

	private final Optional<AEItemDefinition> cell1k;
	private final Optional<AEItemDefinition> cell4k;
	private final Optional<AEItemDefinition> cell16k;
	private final Optional<AEItemDefinition> cell64k;

	private final Optional<AEItemDefinition> spatialCell2;
	private final Optional<AEItemDefinition> spatialCell16;
	private final Optional<AEItemDefinition> spatialCell128;

	private final Optional<AEItemDefinition> facade;
	private final Optional<AEItemDefinition> crystalSeed;

	// rv1
	private final Optional<AEItemDefinition> encodedPattern;
	private final Optional<AEItemDefinition> colorApplicator;

	private final Optional<AEItemDefinition> paintBall;
	private final Optional<AEColoredItemDefinition> coloredPaintBall;
	private final Optional<AEColoredItemDefinition> coloredLumenPaintBall;

	// unsupported dev tools
	private final Optional<AEItemDefinition> toolEraser;
	private final Optional<AEItemDefinition> toolMeteoritePlacer;
	private final Optional<AEItemDefinition> toolDebugCard;
	private final Optional<AEItemDefinition> toolReplicatorCard;

	public ApiItems( FeatureRegistry features, FeatureHandlerRegistry handlers )
	{
		this.features = features;
		this.handlers = handlers;

		this.certusQuartzAxe = this.getMaybeDefinition( new ToolQuartzAxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzHoe = this.getMaybeDefinition( new ToolQuartzHoe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzShovel = this.getMaybeDefinition( new ToolQuartzSpade( AEFeature.CertusQuartzTools ) );
		this.certusQuartzPick = this.getMaybeDefinition( new ToolQuartzPickaxe( AEFeature.CertusQuartzTools ) );
		this.certusQuartzSword = this.getMaybeDefinition( new ToolQuartzSword( AEFeature.CertusQuartzTools ) );
		this.certusQuartzWrench = this.getMaybeDefinition( new ToolQuartzWrench( AEFeature.CertusQuartzTools ) );
		this.certusQuartzKnife = this.getMaybeDefinition( new ToolQuartzCuttingKnife( AEFeature.CertusQuartzTools ) );

		this.netherQuartzAxe = this.getMaybeDefinition( new ToolQuartzAxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzHoe = this.getMaybeDefinition( new ToolQuartzHoe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzShovel = this.getMaybeDefinition( new ToolQuartzSpade( AEFeature.NetherQuartzTools ) );
		this.netherQuartzPick = this.getMaybeDefinition( new ToolQuartzPickaxe( AEFeature.NetherQuartzTools ) );
		this.netherQuartzSword = this.getMaybeDefinition( new ToolQuartzSword( AEFeature.NetherQuartzTools ) );
		this.netherQuartzWrench = this.getMaybeDefinition( new ToolQuartzWrench( AEFeature.NetherQuartzTools ) );
		this.netherQuartzKnife = this.getMaybeDefinition( new ToolQuartzCuttingKnife( AEFeature.NetherQuartzTools ) );

		this.entropyManipulator = this.getMaybeDefinition( new ToolEntropyManipulator() );
		this.wirelessTerminal = this.getMaybeDefinition( new ToolWirelessTerminal() );
		this.biometricCard = this.getMaybeDefinition( new ToolBiometricCard() );
		this.chargedStaff = this.getMaybeDefinition( new ToolChargedStaff() );
		this.massCannon = this.getMaybeDefinition( new ToolMassCannon() );
		this.memoryCard = this.getMaybeDefinition( new ToolMemoryCard() );
		this.networkTool = this.getMaybeDefinition( new ToolNetworkTool() );
		this.portableCell = this.getMaybeDefinition( new ToolPortableCell() );

		this.cellCreative = this.getMaybeDefinition( new ItemCreativeStorageCell() );
		this.viewCell = this.getMaybeDefinition( new ItemViewCell() );

		this.cell1k = this.getMaybeDefinition( new ItemBasicStorageCell( MaterialType.Cell1kPart, 1 ) );
		this.cell4k = this.getMaybeDefinition( new ItemBasicStorageCell( MaterialType.Cell4kPart, 4 ) );
		this.cell16k = this.getMaybeDefinition( new ItemBasicStorageCell( MaterialType.Cell16kPart, 16 ) );
		this.cell64k = this.getMaybeDefinition( new ItemBasicStorageCell( MaterialType.Cell64kPart, 64 ) );

		this.spatialCell2 = this.getMaybeDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 2 ) );
		this.spatialCell16 = this.getMaybeDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 16 ) );
		this.spatialCell128 = this.getMaybeDefinition( new ItemSpatialStorageCell( MaterialType.Cell2SpatialPart, 128 ) );

		this.facade = this.getMaybeDefinition( new ItemFacade() );
		this.crystalSeed = this.getMaybeDefinition( new ItemCrystalSeed() );

		// rv1
		this.encodedPattern = this.getMaybeDefinition( new ItemEncodedPattern() );
		this.colorApplicator = this.getMaybeDefinition( new ToolColorApplicator() );

		this.paintBall = this.getMaybeDefinition( new ItemPaintBall() );
		this.coloredPaintBall = this.paintBall.transform( new ColoredTransformationFunction( new ColoredItemDefinition(), 0 ) );
		this.coloredLumenPaintBall = this.paintBall.transform( new ColoredTransformationFunction( new ColoredItemDefinition(), 20 ) );

		this.toolEraser = this.getMaybeDefinition( new ToolEraser() );
		this.toolMeteoritePlacer = this.getMaybeDefinition( new ToolMeteoritePlacer() );
		this.toolDebugCard = this.getMaybeDefinition( new ToolDebugCard() );
		this.toolReplicatorCard = this.getMaybeDefinition( new ToolReplicatorCard() );
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
	public Optional<AEItemDefinition> certusQuartzAxe()
	{
		return this.certusQuartzAxe;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzHoe()
	{
		return this.certusQuartzHoe;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzShovel()
	{
		return this.certusQuartzShovel;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzPick()
	{
		return this.certusQuartzPick;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzSword()
	{
		return this.certusQuartzSword;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzWrench()
	{
		return this.certusQuartzWrench;
	}

	@Override
	public Optional<AEItemDefinition> certusQuartzKnife()
	{
		return this.certusQuartzKnife;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzAxe()
	{
		return this.netherQuartzAxe;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzHoe()
	{
		return this.netherQuartzHoe;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzShovel()
	{
		return this.netherQuartzShovel;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzPick()
	{
		return this.netherQuartzPick;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzSword()
	{
		return this.netherQuartzSword;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzWrench()
	{
		return this.netherQuartzWrench;
	}

	@Override
	public Optional<AEItemDefinition> netherQuartzKnife()
	{
		return this.netherQuartzKnife;
	}

	@Override
	public Optional<AEItemDefinition> entropyManipulator()
	{
		return this.entropyManipulator;
	}

	@Override
	public Optional<AEItemDefinition> wirelessTerminal()
	{
		return this.wirelessTerminal;
	}

	@Override
	public Optional<AEItemDefinition> biometricCard()
	{
		return this.biometricCard;
	}

	@Override
	public Optional<AEItemDefinition> chargedStaff()
	{
		return this.chargedStaff;
	}

	@Override
	public Optional<AEItemDefinition> massCannon()
	{
		return this.massCannon;
	}

	@Override
	public Optional<AEItemDefinition> memoryCard()
	{
		return this.memoryCard;
	}

	@Override
	public Optional<AEItemDefinition> networkTool()
	{
		return this.networkTool;
	}

	@Override
	public Optional<AEItemDefinition> portableCell()
	{
		return this.portableCell;
	}

	@Override
	public Optional<AEItemDefinition> cellCreative()
	{
		return this.cellCreative;
	}

	@Override
	public Optional<AEItemDefinition> viewCell()
	{
		return this.viewCell;
	}

	@Override
	public Optional<AEItemDefinition> cell1k()
	{
		return this.cell1k;
	}

	@Override
	public Optional<AEItemDefinition> cell4k()
	{
		return this.cell4k;
	}

	@Override
	public Optional<AEItemDefinition> cell16k()
	{
		return this.cell16k;
	}

	@Override
	public Optional<AEItemDefinition> cell64k()
	{
		return this.cell64k;
	}

	@Override
	public Optional<AEItemDefinition> spatialCell2()
	{
		return this.spatialCell2;
	}

	@Override
	public Optional<AEItemDefinition> spatialCell16()
	{
		return this.spatialCell16;
	}

	@Override
	public Optional<AEItemDefinition> spatialCell128()
	{
		return this.spatialCell128;
	}

	@Override
	public Optional<AEItemDefinition> facade()
	{
		return this.facade;
	}

	@Override
	public Optional<AEItemDefinition> crystalSeed()
	{
		return this.crystalSeed;
	}

	@Override
	public Optional<AEItemDefinition> encodedPattern()
	{
		return this.encodedPattern;
	}

	@Override
	public Optional<AEItemDefinition> colorApplicator()
	{
		return this.colorApplicator;
	}

	@Override
	public Optional<AEColoredItemDefinition> coloredPaintBall()
	{
		return this.coloredPaintBall;
	}

	@Override
	public Optional<AEColoredItemDefinition> coloredLumenPaintBall()
	{
		return this.coloredLumenPaintBall;
	}

	public Optional<AEItemDefinition> paintBall()
	{
		return this.paintBall;
	}

	public Optional<AEItemDefinition> toolEraser()
	{
		return this.toolEraser;
	}

	public Optional<AEItemDefinition> toolMeteoritePlacer()
	{
		return this.toolMeteoritePlacer;
	}

	public Optional<AEItemDefinition> toolDebugCard()
	{
		return this.toolDebugCard;
	}

	public Optional<AEItemDefinition> toolReplicatorCard()
	{
		return this.toolReplicatorCard;
	}

	private static final class ColoredTransformationFunction implements Function<AEItemDefinition, AEColoredItemDefinition>
	{
		private final ColoredItemDefinition colorDefinition;
		private final int offset;

		public ColoredTransformationFunction( ColoredItemDefinition colorDefinition, int offset )
		{
			this.colorDefinition = colorDefinition;
			this.offset = offset;
		}

		@Nullable
		@Override
		public AEColoredItemDefinition apply( AEItemDefinition input )
		{
			final Item item = input.item();

			for ( AEColor color : AEColor.VALID_COLORS )
			{
				this.colorDefinition.add( color, new ItemStackSrc( item, this.offset + color.ordinal() ) );
			}

			return this.colorDefinition;
		}
	}
}
