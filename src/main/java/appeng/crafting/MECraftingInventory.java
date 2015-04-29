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
		this.localCache = AEApi.instance().storage().createItemList();
		this.extractedCache = null;
		this.injectedCache = null;
		this.missingCache = null;
		this.logExtracted = false;
		this.logInjections = false;
		this.logMissing = false;
		this.target = null;
		this.par = null;
	}

	public MECraftingInventory( MECraftingInventory parent )
	{
		this.target = parent;
		this.logExtracted = parent.logExtracted;
		this.logInjections = parent.logInjections;
		this.logMissing = parent.logMissing;

		if( this.logMissing )
		{
			this.missingCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.missingCache = null;
		}

		if( this.logExtracted )
		{
			this.extractedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.extractedCache = null;
		}

		if( this.logInjections )
		{
			this.injectedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.injectedCache = null;
		}

		this.localCache = this.target.getAvailableItems( AEApi.instance().storage().createItemList() );

		this.par = parent;
	}

	public MECraftingInventory( IMEMonitor<IAEItemStack> target, BaseActionSource src, boolean logExtracted, boolean logInjections, boolean logMissing )
	{
		this.target = target;
		this.logExtracted = logExtracted;
		this.logInjections = logInjections;
		this.logMissing = logMissing;

		if( logMissing )
		{
			this.missingCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.missingCache = null;
		}

		if( logExtracted )
		{
			this.extractedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.extractedCache = null;
		}

		if( logInjections )
		{
			this.injectedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.injectedCache = null;
		}

		this.localCache = AEApi.instance().storage().createItemList();
		for( IAEItemStack is : target.getStorageList() )
		{
			this.localCache.add( target.extractItems( is, Actionable.SIMULATE, src ) );
		}

		this.par = null;
	}

	public MECraftingInventory( IMEInventory<IAEItemStack> target, boolean logExtracted, boolean logInjections, boolean logMissing )
	{
		this.target = target;
		this.logExtracted = logExtracted;
		this.logInjections = logInjections;
		this.logMissing = logMissing;

		if( logMissing )
		{
			this.missingCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.missingCache = null;
		}

		if( logExtracted )
		{
			this.extractedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.extractedCache = null;
		}

		if( logInjections )
		{
			this.injectedCache = AEApi.instance().storage().createItemList();
		}
		else
		{
			this.injectedCache = null;
		}

		this.localCache = target.getAvailableItems( AEApi.instance().storage().createItemList() );
		this.par = null;
	}

	@Override
	public IAEItemStack injectItems( IAEItemStack input, Actionable mode, BaseActionSource src )
	{
		if( input == null )
		{
			return null;
		}

		if( mode == Actionable.MODULATE )
		{
			if( this.logInjections )
			{
				this.injectedCache.add( input );
			}
			this.localCache.add( input );
		}

		return null;
	}

	@Override
	public IAEItemStack extractItems( IAEItemStack request, Actionable mode, BaseActionSource src )
	{
		if( request == null )
		{
			return null;
		}

		IAEItemStack list = this.localCache.findPrecise( request );
		if( list == null || list.getStackSize() == 0 )
		{
			return null;
		}

		if( list.getStackSize() >= request.getStackSize() )
		{
			if( mode == Actionable.MODULATE )
			{
				list.decStackSize( request.getStackSize() );
				if( this.logExtracted )
				{
					this.extractedCache.add( request );
				}
			}

			return request;
		}

		IAEItemStack ret = request.copy();
		ret.setStackSize( list.getStackSize() );

		if( mode == Actionable.MODULATE )
		{
			list.reset();
			if( this.logExtracted )
			{
				this.extractedCache.add( ret );
			}
		}

		return ret;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems( IItemList<IAEItemStack> out )
	{
		for( IAEItemStack is : this.localCache )
		{
			out.add( is );
		}

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	public IItemList<IAEItemStack> getItemList()
	{
		return this.localCache;
	}

	public boolean commit( BaseActionSource src )
	{
		IItemList<IAEItemStack> added = AEApi.instance().storage().createItemList();
		IItemList<IAEItemStack> pulled = AEApi.instance().storage().createItemList();
		boolean failed = false;

		if( this.logInjections )
		{
			for( IAEItemStack inject : this.injectedCache )
			{
				IAEItemStack result = null;
				added.add( result = this.target.injectItems( inject, Actionable.MODULATE, src ) );

				if( result != null )
				{
					failed = true;
					break;
				}
			}
		}

		if( failed )
		{
			for( IAEItemStack is : added )
			{
				this.target.extractItems( is, Actionable.MODULATE, src );
			}

			return false;
		}

		if( this.logExtracted )
		{
			for( IAEItemStack extra : this.extractedCache )
			{
				IAEItemStack result = null;
				pulled.add( result = this.target.extractItems( extra, Actionable.MODULATE, src ) );

				if( result == null || result.getStackSize() != extra.getStackSize() )
				{
					failed = true;
					break;
				}
			}
		}

		if( failed )
		{
			for( IAEItemStack is : added )
			{
				this.target.extractItems( is, Actionable.MODULATE, src );
			}

			for( IAEItemStack is : pulled )
			{
				this.target.injectItems( is, Actionable.MODULATE, src );
			}

			return false;
		}

		if( this.logMissing && this.par != null )
		{
			for( IAEItemStack extra : this.missingCache )
			{
				this.par.addMissing( extra );
			}
		}

		return true;
	}

	public void addMissing( IAEItemStack extra )
	{
		this.missingCache.add( extra );
	}

	public void ignore( IAEItemStack what )
	{
		IAEItemStack list = this.localCache.findPrecise( what );
		if( list != null )
		{
			list.setStackSize( 0 );
		}
	}
}
