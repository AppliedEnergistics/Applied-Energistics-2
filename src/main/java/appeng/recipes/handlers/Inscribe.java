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


import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;


public class Inscribe implements ICraftHandler, IWebsiteSerializer
{

	public static final HashSet<ItemStack> PLATES = new HashSet<ItemStack>();
	public static final HashSet<ItemStack> INPUTS = new HashSet<ItemStack>();
	public static final LinkedList<InscriberRecipe> RECIPES = new LinkedList<InscriberRecipe>();
	public boolean usePlates = false;
	IIngredient imprintable;
	IIngredient plateA;
	IIngredient plateB;
	IIngredient output;

	@Override
	public void setup( List<List<IIngredient>> input, List<List<IIngredient>> output ) throws RecipeError
	{
		if( output.size() == 1 && output.get( 0 ).size() == 1 )
		{
			if( input.size() == 1 && input.get( 0 ).size() > 1 )
			{
				this.imprintable = input.get( 0 ).get( 0 );

				this.plateA = input.get( 0 ).get( 1 );

				if( input.get( 0 ).size() > 2 )
					this.plateB = input.get( 0 ).get( 2 );

				this.output = output.get( 0 ).get( 0 );
			}
			else
				throw new RecipeError( "Inscriber recipes cannot have rows, and must have more then one input." );
		}
		else
			throw new RecipeError( "Inscriber recipes must produce a single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if( this.imprintable != null )
			Collections.addAll( INPUTS, this.imprintable.getItemStackSet() );

		if( this.plateA != null )
			Collections.addAll( PLATES, this.plateA.getItemStackSet() );

		if( this.plateB != null )
			Collections.addAll( PLATES, this.plateB.getItemStackSet() );

		InscriberRecipe ir = new InscriberRecipe( this.imprintable.getItemStackSet(), this.plateA == null ? null : this.plateA.getItemStack(), this.plateB == null ? null : this.plateB.getItemStack(), this.output.getItemStack(), this.usePlates );
		RECIPES.add( ir );
	}

	@Override
	public String getPattern( RecipeHandler h )
	{
		String o = "inscriber " + this.output.getQty() + '\n';

		o += h.getName( this.output ) + '\n';

		if( this.plateA != null )
			o += h.getName( this.plateA ) + '\n';

		o += h.getName( this.imprintable );

		if( this.plateB != null )
			o += '\n' + h.getName( this.plateB );

		return o;
	}

	@Override
	public boolean canCraft( ItemStack reqOutput ) throws RegistrationError, MissingIngredientError
	{
		return Platform.isSameItemPrecise( this.output.getItemStack(), reqOutput );
	}


	public static class InscriberRecipe
	{

		final public boolean usePlates;
		final public ItemStack plateA;
		final public ItemStack[] imprintable;
		final public ItemStack plateB;
		final public ItemStack output;
		public InscriberRecipe( ItemStack[] imprintable, ItemStack plateA, ItemStack plateB, ItemStack out, boolean usePlates )
		{
			this.imprintable = imprintable;
			this.usePlates = usePlates;
			this.plateA = plateA;
			this.plateB = plateB;
			this.output = out;
		}
	}
}
