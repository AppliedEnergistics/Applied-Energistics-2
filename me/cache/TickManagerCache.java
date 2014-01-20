package appeng.me.cache;

import java.util.HashMap;
import java.util.PriorityQueue;

import net.minecraft.tileentity.TileEntity;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.core.AELog;
import appeng.me.cache.helpers.TickTracker;

public class TickManagerCache implements ITickManager
{

	private long currentTick = 0;

	final IGrid myGrid;

	public TickManagerCache(IGrid g) {
		myGrid = g;
	}

	HashMap<IGridNode, TickTracker> alertable = new HashMap<IGridNode, TickTracker>();

	HashMap<IGridNode, TickTracker> sleeping = new HashMap<IGridNode, TickTracker>();
	HashMap<IGridNode, TickTracker> awake = new HashMap<IGridNode, TickTracker>();

	PriorityQueue<TickTracker> upcomingTicks = new PriorityQueue<TickTracker>();

	public long getCurrentTick()
	{
		return currentTick;
	}

	public long getAvgNanoTime(IGridNode node)
	{
		TickTracker tt = awake.get( node );

		if ( tt == null )
			tt = sleeping.get( node );

		if ( tt == null )
			return -1;

		return tt.getAvgNanos();
	}

	@Override
	public void onUpdateTick(IGrid grid)
	{
		currentTick++;
		while (!upcomingTicks.isEmpty())
		{
			TickTracker tt = upcomingTicks.peek();
			int diff = (int) (currentTick - tt.lastTick);
			if ( diff >= tt.current_rate )
			{
				// remove tt..
				upcomingTicks.poll();
				TickRateModulation mod = tt.gt.tickingRequest( tt.node, diff );

				switch (mod)
				{
				case FASTER:
					tt.setRate( tt.current_rate - 1 );
					break;
				case IDLE:
					tt.setRate( tt.request.maxTickRate );
					break;
				case SAME:
					break;
				case SLEEP:
					sleepDevice( tt.node );
					break;
				case SLOWER:
					tt.setRate( tt.current_rate + 1 );
					break;
				case URGENT:
					tt.setRate( 0 );
					break;
				default:
					break;
				}

				if ( awake.containsKey( tt.node ) )
					addToQueue( tt );
			}
			else
				return; // done!
		}
	}

	private void addToQueue(TickTracker tt)
	{
		tt.lastTick = currentTick;
		upcomingTicks.add( tt );
	}

	@Override
	public boolean alertDevice(IGridNode node)
	{
		TickTracker tt = alertable.get( node );
		if ( tt == null )
			throw new RuntimeException( "Invalid Alertted device, this node is not marked as alertable, or part of this grid." );

		tt.lastTick = -tt.request.maxTickRate;
		addToQueue( tt );
		return true;
	}

	@Override
	public boolean sleepDevice(IGridNode node)
	{
		if ( awake.containsKey( node ) )
		{
			TickTracker gt = awake.get( node );
			awake.remove( node );
			sleeping.put( node, gt );

			return true;
		}

		return false;
	}

	@Override
	public boolean wakeDevice(IGridNode node)
	{
		if ( sleeping.containsKey( node ) )
		{
			TickTracker gt = sleeping.get( node );
			sleeping.remove( node );
			awake.put( node, gt );
			addToQueue( gt );

			return true;
		}

		return false;
	}

	@Override
	public void removeNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof IGridTickable )
		{
			alertable.remove( gridNode );
			sleeping.remove( gridNode );
			awake.remove( gridNode );
		}
	}

	@Override
	public void addNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof IGridTickable )
		{
			TickingRequest tr = ((IGridTickable) machine).getTickingRequest( gridNode );
			if ( tr != null )
			{
				if ( machine instanceof TileEntity && ((TileEntity) machine).canUpdate() )
					AELog.warning( "Tile: " + machine.getClass().getName() + " - reports to use MC ticking, but also requests Network Based Ticking." );

				TickTracker tt = new TickTracker( tr, gridNode, (IGridTickable) machine, currentTick, this );

				if ( tr.canBeAlerted )
					alertable.put( gridNode, tt );

				if ( tr.isSleeping )
					sleeping.put( gridNode, tt );
				else
				{
					awake.put( gridNode, tt );
					addToQueue( tt );
				}

			}
		}
	}

	@Override
	public void onSplit(IGridStorage storageB)
	{

	}

	@Override
	public void onJoin(IGridStorage storageB)
	{

	}

	@Override
	public void populateGridStorage(IGridStorage storage)
	{

	}
}
