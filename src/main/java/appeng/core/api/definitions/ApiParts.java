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

import com.google.common.base.Function;
import com.google.common.base.Optional;

import appeng.api.definitions.IParts;
import appeng.api.parts.IPartHelper;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.api.util.AEItemDefinition;
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.DamagedItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.IStackSrc;
import appeng.core.features.ItemStackSrc;
import appeng.core.features.NullItemDefinition;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;


/**
 * Internal implementation for the API parts
 */
public final class ApiParts implements IParts
{
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	private final Optional<AEColoredItemDefinition> cableSmart;
	private final Optional<AEColoredItemDefinition> cableCovered;
	private final Optional<AEColoredItemDefinition> cableGlass;
	private final Optional<AEColoredItemDefinition> cableDense;
	private final Optional<AEColoredItemDefinition> lumenCableSmart;
	private final Optional<AEColoredItemDefinition> lumenCableCovered;
	private final Optional<AEColoredItemDefinition> lumenCableGlass;
	private final Optional<AEColoredItemDefinition> lumenCableDense;
	private final Optional<AEItemDefinition> quartzFiber;
	private final Optional<AEItemDefinition> toggleBus;
	private final Optional<AEItemDefinition> invertedToggleBus;
	private final Optional<AEItemDefinition> storageBus;
	private final Optional<AEItemDefinition> importBus;
	private final Optional<AEItemDefinition> exportBus;
	private final Optional<AEItemDefinition> iface;
	private final Optional<AEItemDefinition> levelEmitter;
	private final Optional<AEItemDefinition> annihilationPlane;
	private final Optional<AEItemDefinition> formationPlane;
	private final Optional<AEItemDefinition> p2PTunnelME;
	private final Optional<AEItemDefinition> p2PTunnelRedstone;
	private final Optional<AEItemDefinition> p2PTunnelItems;
	private final Optional<AEItemDefinition> p2PTunnelLiquids;
	private final Optional<AEItemDefinition> p2PTunnelMJ;
	private final Optional<AEItemDefinition> p2PTunnelEU;
	private final Optional<AEItemDefinition> p2PTunnelRF;
	private final Optional<AEItemDefinition> p2PTunnelLight;
	private final Optional<AEItemDefinition> cableAnchor;
	private final Optional<AEItemDefinition> monitor;
	private final Optional<AEItemDefinition> semiDarkMonitor;
	private final Optional<AEItemDefinition> darkMonitor;
	private final Optional<AEItemDefinition> interfaceTerminal;
	private final Optional<AEItemDefinition> patternTerminal;
	private final Optional<AEItemDefinition> craftingTerminal;
	private final Optional<AEItemDefinition> terminal;
	private final Optional<AEItemDefinition> storageMonitor;
	private final Optional<AEItemDefinition> conversionMonitor;

	public ApiParts( FeatureRegistry features, FeatureHandlerRegistry handlers, IPartHelper partHelper )
	{
		this.features = features;
		this.handlers = handlers;

		final ItemMultiPart itemMultiPart = new ItemMultiPart( partHelper );
		final Optional<AEItemDefinition> multiPart = this.getMaybeDefinition( itemMultiPart );

		this.cableSmart = multiPart.transform( new ColoredPartTransformationFunction( itemMultiPart, PartType.CableSmart ) );
		this.cableCovered = multiPart.transform( new ColoredPartTransformationFunction( itemMultiPart, PartType.CableCovered ) );
		this.cableGlass = multiPart.transform( new ColoredPartTransformationFunction( itemMultiPart, PartType.CableGlass ) );
		this.cableDense = multiPart.transform( new ColoredPartTransformationFunction( itemMultiPart, PartType.CableDense ) );
		this.lumenCableSmart = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.lumenCableCovered = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.lumenCableGlass = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.lumenCableDense = Optional.absent(); // has yet to be implemented, no PartType defined for it yet
		this.quartzFiber = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.QuartzFiber ) );
		this.toggleBus = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.ToggleBus ) );
		this.invertedToggleBus = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.InvertedToggleBus ) );
		this.storageBus = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.StorageBus ) );
		this.importBus = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.ImportBus ) );
		this.exportBus = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.ExportBus ) );
		this.iface = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.Interface ) );
		this.levelEmitter = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.LevelEmitter ) );
		this.annihilationPlane = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.AnnihilationPlane ) );
		this.formationPlane = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.FormationPlane ) );
		this.p2PTunnelME = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelME ) );
		this.p2PTunnelRedstone = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelRedstone ) );
		this.p2PTunnelItems = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelItems ) );
		this.p2PTunnelLiquids = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelLiquids ) );
		this.p2PTunnelMJ = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelMJ ) );
		this.p2PTunnelEU = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelEU ) );
		this.p2PTunnelRF = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelRF ) );
		this.p2PTunnelLight = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.P2PTunnelLight ) );
		this.cableAnchor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.CableAnchor ) );
		this.monitor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.Monitor ) );
		this.semiDarkMonitor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.SemiDarkMonitor ) );
		this.darkMonitor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.DarkMonitor ) );
		this.interfaceTerminal = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.InterfaceTerminal ) );
		this.patternTerminal = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.PatternTerminal ) );
		this.craftingTerminal = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.CraftingTerminal ) );
		this.terminal = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.Terminal ) );
		this.storageMonitor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.StorageMonitor ) );
		this.conversionMonitor = multiPart.transform( new PartTransformationFunction( itemMultiPart, PartType.ConversionMonitor ) );
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
	public Optional<AEColoredItemDefinition> cableSmart()
	{
		return this.cableSmart;
	}

	@Override
	public Optional<AEColoredItemDefinition> cableCovered()
	{
		return this.cableCovered;
	}

	@Override
	public Optional<AEColoredItemDefinition> cableGlass()
	{
		return this.cableGlass;
	}

	@Override
	public Optional<AEColoredItemDefinition> cableDense()
	{
		return this.cableDense;
	}

	@Override
	public Optional<AEColoredItemDefinition> lumenCableSmart()
	{
		return this.lumenCableSmart;
	}

	@Override
	public Optional<AEColoredItemDefinition> lumenCableCovered()
	{
		return this.lumenCableCovered;
	}

	@Override
	public Optional<AEColoredItemDefinition> lumenCableGlass()
	{
		return this.lumenCableGlass;
	}

	@Override
	public Optional<AEColoredItemDefinition> lumenCableDense()
	{
		return this.lumenCableDense;
	}

	@Override
	public Optional<AEItemDefinition> quartzFiber()
	{
		return this.quartzFiber;
	}

	@Override
	public Optional<AEItemDefinition> toggleBus()
	{
		return this.toggleBus;
	}

	@Override
	public Optional<AEItemDefinition> invertedToggleBus()
	{
		return this.invertedToggleBus;
	}

	@Override
	public Optional<AEItemDefinition> storageBus()
	{
		return this.storageBus;
	}

	@Override
	public Optional<AEItemDefinition> importBus()
	{
		return this.importBus;
	}

	@Override
	public Optional<AEItemDefinition> exportBus()
	{
		return this.exportBus;
	}

	@Override
	public Optional<AEItemDefinition> iface()
	{
		return this.iface;
	}

	@Override
	public Optional<AEItemDefinition> levelEmitter()
	{
		return this.levelEmitter;
	}

	@Override
	public Optional<AEItemDefinition> annihilationPlane()
	{
		return this.annihilationPlane;
	}

	@Override
	public Optional<AEItemDefinition> formationPlane()
	{
		return this.formationPlane;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelME()
	{
		return this.p2PTunnelME;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelRedstone()
	{
		return this.p2PTunnelRedstone;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelItems()
	{
		return this.p2PTunnelItems;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelLiquids()
	{
		return this.p2PTunnelLiquids;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelMJ()
	{
		return this.p2PTunnelMJ;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelEU()
	{
		return this.p2PTunnelEU;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelRF()
	{
		return this.p2PTunnelRF;
	}

	@Override
	public Optional<AEItemDefinition> p2PTunnelLight()
	{
		return this.p2PTunnelLight;
	}

	@Override
	public Optional<AEItemDefinition> cableAnchor()
	{
		return this.cableAnchor;
	}

	@Override
	public Optional<AEItemDefinition> monitor()
	{
		return this.monitor;
	}

	@Override
	public Optional<AEItemDefinition> semiDarkMonitor()
	{
		return this.semiDarkMonitor;
	}

	@Override
	public Optional<AEItemDefinition> darkMonitor()
	{
		return this.darkMonitor;
	}

	@Override
	public Optional<AEItemDefinition> interfaceTerminal()
	{
		return this.interfaceTerminal;
	}

	@Override
	public Optional<AEItemDefinition> patternTerminal()
	{
		return this.patternTerminal;
	}

	@Override
	public Optional<AEItemDefinition> craftingTerminal()
	{
		return this.craftingTerminal;
	}

	@Override
	public Optional<AEItemDefinition> terminal()
	{
		return this.terminal;
	}

	@Override
	public Optional<AEItemDefinition> storageMonitor()
	{
		return this.storageMonitor;
	}

	@Override
	public Optional<AEItemDefinition> conversionMonitor()
	{
		return this.conversionMonitor;
	}

	private static final class ColoredPartTransformationFunction implements Function<AEItemDefinition, AEColoredItemDefinition>
	{
		private final ItemMultiPart multiPart;
		private final PartType type;

		public ColoredPartTransformationFunction( ItemMultiPart multiPart, PartType type )
		{
			this.multiPart = multiPart;
			this.type = type;
		}

		@Nullable
		@Override
		public AEColoredItemDefinition apply( @Nullable AEItemDefinition input )
		{
			final ColoredItemDefinition coloredDefinition = new ColoredItemDefinition();
			for ( AEColor color : this.type.getVariants() )
			{
				ItemStackSrc multiPartSource = this.multiPart.createPart( this.type, color );
				final Optional<ItemStackSrc> maybeSource = Optional.fromNullable( multiPartSource );

				if ( maybeSource.isPresent() )
				{
					coloredDefinition.add( color, multiPartSource );
				}
			}

			return coloredDefinition;
		}
	}


	private static final class PartTransformationFunction implements Function<AEItemDefinition, AEItemDefinition>
	{
		private final ItemMultiPart multiPart;
		private final PartType type;

		public PartTransformationFunction( ItemMultiPart multiPart, PartType type )
		{
			this.multiPart = multiPart;
			this.type = type;
		}

		@Nullable
		@Override
		public AEItemDefinition apply( AEItemDefinition input )
		{
			final IStackSrc stackSource = this.multiPart.createPart( this.type, null );
			final Optional<IStackSrc> maybeSource = Optional.fromNullable( stackSource );

			if ( maybeSource.isPresent() )
			{
				return new DamagedItemDefinition( stackSource );
			}
			else
			{
				return new NullItemDefinition();
			}
		}
	}
}
