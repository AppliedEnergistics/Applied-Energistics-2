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

package appeng.container.slot;


import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class SlotCraftingMatrix extends AppEngSlot
{

	final Container c;

	public SlotCraftingMatrix( Container c, IInventory par1iInventory, int par2, int par3, int par4 )
	{
		super( par1iInventory, par2, par3, par4 );
		this.c = c;
	}

	@Override
	public void clearStack()
	{
		super.clearStack();
		this.c.onCraftMatrixChanged( this.inventory );
	}

	@Override
	public void putStack( ItemStack par1ItemStack )
	{
		super.putStack( par1ItemStack );
		this.c.onCraftMatrixChanged( this.inventory );
	}

	@Override
	public boolean isPlayerSide()
	{
		return true;
	}

	@Override
	public ItemStack decrStackSize( int par1 )
	{
		ItemStack is = super.decrStackSize( par1 );
		this.c.onCraftMatrixChanged( this.inventory );
		return is;
	}
}
