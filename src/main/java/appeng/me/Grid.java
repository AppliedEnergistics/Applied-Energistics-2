package appeng.me;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import appeng.api.AEApi;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.events.MENetworkEvent;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.WorldSettings;
import appeng.hooks.TickHandler;
import appeng.util.ReadOnlyCollection;

public class Grid implements IGrid
{

	GridStorage myStorage;

	final NetworkEventBus bus = new NetworkEventBus();
	final HashMap<Class<? extends IGridHost>, Set> Machines = new HashMap<Class<? extends IGridHost>, Set>();
	final HashMap<Class<? extends IGridCache>, GridCacheWrapper> caches = new HashMap<Class<? extends IGridCache>, GridCacheWrapper>();

	GridNode pivot;
	int isImportant; // how import is this network?

	public Grid(GridNode center) {
		this.pivot = center;

		HashMap<Class<? extends IGridCache>, IGridCache> myCaches = AEApi.instance().registries().gridCache().createCacheInstance( this );
		for (Entry<Class<? extends IGridCache>, IGridCache> c : myCaches.entrySet())
		{
			bus.readClass( c.getKey(), c.getValue().getClass() );
			caches.put( c.getKey(), new GridCacheWrapper( c.getValue() ) );
		}

		postEvent( new MENetworkPostCacheConstruction() );

		TickHandler.instance.addNetwork( this );
		center.setGrid( this );
	}

	public Set<Class<? extends IGridHost>> getMachineClasses()
	{
		return Machines.keySet();
	}

	@Override
	public IGridNode getPivot()
	{
		return pivot;
	}

	public int size()
	{
		int out = 0;
		for (Collection<?> x : Machines.values())
			out += x.size();
		return out;
	}

	public void remove(GridNode gridNode)
	{
		for (IGridCache c : caches.values())
			c.removeNode( gridNode, gridNode.getMachine() );

		Collection<IGridNode> nodes = Machines.get( gridNode.getMachineClass() );
		if ( nodes != null )
			nodes.remove( gridNode );

		gridNode.setGridStorage( null );

		if ( pivot == gridNode )
		{
			Iterator<IGridNode> n = getNodes().iterator();
			if ( n.hasNext() )
				pivot = (GridNode) n.next();
			else
			{
				pivot = null;
				TickHandler.instance.removeNetwork( this );
				myStorage.remove();
			}
		}
	}

	public void add(GridNode gridNode)
	{
		Class<? extends IGridHost> mClass = gridNode.getMachineClass();
		Set<IGridNode> nodes = Machines.get( mClass );
		if ( nodes == null )
		{
			Machines.put( mClass, nodes = new MachineSet( mClass ) );
			bus.readClass( mClass, mClass );
		}

		// handle loading grid storages.
		if ( gridNode.getGridStorage() != null )
		{
			GridStorage gs = gridNode.getGridStorage();

			if ( gs.getGrid() == null )
			{
				myStorage = gs;
				myStorage.setGrid( this );

				for (IGridCache gc : caches.values())
					gc.onJoin( myStorage );
			}
			else if ( gs.getGrid() != this )
			{
				if ( myStorage == null )
				{
					myStorage = WorldSettings.getInstance().getNewGridStorage();
					myStorage.setGrid( this );
				}

				GridStorage tmp = new GridStorage();
				if ( !gs.hasDivided( myStorage ) )
				{
					gs.addDivided( myStorage );

					for (IGridCache gc : ((Grid) gs.getGrid()).caches.values())
						gc.onSplit( tmp );

					for (IGridCache gc : caches.values())
						gc.onJoin( tmp );
				}
			}
		}
		else if ( myStorage == null )
		{
			myStorage = WorldSettings.getInstance().getNewGridStorage();
			myStorage.setGrid( this );
		}

		// update grid node...
		gridNode.setGridStorage( myStorage );

		// track node.
		nodes.add( gridNode );

		for (IGridCache c : caches.values())
			c.addNode( gridNode, gridNode.getMachine() );

		gridNode.gridProxy.gridChanged();
		// postEventTo( gridNode, networkChanged );
	}

	@Override
	public IReadOnlyCollection<IGridNode> getNodes()
	{
		return new NodeIterable( Machines );
	}

	@Override
	public IReadOnlyCollection<Class<? extends IGridHost>> getMachinesClasses()
	{
		return new ReadOnlyCollection<Class<? extends IGridHost>>( Machines.keySet() );
	}

	@Override
	public IMachineSet getMachines(Class<? extends IGridHost> c)
	{
		MachineSet s = (MachineSet) Machines.get( c );
		if ( s == null )
			return new MachineSet( c );
		return s;
	}

	@Override
	public <C extends IGridCache> C getCache(Class<? extends IGridCache> iface)
	{
		return (C) caches.get( iface ).myCache;
	}

	@Override
	public MENetworkEvent postEventTo(IGridNode node, MENetworkEvent ev)
	{
		return bus.postEventTo( this, (GridNode) node, ev );
	}

	@Override
	public MENetworkEvent postEvent(MENetworkEvent ev)
	{
		return bus.postEvent( this, ev );
	}

	public void requestSave()
	{
		myStorage.markDirty();
		WorldSettings.getInstance().save();
	}

	public void update()
	{
		for (IGridCache gc : caches.values())
		{
			// are there any nodes left?
			if ( pivot != null )
				gc.onUpdateTick();
		}
	}

	public Iterable<GridCacheWrapper> getCacheWrappers()
	{
		return caches.values();
	}

	@Override
	public boolean isEmpty()
	{
		return pivot == null;
	}

	public void saveState()
	{
		for (IGridCache c : caches.values())
		{
			c.populateGridStorage( myStorage );
		}
	}

	public void setImportantFlag(int i, boolean publicHasPower)
	{
		int flag = 1 << i;
		isImportant = (isImportant & ~flag) | (publicHasPower ? flag : 0);
	}

}
