/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.integration.modules.crafttweaker;


import java.util.Collections;

import net.minecraft.item.ItemStack;

import crafttweaker.IAction;
import crafttweaker.api.item.IItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import appeng.api.AEApi;
import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;


@ZenClass( "mods.appliedenergistics2.Inscriber" )
public class InscriberRecipes
{
	@ZenMethod
	public static void addRecipe( IItemStack output, IItemStack input, boolean inscribe, @Optional IItemStack top, @Optional IItemStack bottom )
	{

		IInscriberRecipeBuilder builder = AEApi.instance().registries().inscriber().builder();
		builder.withProcessType( inscribe ? InscriberProcessType.INSCRIBE : InscriberProcessType.PRESS )
				.withOutput( CTModule.toStack( output ) )
				.withInputs( Collections.singleton( CTModule.toStack( input ) ) );

		final ItemStack s1 = CTModule.toStack( top );
		if( !s1.isEmpty() )
		{
			builder.withTopOptional( s1 );
		}
		final ItemStack s2 = CTModule.toStack( bottom );
		if( !s2.isEmpty() )
		{
			builder.withBottomOptional( s2 );
		}

		CTModule.ADDITIONS.add( new Add( builder.build() ) );
	}

	@ZenMethod
	public static void removeRecipe( IItemStack output )
	{
		CTModule.REMOVALS.add( new Remove( (ItemStack) output.getInternal() ) );
	}

	private static class Add implements IAction
	{
		private final IInscriberRecipe entry;

		private Add( IInscriberRecipe entry )
		{
			this.entry = entry;
		}

		@Override
		public void apply()
		{
			AEApi.instance().registries().inscriber().addRecipe( entry );
		}

		@Override
		public String describe()
		{
			return "Adding Inscriber Entry for " + entry.getOutput().getDisplayName();
		}
	}

	private static class Remove implements IAction
	{
		private final ItemStack stack;

		private Remove( ItemStack stack )
		{
			this.stack = stack;
		}

		@Override
		public void apply()
		{
			final IInscriberRegistry inscriber = AEApi.instance().registries().inscriber();
			inscriber.getRecipes()
					.stream()
					.filter( r -> r.getOutput().isItemEqual( this.stack ) )
					.forEach( inscriber::removeRecipe );
		}

		@Override
		public String describe()
		{
			return "Removing Inscriber Entry for " + stack.getDisplayName();
		}
	}

}
