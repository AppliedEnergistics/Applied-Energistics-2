/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.bootstrap;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import appeng.bootstrap.components.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.definitions.IItemDefinition;
import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.ColoredItemDefinition;
import appeng.core.features.ItemStackSrc;
import appeng.util.Platform;


public class FeatureFactory
{

	private final AEFeature[] defaultFeatures;

	private final List<IBootstrapComponent> bootstrapComponents;

	@SideOnly( Side.CLIENT )
	ModelOverrideComponent modelOverrideComponent;

	@SideOnly( Side.CLIENT )
	private BuiltInModelComponent builtInModelComponent;

	public final TileEntityComponent tileEntityComponent;

	public FeatureFactory()
	{
		this.defaultFeatures = new AEFeature[] { AEFeature.CORE };
		this.bootstrapComponents = new ArrayList<>();

		this.tileEntityComponent = new TileEntityComponent();
		this.bootstrapComponents.add( tileEntityComponent );

		if( Platform.isClient() )
		{
			modelOverrideComponent = new ModelOverrideComponent();
			bootstrapComponents.add( modelOverrideComponent );

			builtInModelComponent = new BuiltInModelComponent();
			bootstrapComponents.add( builtInModelComponent );
		}
	}

	private FeatureFactory( FeatureFactory parent, AEFeature... defaultFeatures )
	{
		this.defaultFeatures = defaultFeatures.clone();
		this.bootstrapComponents = parent.bootstrapComponents;
		this.tileEntityComponent = parent.tileEntityComponent;
		if( Platform.isClient() )
		{
			this.modelOverrideComponent = parent.modelOverrideComponent;
			this.builtInModelComponent = parent.builtInModelComponent;
		}
	}

	public IBlockBuilder block( String id, Supplier<Block> block )
	{
		return new BlockDefinitionBuilder( this, id, block ).features( defaultFeatures );
	}

	public IItemBuilder item( String id, Supplier<Item> item )
	{
		return new ItemDefinitionBuilder( this, id, item ).features( defaultFeatures );
	}

	public AEColoredItemDefinition colored( IItemDefinition target, int offset )
	{
		ColoredItemDefinition definition = new ColoredItemDefinition();

		target.maybeItem().ifPresent( targetItem -> {
			for( final AEColor color : AEColor.VALID_COLORS )
			{
				final ActivityState state = ActivityState.from( target.isEnabled() );

				definition.add( color, new ItemStackSrc( targetItem, offset + color.ordinal(), state ) );
			}
		} );

		return definition;
	}

	public FeatureFactory features( AEFeature... features )
	{
		return new FeatureFactory( this, features );
	}

	void addBootstrapComponent( IBootstrapComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addPreInit( PreInitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addInit( InitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addModelReg( ModelRegComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	void addPostInit( PostInitComponent component )
	{
		this.bootstrapComponents.add( component );
	}

	@SideOnly( Side.CLIENT )
	void addBuiltInModel( String path, IModel model )
	{
		builtInModelComponent.addModel( path, model );
	}

	public List<IBootstrapComponent> getBootstrapComponents()
	{
		return bootstrapComponents;
	}
}
