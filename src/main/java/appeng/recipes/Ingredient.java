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

package appeng.recipes;


import java.util.List;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingIngredientException;
import appeng.api.exceptions.RecipeException;
import appeng.api.exceptions.RegistrationException;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;


public class Ingredient implements IIngredient
{

	private final boolean isAir;
	private final String nameSpace;
	private final String itemName;
	private final int meta;
	private final int qty;
	private NBTTagCompound nbt = null;
	private ItemStack[] baked;

	public Ingredient( final RecipeHandler handler, final String input, final int qty ) throws RecipeException, MissedIngredientSet
	{
		Preconditions.checkNotNull( handler );
		Preconditions.checkNotNull( input );
		Preconditions.checkState( qty > 0 );

		// works no matter wat!
		this.qty = qty;

		if( input.equals( "_" ) )
		{
			this.isAir = true;
			this.nameSpace = "";
			this.itemName = "";
			this.meta = OreDictionary.WILDCARD_VALUE;
			return;
		}

		this.isAir = false;
		final String[] parts = input.split( ":" );
		if( parts.length >= 2 )
		{
			this.nameSpace = handler.alias( parts[0] );
			String tmpName = handler.alias( parts[1] );

			if( parts.length != 3 )
			{
				int sel = 0;

				if( this.nameSpace.equalsIgnoreCase( "oreDictionary" ) )
				{
					if( parts.length == 3 )
					{
						throw new RecipeException( "Cannot specify meta when using ore dictionary." );
					}
					sel = OreDictionary.WILDCARD_VALUE;
				}
				else
				{
					try
					{
						final Object ro = AEApi.instance().registries().recipes().resolveItem( this.nameSpace, tmpName );
						if( ro instanceof ResolverResult )
						{
							final ResolverResult rr = (ResolverResult) ro;
							tmpName = rr.itemName;
							sel = rr.damageValue;
							this.nbt = rr.compound;
						}
						else if( ro instanceof ResolverResultSet )
						{
							throw new MissedIngredientSet( (ResolverResultSet) ro );
						}
					}
					catch( final IllegalArgumentException e )
					{
						throw new RecipeException( tmpName + " is not a valid ae2 item definition." );
					}
				}

				this.meta = sel;
			}
			else
			{
				if( parts[2].equals( "*" ) )
				{
					this.meta = OreDictionary.WILDCARD_VALUE;
				}
				else
				{
					try
					{
						this.meta = Integer.parseInt( parts[2] );
					}
					catch( final NumberFormatException e )
					{
						throw new RecipeException( "Invalid Metadata." );
					}
				}
			}
			this.itemName = tmpName;
		}
		else
		{
			throw new RecipeException( input + " : Needs at least Namespace and Name." );
		}

		handler.getData().knownItem.add( this.toString() );
	}

	@Override
	public String toString()
	{
		return this.nameSpace + ':' + this.itemName + ':' + this.meta;
	}

	@Override
	public ItemStack getItemStack() throws RegistrationException, MissingIngredientException
	{
		if( this.isAir )
		{
			throw new RegistrationException( "Found blank item and expected a real item." );
		}

		if( this.nameSpace.equalsIgnoreCase( "oreDictionary" ) )
		{
			throw new RegistrationException( "Recipe format expected a single item, but got a set of items." );
		}

		Block blk = Block.getBlockFromName( this.nameSpace + ":" + this.itemName );
		if( blk == null )
		{
			blk = Block.getBlockFromName( this.nameSpace + ":" + "tile." + this.itemName );
		}

		if( blk != null )
		{
			final Item it = Item.getItemFromBlock( blk );
			if( it != Items.AIR )
			{
				return this.makeItemStack( it, this.qty, this.meta, this.nbt );
			}
		}

		Item it = Item.getByNameOrId( this.nameSpace + ":" + this.itemName );
		if( it == null )
		{
			it = Item.getByNameOrId( this.nameSpace + ":" + "item." + this.itemName );
		}

		if( it != null )
		{
			return this.makeItemStack( it, this.qty, this.meta, this.nbt );
		}

		/*
		 * Object o = Item.itemRegistry.getObject( nameSpace + ":" + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 * if ( o instanceof Block ) return new ItemStack( (Block) o, qty, meta );
		 * o = Item.itemRegistry.getObject( nameSpace + ":item." + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 * o = Block.blockRegistry.getObject( nameSpace + ":tile." + itemName ); if ( o instanceof Block && (!(o
		 * instanceof BlockAir)) ) return new ItemStack( (Block) o, qty, meta );
		 */

		throw new MissingIngredientException( "Unable to find item: " + this.toString() );
	}

	private ItemStack makeItemStack( final Item it, final int quantity, final int damageValue, final NBTTagCompound compound )
	{
		final ItemStack is = new ItemStack( it, quantity, damageValue );
		is.setTagCompound( compound );
		return is;
	}

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationException, MissingIngredientException
	{
		if( this.baked != null )
		{
			return this.baked;
		}

		if( this.nameSpace.equalsIgnoreCase( "oreDictionary" ) )
		{
			final List<ItemStack> ores = OreDictionary.getOres( this.itemName );
			final ItemStack[] set = ores.toArray( new ItemStack[ores.size()] );

			// clone and set qty.
			for( int x = 0; x < set.length; x++ )
			{
				final ItemStack is = set[x].copy();
				is.setCount( this.qty );
				set[x] = is;
			}

			if( set.length == 0 )
			{
				throw new MissingIngredientException( this.itemName + " - ore dictionary could not be resolved to any items." );
			}

			return set;
		}

		return new ItemStack[] { this.getItemStack() };
	}

	@Override
	public String getNameSpace()
	{
		return this.nameSpace;
	}

	@Override
	public String getItemName()
	{
		return this.itemName;
	}

	@Override
	public int getDamageValue()
	{
		return this.meta;
	}

	@Override
	public int getQty()
	{
		return this.qty;
	}

	@Override
	public boolean isAir()
	{
		return this.isAir;
	}

	@Override
	public void bake() throws RegistrationException, MissingIngredientException
	{
		this.baked = null;
		this.baked = this.getItemStackSet();
	}
}
