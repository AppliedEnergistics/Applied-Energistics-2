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


import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;
import appeng.recipes.game.ShapedRecipe;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class Shaped implements ICraftHandler, IWebsiteSerializer
{

	private List<List<IIngredient>> inputs;
	private IIngredient output;
	private int rows;
	private int cols;

	@Override
	public void setup( final List<List<IIngredient>> input, final List<List<IIngredient>> output ) throws RecipeError
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
							throw new RecipeError( "all rows in a shaped crafting recipe must contain the same number of ingredients." );
						}
					}

					this.inputs = input;
					this.output = output.get( 0 ).get( 0 );
				}
				else
				{
					throw new RecipeError( "Crafting recipes must have 1-3 columns." );
				}
			}
			else
			{
				throw new RecipeError( "shaped crafting recipes must have 1-3 rows." );
			}
		}
		else
		{
			throw new RecipeError( "Crafting must produce a single output." );
		}
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		char first = 'A';
		final List<Object> args = new ArrayList<Object>();

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
			GameRegistry.addRecipe( new ShapedRecipe( outIS, args.toArray( new Object[args.size()] ) ) );
		}
		catch( final Throwable e )
		{
			AELog.debug( e );
			throw new RegistrationError( "Error while adding shaped recipe." );
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
	public boolean canCraft( final ItemStack reqOutput ) throws RegistrationError, MissingIngredientError
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
						if( Platform.isSameItemPrecise( r, reqOutput ) )
						{
							return false;
						}
					}
				}
			}
		}

		return Platform.isSameItemPrecise( this.output.getItemStack(), reqOutput );
	}
}
