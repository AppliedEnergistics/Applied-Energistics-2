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
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.IItemHandler;

import appeng.container.slot.SlotFake;


/**
 * @author BrockWS
 * @version rv6 - 1/05/2018
 * @since rv6 1/05/2018
 */
public class SlotFakeFluid extends SlotFake implements IFluidSlot
{
	public SlotFakeFluid( IItemHandler inv, int idx, int x, int y )
	{
		super( inv, idx, x, y );
		this.setIIcon( 12 * 16 + 15 );
	}

	@Override
	public void putStack( ItemStack is )
	{
		if( this.isItemValid( is ) || is.isEmpty() )
		{
			super.putStack( is );
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
	public FluidStack getFluidStack()
	{
		if( !this.getStack().isEmpty() )
		{
			IFluidHandlerItem fh = this.getStack().getCapability( CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null );
			if( fh == null )
			{
				throw new NullPointerException( "Item did not give IFluidHandlerItem: " + this.getStack().getDisplayName() );
			}
			return fh.drain( Integer.MAX_VALUE, false );
		}
		return null;
	}
}
