package appeng.me.storage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.cache.SecurityCache;

public class NetworkInventoryHandler<T extends IAEStack<T>> implements IMEInventoryHandler<T>
{

	private final static Comparator prioritySorter = new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2)
		{
			return o2 - o1;
		}

	};

	final StorageChannel myChannel;
	final SecurityCache security;

	// final TreeMultimap<Integer, IMEInventoryHandler<T>> prorityInventory;
	final TreeMap<Integer, List<IMEInventoryHandler<T>>> prorityInventory;

	public NetworkInventoryHandler(StorageChannel chan, SecurityCache security) {
		myChannel = chan;
		this.security = security;
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
	static final ThreadLocal<LinkedList> depth = new ThreadLocal<LinkedList>();

	private LinkedList getDepth()
	{
		LinkedList s = depth.get();

		if ( s == null )
			depth.set( s = new LinkedList() );

		return s;
	}

	private boolean diveList(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		LinkedList cDepth = getDepth();
		if ( cDepth.contains( networkInventoryHandler ) )
			return true;

		cDepth.push( this );
		return false;
	}

	private boolean diveIteration(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		if ( getDepth().isEmpty() )
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

		getDepth().push( this );
		return false;
	}

	private void surface(NetworkInventoryHandler<T> networkInventoryHandler)
	{
		if ( getDepth().pop() != this )
			throw new RuntimeException( "Invalid Access to Networked Storage API detected." );
	}

	private boolean testPermission(BaseActionSource src, SecurityPermissions permission)
	{
		if ( src.isPlayer() )
		{
			if ( !security.hasPermission( ((PlayerSource) src).player, permission ) )
				return true;
		}
		else if ( src.isMachine() )
		{
			if ( security.isAvailable() )
			{
				IGridNode n = ((MachineSource) src).via.getActionableNode();
				if ( n == null )
					return true;

				IGrid gn = n.getGrid();
				if ( gn != security.myGrid )
				{
					int playerID = -1;

					ISecurityGrid sg = gn.getCache( ISecurityGrid.class );
					playerID = sg.getOwner();

					if ( !security.hasPermission( playerID, permission ) )
						return true;
				}
			}
		}

		return false;
	}

	@Override
	public T injectItems(T input, Actionable type, BaseActionSource src)
	{
		if ( diveList( this ) )
			return input;

		if ( testPermission( src, SecurityPermissions.INJECT ) )
		{
			surface( this );
			return input;
		}

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

		if ( testPermission( src, SecurityPermissions.EXTRACT ) )
		{
			surface( this );
			return null;
		}

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

	@Override
	public boolean validForPass(int i)
	{
		return true;
	}

}
