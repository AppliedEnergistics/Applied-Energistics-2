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

package appeng.crafting;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public class MECraftingInventory implements IMEInventory<IAEItemStack>
{

	final MECraftingInventory par;

	final IMEInventory<IAEItemStack> target;
	final IItemList<IAEItemStack> localCache;

	final boolean logExtracted;
	final IItemList<IAEItemStack> extractedCache;

	final boolean logInjections;
	final IItemList<IAEItemStack> injectedCache;

	final boolean logMissing;
	final IItemList<IAEItemStack> missingCache;

	public MECraftingInventory()
	{
		localCache = AEApi.instance().storage().createItemList();
		extractedCache = null;
		injectedCache = null;
		missingCache = null;
		this.logExtracted = false;
		this.logInjections = false;
		this.logMissing = false;
		target = null;
		par = null;
	}

	public MECraftingInventory(MECraftingInventory parent)
	{
		this.target = parent;
		this.logExtracted = parent.logExtracted;
		this.logInjections = parent.logInjections;
		this.logMissing = parent.logMissing;

		if ( logMissing )
			missingCache = AEApi.instance().storage().createItemList();
		else
			missingCache = null;

		if ( logExtracted )
			extractedCache = AEApi.instance().storage().createItemList();
		else
			extractedCache = null;

		if ( logInjections )
			injectedCache = AEApi.instance().storage().createItemList();
		else
			injectedCache = null;

		localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );

		par = parent;
	}

	public MECraftingInventory(IMEMonitor<IAEItemStack> target, BaseActionSource src, boolean logExtracted, boolean logInjections, boolean logMissing)
	{
		this.target = target;
		this.logExtracted = logExtracted;
		this.logInjections = logInjections;
		this.logMissing = logMissing;

		if ( logMissing )
			missingCache = AEApi.instance().storage().createItemList();
		else
			missingCache = null;

		if ( logExtracted )
			extractedCache = AEApi.instance().storage().createItemList();
		else
			extractedCache = null;

		if ( logInjections )
			injectedCache = AEApi.instance().storage().createItemList();
		else
			injectedCache = null;

		localCache = AEApi.instance().storage().createItemList();
		for (IAEItemStack is : target.getStorageList())
			localCache.add( target.extractItems( is, Actionable.SIMULATE, src ) );

		par = null;
	}

	public MECraftingInventory(IMEInventory<IAEItemStack> target, boolean logExtracted, boolean logInjections, boolean logMissing)
	{
		this.target = target;
		this.logExtracted = logExtracted;
		this.logInjections = logInjections;
		this.logMissing = logMissing;

		if ( logMissing )
			missingCache = AEApi.instance().storage().createItemList();
		else
			missingCache = null;

		if ( logExtracted )
			extractedCache = AEApi.instance().storage().createItemList();
		else
			extractedCache = null;

		if ( logInjections )
			injectedCache = AEApi.instance().storage().createItemList();
		else
			injectedCache = null;

		localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );
		par = null;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src)
	{
		if ( input == null )
			return null;

		if ( mode == Actionable.MODULATE )
		{
			if ( logInjections )
				injectedCache.add( input );
			localCache.add( input );
		}

		return null;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( request == null )
			return null;

		IAEItemStack list = localCache.findPrecise( request );
		if ( list == null || list.getStackSize() == 0 )
			return null;

		if ( list.getStackSize() >= request.getStackSize() )
		{
			if ( mode == Actionable.MODULATE )
			{
				list.decStackSize( request.getStackSize() );
				if ( logExtracted )
					extractedCache.add( request );
			}

			return request;
		}

		IAEItemStack ret = request.copy();
		ret.setStackSize( list.getStackSize() );

		if ( mode == Actionable.MODULATE )
		{
			list.reset();
			if ( logExtracted )
				extractedCache.add( ret );
		}

		return ret;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out)
	{
		for (IAEItemStack is : localCache)
			out.add( is );

		return out;
	}

	public IItemList<IAEItemStack> getItemList()
	{
		return localCache;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	public boolean commit(BaseActionSource src)
	{
		IItemList<IAEItemStack> added = AEApi.instance().storage().createItemList();
		IItemList<IAEItemStack> pulled = AEApi.instance().storage().createItemList();
		boolean failed = false;

		if ( logInjections )
		{
			for (IAEItemStack inject : injectedCache)
			{
				IAEItemStack result = null;
				added.add( result = target.injectItems( inject, Actionable.MODULATE, src ) );

				if ( result != null )
				{
					failed = true;
					break;
				}
			}
		}

		if ( failed )
		{
			for (IAEItemStack is : added)
				target.extractItems( is, Actionable.MODULATE, src );

			return false;
		}

		if ( logExtracted )
		{
			for (IAEItemStack extra : extractedCache)
			{
				IAEItemStack result = null;
				pulled.add( result = target.extractItems( extra, Actionable.MODULATE, src ) );

				if ( result == null || result.getStackSize() != extra.getStackSize() )
				{
					failed = true;
					break;
				}
			}
		}

		if ( failed )
		{
			for (IAEItemStack is : added)
				target.extractItems( is, Actionable.MODULATE, src );

			for (IAEItemStack is : pulled)
				target.injectItems( is, Actionable.MODULATE, src );

			return false;
		}

		if ( logMissing && par != null )
		{
			for (IAEItemStack extra : missingCache)
				par.addMissing( extra );
		}

		return true;
	}

	public void addMissing(IAEItemStack extra)
	{
		missingCache.add( extra );
	}

	public void ignore(IAEItemStack what)
	{
		IAEItemStack list = localCache.findPrecise( what );
		if ( list != null )
			list.setStackSize( 0 );
	}
}
