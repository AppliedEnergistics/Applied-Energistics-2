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

package appeng.util.inv;

import net.minecraft.item.ItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;

public class ItemSlot
{

	public int slot;

	// one or the other..
	private IAEItemStack aeItemStack;
	private ItemStack itemStack;

	public boolean isExtractable;

	public void setItemStack(ItemStack is)
	{
		aeItemStack = null;
		itemStack = is;
	}

	public void setAEItemStack(IAEItemStack is)
	{
		aeItemStack = is;
		itemStack = null;
	}

	public ItemStack getItemStack()
	{
		return itemStack == null ? (aeItemStack == null ? null : (itemStack = aeItemStack.getItemStack())) : itemStack;
	}

	public IAEItemStack getAEItemStack()
	{
		return aeItemStack == null ? (itemStack == null ? null : (aeItemStack = AEItemStack.create( itemStack ))) : aeItemStack;
	}

}
