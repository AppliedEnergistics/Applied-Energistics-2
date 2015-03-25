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

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.items.contents.CellConfig;
import appeng.util.item.AEItemStack;


public class CreativeCellInventory implements IMEInventoryHandler<IAEItemStack>
{

	final IItemList<IAEItemStack> itemListCache = AEApi.instance().storage().createItemList();

	protected CreativeCellInventory( ItemStack o )
	{
		CellConfig cc = new CellConfig( o );
		for( ItemStack is : cc )
			if( is != null )
			{
				IAEItemStack i = AEItemStack.create( is );
				i.setStackSize( Integer.MAX_VALUE );
				this.itemListCache.add( i );
			}
	}

	public static IMEInventoryHandler getCell( ItemStack o )
	{
		return new CellInventoryHandler( new CreativeCellInventory( o ) );
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		IAEItemStack local = this.itemListCache.findPrecise( input );
		if( local == null )
			return input;

		return null;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		IAEItemStack local = this.itemListCache.findPrecise( request );
		if( local == null )
			return null;

		return request.copy();
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList out )
	{
		for( IAEItemStack ais : this.itemListCache )
			out.add( ais );
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized( IAEItemStack input )
	{
		return this.itemListCache.findPrecise( input ) != null;
	}

	@Override
	public boolean canAccept( IAEItemStack input )
	{
		return this.itemListCache.findPrecise( input ) != null;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public boolean validForPass( int i )
	{
		return true;
	}
}
