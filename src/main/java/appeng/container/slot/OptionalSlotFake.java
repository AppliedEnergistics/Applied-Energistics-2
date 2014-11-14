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

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class OptionalSlotFake extends SlotFake
{

	final int invSlot;
	final int groupNum;
	final IOptionalSlotHost host;

	public boolean renderDisabled = true;

	public final int srcX;
	public final int srcY;

	public OptionalSlotFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
		super( inv, idx, x + offX * 18, y + offY * 18 );
		srcX = x;
		srcY = y;
		invSlot = idx;
		this.groupNum = groupNum;
		host = containerBus;
	}

	@Override
	public ItemStack getStack()
	{
		if ( !isEnabled() )
		{
			if ( getDisplayStack() != null )
				clearStack();
		}

		return super.getStack();
	}

	@Override
	public boolean isEnabled()
	{
		if ( host == null )
			return false;

		return host.isSlotEnabled( groupNum );
	}

	public boolean renderDisabled()
	{
		return renderDisabled;
	}

}
