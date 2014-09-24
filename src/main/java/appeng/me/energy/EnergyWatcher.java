package appeng.me.energy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.me.cache.EnergyGridCache;

/**
 * Maintain my interests, and a global watch list, they should always be fully synchronized.
 */
public class EnergyWatcher implements IEnergyWatcher
{

	class EnergyWatcherIterator implements Iterator<Double>
	{

		final EnergyWatcher watcher;
		final Iterator<EnergyThreshold> interestIterator;
		EnergyThreshold myLast;

		public EnergyWatcherIterator(EnergyWatcher parent, Iterator<EnergyThreshold> i) {
			watcher = parent;
			interestIterator = i;
		}

		@Override
		public boolean hasNext()
		{
			return interestIterator.hasNext();
		}

		@Override
		public Double next()
		{
			myLast = interestIterator.next();
			return myLast.Limit;
		}

		@Override
		public void remove()
		{
			gsc.interests.remove( myLast );
			interestIterator.remove();
		}

	};

	EnergyGridCache gsc;
	IEnergyWatcherHost myObject;
	HashSet<EnergyThreshold> myInterests = new HashSet();

	public void post(EnergyGridCache energyGridCache)
	{
		myObject.onThresholdPass( energyGridCache );
	}

	public EnergyWatcher(EnergyGridCache cache, IEnergyWatcherHost host) {
		gsc = cache;
		myObject = host;
	}

	public IEnergyWatcherHost getHost()
	{
		return myObject;
	}

	@Override
	public boolean add(Double e)
	{
		if ( myInterests.contains( e ) )
			return false;

		EnergyThreshold eh = new EnergyThreshold( e, this );
		return gsc.interests.add( eh ) && myInterests.add( eh );
	}

	@Override
	public boolean addAll(Collection<? extends Double> c)
	{
		boolean didChange = false;

		for (Double o : c)
			didChange = add( o ) || didChange;

		return didChange;
	}

	@Override
	public void clear()
	{
		Iterator<EnergyThreshold> i = myInterests.iterator();
		while (i.hasNext())
		{
			gsc.interests.remove( i.next() );
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
	public Iterator<Double> iterator()
	{
		return new EnergyWatcherIterator( this, myInterests.iterator() );
	}

	@Override
	public boolean remove(Object o)
	{
		EnergyThreshold eh = new EnergyThreshold( (Double) o, this );
		return myInterests.remove( eh ) && gsc.interests.remove( eh );
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
		Iterator<Double> i = iterator();

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
