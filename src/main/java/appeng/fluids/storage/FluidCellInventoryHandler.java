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

package appeng.fluids.storage;


import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.items.FluidDummyItem;
import appeng.me.storage.AbstractCellInventoryHandler;
import appeng.util.item.AEFluidStack;


public class FluidCellInventoryHandler extends AbstractCellInventoryHandler<IAEFluidStack>
{

	public FluidCellInventoryHandler( IMEInventory c )
	{
		super( c, AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) );
	}

	@Override
	protected IAEFluidStack createConfigStackFromItem( ItemStack is )
	{
		if ( is.getItem() instanceof FluidDummyItem ){
			FluidStack fs = ((FluidDummyItem)is.getItem()).getFluidStack( is );
			return fs != null ? AEFluidStack.fromFluidStack( fs ) : null;
		}
		return null;
	}
}
