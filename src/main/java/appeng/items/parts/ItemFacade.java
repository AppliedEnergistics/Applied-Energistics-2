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

package appeng.items.parts;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IAlphaPassItem;
import appeng.api.util.AEPartLocation;
import appeng.core.FacadeConfig;
import appeng.core.features.AEFeature;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;


public class ItemFacade extends AEBaseItem implements IFacadeItem, IAlphaPassItem
{

	private List<ItemStack> subTypes = null;

	public ItemFacade()
	{
		this.setFeature( EnumSet.of( AEFeature.Facades ) );
		this.setHasSubtypes( true );
	}

	@Override
	public EnumActionResult onItemUseFirst( final ItemStack is, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final EnumHand hand )
	{
		return AEApi.instance().partHelper().placeBus( is, pos, side, player, hand, world );
	}

	@Override
	public String getItemStackDisplayName( final ItemStack is )
	{
		try
		{
			final ItemStack in = this.getTextureItem( is );
			if( in != null )
			{
				return super.getItemStackDisplayName( is ) + " - " + in.getDisplayName();
			}
		}
		catch( final Throwable ignored )
		{

		}

		return super.getItemStackDisplayName( is );
	}

	@Override
	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		this.calculateSubTypes();
		itemStacks.addAll( this.subTypes );
	}

	private void calculateSubTypes()
	{
		if( this.subTypes == null )
		{
			this.subTypes = new ArrayList<ItemStack>( 1000 );
			for( final Object blk : Block.REGISTRY )
			{
				final Block b = (Block) blk;
				try
				{
					final Item item = Item.getItemFromBlock( b );

					final List<ItemStack> tmpList = new ArrayList<ItemStack>( 100 );
					b.getSubBlocks( item, b.getCreativeTabToDisplayOn(), tmpList );
					for( final ItemStack l : tmpList )
					{
						final ItemStack facade = this.createFacadeForItem( l, false );
						if( facade != null )
						{
							this.subTypes.add( facade );
						}
					}
				}
				catch( final Throwable t )
				{
					// just absorb..
				}
			}

			if( FacadeConfig.instance.hasChanged() )
			{
				FacadeConfig.instance.save();
			}
		}
	}

	public ItemStack createFacadeForItem( final ItemStack l, final boolean returnItem )
	{
		if( l == null )
		{
			return null;
		}

		final Block b = Block.getBlockFromItem( l.getItem() );
		if( b == null || l.hasTagCompound() )
		{
			return null;
		}

		final int metadata = l.getItem().getMetadata( l.getItemDamage() );

		// TODO 1.10.2-R - XD
		final boolean defaultValue = true || b instanceof BlockGlass || b instanceof BlockStainedGlass;
		if( FacadeConfig.instance.checkEnabled( b, metadata, defaultValue ) )
		{
			if( returnItem )
			{
				return l;
			}

			final ItemStack is = new ItemStack( this );
			final NBTTagCompound data = new NBTTagCompound();
			final int[] ds = new int[2];
			ds[0] = Item.getIdFromItem( l.getItem() );
			ds[1] = metadata;
			data.setIntArray( "x", ds );
			final ResourceLocation ui = Item.REGISTRY.getNameForObject( l.getItem() );
			data.setString( "modid", ui.getResourceDomain() );
			data.setString( "itemname", ui.getResourcePath() );
			is.setTagCompound( data );
			return is;
		}
		return null;
	}

	@Override
	public FacadePart createPartFromItemStack( final ItemStack is, final AEPartLocation side )
	{
		final ItemStack in = this.getTextureItem( is );
		if( in != null )
		{
			return new FacadePart( is, side );
		}
		return null;
	}

	@Override
	public ItemStack getTextureItem( final ItemStack is )
	{
		final Block blk = this.getBlock( is );
		if( blk != null )
		{
			return new ItemStack( blk, 1, this.getMeta( is ) );
		}
		return null;
	}

	@Override
	public int getMeta( final ItemStack is )
	{
		final NBTTagCompound data = is.getTagCompound();
		if( data != null )
		{
			final int[] blk = data.getIntArray( "x" );
			if( blk != null && blk.length == 2 )
			{
				return blk[1];
			}
		}
		return 0;
	}

	@Override
	public Block getBlock( final ItemStack is )
	{
		final NBTTagCompound data = is.getTagCompound();
		if( data != null )
		{
			if( data.hasKey( "modid" ) && data.hasKey( "itemname" ) )
			{
				if( data.getString( "modid" ).equals( "minecraft" ) )
				{
					return Block.getBlockFromName( data.getString( "itemname" ) );
				}

				return GameRegistry.findBlock( data.getString( "modid" ), data.getString( "itemname" ) );
			}
			else
			{
				final int[] blk = data.getIntArray( "x" );
				if( blk != null && blk.length == 2 )
				{
					return Block.getBlockById( blk[0] );
				}
			}
		}
		return Blocks.GLASS;
	}

	public List<ItemStack> getFacades()
	{
		this.calculateSubTypes();
		return this.subTypes;
	}

	public ItemStack getCreativeTabIcon()
	{
		this.calculateSubTypes();
		if( this.subTypes.isEmpty() )
		{
			return new ItemStack( Items.CAKE );
		}
		return this.subTypes.get( 0 );
	}

	public ItemStack createFromIDs( final int[] ids )
	{
		for( final ItemStack facadeStack : AEApi.instance().definitions().items().facade().maybeStack( 1 ).asSet() )
		{
			final NBTTagCompound facadeTag = new NBTTagCompound();
			facadeTag.setIntArray( "x", ids.clone() );
			facadeStack.setTagCompound( facadeTag );

			return facadeStack;
		}

		throw new MissingDefinition( "Tried to create a facade, while facades are being deactivated." );
	}

	@Override
	public boolean useAlphaPass( final ItemStack is )
	{
		final ItemStack out = this.getTextureItem( is );

		if( out == null || out.getItem() == null )
		{
			return false;
		}

		final Block blk = Block.getBlockFromItem( out.getItem() );
		if( blk != null && blk.canRenderInLayer( BlockRenderLayer.TRANSLUCENT ) )
		{
			return true;
		}

		return false;
	}
}
