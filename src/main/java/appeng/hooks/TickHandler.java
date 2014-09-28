package appeng.hooks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.parts.CableRenderMode;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.sync.packets.PacketPaintedEntity;
import appeng.crafting.CraftingJob;
import appeng.entity.EntityFloatingItem;
import appeng.me.Grid;
import appeng.me.NetworkList;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

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

	}

	final public static TickHandler instance = new TickHandler();

	final private WeakHashMap<World, Queue<Callable>> callQueue = new WeakHashMap<World, Queue<Callable>>();
	Queue<Callable> serverQueue = new LinkedList<Callable>();

	final private HandlerRep server = new HandlerRep();
	final private HandlerRep client = new HandlerRep();

	static public class PlayerColor
	{

		public final AEColor myColor;
		protected final int myEntity;
		protected int ticksLeft;

		public PacketPaintedEntity getPacket()
		{
			return new PacketPaintedEntity( myEntity, myColor, ticksLeft );
		}

		public PlayerColor(int id, AEColor col, int ticks) {
			myEntity = id;
			myColor = col;
			ticksLeft = ticks;
		}

	}

	final private HashMap<Integer, PlayerColor> cliPlayerColors = new HashMap();
	final private HashMap<Integer, PlayerColor> srvPlayerColors = new HashMap();

	public HashMap<Integer, PlayerColor> getPlayerColors()
	{
		if ( Platform.isServer() )
			return srvPlayerColors;
		return cliPlayerColors;
	}

	private void tickColors(HashMap<Integer, PlayerColor> playerSet)
	{
		Iterator<PlayerColor> i = playerSet.values().iterator();
		while (i.hasNext())
		{
			PlayerColor pc = i.next();
			if ( pc.ticksLeft-- <= 0 )
				i.remove();
		}
	}

	HandlerRep getRepo()
	{
		if ( Platform.isServer() )
			return server;
		return client;
	}

	public void addCallable(World w, Callable c)
	{
		if ( w == null )
			serverQueue.add( c );
		else
		{
			Queue<Callable> queue = callQueue.get( w );

			if ( queue == null )
				callQueue.put( w, queue = new LinkedList<Callable>() );

			queue.add( c );
		}
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
	public void onChunkLoad(ChunkEvent.Load load)
	{
		for (Object te : load.getChunk().chunkTileEntityMap.values())
		{
			if ( te instanceof AEBaseTile )
			{
				((AEBaseTile) te).onChunkLoad();
			}
		}
	}

	CableRenderMode crm = CableRenderMode.Standard;

	@SubscribeEvent
	public void onTick(TickEvent ev)
	{

		if ( ev.type == Type.CLIENT && ev.phase == Phase.START )
		{
			tickColors( cliPlayerColors );
			EntityFloatingItem.ageStatic = (EntityFloatingItem.ageStatic + 1) % 60000;
			CableRenderMode currentMode = AEApi.instance().partHelper().getCableRenderMode();
			if ( currentMode != crm )
			{
				crm = currentMode;
				CommonHelper.proxy.triggerUpdates();
			}
		}

		// rwar!
		if ( ev.type == Type.WORLD && ev.phase == Phase.END )
		{
			WorldTickEvent wte = (WorldTickEvent) ev;
			synchronized (craftingJobs)
			{
				Collection<CraftingJob> jobSet = craftingJobs.get( wte.world );
				if ( !jobSet.isEmpty() )
				{
					int simTime = Math.max( 1, AEConfig.instance.craftingCalculationTimePerTick / jobSet.size() );
					Iterator<CraftingJob> i = jobSet.iterator();
					while (i.hasNext())
					{
						CraftingJob cj = i.next();
						if ( !cj.simulateFor( simTime ) )
							i.remove();
					}
				}
			}
		}

		// for no there is no reason to care about this on the client...
		else if ( ev.type == Type.SERVER && ev.phase == Phase.END )
		{
			tickColors( srvPlayerColors );
			// ready tiles.
			HandlerRep repo = getRepo();
			while (!repo.tiles.isEmpty())
			{
				AEBaseTile bt = repo.tiles.poll();
				if ( !bt.isInvalid() )
					bt.onReady();
			}

			// tick networks.
			for (Grid g : getRepo().networks)
				g.update();

			// cross world queue.
			processQueue( serverQueue );
		}

		// world synced queue(s)
		if ( ev.type == Type.WORLD && ev.phase == Phase.START )
		{
			processQueue( callQueue.get( ((WorldTickEvent) ev).world ) );
		}
	}

	private void processQueue(Queue<Callable> queue)
	{
		if ( queue == null )
			return;

		Stopwatch sw = Stopwatch.createStarted();

		Callable c = null;
		while ((c = queue.poll()) != null)
		{
			try
			{
				c.call();

				if ( sw.elapsed( TimeUnit.MILLISECONDS ) > 50 )
					break;
			}
			catch (Exception e)
			{
				AELog.error( e );
			}
		}

		// long time = sw.elapsed( TimeUnit.MILLISECONDS );
		// if ( time > 0 )
		// AELog.info( "processQueue Time: " + time + "ms" );
	}

	Multimap<World, CraftingJob> craftingJobs = LinkedListMultimap.create();

	public void registerCraftingSimulation(World world, CraftingJob craftingJob)
	{
		synchronized (craftingJobs)
		{
			craftingJobs.put( world, craftingJob );
		}
	}

}
