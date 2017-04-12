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
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;


public class OreRegistration implements ICraftHandler
{

	private final List<IIngredient> inputs;
	private final String name;

	public OreRegistration( final List<IIngredient> in, final String out )
	{
		this.inputs = in;
		this.name = out;
	}

	@Override
	public void setup( final List<List<IIngredient>> input, final List<List<IIngredient>> output ) throws RecipeError
	{

	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		for( final IIngredient i : this.inputs )
		{
			for( final ItemStack is : i.getItemStackSet() )
			{
				OreDictionary.registerOre( this.name, is );
			}
		}
	}
}
