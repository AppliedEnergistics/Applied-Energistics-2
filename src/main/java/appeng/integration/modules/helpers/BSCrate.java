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

package appeng.integration.modules.helpers;


import net.mcft.copy.betterstorage.api.crate.ICrateStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;


public final class BSCrate implements IMEInventory<IAEItemStack>
{
	private final ICrateStorage crateStorage;
	private final ForgeDirection side;

	public BSCrate( Object object, ForgeDirection d )
	{
		this.crateStorage = (ICrateStorage) object;
		this.side = d;
	}

	@Override
	public final IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			return null;
		}

		ItemStack failed = this.crateStorage.insertItems( input.getItemStack() );
		if( failed == null )
		{
			return null;
		}
		input.setStackSize( failed.stackSize );
		return input;
	}

	@Override
	public final IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			int howMany = this.crateStorage.getItemCount( request.getItemStack() );
			return howMany > request.getStackSize() ? request : request.copy().setStackSize( howMany );
		}

		ItemStack obtained = this.crateStorage.extractItems( request.getItemStack(), (int) request.getStackSize() );
		return AEItemStack.create( obtained );
	}

	@Override
	public final IItemList getAvailableItems( IItemList out )
	{
		for( ItemStack is : this.crateStorage.getContents() )
		{
			out.add( AEItemStack.create( is ) );
		}
		return out;
	}

	@Override
	public final StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}
}
