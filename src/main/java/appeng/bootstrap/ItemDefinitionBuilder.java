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
			itemRendering = new ItemRendering();
		}
	}

	@Override
	public ItemDefinitionBuilder preInit( Consumer<Item> callback )
	{
		preInitCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder init( Consumer<Item> callback )
	{
		initCallbacks.add( callback );
		return this;
	}

	@Override
	public ItemDefinitionBuilder postInit( Consumer<Item> callback )
	{
		postInitCallbacks.add( callback );
		return this;
	}

	@Override
	public IItemBuilder features( AEFeature... features )
	{
		this.features.clear();
		addFeatures( features );
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
			customizeForClient( callback );
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
		callback.customize( itemRendering );
	}

	@Override
	public ItemDefinition build()
	{
		if( !AEConfig.instance().areFeaturesEnabled( features ) )
		{
			return new ItemDefinition( registryName, null );
		}

		Item item = itemSupplier.get();
		item.setRegistryName( AppEng.MOD_ID, registryName );

		ItemDefinition definition = new ItemDefinition( registryName, item );

		item.setUnlocalizedName( "appliedenergistics2." + registryName );
		item.setCreativeTab( creativeTab );

		// Register all extra handlers
		preInitCallbacks.forEach( consumer -> factory.addPreInit( side -> consumer.accept( item ) ) );
		initCallbacks.forEach( consumer -> factory.addInit( side -> consumer.accept( item ) ) );
		modelRegCallbacks.forEach( consumer -> factory.addModelReg( side -> consumer.accept( item ) ) );
		postInitCallbacks.forEach( consumer -> factory.addPostInit( side -> consumer.accept( item ) ) );

		// Register custom dispenser behavior if requested
		if( dispenserBehaviorSupplier != null )
		{
			factory.addPostInit( side ->
			{
				IBehaviorDispenseItem behavior = dispenserBehaviorSupplier.get();
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject( item, behavior );
			} );
		}

		factory.addPreInit( side -> Registration.addItemToRegister( item ) );

		if( Platform.isClient() )
		{
			itemRendering.apply( factory, item );
		}

		return definition;
	}
}
