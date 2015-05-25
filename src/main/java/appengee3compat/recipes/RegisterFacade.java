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

package appengee3compat.recipes;


import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItemDefinition;
import appeng.facade.IFacadeItem;
import appeng.items.parts.ItemFacade;
import appengee3compat.core.AELog;
import com.pahimar.ee3.api.exchange.RecipeRegistryProxy;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class RegisterFacade
{
	public static void initRecipes()
	{
		int recipeCount = 0;

		final IDefinitions definitions = AEApi.instance().definitions();
		final ItemFacade facade = ( ItemFacade ) definitions.items().facade().maybeItem().get();
		final IItemDefinition anchorDefinition = definitions.parts().cableAnchor();

		final List<ItemStack> facades = facade.getFacades();
		for ( ItemStack anchorStack : anchorDefinition.maybeStack( 1 ).asSet() )
		{
			for ( ItemStack is : facades )
			{
				recipeCount++;
				addItem( facade, anchorStack, is );
			}
		}

		AELog.info( "Told EE3 about " + recipeCount + " facade recipes..." );
	}

	private static void addItem( IFacadeItem facade, ItemStack anchor, ItemStack output )
	{
		anchor.stackSize = 4;
		output.stackSize = 4;

		List<ItemStack> input = new ArrayList<ItemStack>();

		input.add( anchor );
		input.add( facade.getTextureItem( output ) );

		AELog.debug( ">>> EE3 Recipe Register >>> Output: " + output.toString() + " >>> Input(s): " + input.toString() );
		RecipeRegistryProxy.addRecipe( output, input );
	}
}
