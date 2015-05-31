/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;


public class OptionalSlotFake extends SlotFake
{

	public final int srcX;
	public final int srcY;
	final int invSlot;
	final int groupNum;
	final IOptionalSlotHost host;
	public boolean renderDisabled = true;

	public OptionalSlotFake( IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum )
	{
		super( inv, idx, x + offX * 18, y + offY * 18 );
		this.srcX = x;
		this.srcY = y;
		this.invSlot = idx;
		this.groupNum = groupNum;
		this.host = containerBus;
	}

	@Override
	public final ItemStack getStack()
	{
		if( !this.isEnabled() )
		{
			if( this.getDisplayStack() != null )
			{
				this.clearStack();
			}
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled()
	{
		if( this.host == null )
		{
			return false;
		}

		return this.host.isSlotEnabled( this.groupNum );
	}

	public final boolean renderDisabled()
	{
		return this.renderDisabled;
	}
}
