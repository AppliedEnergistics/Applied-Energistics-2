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


import appeng.api.AEApi;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.InscriberProcessType;
import appeng.core.features.registries.entries.InscriberRecipe;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * recipe translation for inscribe process
 *
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public final class Inscribe extends InscriberProcess
{
	@Override
	public void register() throws RegistrationError, MissingIngredientError
	{
		if( this.getImprintable() == null )
		{
			return;
		}
		if( this.getOutput() == null )
		{
			return;
		}

		final ItemStack[] realInput = this.getImprintable().getItemStackSet();
		final List<ItemStack> inputs = new ArrayList<ItemStack>( realInput.length );
		Collections.addAll( inputs, realInput );
		final ItemStack top = ( this.getTopOptional() == null ) ? null : this.getTopOptional().getItemStack();
		final ItemStack bot = ( this.getBotOptional() == null ) ? null : this.getBotOptional().getItemStack();
		final ItemStack output = this.getOutput().getItemStack();
		final InscriberProcessType type = InscriberProcessType.Inscribe;

		final IInscriberRecipe recipe = new InscriberRecipe( inputs, output, top, bot, type );

		AEApi.instance().registries().inscriber().addRecipe( recipe );
	}
}
