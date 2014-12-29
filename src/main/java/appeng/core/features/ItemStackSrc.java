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

package appeng.core.features;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemStackSrc implements IStackSrc
{

	public final Item item;
	public final Block block;
	public final int damage;

	public ItemStackSrc(Item i, int dmg) {
		this.block = null;
		this.item = i;
		this.damage = dmg;
	}

	public ItemStackSrc(Block b, int dmg) {
		this.item = null;
		this.block = b;
		this.damage = dmg;
	}

	@Override
	public ItemStack stack(int i)
	{
		if ( this.block != null )
			return new ItemStack( this.block, i, this.damage );

		if ( this.item != null )
			return new ItemStack( this.item, i, this.damage );

		return null;
	}

	@Override
	public Item getItem()
	{
		return this.item;
	}

	@Override
	public int getDamage()
	{
		return this.damage;
	}
}
