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
		this.aeItemStack = null;
		this.itemStack = is;
	}

	public void setAEItemStack(IAEItemStack is)
	{
		this.aeItemStack = is;
		this.itemStack = null;
	}

	public ItemStack getItemStack()
	{
		return this.itemStack == null ? (this.aeItemStack == null ? null : (this.itemStack = this.aeItemStack.getItemStack())) : this.itemStack;
	}

	public IAEItemStack getAEItemStack()
	{
		return this.aeItemStack == null ? (this.itemStack == null ? null : (this.aeItemStack = AEItemStack.create( this.itemStack ))) : this.aeItemStack;
	}

}
