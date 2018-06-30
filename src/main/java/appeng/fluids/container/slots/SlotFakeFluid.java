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

package appeng.fluids.container.slots;


import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.container.slot.SlotFake;
import appeng.fluids.items.FluidDummyItem;


/**
 * @author BrockWS
 * @version rv6 - 1/05/2018
 * @since rv6 1/05/2018
 */
public class SlotFakeFluid extends SlotFake implements IFluidSlot
{
	private FluidStack cachedStack = null;

	public SlotFakeFluid( IItemHandler inv, int idx, int x, int y )
	{
		super( inv, idx, x, y );
		this.setIIcon( 12 * 16 + 15 );
		this.updateFluidStack();
	}

	@Override
	public void putStack( ItemStack is )
	{
		if( is.isEmpty() || is.getItem() instanceof FluidDummyItem )
		{
			super.putStack( is );
		}
		else if( this.isItemValid( is ) )
		{
			super.putStack( FluidDummyItem.createStackfromFluidContainer( is ) );
		}
	}

	@Override
	public boolean isItemValid( ItemStack stack )
	{
		return stack.hasCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null ) && this.getContainer().isValidForSlot( this, stack );
	}

	@Override
	public boolean renderIconWithItem()
	{
		return true;
	}

	@Override
	public void onSlotChanged()
	{
		super.onSlotChanged();
		this.updateFluidStack();
	}

	private void updateFluidStack()
	{
		this.cachedStack = FluidDummyItem.getFluidFromStack( this.getStack() );
	}

	@Override
	public FluidStack getFluidStack()
	{
		return this.cachedStack;
	}
}
