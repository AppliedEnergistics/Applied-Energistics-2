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
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IAlphaPassItem;
import appeng.block.solids.OreQuartz;
import appeng.client.render.BusRenderer;
import appeng.core.FacadeConfig;
import appeng.core.features.AEFeature;
import appeng.facade.FacadePart;
import appeng.facade.IFacadeItem;
import appeng.items.AEBaseItem;
import appeng.util.Platform;


public class ItemFacade extends AEBaseItem implements IFacadeItem, IAlphaPassItem
{

	private List<ItemStack> subTypes = null;

	public ItemFacade()
	{
		this.setFeature( EnumSet.of( AEFeature.Facades ) );
		this.setHasSubtypes( true );
		if( Platform.isClient() )
		{
			MinecraftForgeClient.registerItemRenderer( this, BusRenderer.INSTANCE );
		}
	}

	@Override
	@SideOnly( Side.CLIENT )
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public boolean onItemUse( ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ )
	{
		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public String getItemStackDisplayName( ItemStack is )
	{
		try
		{
			ItemStack in = this.getTextureItem( is );
			if( in != null )
			{
				return super.getItemStackDisplayName( is ) + " - " + in.getDisplayName();
			}
		}
		catch( Throwable ignored )
		{

		}

		return super.getItemStackDisplayName( is );
	}

	@Override
	protected void getCheckedSubItems( Item sameItem, CreativeTabs creativeTab, List<ItemStack> itemStacks )
	{
		this.calculateSubTypes();
		itemStacks.addAll( this.subTypes );
	}

	private void calculateSubTypes()
	{
		if( this.subTypes == null )
		{
			this.subTypes = new ArrayList<ItemStack>( 1000 );
			for( Object blk : Block.blockRegistry )
			{
				Block b = (Block) blk;
				try
				{
					Item item = Item.getItemFromBlock( b );

					List<ItemStack> tmpList = new ArrayList<ItemStack>( 100 );
					b.getSubBlocks( item, b.getCreativeTabToDisplayOn(), tmpList );
					for( ItemStack l : tmpList )
					{
						ItemStack facade = this.createFacadeForItem( l, false );
						if( facade != null )
						{
							this.subTypes.add( facade );
						}
					}
				}
				catch( Throwable t )
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

	public ItemStack createFacadeForItem( ItemStack l, boolean returnItem )
	{
		if( l == null )
		{
			return null;
		}

		Block b = Block.getBlockFromItem( l.getItem() );
		if( b == null || l.hasTagCompound() )
		{
			return null;
		}

		int metadata = l.getItem().getMetadata( l.getItemDamage() );

		boolean hasTile = b.hasTileEntity( metadata );
		boolean enableGlass = b instanceof BlockGlass || b instanceof BlockStainedGlass;
		boolean disableOre = b instanceof OreQuartz;

		boolean defaultValue = ( b.isOpaqueCube() && !b.getTickRandomly() && !hasTile && !disableOre ) || enableGlass;
		if( FacadeConfig.instance.checkEnabled( b, metadata, defaultValue ) )
		{
			if( returnItem )
			{
				return l;
			}

			ItemStack is = new ItemStack( this );
			NBTTagCompound data = new NBTTagCompound();
			int[] ds = new int[2];
			ds[0] = Item.getIdFromItem( l.getItem() );
			ds[1] = metadata;
			data.setIntArray( "x", ds );
			UniqueIdentifier ui = GameRegistry.findUniqueIdentifierFor( l.getItem() );
			data.setString( "modid", ui.modId );
			data.setString( "itemname", ui.name );
			is.setTagCompound( data );
			return is;
		}
		return null;
	}

	@Override
	public FacadePart createPartFromItemStack( ItemStack is, ForgeDirection side )
	{
		ItemStack in = this.getTextureItem( is );
		if( in != null )
		{
			return new FacadePart( is, side );
		}
		return null;
	}

	@Override
	public ItemStack getTextureItem( ItemStack is )
	{
		Block blk = this.getBlock( is );
		if( blk != null )
		{
			return new ItemStack( blk, 1, this.getMeta( is ) );
		}
		return null;
	}

	@Override
	public int getMeta( ItemStack is )
	{
		NBTTagCompound data = is.getTagCompound();
		if( data != null )
		{
			int[] blk = data.getIntArray( "x" );
			if( blk != null && blk.length == 2 )
			{
				return blk[1];
			}
		}
		return 0;
	}

	@Override
	public Block getBlock( ItemStack is )
	{
		NBTTagCompound data = is.getTagCompound();
		if( data != null )
		{
			if( data.hasKey( "modid" ) && data.hasKey( "itemname" ) )
			{
				return GameRegistry.findBlock( data.getString( "modid" ), data.getString( "itemname" ) );
			}
			else
			{
				int[] blk = data.getIntArray( "x" );
				if( blk != null && blk.length == 2 )
				{
					return Block.getBlockById( blk[0] );
				}
			}
		}
		return Blocks.glass;
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
			return new ItemStack( Items.cake );
		}
		return this.subTypes.get( 0 );
	}

	public ItemStack createFromIDs( int[] ids )
	{
		for( ItemStack facadeStack : AEApi.instance().definitions().items().facade().maybeStack( 1 ).asSet() )
		{
			NBTTagCompound facadeTag = new NBTTagCompound();
			facadeTag.setIntArray( "x", ids.clone() );
			facadeStack.setTagCompound( facadeTag );

			return facadeStack;
		}

		throw new MissingDefinition( "Tried to create a facade, while facades are being deactivated." );
	}

	@Override
	public boolean useAlphaPass( ItemStack is )
	{
		ItemStack out = this.getTextureItem( is );

		if( out == null || out.getItem() == null )
		{
			return false;
		}

		Block blk = Block.getBlockFromItem( out.getItem() );
		if( blk != null && blk.canRenderInPass( 1 ) )
		{
			return true;
		}

		return false;
	}
}
