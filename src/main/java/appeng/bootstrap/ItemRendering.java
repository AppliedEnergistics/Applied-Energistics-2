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


import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.bootstrap.components.ItemColorComponent;
import appeng.bootstrap.components.ItemMeshDefinitionComponent;
import appeng.bootstrap.components.ItemModelComponent;
import appeng.bootstrap.components.ItemVariantsComponent;


class ItemRendering implements IItemRendering
{

	@OnlyIn( Dist.CLIENT )
	private IItemColor itemColor;

//  FIXME
//	@OnlyIn( Dist.CLIENT )
//	private ItemMeshDefinition itemMeshDefinition;

	@OnlyIn( Dist.CLIENT )
	private Map<Integer, ModelResourceLocation> itemModels = new HashMap<>();

	@OnlyIn( Dist.CLIENT )
	private Set<ResourceLocation> variants = new HashSet<>();

	@OnlyIn( Dist.CLIENT )
	private Map<String, IUnbakedModel> builtInModels = new HashMap<>();

// FIXME
//	@Override
//	@OnlyIn( Dist.CLIENT )
//	public IItemRendering meshDefinition( ItemMeshDefinition meshDefinition )
//	{
//		this.itemMeshDefinition = meshDefinition;
//		return this;
//	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public IItemRendering model( int meta, ModelResourceLocation model )
	{
		this.itemModels.put( meta, model );
		return this;
	}

	@Override
	public IItemRendering variants( Collection<ResourceLocation> resources )
	{
		this.variants.addAll( resources );
		return this;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public IItemRendering color( IItemColor itemColor )
	{
		this.itemColor = itemColor;
		return this;
	}

	@Override
	public IItemRendering builtInModel( String name, IUnbakedModel model )
	{
		this.builtInModels.put( name, model );
		return this;
	}

	void apply( FeatureFactory factory, Item item )
	{
//FIXME		if( this.itemMeshDefinition != null )
//FIXME		{
//FIXME			factory.addBootstrapComponent( new ItemMeshDefinitionComponent( item, this.itemMeshDefinition ) );
//FIXME		}

		if( !this.itemModels.isEmpty() )
		{
			factory.addBootstrapComponent( new ItemModelComponent( item, this.itemModels ) );
		}

		Set<ResourceLocation> resources = new HashSet<>( this.variants );

		// Register a default item model if neither items by meta nor an item mesh definition exist
		if( /* FIXME this.itemMeshDefinition == null  && */ this.itemModels.isEmpty() )
		{
//			ModelResourceLocation model;
//
//			// For block items, the default will try to use the default state of the associated block
//			if( item instanceof BlockItem )
//			{
//				Block block = ( (BlockItem) item ).getBlock();
//
//				// We can only do this once the blocks are actually registered...
//				model = new ModelResourceLocation(
//						new ResourceLocation(
//								block.getRegistryName().getNamespace(),
//								"block/" + block.getRegistryName().getPath()
//						)
//						, "inventory" );
//			}
//			else
//			{
//				model = new ModelResourceLocation( item.getRegistryName(), "inventory" );
//			}
//			factory.addBootstrapComponent( new ItemModelComponent( item, ImmutableMap.of( 0, model ) ) );
		}

		// TODO : 1.12
		this.builtInModels.forEach( factory::addBuiltInModel );

		if( !resources.isEmpty() )
		{
			factory.addBootstrapComponent( new ItemVariantsComponent( item, resources ) );
		}
		// FIXME else if( this.itemMeshDefinition != null )
		// FIXME {
		// FIXME 	// Adding an empty variant list here will prevent Vanilla from trying to load the default item model in this
		// FIXME 	// case
		// FIXME 	factory.addBootstrapComponent( new ItemVariantsComponent( item, Collections.emptyList() ) );
		// FIXME }

		if( this.itemColor != null )
		{
			factory.addBootstrapComponent( new ItemColorComponent( item, this.itemColor ) );
		}
	}

}
