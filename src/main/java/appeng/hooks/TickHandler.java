/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.hooks;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.Type;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

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

public class TickHandler
{

	static class HandlerRep
	{

		public Queue<AEBaseTile> tiles = new LinkedList<AEBaseTile>();

		public Collection<Grid> networks = new NetworkList();

		public void clear()
		{
			this.tiles = new LinkedList<AEBaseTile>();
			this.networks = new NetworkList();
		}

	}

	final public static TickHandler instance = new TickHandler();

	final private WeakHashMap<World, Queue<Callable>> callQueue = new WeakHashMap<World, Queue<Callable>>();
	final Queue<Callable> serverQueue = new LinkedList<Callable>();

	final private HandlerRep server = new HandlerRep();
	final private HandlerRep client = new HandlerRep();

	static public class PlayerColor
	{

		public final AEColor myColor;
		protected final int myEntity;
		protected int ticksLeft;

		public PacketPaintedEntity getPacket()
		{
			return new PacketPaintedEntity( this.myEntity, this.myColor, this.ticksLeft );
		}

		public PlayerColor(int id, AEColor col, int ticks) {
			this.myEntity = id;
			this.myColor = col;
			this.ticksLeft = ticks;
		}

	}

	final private HashMap<Integer, PlayerColor> cliPlayerColors = new HashMap<Integer, PlayerColor>();
	final private HashMap<Integer, PlayerColor> srvPlayerColors = new HashMap<Integer, PlayerColor>();

	public HashMap<Integer, PlayerColor> getPlayerColors()
	{
		if ( Platform.isServer() )
			return this.srvPlayerColors;
		return this.cliPlayerColors;
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
			return this.server;
		return this.client;
	}

	public void addCallable(World w, Callable c)
	{
		if ( w == null )
			this.serverQueue.add( c );
		else
		{
			Queue<Callable> queue = this.callQueue.get( w );

			if ( queue == null )
				this.callQueue.put( w, queue = new LinkedList<Callable>() );

			queue.add( c );
		}
	}

	public void addInit(AEBaseTile tile)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			this.getRepo().tiles.add( tile );
	}

	public void addNetwork(Grid grid)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			this.getRepo().networks.add( grid );
	}

	public void removeNetwork(Grid grid)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
			this.getRepo().networks.remove( grid );
	}

	public Iterable<Grid> getGridList()
	{
		return this.getRepo().networks;
	}

	public void shutdown()
	{
		this.getRepo().clear();
	}

	@SubscribeEvent
	public void unloadWorld(WorldEvent.Unload ev)
	{
		if ( Platform.isServer() ) // for no there is no reason to care about this on the client...
		{
			LinkedList<IGridNode> toDestroy = new LinkedList<IGridNode>();

			for (Grid g : this.getRepo().networks)
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
			this.tickColors( this.cliPlayerColors );
			EntityFloatingItem.ageStatic = (EntityFloatingItem.ageStatic + 1) % 60000;
			CableRenderMode currentMode = AEApi.instance().partHelper().getCableRenderMode();
			if ( currentMode != this.crm )
			{
				this.crm = currentMode;
				CommonHelper.proxy.triggerUpdates();
			}
		}

		if ( ev.type == Type.WORLD && ev.phase == Phase.END )
		{
			WorldTickEvent wte = (WorldTickEvent) ev;
			synchronized (this.craftingJobs)
			{
				Collection<CraftingJob> jobSet = this.craftingJobs.get( wte.world );
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
			this.tickColors( this.srvPlayerColors );
			// ready tiles.
			HandlerRep repo = this.getRepo();
			while (!repo.tiles.isEmpty())
			{
				AEBaseTile bt = repo.tiles.poll();
				if ( !bt.isInvalid() )
					bt.onReady();
			}

			// tick networks.
			for (Grid g : this.getRepo().networks)
				g.update();

			// cross world queue.
			this.processQueue( this.serverQueue );
		}

		// world synced queue(s)
		if ( ev.type == Type.WORLD && ev.phase == Phase.START )
		{
			this.processQueue( this.callQueue.get( ((WorldTickEvent) ev).world ) );
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

	final Multimap<World, CraftingJob> craftingJobs = LinkedListMultimap.create();

	public void registerCraftingSimulation(World world, CraftingJob craftingJob)
	{
		synchronized (this.craftingJobs)
		{
			this.craftingJobs.put( world, craftingJob );
		}
	}

}
