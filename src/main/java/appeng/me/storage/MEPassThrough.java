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

package appeng.me.storage;


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;


public class MEPassThrough<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	protected final StorageChannel channel;
	private IMEInventory<T> internal;

	public MEPassThrough( IMEInventory<T> i, StorageChannel channel )
	{
		this.channel = channel;
		this.setInternal( i );
	}

	protected final IMEInventory<T> getInternal()
	{
		return this.internal;
	}

	public void setInternal( IMEInventory<T> i )
	{
		this.internal = i;
	}

	@Override
	public final T injectItems( T input, Actionable type, BaseActionSource src )
	{
		return this.internal.injectItems( input, type, src );
	}

	@Override
	public final T extractItems( T request, Actionable type, BaseActionSource src )
	{
		return this.internal.extractItems( request, type, src );
	}

	@Override
	public IItemList<T> getAvailableItems( IItemList out )
	{
		return this.internal.getAvailableItems( out );
	}

	@Override
	public final StorageChannel getChannel()
	{
		return this.internal.getChannel();
	}

	@Override
	public final AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public final boolean isPrioritized( T input )
	{
		return false;
	}

	@Override
	public final boolean canAccept( T input )
	{
		return true;
	}

	@Override
	public final int getPriority()
	{
		return 0;
	}

	@Override
	public final int getSlot()
	{
		return 0;
	}

	@Override
	public final boolean validForPass( int i )
	{
		return true;
	}
}
