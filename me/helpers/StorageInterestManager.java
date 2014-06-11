package appeng.me.helpers;

import java.util.LinkedList;
import java.util.Set;

import appeng.api.storage.data.IAEStack;
import appeng.me.storage.ItemWatcher;

import com.google.common.collect.SetMultimap;

public class StorageInterestManager {
	
	class SavedTransactions {
		public final boolean put;
		public final IAEStack stack;
		public final ItemWatcher iw;		

		public SavedTransactions( boolean putOperation, IAEStack myStack, ItemWatcher watcher )
		{
			put = putOperation;
			stack = myStack;
			iw = watcher;
		}
	};

	private final SetMultimap<IAEStack, ItemWatcher>  container;
	private LinkedList<SavedTransactions> transactions = null;
	
	public StorageInterestManager(SetMultimap<IAEStack, ItemWatcher> interests) {
		container = interests;
	}

	public void enableTransactions()
	{
		transactions = new LinkedList();
	}

	public void disableTransactions()
	{
		LinkedList<SavedTransactions>  myActions = transactions;
		transactions = null;

		for ( SavedTransactions t : myActions )
		{
			if ( t.put )
				put( t.stack, t.iw );
			else
				remove( t.stack, t.iw );
		}
	}
	
	public boolean containsKey( IAEStack stack )
	{
		return container.containsKey( stack );
	}
	
	public Set<ItemWatcher> get( IAEStack stack )
	{
		return container.get( stack );
	}

	public boolean put( IAEStack stack, ItemWatcher iw )
	{
		if ( transactions != null )
		{
			transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return container.put( stack, iw );
	}

	public boolean remove( IAEStack stack, ItemWatcher iw )
	{
		if (  transactions != null )
		{
			transactions.add( new SavedTransactions( true, stack, iw ) );
			return true;
		}
		else
			return container.remove( stack, iw );
	}

}
