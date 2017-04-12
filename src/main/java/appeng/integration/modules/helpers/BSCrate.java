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

package appeng.integration.modules.helpers;


import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;


public class BSCrate implements IMEInventory<IAEItemStack>
{
	private final ICrateStorage crateStorage;

	public BSCrate( final Object object )
	{
		this.crateStorage = (ICrateStorage) object;
	}

	@Override
	public IAEItemStack injectItems( final IAEItemStack input, final Actionable mode, final BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			return null;
		}

		final ItemStack failed = this.crateStorage.insertItems( input.getItemStack() );
		if( failed == null )
		{
			return null;
		}
		input.setStackSize( failed.stackSize );
		return input;
	}

	@Override
	public IAEItemStack extractItems( final IAEItemStack request, final Actionable mode, final BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			final int howMany = this.crateStorage.getItemCount( request.getItemStack() );
			return howMany > request.getStackSize() ? request : request.copy().setStackSize( howMany );
		}

		final ItemStack obtained = this.crateStorage.extractItems( request.getItemStack(), (int) request.getStackSize() );
		return AEItemStack.create( obtained );
	}

	@Override
	public IItemList getAvailableItems( final IItemList out )
	{
		for( final ItemStack is : this.crateStorage.getContents() )
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
