/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.fluids.container;


import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.implementations.IUpgradeableHost;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.fluids.container.slots.OptionalSlotFakeFluid;
import appeng.fluids.container.slots.SlotFakeFluid;


/**
 * @author BrockWS
 * @version rv5 - 1/05/2018
 * @since rv5 1/05/2018
 */
public class ContainerFluidIO extends ContainerUpgradeable
{
	public ContainerFluidIO( InventoryPlayer ip, IUpgradeableHost te )
	{
		super( ip, te );
	}

	@Override
	protected void setupConfig()
	{
		this.setupUpgrades();

		final IItemHandler inv = this.getUpgradeable().getInventoryByName( "config" );
		final int y = 40;
		final int x = 80;
		this.addSlotToContainer( new SlotFakeFluid( inv, 0, x, y ) );

		if( this.supportCapacity() )
		{
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 1, x, y, -1, 0, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 2, x, y, 1, 0, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 3, x, y, 0, -1, 1 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 4, x, y, 0, 1, 1 ) );

			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 5, x, y, -1, -1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 6, x, y, 1, -1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 7, x, y, -1, 1, 2 ) );
			this.addSlotToContainer( new OptionalSlotFakeFluid( inv, this, 8, x, y, 1, 1, 2 ) );
		}
	}

	@Override
	public boolean isValidForSlot( Slot s, ItemStack i )
	{
		return s instanceof SlotFakeFluid ? i.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) : super.isValidForSlot( s, i );
	}
}
