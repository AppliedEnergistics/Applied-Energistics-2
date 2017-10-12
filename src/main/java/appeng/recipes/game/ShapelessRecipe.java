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

package appeng.recipes.game;


import java.util.ArrayList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.exceptions.MissingIngredientException;
import appeng.api.exceptions.RegistrationException;
import appeng.api.recipes.IIngredient;


public class ShapelessRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe, IRecipeBakeable
{

	private final ArrayList<Object> input = new ArrayList<>();
	private ItemStack output = ItemStack.EMPTY;
	private boolean disable = false;

	public ShapelessRecipe( final ItemStack result, final Object... recipe )
	{
		this.output = result.copy();
		for( final Object in : recipe )
		{
			if( in instanceof IIngredient )
			{
				this.input.add( in );
			}
			else
			{
				final StringBuilder ret = new StringBuilder( "Invalid shapeless ore recipe: " );
				for( final Object tmp : recipe )
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
	public boolean matches( final InventoryCrafting var1, final World world )
	{
		if( this.disable )
		{
			return false;
		}

		final ArrayList<Object> required = new ArrayList<>( this.input );

		for( int x = 0; x < var1.getSizeInventory(); x++ )
		{
			final ItemStack slot = var1.getStackInSlot( x );

			if( !slot.isEmpty() )
			{
				boolean inRecipe = false;

				for( final Object next : required )
				{
					boolean match = false;

					if( next instanceof IIngredient )
					{
						try
						{
							for( final ItemStack item : ( (IIngredient) next ).getItemStackSet() )
							{
								match = match || this.checkItemEquals( item, slot );
							}
						}
						catch( final RegistrationException e )
						{
							// :P
						}
						catch( final MissingIngredientException e )
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
	public ItemStack getCraftingResult( final InventoryCrafting var1 )
	{
		return this.output.copy();
	}

	@Override
	public boolean canFit( int i, int i1 )
	{
		return false;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return this.output;
	}

	private boolean checkItemEquals( final ItemStack target, final ItemStack input )
	{
		return( target.getItem() == input
				.getItem() && ( target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage() ) );
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 *
	 * @return The recipes input vales.
	 */
	public ArrayList<Object> getInput()
	{
		return this.input;
	}

	@Override
	public void bake() throws RegistrationException
	{
		try
		{
			this.disable = false;
			for( final Object o : this.input )
			{
				if( o instanceof IIngredient )
				{
					( (IIngredient) o ).bake();
				}
			}
		}
		catch( final MissingIngredientException e )
		{
			this.disable = true;
		}
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems( final InventoryCrafting inv )
	{
		return ForgeHooks.defaultRecipeGetRemainingItems( inv );
	}
}