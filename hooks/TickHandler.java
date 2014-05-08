package appeng.hooks;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;

import net.minecraftforge.event.world.WorldEvent;
import appeng.api.networking.IGridNode;
import appeng.core.AELog;
import appeng.me.Grid;
import appeng.me.NetworkList;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;

public class TickHandler
{

	class HandlerRep
	{

		public Queue<AEBaseTile> tiles = new LinkedList();

		public Collection<Grid> networks = new NetworkList();

		public void clear()
		{
			tiles = new LinkedList();
			networks = new NetworkList();
		}

	};

	final public static TickHandler instance = new TickHandler();

	final private Queue<Callable> callQueue = new LinkedList();

	final private HandlerRep server = new HandlerRep();
	final private HandlerRep client = new HandlerRep();

	HandlerRep getRepo()
	{
		if ( Platform.isServer() )
			return server;
		return client;
	}

	public void addCallable(Callable c)
	{
		callQueue.add( c );
	}

	public void addInit(AEBaseTile tile)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			getRepo().tiles.add( tile );
	}

	public void addNetwork(Grid grid)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			getRepo().networks.add( grid );
	}

	public void removeNetwork(Grid grid)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			getRepo().networks.remove( grid );
	}

	public Iterable<Grid> getGridList()
	{
		return getRepo().networks;
	}

	public void shutdown()
	{
		getRepo().clear();
	}

	@SubscribeEvent
	public void unloadWorld(WorldEvent.Unload ev)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
		{
			LinkedList<IGridNode> toDestroy = new LinkedList();

			for (Grid g : getRepo().networks)
			{
				for (IGridNode n : g.getNodes())
				{
					if ( n.getWorld() == ev.world )
						toDestroy.add( n );
				}
			}

			for (IGridNode n : toDestroy)
				n.destroy();
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent ev)
	{
		if ( ev.type == Type.SERVER && ev.phase == Phase.END ) // for no there is no reason to care about this on the client...
		{
			HandlerRep repo = getRepo();
			while (!repo.tiles.isEmpty())
			{
				AEBaseTile bt = repo.tiles.poll();
				bt.onReady();
			}

			for (Grid g : getRepo().networks)
			{
				g.update();
			}

			Callable c = null;
			while ((c = callQueue.poll()) != null)
			{
				try
				{
					c.call();
				}
				catch (Exception e)
				{
					AELog.error( e );
				}
			}
		}
	}

}
