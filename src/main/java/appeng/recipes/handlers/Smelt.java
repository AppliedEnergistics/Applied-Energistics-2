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

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RecipeError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.ICraftHandler;
import appeng.api.recipes.IIngredient;
import appeng.recipes.RecipeHandler;
import appeng.util.Platform;


public final class Smelt implements ICraftHandler, IWebsiteSerializer
{

	IIngredient in;
	IIngredient out;

	@Override
	public final void setup( List<List<IIngredient>> input, List<List<IIngredient>> output ) throws RecipeError
	{
		if( input.size() == 1 && output.size() == 1 )
		{
			List<IIngredient> inputList = input.get( 0 );
			List<IIngredient> outputList = output.get( 0 );
			if( inputList.size() == 1 && outputList.size() == 1 )
			{
				this.in = inputList.get( 0 );
				this.out = outputList.get( 0 );
				return;
			}
		}
		throw new RecipeError( "Smelting recipe can only have a single input and output." );
	}

	@Override
	public final void register() throws RegistrationError, MissingIngredientError
	{
		if( this.in.getItemStack().getItem() == null )
		{
			throw new RegistrationError( this.in.toString() + ": Smelting Input is not a valid item." );
		}

		if( this.out.getItemStack().getItem() == null )
		{
			throw new RegistrationError( this.out.toString() + ": Smelting Output is not a valid item." );
		}

		GameRegistry.addSmelting( this.in.getItemStack(), this.out.getItemStack(), 0 );
	}

	@Override
	public String getPattern( RecipeHandler h )
	{
		return "smelt " + this.out.getQty() + '\n' +
				h.getName( this.out ) + '\n' +
				h.getName( this.in );
	}

	@Override
	public boolean canCraft( ItemStack reqOutput ) throws RegistrationError, MissingIngredientError
	{
		return Platform.isSameItemPrecise( this.out.getItemStack(), reqOutput );
	}
}
