package appeng.me.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

public class NetworkInventoryHandler<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	private final static Comparator prioritySorter = new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2)
		{
			return o1 - o2;
		}

	};

	final StorageChannel myChannel;
	// final TreeMultimap<Integer, IMEInventoryHandler<T>> prorityInventory;
	final TreeMap<Integer, List<IMEInventoryHandler<T>>> prorityInventory;

	public NetworkInventoryHandler(StorageChannel chan) {
		myChannel = chan;
		prorityInventory = new TreeMap( prioritySorter ); // TreeMultimap.create( prioritySorter, hashSorter );
	}

	public void addNewStorage(IMEInventoryHandler<T> h)
	{
		int priority = h.getPriority();
		List<IMEInventoryHandler<T>> list = prorityInventory.get( priority );
		if ( list == null )
			prorityInventory.put( priority, list = new ArrayList() );

		list.add( h );
	}

	static int currentPass = 0;
	int myPass = 0;
	final static public LinkedList depth = new LinkedList();

	private boolean diveList(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		if ( depth.contains( networkInventoryHandler ) )
			return true;

		depth.push( this );
		return false;
	}

	private boolean diveIteration(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		if ( depth.isEmpty() )
		{
			currentPass++;
			myPass = currentPass;
		}
		else
		{
			if ( currentPass == myPass )
				return true;
			else
				myPass = currentPass;
		}

		depth.push( this );
		return false;
	}

	private void surface(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		Object last = depth.pop();
		if ( last != this )
			throw new RuntimeException( "Invalid Access to Networked Storage API detected." );
	}

	@Override
	public T injectItems(T input, Actionable type, BaseActionSource src)
	{
		if ( diveList( this ) )
			return input;

		Iterator<List<IMEInventoryHandler<T>>> i = prorityInventory.values().iterator();// asMap().entrySet().iterator();

		while (i.hasNext())
		{
			List<IMEInventoryHandler<T>> invList = i.next();

			Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
			while (ii.hasNext() && input != null)
			{
				IMEInventoryHandler<T> inv = ii.next();

				if ( inv.canAccept( input ) && (inv.extractItems( input, Actionable.SIMULATE, src ) != null || inv.isPrioritized( input )) )
					input = inv.injectItems( input, type, src );
			}

			ii = invList.iterator();
			while (ii.hasNext() && input != null)
			{
				IMEInventoryHandler<T> inv = ii.next();
				if ( inv.canAccept( input ) )
					input = inv.injectItems( input, type, src );
			}
		}

		surface( this );

		return input;
	}

	@Override
	public T extractItems(T request, Actionable mode, BaseActionSource src)
	{
		if ( diveList( this ) )
			return null;

		Iterator<List<IMEInventoryHandler<T>>> i = prorityInventory.descendingMap().values().iterator();// prorityInventory.asMap().descendingMap().entrySet().iterator();

		T output = request.copy();
		request = request.copy();
		output.setStackSize( 0 );
		long req = request.getStackSize();

		while (i.hasNext())
		{
			List<IMEInventoryHandler<T>> invList = i.next();

			Iterator<IMEInventoryHandler<T>> ii = invList.iterator();
			while (ii.hasNext() && output.getStackSize() < req)
			{
				IMEInventoryHandler<T> inv = ii.next();

				request.setStackSize( req - output.getStackSize() );
				output.add( inv.extractItems( request, mode, src ) );
			}
		}

		surface( this );

		if ( output.getStackSize() <= 0 )
			return null;

		return output;
	}

	@Override
	public IItemList<T> getAvailableItems(IItemList out)
	{
		if ( diveIteration( this ) )
			return out;

		// for (Entry<Integer, IMEInventoryHandler<T>> h : prorityInventory.entries())
		for (List<IMEInventoryHandler<T>> i : prorityInventory.values())
			for (IMEInventoryHandler<T> j : i)
				out = j.getAvailableItems( out );

		surface( this );

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return myChannel;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized(T input)
	{
		return false;
	}

	@Override
	public boolean canAccept(T input)
	{
		return true;
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

}
