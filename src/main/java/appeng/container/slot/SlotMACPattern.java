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

import appeng.container.implementations.ContainerMAC;


public final class SlotMACPattern extends AppEngSlot
{

	final ContainerMAC mac;

	public SlotMACPattern( ContainerMAC mac, IInventory i, int slotIdx, int x, int y )
	{
		super( i, slotIdx, x, y );
		this.mac = mac;
	}

	@Override
	public final boolean isItemValid( ItemStack i )
	{
		return this.mac.isValidItemForSlot( this.getSlotIndex(), i );
	}
}
