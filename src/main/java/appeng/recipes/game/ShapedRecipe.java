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
import java.util.HashMap;
import java.util.Map;

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


public class ShapedRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe, IRecipeBakeable
{
	// Added in for future ease of change, but hard coded for now.
	private static final int MAX_CRAFT_GRID_WIDTH = 3;
	private static final int MAX_CRAFT_GRID_HEIGHT = 3;

	private ItemStack output = ItemStack.EMPTY;
	private Object[] input = null;
	private int width = 0;
	private int height = 0;
	private boolean mirrored = true;
	private boolean disable = false;

	public ShapedRecipe( final ItemStack result, Object... recipe )
	{
		this.output = result.copy();

		final StringBuilder shape = new StringBuilder();
		int idx = 0;

		if( recipe[idx] instanceof Boolean )
		{
			this.mirrored = (Boolean) recipe[idx];
			if( recipe[idx + 1] instanceof Object[] )
			{
				recipe = (Object[]) recipe[idx + 1];
			}
			else
			{
				idx = 1;
			}
		}

		if( recipe[idx] instanceof String[] )
		{
			final String[] parts = ( (String[]) recipe[idx] );
			idx++;

			for( final String s : parts )
			{
				this.width = s.length();
				shape.append( s );
			}

			this.height = parts.length;
		}
		else
		{
			while( recipe[idx] instanceof String )
			{
				final String s = (String) recipe[idx];
				idx++;
				shape.append( s );
				this.width = s.length();
				this.height++;
			}
		}

		if( this.width * this.height != shape.length() )
		{
			final StringBuilder ret = new StringBuilder( "Invalid shaped ore recipe: " );
			for( final Object tmp : recipe )
			{
				ret.append( tmp ).append( ", " );
			}
			ret.append( this.output );
			throw new IllegalStateException( ret.toString() );
		}

		final Map<Character, IIngredient> itemMap = new HashMap<>();

		for( ; idx < recipe.length; idx += 2 )
		{
			final Character chr = (Character) recipe[idx];
			final Object in = recipe[idx + 1];

			if( in instanceof IIngredient )
			{
				itemMap.put( chr, (IIngredient) in );
			}
			else
			{
				final StringBuilder ret = new StringBuilder( "Invalid shaped ore recipe: " );
				for( final Object tmp : recipe )
				{
					ret.append( tmp ).append( ", " );
				}
				ret.append( this.output );
				throw new IllegalStateException( ret.toString() );
			}
		}

		this.input = new Object[this.width * this.height];
		int x = 0;
		for( final char chr : shape.toString().toCharArray() )
		{
			this.input[x] = itemMap.get( chr );
			x++;
		}
	}

	public boolean isEnabled()
	{
		return !this.disable;
	}

	@Override
	public boolean matches( final InventoryCrafting inv, final World world )
	{
		if( this.disable )
		{
			return false;
		}

		for( int x = 0; x <= MAX_CRAFT_GRID_WIDTH - this.width; x++ )
		{
			for( int y = 0; y <= MAX_CRAFT_GRID_HEIGHT - this.height; ++y )
			{
				if( this.checkMatch( inv, x, y, false ) )
				{
					return true;
				}

				if( this.mirrored && this.checkMatch( inv, x, y, true ) )
				{
					return true;
				}
			}
		}

		return false;
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

	@SuppressWarnings( "unchecked" )
	private boolean checkMatch( final InventoryCrafting inv, final int startX, final int startY, final boolean mirror )
	{
		if( this.disable )
		{
			return false;
		}

		for( int x = 0; x < MAX_CRAFT_GRID_WIDTH; x++ )
		{
			for( int y = 0; y < MAX_CRAFT_GRID_HEIGHT; y++ )
			{
				final int subX = x - startX;
				final int subY = y - startY;
				Object target = null;

				if( subX >= 0 && subY >= 0 && subX < this.width && subY < this.height )
				{
					if( mirror )
					{
						target = this.input[this.width - subX - 1 + subY * this.width];
					}
					else
					{
						target = this.input[subX + subY * this.width];
					}
				}

				final ItemStack slot = inv.getStackInRowAndColumn( x, y );

				if( target instanceof IIngredient )
				{
					boolean matched = false;

					try
					{
						for( final ItemStack item : ( (IIngredient) target ).getItemStackSet() )
						{
							matched = matched || this.checkItemEquals( item, slot );
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

					if( !matched )
					{
						return false;
					}
				}
				else if( target instanceof ArrayList )
				{
					boolean matched = false;

					for( final ItemStack item : (Iterable<ItemStack>) target )
					{
						matched = matched || this.checkItemEquals( item, slot );
					}

					if( !matched )
					{
						return false;
					}
				}
				else if( target == null && !slot.isEmpty() )
				{
					return false;
				}
			}
		}

		return true;
	}

	private boolean checkItemEquals( final ItemStack target, final ItemStack input )
	{
		if( input.isEmpty() && !target.isEmpty() || !input.isEmpty() && target.isEmpty() )
		{
			return false;
		}
		return( target.getItem() == input
				.getItem() && ( target.getItemDamage() == OreDictionary.WILDCARD_VALUE || target.getItemDamage() == input.getItemDamage() ) );
	}

	public ShapedRecipe setMirrored( final boolean mirror )
	{
		this.mirrored = mirror;
		return this;
	}

	/**
	 * Returns the input for this recipe, any mod accessing this value should never manipulate the values in this array
	 * as it will effect the recipe itself.
	 *
	 * @return The recipes input vales.
	 */
	public Object[] getInput()
	{
		return this.input;
	}

	public int getWidth()
	{
		return this.width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public Object[] getIIngredients()
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
		catch( final MissingIngredientException err )
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