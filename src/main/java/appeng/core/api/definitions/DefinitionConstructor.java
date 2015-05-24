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


import net.minecraft.item.Item;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.FeatureHandlerRegistry;
import appeng.core.FeatureRegistry;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.IAEFeature;
import appeng.core.features.IFeatureHandler;
import appeng.core.features.ItemStackSrc;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;


public class DefinitionConstructor
{
	private final FeatureRegistry features;
	private final FeatureHandlerRegistry handlers;

	public DefinitionConstructor( FeatureRegistry features, FeatureHandlerRegistry handlers )
	{
		this.features = features;
		this.handlers = handlers;
	}

	public final ITileDefinition registerTileDefinition( IAEFeature feature )
	{
		final IBlockDefinition definition = this.registerBlockDefinition( feature );

		if( definition instanceof ITileDefinition )
		{
			return ( (ITileDefinition) definition );
		}

		throw new IllegalStateException( "No tile definition for " + feature );
	}

	public final IBlockDefinition registerBlockDefinition( IAEFeature feature )
	{
		final IItemDefinition definition = this.registerItemDefinition( feature );

		if( definition instanceof IBlockDefinition )
		{
			return ( (IBlockDefinition) definition );
		}

		throw new IllegalStateException( "No block definition for " + feature );
	}

	public final IItemDefinition registerItemDefinition( IAEFeature feature )
	{
		final IFeatureHandler handler = feature.handler();

		if( handler.isFeatureAvailable() )
		{
			this.handlers.addFeatureHandler( handler );
			this.features.addFeature( feature );
		}

		final IItemDefinition definition = handler.getDefinition();

		return definition;
	}

	public final AEColoredItemDefinition constructColoredDefinition( IItemDefinition target, int offset )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		for( Item targetItem : target.maybeItem().asSet() )
		{
			for( AEColor color : AEColor.VALID_COLORS )
			{
				final ActivityState state = ActivityState.from( target.isEnabled() );

				definition.add( color, new ItemStackSrc( targetItem, offset + color.ordinal(), state ) );
			}
		}

		return definition;
	}

	public final AEColoredItemDefinition constructColoredDefinition( ItemMultiPart target, PartType type )
	{
		final ColoredItemDefinition definition = new ColoredItemDefinition();

		for( AEColor color : AEColor.values() )
		{
			final ItemStackSrc multiPartSource = target.createPart( type, color );

			definition.add( color, multiPartSource );
		}

		return definition;
	}
}
