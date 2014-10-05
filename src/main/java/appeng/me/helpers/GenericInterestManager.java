package appeng.me.helpers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import appeng.api.storage.data.IAEStack;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

public class GenericInterestManager<T>
{

	class SavedTransactions
	{

		public final boolean put;
		public final IAEStack stack;
		public final T iw;

		public SavedTransactions(boolean putOperation, IAEStack myStack, T watcher) {
			put = putOperation;
			stack = myStack;
			iw = watcher;
		}
	}

	private final Multimap<IAEStack, T> container;
	private LinkedList<SavedTransactions> transactions = null;
	private int transDepth = 0;

	public GenericInterestManager(Multimap<IAEStack, T> interests) {
		container = interests;
	}

	public void enableTransactions()
	{
		if ( transDepth == 0 )
			transactions = new LinkedList<SavedTransactions>();

		transDepth++;
	}

	public void disableTransactions()
	{
		transDepth--;

		if ( transDepth == 0 )
		{
			LinkedList<SavedTransactions> myActions = transactions;
			transactions = null;

			for (SavedTransactions t : myActions)
			{
				if ( t.put )
					put( t.stack, t.iw );
				else
					remove( t.stack, t.iw );
			}
		}
	}

	public boolean containsKey(IAEStack stack)
	{
		return container.containsKey( stack );
	}

	public Collection<T> get(IAEStack stack)
	{
		return container.get( stack );
	}

	public boolean put(IAEStack stack, T iw)
	{
		if ( transactions != null )
		{
			transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return container.put( stack, iw );
	}

	public boolean remove(IAEStack stack, T iw)
	{
		if ( transactions != null )
		{
			transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return container.remove( stack, iw );
	}

}
