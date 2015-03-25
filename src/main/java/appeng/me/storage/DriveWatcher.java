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

package appeng.me.storage;


import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEStack;


public class DriveWatcher<T extends IAEStack<T>> extends MEInventoryHandler<T>
{

	final int oldStatus = 0;
	final ItemStack is;
	final ICellHandler handler;
	final IChestOrDrive cord;

	public DriveWatcher( IMEInventory<T> i, ItemStack is, ICellHandler han, IChestOrDrive cod )
	{
		super( i, i.getChannel() );
		this.is = is;
		this.handler = han;
		this.cord = cod;
	}

	@Override
	public T injectItems( T input, Actionable type, BaseActionSource src )
	{
		long size = input.getStackSize();

		T a = super.injectItems( input, type, src );

		if( a == null || a.getStackSize() != size )
		{
			int newStatus = this.handler.getStatusForCell( this.is, this.getInternal() );

			if( newStatus != this.oldStatus )
			{
				this.cord.blinkCell( this.getSlot() );
			}
		}

		return a;
	}

	@Override
	public T extractItems( T request, Actionable type, BaseActionSource src )
	{
		T a = super.extractItems( request, type, src );

		if( a != null )
		{
			int newStatus = this.handler.getStatusForCell( this.is, this.getInternal() );

			if( newStatus != this.oldStatus )
			{
				this.cord.blinkCell( this.getSlot() );
			}
		}

		return a;
	}
}
