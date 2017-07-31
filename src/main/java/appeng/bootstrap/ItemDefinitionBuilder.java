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
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.Registration;
import appeng.core.features.AEFeature;
import appeng.core.features.ItemDefinition;
import appeng.util.Platform;


class ItemDefinitionBuilder implements IItemBuilder
{

	private final FeatureFactory factory;

	private final String registryName;

	private final Supplier<Item> itemSupplier;

	private final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	private final List<Consumer<Item>> preInitCallbacks = new ArrayList<>();

	private final List<Consumer<Item>> initCallbacks = new ArrayList<>();

	private final List<Consumer<Item>> modelRegCallbacks = new ArrayList<>();

	private final List<Consumer<Item>> postInitCallbacks = new ArrayList<>();

	private Supplier<IBehaviorDispenseItem> dispenserBehaviorSupplier;

	@SideOnly( Side.CLIENT )
	private ItemRendering itemRendering;

	private CreativeTabs creativeTab = CreativeTab.instance;

	ItemDefinitionBuilder( FeatureFactory factory, String registryName, Supplier<Item> itemSupplier )
	{
		this.factory = factory;
		this.registryName = registryName;
		this.itemSupplier = itemSupplier;
		if( Platform.isClient() )
		{
			this.itemRendering = new ItemRendering();
		}
	}

	@Override
	public ItemDefinitionBuilder preInit( Consumer<Item> callback )
	{
		this.preInitCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder init( Consumer<Item> callback )
	{
		this.initCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder postInit( Consumer<Item> callback )
	{
		this.postInitCallbacks.add( callback );
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
	public IItemBuilder creativeTab( CreativeTabs tab )
	{
		this.creativeTab = tab;
		return this;
	}

	@Override
	public IItemBuilder rendering( ItemRenderingCustomizer callback )
	{
		if( Platform.isClient() )
		{
			this.customizeForClient( callback );
		}

		return this;
	}

	@Override
	public IItemBuilder dispenserBehavior( Supplier<IBehaviorDispenseItem> behavior )
	{
		this.dispenserBehaviorSupplier = behavior;
		return this;
	}

	@SideOnly( Side.CLIENT )
	private void customizeForClient( ItemRenderingCustomizer callback )
	{
		callback.customize( this.itemRendering );
	}

	@Override
	public ItemDefinition build()
	{
		if( !AEConfig.instance().areFeaturesEnabled( this.features ) )
		{
			return new ItemDefinition( this.registryName, null );
		}

		Item item = this.itemSupplier.get();
		item.setRegistryName( AppEng.MOD_ID, this.registryName );

		ItemDefinition definition = new ItemDefinition( this.registryName, item );

		item.setUnlocalizedName( "appliedenergistics2." + this.registryName );
		item.setCreativeTab( this.creativeTab );

		// Register all extra handlers
		this.preInitCallbacks.forEach( consumer -> this.factory.addPreInit( side -> consumer.accept( item ) ) );
		this.initCallbacks.forEach( consumer -> this.factory.addInit( side -> consumer.accept( item ) ) );
		this.modelRegCallbacks.forEach( consumer -> this.factory.addModelReg( ( side, reg ) -> consumer.accept( item ) ) );
		this.postInitCallbacks.forEach( consumer -> this.factory.addPostInit( side -> consumer.accept( item ) ) );

		// Register custom dispenser behavior if requested
		if( this.dispenserBehaviorSupplier != null )
		{
			this.factory.addPostInit( side ->
			{
				IBehaviorDispenseItem behavior = this.dispenserBehaviorSupplier.get();
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject( item, behavior );
			} );
		}

		this.factory.addPreInit( side -> Registration.addItemToRegister( item ) );

		if( Platform.isClient() )
		{
			this.itemRendering.apply( this.factory, item );
		}

		return definition;
	}
}
