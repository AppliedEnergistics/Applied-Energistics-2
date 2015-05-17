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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.ResolverResult;
import appeng.api.recipes.ResolverResultSet;


public class Ingredient implements IIngredient
{

	public final boolean isAir;

	public final String nameSpace;
	public final String itemName;
	public final int meta;
	public final int qty;
	NBTTagCompound nbt = null;
	ItemStack[] baked;

	public Ingredient( RecipeHandler handler, String input, int qty ) throws RecipeError, MissedIngredientSet
	{

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
		String[] parts = input.split( ":" );
		if( parts.length >= 2 )
		{
			this.nameSpace = handler.alias( parts[0] );
			String tmpName = handler.alias( parts[1] );

			if( parts.length != 3 )
			{
				int sel = 0;

				if( this.nameSpace.equals( "oreDictionary" ) )
				{
					if( parts.length == 3 )
					{
						throw new RecipeError( "Cannot specify meta when using ore dictionary." );
					}
					sel = OreDictionary.WILDCARD_VALUE;
				}
				else
				{
					try
					{
						Object ro = AEApi.instance().registries().recipes().resolveItem( this.nameSpace, tmpName );
						if( ro instanceof ResolverResult )
						{
							ResolverResult rr = (ResolverResult) ro;
							tmpName = rr.itemName;
							sel = rr.damageValue;
							this.nbt = rr.compound;
						}
						else if( ro instanceof ResolverResultSet )
						{
							throw new MissedIngredientSet( (ResolverResultSet) ro );
						}
					}
					catch( IllegalArgumentException e )
					{
						throw new RecipeError( tmpName + " is not a valid ae2 item definition." );
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
					catch( NumberFormatException e )
					{
						throw new RecipeError( "Invalid Metadata." );
					}
				}
			}
			this.itemName = tmpName;
		}
		else
		{
			throw new RecipeError( input + " : Needs at least Namespace and Name." );
		}

		handler.data.knownItem.add( this.toString() );
	}

	@Override
	public String toString()
	{
		return this.nameSpace + ':' + this.itemName + ':' + this.meta;
	}

	@Override
	public ItemStack getItemStack() throws RegistrationError, MissingIngredientError
	{
		if( this.isAir )
		{
			throw new RegistrationError( "Found blank item and expected a real item." );
		}

		if( this.nameSpace.equalsIgnoreCase( "oreDictionary" ) )
		{
			throw new RegistrationError( "Recipe format expected a single item, but got a set of items." );
		}

		Block blk = GameRegistry.findBlock( this.nameSpace, this.itemName );
		if( blk == null )
		{
			blk = GameRegistry.findBlock( this.nameSpace, "tile." + this.itemName );
		}

		if( blk != null )
		{
			Item it = Item.getItemFromBlock( blk );
			if( it != null )
			{
				return this.makeItemStack( it, this.qty, this.meta, this.nbt );
			}
		}

		Item it = GameRegistry.findItem( this.nameSpace, this.itemName );
		if( it == null )
		{
			it = GameRegistry.findItem( this.nameSpace, "item." + this.itemName );
		}

		if( it != null )
		{
			return this.makeItemStack( it, this.qty, this.meta, this.nbt );
		}

		/*
		 * Object o = Item.itemRegistry.getObject( nameSpace + ":" + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 *
		 * if ( o instanceof Block ) return new ItemStack( (Block) o, qty, meta );
		 *
		 * o = Item.itemRegistry.getObject( nameSpace + ":item." + itemName ); if ( o instanceof Item ) return new
		 * ItemStack( (Item) o, qty, meta );
		 *
		 * o = Block.blockRegistry.getObject( nameSpace + ":tile." + itemName ); if ( o instanceof Block && (!(o
		 * instanceof BlockAir)) ) return new ItemStack( (Block) o, qty, meta );
		 */

		throw new MissingIngredientError( "Unable to find item: " + this.toString() );
	}

	private ItemStack makeItemStack( Item it, int quantity, int damageValue, NBTTagCompound compound )
	{
		ItemStack is = new ItemStack( it, quantity, damageValue );
		is.setTagCompound( compound );
		return is;
	}

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationError, MissingIngredientError
	{
		if( this.baked != null )
		{
			return this.baked;
		}

		if( this.nameSpace.equalsIgnoreCase( "oreDictionary" ) )
		{
			List<ItemStack> ores = OreDictionary.getOres( this.itemName );
			ItemStack[] set = ores.toArray( new ItemStack[ores.size()] );

			// clone and set qty.
			for( int x = 0; x < set.length; x++ )
			{
				ItemStack is = set[x].copy();
				is.stackSize = this.qty;
				set[x] = is;
			}

			if( set.length == 0 )
			{
				throw new MissingIngredientError( this.itemName + " - ore dictionary could not be resolved to any items." );
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
	public void bake() throws RegistrationError, MissingIngredientError
	{
		this.baked = null;
		this.baked = this.getItemStackSet();
	}
}
