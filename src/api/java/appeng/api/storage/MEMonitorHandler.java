/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.storage;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;


/**
 * Common implementation of a simple class that monitors injection/extraction of a inventory to send events to a list of
 * listeners.
 *
 * @param <StackType>
 */
public class MEMonitorHandler<StackType extends IAEStack> implements IMEMonitor<StackType>
{

	private final IMEInventoryHandler<StackType> internalHandler;
	private final IItemList<StackType> cachedList;
	private final HashMap<IMEMonitorHandlerReceiver<StackType>, Object> listeners = new HashMap<IMEMonitorHandlerReceiver<StackType>, Object>();

	protected boolean hasChanged = true;

	public MEMonitorHandler( IMEInventoryHandler<StackType> t )
	{
		this.internalHandler = t;
		this.cachedList = (IItemList<StackType>) t.getChannel().createList();
	}

	public MEMonitorHandler( IMEInventoryHandler<StackType> t, StorageChannel chan )
	{
		this.internalHandler = t;
		this.cachedList = (IItemList<StackType>) chan.createList();
	}

	@Override
	public void addListener( IMEMonitorHandlerReceiver<StackType> l, Object verificationToken )
	{
		this.listeners.put( l, verificationToken );
	}

	@Override
	public void removeListener( IMEMonitorHandlerReceiver<StackType> l )
	{
		this.listeners.remove( l );
	}

	@Override
	public StackType injectItems( StackType input, Actionable mode, BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			return this.getHandler().injectItems( input, mode, src );
		}
		return this.monitorDifference( input.copy(), this.getHandler().injectItems( input, mode, src ), false, src );
	}

	protected IMEInventoryHandler<StackType> getHandler()
	{
		return this.internalHandler;
	}

	private StackType monitorDifference( IAEStack original, StackType leftOvers, boolean extraction, BaseActionSource src )
	{
		StackType diff = (StackType) original.copy();

		if( extraction )
		{
			diff.setStackSize( leftOvers == null ? 0 : -leftOvers.getStackSize() );
		}
		else if( leftOvers != null )
		{
			diff.decStackSize( leftOvers.getStackSize() );
		}

		if( diff.getStackSize() != 0 )
		{
			this.postChangesToListeners( ImmutableList.of( diff ), src );
		}

		return leftOvers;
	}

	protected void postChangesToListeners( Iterable<StackType> changes, BaseActionSource src )
	{
		this.notifyListenersOfChange( changes, src );
	}

	protected void notifyListenersOfChange( Iterable<StackType> diff, BaseActionSource src )
	{
		this.hasChanged = true;// need to update the cache.
		Iterator<Entry<IMEMonitorHandlerReceiver<StackType>, Object>> i = this.getListeners();
		while( i.hasNext() )
		{
			Entry<IMEMonitorHandlerReceiver<StackType>, Object> o = i.next();
			IMEMonitorHandlerReceiver<StackType> receiver = o.getKey();
			if( receiver.isValid( o.getValue() ) )
			{
				receiver.postChange( this, diff, src );
			}
			else
			{
				i.remove();
			}
		}
	}

	protected Iterator<Entry<IMEMonitorHandlerReceiver<StackType>, Object>> getListeners()
	{
		return this.listeners.entrySet().iterator();
	}

	@Override
	public StackType extractItems( StackType request, Actionable mode, BaseActionSource src )
	{
		if( mode == Actionable.SIMULATE )
		{
			return this.getHandler().extractItems( request, mode, src );
		}
		return this.monitorDifference( request.copy(), this.getHandler().extractItems( request, mode, src ), true, src );
	}

	@Override
	public StorageChannel getChannel()
	{
		return this.getHandler().getChannel();
	}

	@Override
	public AccessRestriction getAccess()
	{
		return this.getHandler().getAccess();
	}	@Override
	public IItemList<StackType> getStorageList()
	{
		if( this.hasChanged )
		{
			this.hasChanged = false;
			this.cachedList.resetStatus();
			return this.getAvailableItems( this.cachedList );
		}

		return this.cachedList;
	}

	@Override
	public boolean isPrioritized( StackType input )
	{
		return this.getHandler().isPrioritized( input );
	}

	@Override
	public boolean canAccept( StackType input )
	{
		return this.getHandler().canAccept( input );
	}	@Override
	public IItemList<StackType> getAvailableItems( IItemList out )
	{
		return this.getHandler().getAvailableItems( out );
	}

	@Override
	public int getPriority()
	{
		return this.getHandler().getPriority();
	}

	@Override
	public int getSlot()
	{
		return this.getHandler().getSlot();
	}

	@Override
	public boolean validForPass( int i )
	{
		return this.getHandler().validForPass( i );
	}




}
