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

package appeng.recipes.handlers;


import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;


/**
 * basic inscriber process for recipes
 *
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public abstract class InscriberProcess implements ICraftHandler, IWebsiteSerializer
{
	@Nullable
	private IIngredient imprintable;

	@Nullable
	private IIngredient topOptional;

	@Nullable
	private IIngredient botOptional;

	@Nullable
	private IIngredient output;

	@Override
	public void setup( final List<List<IIngredient>> input, final List<List<IIngredient>> output ) throws RecipeError
	{
		if( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if( input.size() == 1 && input.get( 0 ).size() > 1 )
			{
				this.imprintable = input.get( 0 ).get( 0 );

				this.topOptional = input.get( 0 ).get( 1 );

				if( input.get( 0 ).size() > 2 )
				{
					this.botOptional = input.get( 0 ).get( 2 );
				}

				this.output = output.get( 0 ).get( 0 );
			}
			else
			{
				throw new RecipeError( "Inscriber recipes cannot have rows, and must have more then one input." );
			}
		}
		else
		{
			throw new RecipeError( "Inscriber recipes must produce a single output." );
		}
	}

	@Override
	public boolean canCraft( final ItemStack reqOutput ) throws RegistrationError, MissingIngredientError
	{
		return this.output != null && Platform.itemComparisons().isSameItem( this.output.getItemStack(), reqOutput );
	}

	@Override
	public String getPattern( final RecipeHandler handler )
	{
		String pattern = "inscriber ";

		if( this.output != null )
		{
			pattern += this.output.getQty() + '\n';
			pattern += handler.getName( this.output ) + '\n';
		}

		if( this.topOptional != null )
		{
			pattern += handler.getName( this.topOptional ) + '\n';
		}

		if( this.imprintable != null )
		{
			pattern += handler.getName( this.imprintable );
		}

		if( this.botOptional != null )
		{
			pattern += '\n' + handler.getName( this.botOptional );
		}

		return pattern;
	}

	@Nullable
	protected IIngredient getImprintable()
	{
		return this.imprintable;
	}

	@Nullable
	protected IIngredient getTopOptional()
	{
		return this.topOptional;
	}

	@Nullable
	protected IIngredient getBotOptional()
	{
		return this.botOptional;
	}

	@Nullable
	protected IIngredient getOutput()
	{
		return this.output;
	}
}
