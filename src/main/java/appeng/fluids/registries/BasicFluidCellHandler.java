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

package appeng.fluids.registries;


import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.fluids.storage.FluidCellInventory;
import appeng.fluids.storage.FluidCellInventoryHandler;
import appeng.util.Platform;


public class BasicFluidCellHandler implements ICellHandler
{

	@Override
	public boolean isCell( final ItemStack is )
	{
		return FluidCellInventory.isCell( is );
	}

	@Override
	public <T extends IAEStack<T>> IMEInventoryHandler<T> getCellInventory( final ItemStack is, final ISaveProvider container, final IStorageChannel<T> channel )
	{
		if( channel == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
		{
			return (IMEInventoryHandler<T>) FluidCellInventory.getCell( is, container );
		}

		return null;
	}

	@Override
	public void openChestGui( final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler, final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan )
	{
		if( chan == AEApi.instance().storage().getStorageChannel( IFluidStorageChannel.class ) )
		{
			Platform.openGUI( player, (TileEntity) chest, AEPartLocation.fromFacing( chest.getUp() ), GuiBridge.GUI_FLUID_TERMINAL );
		}
	}

	@Override
	public int getStatusForCell( final ItemStack is, final IMEInventory handler )
	{
		if( handler instanceof FluidCellInventoryHandler )
		{
			final FluidCellInventoryHandler ci = (FluidCellInventoryHandler) handler;
			return ci.getStatusForCell();
		}
		return 0;
	}

	@Override
	public double cellIdleDrain( final ItemStack is, final IMEInventory handler )
	{
		final ICellInventory inv = ( (ICellInventoryHandler) handler ).getCellInv();
		return inv.getIdleDrain();
	}
}
