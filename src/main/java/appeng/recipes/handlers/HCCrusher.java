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
import appeng.util.Platform;
import cpw.mods.fml.common.event.FMLInterModComms;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;


public class HCCrusher implements ICraftHandler, IWebsiteSerializer
{

	private IIngredient pro_input;
	private IIngredient[] pro_output;

	@Override
	public void setup( final List<List<IIngredient>> input, final List<List<IIngredient>> output ) throws RecipeError
	{
		if( input.size() == 1 && output.size() == 1 )
		{
			final int outs = output.get( 0 ).size();
			if( input.get( 0 ).size() == 1 && outs == 1 )
			{
				this.pro_input = input.get( 0 ).get( 0 );
				this.pro_output = output.get( 0 ).toArray( new IIngredient[outs] );
				return;
			}
		}
		throw new RecipeError( "Crusher must have a single input, and single output." );
	}

	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		for( final ItemStack beginStack : this.pro_input.getItemStackSet() )
		{
			try
			{
				final NBTTagCompound toRegister = new NBTTagCompound();

				final ItemStack endStack = this.pro_output[0].getItemStack();

				final NBTTagCompound itemFrom = new NBTTagCompound();
				final NBTTagCompound itemTo = new NBTTagCompound();

				beginStack.writeToNBT( itemFrom );
				endStack.writeToNBT( itemTo );

				toRegister.setTag( "itemFrom", itemFrom );
				toRegister.setTag( "itemTo", itemTo );
				toRegister.setFloat( "pressureRatio", 1.0F );

				FMLInterModComms.sendMessage( "HydCraft", "registerCrushingRecipe", toRegister );
			}
			catch( final java.lang.RuntimeException err )
			{
				AELog.info( "Hydraulicraft not happy - " + err.getMessage() );
			}
		}
	}

	@Override
	public String getPattern( final RecipeHandler h )
	{
		return null;
	}

	@Override
	public boolean canCraft( final ItemStack output ) throws RegistrationError, MissingIngredientError
	{
		return Platform.isSameItemPrecise( this.pro_output[0].getItemStack(), output );
	}
}
