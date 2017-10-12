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

import com.google.common.base.Preconditions;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.exceptions.MissingIngredientException;
import appeng.api.exceptions.RegistrationException;
import appeng.api.recipes.IIngredient;
import appeng.api.recipes.ResolverResultSet;


public class IngredientSet implements IIngredient
{

	private final int qty;
	private final String name;
	private final List<ItemStack> items;
	private final boolean isInside = false;
	private ItemStack[] baked;

	public IngredientSet( final ResolverResultSet rr, final int qty )
	{
		Preconditions.checkNotNull( rr );
		Preconditions.checkNotNull( rr.name );
		Preconditions.checkNotNull( rr.results );
		Preconditions.checkState( qty > 0 );

		this.name = rr.name;
		this.items = rr.results;
		this.qty = qty;
	}

	@Override
	public ItemStack getItemStack() throws RegistrationException, MissingIngredientException
	{
		throw new RegistrationException( "Cannot pass group of items to a recipe which desires a single recipe item." );
	}

	@Override
	public ItemStack[] getItemStackSet() throws RegistrationException, MissingIngredientException
	{
		if( this.baked != null )
		{
			return this.baked;
		}

		if( this.isInside )
		{
			return new ItemStack[0];
		}

		final List<ItemStack> out = new LinkedList<>();
		out.addAll( this.items );

		if( out.isEmpty() )
		{
			throw new MissingIngredientException( this.toString() + " - group could not be resolved to any items." );
		}

		for( final ItemStack is : out )
		{
			is.setCount( this.qty );
		}

		return out.toArray( new ItemStack[out.size()] );
	}

	@Override
	public boolean isAir()
	{
		return false;
	}

	@Override
	public String getNameSpace()
	{
		return "";
	}

	@Override
	public String getItemName()
	{
		return this.name;
	}

	@Override
	public int getDamageValue()
	{
		return OreDictionary.WILDCARD_VALUE;
	}

	@Override
	public int getQty()
	{
		return this.qty;
	}

	@Override
	public void bake() throws RegistrationException, MissingIngredientException
	{
		this.baked = null;
		this.baked = this.getItemStackSet();
	}
}
