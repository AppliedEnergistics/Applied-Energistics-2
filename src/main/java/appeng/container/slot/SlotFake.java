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


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class SlotFake extends AppEngSlot implements IJEITargetSlot
{

	public SlotFake( final IItemHandler inv, final int idx, final int x, final int y )
	{
		super( inv, idx, x, y );
	}

	@Override
	public ItemStack onTake( final EntityPlayer par1EntityPlayer, final ItemStack par2ItemStack )
	{
		return par2ItemStack;
	}

	@Override
	public ItemStack decrStackSize( final int par1 )
	{
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isItemValid( final ItemStack par1ItemStack )
	{
		return false;
	}

	@Override
	public void putStack( ItemStack is )
	{
		if( !is.isEmpty() )
		{
			is = is.copy();
		}

		super.putStack( is );
	}

	@Override
	public boolean canTakeStack( final EntityPlayer par1EntityPlayer )
	{
		return false;
	}
}
