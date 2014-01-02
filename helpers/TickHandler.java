package appeng.helpers;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent;
import appeng.api.networking.IGridNode;
import appeng.me.Grid;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler implements ITickHandler
{

	class HandlerRep
	{

		public Queue<AEBaseTile> tiles = new LinkedList();

		public LinkedList<Grid> networks = new LinkedList();

		public void clear()
		{
			tiles = new LinkedList();
			networks = new LinkedList();
		}

	};

	final public static TickHandler instance = new TickHandler();

	final private HandlerRep server = new HandlerRep();
	final private HandlerRep client = new HandlerRep();

	HandlerRep getRepo()
	{
		if ( Platform.isServer() )
			return server;
		return client;
	}

	public void addInit(AEBaseTile tile)
	{
		getRepo().tiles.add( tile );
	}

	public void addNetwork(Grid grid)
	{
		if ( Platform.isServer() )
			getRepo().networks.add( grid );
	}

	public void removeNetwork(Grid grid)
	{
		if ( Platform.isServer() )
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

	@ForgeSubscribe
	public void unloadWorld(WorldEvent.Unload ev)
	{
		if ( Platform.isServer() )
		{
			while (!getRepo().networks.isEmpty())
			{
				Grid g = getRepo().networks.poll();
				for (IGridNode n : g.getNodes())
				{
					if ( n.getWorld() == ev.world )
						n.destroy();
				}
			}
		}
	}

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData)
	{

	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData)
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
	}

	@Override
	public EnumSet<TickType> ticks()
	{
		return EnumSet.of( TickType.WORLD );
	}

	@Override
	public String getLabel()
	{
		return "AE-TickHandler";
	}
}
