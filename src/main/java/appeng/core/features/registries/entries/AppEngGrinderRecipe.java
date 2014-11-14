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

import net.minecraft.item.ItemStack;
import appeng.api.features.IGrinderEntry;

public class AppEngGrinderRecipe implements IGrinderEntry
{

	private ItemStack in;
	private ItemStack out;

	private float optionalChance;
	private ItemStack optionalOutput;

	private float optionalChance2;
	private ItemStack optionalOutput2;

	private int energy;

	public AppEngGrinderRecipe(ItemStack a, ItemStack b, int cost) {
		in = a;
		out = b;
		energy = cost;
	}

	public AppEngGrinderRecipe(ItemStack a, ItemStack b, ItemStack c, float chance, int cost) {
		in = a;
		out = b;

		optionalOutput = c;
		optionalChance = chance;

		energy = cost;
	}

	public AppEngGrinderRecipe(ItemStack a, ItemStack b, ItemStack c, ItemStack d, float chance, float chance2, int cost) {
		in = a;
		out = b;

		optionalOutput = c;
		optionalChance = chance;

		optionalOutput2 = d;
		optionalChance2 = chance2;

		energy = cost;
	}

	@Override
	public ItemStack getInput()
	{
		return in;
	}

	@Override
	public void setInput(ItemStack i)
	{
		in = i.copy();
	}

	@Override
	public ItemStack getOutput()
	{
		return out;
	}

	@Override
	public void setOutput(ItemStack o)
	{
		out = o.copy();
	}

	@Override
	public int getEnergyCost()
	{
		return energy;
	}

	@Override
	public void setEnergyCost(int c)
	{
		energy = c;
	}

	@Override
	public ItemStack getOptionalOutput()
	{
		return optionalOutput;
	}

	@Override
	public void setOptionalOutput(ItemStack output, float chance)
	{
		optionalOutput = output.copy();
		optionalChance = chance;
	}

	@Override
	public float getOptionalChance()
	{
		return optionalChance;
	}

	@Override
	public ItemStack getSecondOptionalOutput()
	{
		return optionalOutput2;
	}

	@Override
	public void setSecondOptionalOutput(ItemStack output, float chance)
	{
		optionalChance2 = chance;
		optionalOutput2 = output.copy();
	}

	@Override
	public float getSecondOptionalChance()
	{
		return optionalChance2;
	}

}
