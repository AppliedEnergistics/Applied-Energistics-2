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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;
import appeng.block.AEBaseItemBlock;
import appeng.block.AEBaseTileBlock;
import appeng.bootstrap.definitions.TileEntityDefinition;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.CreativeTab;
import appeng.core.Registration;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockDefinition;
import appeng.core.features.BlockStackSrc;
import appeng.core.features.TileDefinition;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;


class BlockDefinitionBuilder implements IBlockBuilder
{

	private final FeatureFactory factory;

	private final String registryName;

	private final Supplier<? extends Block> blockSupplier;

	private final List<BiConsumer<Block, Item>> preInitCallbacks = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> initCallbacks = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> modelRegCallbacks = new ArrayList<>();

	private final List<BiConsumer<Block, Item>> postInitCallbacks = new ArrayList<>();

	private final EnumSet<AEFeature> features = EnumSet.noneOf( AEFeature.class );

	private CreativeTabs creativeTab = CreativeTab.instance;

	private TileEntityDefinition tileEntityDefinition;

	private boolean disableItem = false;

	private Function<Block, ItemBlock> itemFactory;

	@SideOnly( Side.CLIENT )
	private BlockRendering blockRendering;

	@SideOnly( Side.CLIENT )
	private ItemRendering itemRendering;

	BlockDefinitionBuilder( FeatureFactory factory, String id, Supplier<? extends Block> blockSupplier )
	{
		this.factory = factory;
		this.registryName = id;
		this.blockSupplier = blockSupplier;

		if( Platform.isClient() )
		{
			this.blockRendering = new BlockRendering();
			this.itemRendering = new ItemRendering();
		}
	}

	@Override
	public BlockDefinitionBuilder preInit( BiConsumer<Block, Item> callback )
	{
		this.preInitCallbacks.add( callback );
		return this;
	}

	@Override
	public BlockDefinitionBuilder init( BiConsumer<Block, Item> callback )
	{
		this.initCallbacks.add( callback );
		return this;
	}

	@Override
	public BlockDefinitionBuilder modelRegInit( BiConsumer<Block, Item> callback )
	{
		this.modelRegCallbacks.add( callback );
		return this;
	}

	@Override
	public BlockDefinitionBuilder postInit( BiConsumer<Block, Item> callback )
	{
		this.postInitCallbacks.add( callback );
		return this;
	}

	@Override
	public IBlockBuilder features( AEFeature... features )
	{
		this.features.clear();
		this.addFeatures( features );
		return this;
	}

	@Override
	public IBlockBuilder addFeatures( AEFeature... features )
	{
		Collections.addAll( this.features, features );
		return this;
	}

	@Override
	public BlockDefinitionBuilder rendering( BlockRenderingCustomizer callback )
	{
		if( Platform.isClient() )
		{
			this.customizeForClient( callback );
		}

		return this;
	}

	@Override
	public IBlockBuilder tileEntity( TileEntityDefinition tileEntityDefinition )
	{
		this.tileEntityDefinition = tileEntityDefinition;
		return this;
	}

	@Override
	public IBlockBuilder useCustomItemModel()
	{
		this.rendering( new BlockRenderingCustomizer()
		{
			@Override
			@SideOnly( Side.CLIENT )
			public void customize( IBlockRendering rendering, IItemRendering itemRendering )
			{
				ModelResourceLocation model = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, BlockDefinitionBuilder.this.registryName ), "inventory" );
				itemRendering.model( model ).variants( model );
			}
		} );

		return this;
	}

	@Override
	public IBlockBuilder item( Function<Block, ItemBlock> factory )
	{
		this.itemFactory = factory;
		return this;
	}

	@Override
	public IBlockBuilder disableItem()
	{
		this.disableItem = true;
		return this;
	}

	@SideOnly( Side.CLIENT )
	private void customizeForClient( BlockRenderingCustomizer callback )
	{
		callback.customize( this.blockRendering, this.itemRendering );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public <T extends IBlockDefinition> T build()
	{
		if( !AEConfig.instance().areFeaturesEnabled( this.features ) )
		{
			return (T) new TileDefinition( this.registryName, null, null );
		}

		// Create block and matching item, and set factory name of both
		Block block = this.blockSupplier.get();
		block.setRegistryName( AppEng.MOD_ID, this.registryName );
		block.setUnlocalizedName( "appliedenergistics2." + this.registryName );

		ItemBlock item = this.constructItemFromBlock( block );
		if( item != null )
		{
			item.setRegistryName( AppEng.MOD_ID, this.registryName );
		}

		// Register the item and block with the game
		this.factory.addPreInit( side ->
		{
			Registration.addBlockToRegister( block );
			if( item != null )
			{
				Registration.addItemToRegister( item );
			}
		} );

		block.setCreativeTab( this.creativeTab );

		// Register all extra handlers
		this.preInitCallbacks.forEach( consumer -> this.factory.addPreInit( side -> consumer.accept( block, item ) ) );
		this.initCallbacks.forEach( consumer -> this.factory.addInit( side -> consumer.accept( block, item ) ) );
		this.modelRegCallbacks.forEach( consumer -> this.factory.addModelReg( side -> consumer.accept( block, item ) ) );
		this.postInitCallbacks.forEach( consumer -> this.factory.addPostInit( side -> consumer.accept( block, item ) ) );

		if( this.tileEntityDefinition != null && block instanceof AEBaseTileBlock )
		{
			( (AEBaseTileBlock) block ).setTileEntity( this.tileEntityDefinition.getTileEntityClass() );
			if( this.tileEntityDefinition.getName() == null )
			{
				this.tileEntityDefinition.setName( this.registryName );
			}

		}

		if( Platform.isClient() )
		{
			if( block instanceof AEBaseTileBlock )
			{
				AEBaseTileBlock tileBlock = (AEBaseTileBlock) block;
				this.blockRendering.apply( this.factory, block, tileBlock.getTileEntityClass() );
			}
			else
			{
				this.blockRendering.apply( this.factory, block, null );
			}

			if( item != null )
			{
				this.itemRendering.apply( this.factory, item );
			}
		}

		if( block instanceof AEBaseTileBlock )
		{
			this.factory.addPreInit( side ->
			{
				AEBaseTile.registerTileItem(
						this.tileEntityDefinition == null ? ( (AEBaseTileBlock) block ).getTileEntityClass() : this.tileEntityDefinition.getTileEntityClass(),
						new BlockStackSrc( block, 0, ActivityState.Enabled ) );
			} );

			if( this.tileEntityDefinition != null )
			{
				this.factory.tileEntityComponent.addTileEntity( this.tileEntityDefinition );
			}

			return (T) new TileDefinition( this.registryName, (AEBaseTileBlock) block, item );
		}
		else
		{
			return (T) new BlockDefinition( this.registryName, block, item );
		}
	}

	@Nullable
	private ItemBlock constructItemFromBlock( Block block )
	{
		if( this.disableItem )
		{
			return null;
		}

		if( this.itemFactory != null )
		{
			return this.itemFactory.apply( block );
		}
		else if( block instanceof AEBaseBlock )
		{
			return new AEBaseItemBlock( block );
		}
		else
		{
			return new ItemBlock( block );
		}
	}
}
