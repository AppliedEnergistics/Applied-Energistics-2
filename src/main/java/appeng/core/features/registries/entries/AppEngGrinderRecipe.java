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

package appeng.core.features.registries.entries;


import appeng.api.features.IGrinderEntry;
import net.minecraft.item.ItemStack;


public class AppEngGrinderRecipe implements IGrinderEntry
{

	private ItemStack in;
	private ItemStack out;

	private float optionalChance;
	private ItemStack optionalOutput;

	private float optionalChance2;
	private ItemStack optionalOutput2;

	private int energy;

	public AppEngGrinderRecipe( final ItemStack a, final ItemStack b, final int cost )
	{
		this.in = a;
		this.out = b;
		this.energy = cost;
	}

	public AppEngGrinderRecipe( final ItemStack a, final ItemStack b, final ItemStack c, final float chance, final int cost )
	{
		this.in = a;
		this.out = b;

		this.optionalOutput = c;
		this.optionalChance = chance;

		this.energy = cost;
	}

	public AppEngGrinderRecipe( final ItemStack a, final ItemStack b, final ItemStack c, final ItemStack d, final float chance, final float chance2, final int cost )
	{
		this.in = a;
		this.out = b;

		this.optionalOutput = c;
		this.optionalChance = chance;

		this.optionalOutput2 = d;
		this.optionalChance2 = chance2;

		this.energy = cost;
	}

	@Override
	public ItemStack getInput()
	{
		return this.in;
	}

	@Override
	public void setInput( final ItemStack i )
	{
		this.in = i.copy();
	}

	@Override
	public ItemStack getOutput()
	{
		return this.out;
	}

	@Override
	public void setOutput( final ItemStack o )
	{
		this.out = o.copy();
	}

	@Override
	public ItemStack getOptionalOutput()
	{
		return this.optionalOutput;
	}

	@Override
	public ItemStack getSecondOptionalOutput()
	{
		return this.optionalOutput2;
	}

	@Override
	public void setOptionalOutput( final ItemStack output, final float chance )
	{
		this.optionalOutput = output.copy();
		this.optionalChance = chance;
	}

	@Override
	public float getOptionalChance()
	{
		return this.optionalChance;
	}

	@Override
	public void setSecondOptionalOutput( final ItemStack output, final float chance )
	{
		this.optionalChance2 = chance;
		this.optionalOutput2 = output.copy();
	}

	@Override
	public float getSecondOptionalChance()
	{
		return this.optionalChance2;
	}

	@Override
	public int getEnergyCost()
	{
		return this.energy;
	}

	@Override
	public void setEnergyCost( final int c )
	{
		this.energy = c;
	}
}
