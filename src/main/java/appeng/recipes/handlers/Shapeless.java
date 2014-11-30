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

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.core.AELog;
import appeng.recipes.RecipeHandler;
import appeng.recipes.game.ShapelessRecipe;
import appeng.util.Platform;

public class Shapeless implements ICraftHandler, IWebsiteSerializer
{

	List<IIngredient> inputs;
	IIngredient output;

	@Override
	public void setup(List<List<IIngredient>> input, List<List<IIngredient>> output) throws RecipeError
	{
		if ( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if ( input.size() == 1 )
			{
				inputs = input.get( 0 );
				this.output = output.get( 0 ).get( 0 );
			}
			else
				throw new RecipeError( "Shapeless crafting recipes cannot have rows." );
		}
		else
			throw new RecipeError( "Crafting must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		List<Object> args = new ArrayList<Object>();
		for (IIngredient i : inputs)
			args.add( i );

		ItemStack outIS = output.getItemStack();

		try
		{
			GameRegistry.addRecipe( new ShapelessRecipe( outIS, args.toArray( new Object[args.size()] ) ) );
		}
		catch (Throwable e)
		{
			AELog.error( e );
			throw new RegistrationError( "Error while adding shapeless recipe." );
		}
	}

	@Override
	public boolean canCraft(ItemStack reqOutput) throws RegistrationError, MissingIngredientError
	{

		for (IIngredient i : inputs)
		{
			if ( !i.isAir() )
			{
				for (ItemStack r : i.getItemStackSet())
				{
					if ( Platform.isSameItemPrecise( r, reqOutput ) )
					{
						return false;
					}
				}
			}
		}

		return Platform.isSameItemPrecise( output.getItemStack(), reqOutput );
	}

	@Override
	public String getPattern(RecipeHandler h)
	{
		StringBuilder o = new StringBuilder( "shapeless " + output.getQty() + '\n' );

		o.append( h.getName( output ) ).append( '\n' );

		for (int y = 0; y < inputs.size(); y++)
		{
			IIngredient i = inputs.get( y );

			if ( i.isAir() )
			{
				o.append( "air" );
			}
			else
			{
				o.append( h.getName( i ) );
			}

			if ( y + 1 == this.inputs.size() )
			{
				o.append( '\n' );
			}
			else
			{
				o.append( ' ' );
			}
		}

		return o.toString().trim();
	}

}
