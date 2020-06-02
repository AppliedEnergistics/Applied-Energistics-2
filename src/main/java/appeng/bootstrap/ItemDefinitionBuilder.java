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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

import appeng.bootstrap.components.IItemRegistrationComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.api.features.AEFeature;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;


class ItemDefinitionBuilder implements IItemBuilder
{

	private final FeatureFactory factory;

	private final String registryName;

	private final Supplier<Item> itemSupplier;

	private final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	private final List<Function<Item, IBootstrapComponent>> boostrapComponents = new ArrayList<>();

	private Supplier<IDispenseItemBehavior> dispenserBehaviorSupplier;

// FIXME
//	@OnlyIn( Dist.CLIENT )
//	private ItemRendering itemRendering;
//
	private ItemGroup itemGroup = CreativeTab.instance;

	ItemDefinitionBuilder( FeatureFactory factory, String registryName, Supplier<Item> itemSupplier )
	{
		this.factory = factory;
		this.registryName = registryName;
		this.itemSupplier = itemSupplier;
		if( Platform.isClient() )
		{
			// FIXME this.itemRendering = new ItemRendering();
		}
	}

	@Override
	public IItemBuilder bootstrap( Function<Item, IBootstrapComponent> component )
	{
		this.boostrapComponents.add( component );
		return this;
	}

	@Override
	public IItemBuilder features( AEFeature... features )
	{
		this.features.clear();
		this.addFeatures( features );
		return this;
	}

	@Override
	public IItemBuilder addFeatures( AEFeature... features )
	{
		Collections.addAll( this.features, features );
		return this;
	}

	@Override
	public IItemBuilder itemGroup( ItemGroup itemGroup )
	{
		this.itemGroup = itemGroup;
		return this;
	}

// FIXME	@Override
// FIXME	public IItemBuilder rendering( ItemRenderingCustomizer callback )
// FIXME	{
// FIXME		if( Platform.isClient() )
// FIXME		{
// FIXME			this.customizeForClient( callback );
// FIXME		}
// FIXME
// FIXME		return this;
// FIXME	}

	@Override
	public IItemBuilder dispenserBehavior( Supplier<IDispenseItemBehavior> behavior )
	{
		this.dispenserBehaviorSupplier = behavior;
		return this;
	}

// FIXME	@OnlyIn( Dist.CLIENT )
// FIXME	private void customizeForClient( ItemRenderingCustomizer callback )
// FIXME	{
// FIXME		callback.customize( this.itemRendering );
// FIXME	}

	@Override
	public ItemDefinition build()
	{
		Item item = this.itemSupplier.get();
		item.setRegistryName( AppEng.MOD_ID, this.registryName );

		ItemDefinition definition = new ItemDefinition( this.registryName, item, features );

		// Register all extra handlers
		this.boostrapComponents.forEach( component -> this.factory.addBootstrapComponent( component.apply( item ) ) );

		// Register custom dispenser behavior if requested
		if( this.dispenserBehaviorSupplier != null )
		{
			this.factory.addBootstrapComponent( (IPostInitComponent) side ->
			{
				IDispenseItemBehavior behavior = this.dispenserBehaviorSupplier.get();
				DispenserBlock.registerDispenseBehavior( item, behavior );
			} );
		}

		this.factory.addBootstrapComponent( (IItemRegistrationComponent) ( side, reg ) -> reg.register( item ) );

		if( Platform.isClient() )
		{
// FIXME			this.itemRendering.apply( this.factory, item );
		}

		return definition;
	}

}
