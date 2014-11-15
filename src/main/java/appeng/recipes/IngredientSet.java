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

package appeng.recipes;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.exceptions.MissingIngredientError;
import appeng.api.exceptions.RegistrationError;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.ResolverResultSet;

public class IngredientSet implements IIngredient
{

	final int qty = 0;
	final String name;
	final List<ItemStack> items;
	ItemStack[] baked;

	public IngredientSet(ResolverResultSet rr) {
		name = rr.name;
		items = rr.results;
	}

	final boolean isInside = false;

	@Override
	public int getDamageValue()
	{
		return OreDictionary.WILDCARD_VALUE;
	}

	@Override
	public String getItemName()
	{
		return name;
	}

	@Override
	public ItemStack getItemStack() throws RegistrationError, MissingIngredientError
	{
		throw new RegistrationError( "Cannot pass group of items to a recipe which desires a single recipe item." );
	}

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationError, MissingIngredientError
	{
		if ( baked != null )
			return baked;

		if ( isInside )
			return new ItemStack[0];

		List<ItemStack> out = new LinkedList<ItemStack>();
		out.addAll( items );

		if ( out.size() == 0 )
			throw new MissingIngredientError( toString() + " - group could not be resolved to any items." );

		for (ItemStack is : out)
			is.stackSize = qty;

		return out.toArray( new ItemStack[out.size()] );
	}

	@Override
	public String getNameSpace()
	{
		return "";
	}

	@Override
	public int getQty()
	{
		return 0;
	}

	@Override
	public boolean isAir()
	{
		return false;
	}

	@Override
	public void bake() throws RegistrationError, MissingIngredientError
	{
		baked = null;
		baked = getItemStackSet();
	}

}
