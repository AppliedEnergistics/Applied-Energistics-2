package appeng.me.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.storage.data.IAEStack;
import appeng.me.cache.GridStorageCache;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class ItemWatcher implements IStackWatcher
{

	class ItemWatcherIterator implements Iterator<IAEStack>
	{

		final ItemWatcher watcher;
		final Iterator<IAEStack> interestIterator;
		IAEStack myLast;

		public ItemWatcherIterator(ItemWatcher parent, Iterator<IAEStack> i) {
			watcher = parent;
			interestIterator = i;
		}

		@Override
		public boolean hasNext()
		{
			return interestIterator.hasNext();
		}

		@Override
		public IAEStack next()
		{
			return myLast = interestIterator.next();
		}

		@Override
		public void remove()
		{
			gsc.interestManager.remove( myLast, watcher );
			interestIterator.remove();
		}

	};

	GridStorageCache gsc;
	IStackWatcherHost myObject;
	HashSet<IAEStack> myInterests = new HashSet();

	public ItemWatcher(GridStorageCache cache, IStackWatcherHost host) {
		gsc = cache;
		myObject = host;
	}

	public IStackWatcherHost getHost()
	{
		return myObject;
	}

	@Override
	public boolean add(IAEStack e)
	{
		if ( myInterests.contains( e ) )
			return false;

		return myInterests.add( e.copy() ) && gsc.interestManager.put( e, this );
	}

	@Override
	public boolean addAll(Collection<? extends IAEStack> c)
	{
		boolean didChange = false;

		for (IAEStack o : c)
			didChange = add( o ) || didChange;

		return didChange;
	}

	@Override
	public void clear()
	{
		Iterator<IAEStack> i = myInterests.iterator();
		while (i.hasNext())
		{
			gsc.interestManager.remove( i.next(), this );
			i.remove();
		}
	}

	@Override
	public boolean contains(Object o)
	{
		return myInterests.contains( o );
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return myInterests.containsAll( c );
	}

	@Override
	public boolean isEmpty()
	{
		return myInterests.isEmpty();
	}

	@Override
	public Iterator<IAEStack> iterator()
	{
		return new ItemWatcherIterator( this, myInterests.iterator() );
	}

	@Override
	public boolean remove(Object o)
	{
		return myInterests.remove( o ) && gsc.interestManager.remove( (IAEStack)o, this );
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean didSomething = false;
		for (Object o : c)
			didSomething = remove( o ) || didSomething;
		return didSomething;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean changed = false;
		Iterator<IAEStack> i = iterator();

		while (i.hasNext())
		{
			if ( !c.contains( i.next() ) )
			{
				i.remove();
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public int size()
	{
		return myInterests.size();
	}

	@Override
	public Object[] toArray()
	{
		return myInterests.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return myInterests.toArray( a );
	}

}
