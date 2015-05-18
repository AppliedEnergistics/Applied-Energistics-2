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

package appeng.recipes.game;


import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;


public class ShapelessRecipe implements IRecipe, IRecipeBakeable
{

	private final ArrayList<Object> input = new ArrayList<Object>();
	private ItemStack output = null;
	private boolean disable = false;

	public ShapelessRecipe( ItemStack result, Object... recipe )
	{
		this.output = result.copy();
		for( Object in : recipe )
		{
			if( in instanceof IIngredient )
			{
				this.input.add( in );
			}
			else
			{
				StringBuilder ret = new StringBuilder( "Invalid shapeless ore recipe: " );
				for( Object tmp : recipe )
				{
					ret.append( tmp ).append( ", " );
				}
				ret.append( this.output );
				throw new IllegalArgumentException( ret.toString() );
			}
		}
	}

	public boolean isEnabled()
	{
		return !this.disable;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean matches( InventoryCrafting var1, World world )
	{
		if( this.disable )
		{
			return false;
		}

		ArrayList<Object> required = new ArrayList<Object>( this.input );

		for( int x = 0; x < var1.getSizeInventory(); x++ )
		{
			ItemStack slot = var1.getStackInSlot( x );

			if( slot != null )
			{
				boolean inRecipe = false;

				for( Object next : required )
				{
					boolean match = false;

					if( next instanceof IIngredient )
					{
						try
						{
							for( ItemStack item : ( (IIngredient) next ).getItemStackSet() )
							{
								match = match || this.checkItemEquals( item, slot );
							}
						}
						catch( RegistrationError e )
						{
							// :P
						}
						catch( MissingIngredientError e )
						{
							// :P
						}
					}

					if( match )
					{
						inRecipe = true;
						required.remove( next );
						break;
					}
				}

				if( !inRecipe )
				{
					return false;
				}
			}
		}

		return required.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult( InventoryCrafting var1 )
	{
		return this.output.copy();
	}

	@Override
	public int getRecipeSize()
	{
		return this.input.size();
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	private boolean checkItemEquals( ItemStack target, ItemStack input )
	{
		return ( target.getItem() == input.getItem() && ( target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage() ) );
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 *
	 * @return The recipes input vales.
	 */
	public final ArrayList<Object> getInput()
	{
		return this.input;
	}

	@Override
	public void bake() throws RegistrationError
	{
		try
		{
			this.disable = false;
			for( Object o : this.input )
			{
				if( o instanceof IIngredient )
				{
					( (IIngredient) o ).bake();
				}
			}
		}
		catch( MissingIngredientError e )
		{
			this.disable = true;
		}
	}
}