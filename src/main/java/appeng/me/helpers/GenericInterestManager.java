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

package appeng.me.helpers;


import java.util.Collection;
import java.util.LinkedList;

import com.google.common.collect.Multimap;

import appeng.api.storage.data.IAEStack;


public class GenericInterestManager<T>
{

	private final Multimap<IAEStack, T> container;
	private LinkedList<SavedTransactions> transactions = null;
	private int transDepth = 0;

	public GenericInterestManager( Multimap<IAEStack, T> interests )
	{
		this.container = interests;
	}

	public void enableTransactions()
	{
		if( this.transDepth == 0 )
			this.transactions = new LinkedList<SavedTransactions>();

		this.transDepth++;
	}

	public void disableTransactions()
	{
		this.transDepth--;

		if( this.transDepth == 0 )
		{
			LinkedList<SavedTransactions> myActions = this.transactions;
			this.transactions = null;

			for( SavedTransactions t : myActions )
			{
				if( t.put )
					this.put( t.stack, t.iw );
				else
					this.remove( t.stack, t.iw );
			}
		}
	}

	public boolean put( IAEStack stack, T iw )
	{
		if( this.transactions != null )
		{
			this.transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return this.container.put( stack, iw );
	}

	public boolean remove( IAEStack stack, T iw )
	{
		if( this.transactions != null )
		{
			this.transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return this.container.remove( stack, iw );
	}

	public boolean containsKey( IAEStack stack )
	{
		return this.container.containsKey( stack );
	}

	public Collection<T> get( IAEStack stack )
	{
		return this.container.get( stack );
	}

	class SavedTransactions
	{

		public final boolean put;
		public final IAEStack stack;
		public final T iw;

		public SavedTransactions( boolean putOperation, IAEStack myStack, T watcher )
		{
			this.put = putOperation;
			this.stack = myStack;
			this.iw = watcher;
		}
	}
}
