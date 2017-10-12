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

package appeng.recipes.handlers;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.api.exceptions.MissingIngredientException;
import appeng.api.exceptions.RecipeException;
import appeng.api.exceptions.RegistrationException;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;


public class Shaped implements ICraftHandler, IWebsiteSerializer
{

	private List<List<IIngredient>> inputs;
	private IIngredient output;
	private int rows;
	private int cols;

	@Override
	public void setup( final List<List<IIngredient>> input, final List<List<IIngredient>> output ) throws RecipeException
	{
		if( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			this.rows = input.size();
			if( this.rows > 0 && input.size() <= 3 )
			{
				this.cols = input.get( 0 ).size();
				if( this.cols <= 3 && this.cols >= 1 )
				{
					for( final List<IIngredient> anInput : input )
					{
						if( anInput.size() != this.cols )
						{
							throw new RecipeException( "all rows in a shaped crafting recipe must contain the same number of ingredients." );
						}
					}

					this.inputs = input;
					this.output = output.get( 0 ).get( 0 );
				}
				else
				{
					throw new RecipeException( "Crafting recipes must have 1-3 columns." );
				}
			}
			else
			{
				throw new RecipeException( "shaped crafting recipes must have 1-3 rows." );
			}
		}
		else
		{
			throw new RecipeException( "Crafting must produce a single output." );
		}
	}

	@Override
	public void register() throws RegistrationException, MissingIngredientException
	{
		char first = 'A';
		final List<Object> args = new ArrayList<>();

		for( int y = 0; y < this.rows; y++ )
		{
			final StringBuilder row = new StringBuilder();
			for( int x = 0; x < this.cols; x++ )
			{
				if( this.inputs.get( y ).get( x ).isAir() )
				{
					row.append( ' ' );
				}
				else
				{
					row.append( first );
					args.add( first );
					args.add( this.inputs.get( y ).get( x ) );

					first++;
				}
			}
			args.add( y, row.toString() );
		}

		final ItemStack outIS = this.output.getItemStack();

		try
		{
			// TODO : 1.12 Cleanup/remove the old recipe loader.
			// Registration.addRecipeToRegister( new ShapedRecipe( outIS, args.toArray( new Object[args.size()] ) ) );
		}
		catch( final Throwable e )
		{
			AELog.debug( e );
			throw new RegistrationException( "Error while adding shaped recipe." );
		}
	}

	@Override
	public String getPattern( final RecipeHandler h )
	{
		String o = "shaped " + this.output.getQty() + ' ' + this.cols + 'x' + this.rows + '\n';

		o += h.getName( this.output ) + '\n';

		for( int y = 0; y < this.rows; y++ )
		{
			for( int x = 0; x < this.cols; x++ )
			{
				final IIngredient i = this.inputs.get( y ).get( x );

				if( i.isAir() )
				{
					o += "air" + ( x + 1 == this.cols ? "\n" : " " );
				}
				else
				{
					o += h.getName( i ) + ( x + 1 == this.cols ? "\n" : " " );
				}
			}
		}

		return o.trim();
	}

	@Override
	public boolean canCraft( final ItemStack reqOutput ) throws RegistrationException, MissingIngredientException
	{
		for( int y = 0; y < this.rows; y++ )
		{
			for( int x = 0; x < this.cols; x++ )
			{
				final IIngredient i = this.inputs.get( y ).get( x );

				if( !i.isAir() )
				{
					for( final ItemStack r : i.getItemStackSet() )
					{
						if( Platform.itemComparisons().isSameItem( r, reqOutput ) )
						{
							return false;
						}
					}
				}
			}
		}

		return Platform.itemComparisons().isSameItem( this.output.getItemStack(), reqOutput );
	}
}
