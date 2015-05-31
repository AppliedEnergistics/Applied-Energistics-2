/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.me.cache;


import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.stats.Achievements;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ControllerChannelUpdater;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.IPathItem;
import appeng.me.pathfinding.PathSegment;
import appeng.tile.networking.TileController;
import appeng.util.Platform;


public final class PathGridCache implements IPathingGrid
{

	final LinkedList<PathSegment> active = new LinkedList<PathSegment>();
	final Set<TileController> controllers = new HashSet<TileController>();
	final Set<IGridNode> requireChannels = new HashSet<IGridNode>();
	final Set<IGridNode> blockDense = new HashSet<IGridNode>();
	final IGrid myGrid;
	public int channelsInUse = 0;
	public int channelsByBlocks = 0;
	public double channelPowerUsage = 0.0;
	boolean recalculateControllerNextTick = true;
	boolean updateNetwork = true;
	boolean booting = false;
	ControllerState controllerState = ControllerState.NO_CONTROLLER;
	int instance = Integer.MIN_VALUE;
	int ticksUntilReady = 20;
	int lastChannels = 0;
	private HashSet<IPathItem> semiOpen = new HashSet<IPathItem>();

	public PathGridCache( IGrid g )
	{
		this.myGrid = g;
	}

	@Override
	public final void onUpdateTick()
	{
		if( this.recalculateControllerNextTick )
		{
			this.recalcController();
		}

		if( this.updateNetwork )
		{
			if( !this.booting )
			{
				this.myGrid.postEvent( new MENetworkBootingStatusChange() );
			}

			this.booting = true;
			this.updateNetwork = false;
			this.instance++;
			this.channelsInUse = 0;

			if( !AEConfig.instance.isFeatureEnabled( AEFeature.Channels ) )
			{
				int used = this.calculateRequiredChannels();

				int nodes = this.myGrid.getNodes().size();
				this.ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				this.channelsByBlocks = nodes * used;
				this.channelPowerUsage = this.channelsByBlocks / 128.0;

				this.myGrid.getPivot().beginVisit( new AdHocChannelUpdater( used ) );
			}
			else if( this.controllerState == ControllerState.NO_CONTROLLER )
			{
				int requiredChannels = this.calculateRequiredChannels();
				int used = requiredChannels;
				if( requiredChannels > 8 )
				{
					used = 0;
				}

				int nodes = this.myGrid.getNodes().size();
				this.channelsInUse = used;

				this.ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				this.channelsByBlocks = nodes * used;
				this.channelPowerUsage = this.channelsByBlocks / 128.0;

				this.myGrid.getPivot().beginVisit( new AdHocChannelUpdater( used ) );
			}
			else if( this.controllerState == ControllerState.CONTROLLER_CONFLICT )
			{
				this.ticksUntilReady = 20;
				this.myGrid.getPivot().beginVisit( new AdHocChannelUpdater( 0 ) );
			}
			else
			{
				int nodes = this.myGrid.getNodes().size();
				this.ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				HashSet<IPathItem> closedList = new HashSet<IPathItem>();
				this.semiOpen = new HashSet<IPathItem>();

				// myGrid.getPivot().beginVisit( new AdHocChannelUpdater( 0 )
				// );
				for( IGridNode node : this.myGrid.getMachines( TileController.class ) )
				{
					closedList.add( (IPathItem) node );
					for( IGridConnection gcc : node.getConnections() )
					{
						GridConnection gc = (GridConnection) gcc;
						if( !( gc.getOtherSide( node ).getMachine() instanceof TileController ) )
						{
							List<IPathItem> open = new LinkedList<IPathItem>();
							closedList.add( gc );
							open.add( gc );
							gc.setControllerRoute( (GridNode) node, true );
							this.active.add( new PathSegment( this, open, this.semiOpen, closedList ) );
						}
					}
				}
			}
		}

		if( !this.active.isEmpty() || this.ticksUntilReady > 0 )
		{
			Iterator<PathSegment> i = this.active.iterator();
			while( i.hasNext() )
			{
				PathSegment pat = i.next();
				if( pat.step() )
				{
					pat.isDead = true;
					i.remove();
				}
			}

			this.ticksUntilReady--;

			if( this.active.isEmpty() && this.ticksUntilReady <= 0 )
			{
				if( this.controllerState == ControllerState.CONTROLLER_ONLINE )
				{
					final Iterator<TileController> controllerIterator = this.controllers.iterator();
					if( controllerIterator.hasNext() )
					{
						final TileController controller = controllerIterator.next();
						controller.getGridNode( ForgeDirection.UNKNOWN ).beginVisit( new ControllerChannelUpdater() );
					}
				}

				// check for achievements
				this.achievementPost();

				this.booting = false;
				this.channelPowerUsage = this.channelsByBlocks / 128.0;
				this.myGrid.postEvent( new MENetworkBootingStatusChange() );
			}
		}
	}

	@Override
	public final void removeNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof TileController )
		{
			this.controllers.remove( machine );
			this.recalculateControllerNextTick = true;
		}

		EnumSet<GridFlags> flags = gridNode.getGridBlock().getFlags();

		if( flags.contains( GridFlags.REQUIRE_CHANNEL ) )
		{
			this.requireChannels.remove( gridNode );
		}

		if( flags.contains( GridFlags.CANNOT_CARRY_COMPRESSED ) )
		{
			this.blockDense.remove( gridNode );
		}

		this.repath();
	}

	@Override
	public final void addNode( IGridNode gridNode, IGridHost machine )
	{
		if( machine instanceof TileController )
		{
			this.controllers.add( (TileController) machine );
			this.recalculateControllerNextTick = true;
		}

		EnumSet<GridFlags> flags = gridNode.getGridBlock().getFlags();

		if( flags.contains( GridFlags.REQUIRE_CHANNEL ) )
		{
			this.requireChannels.add( gridNode );
		}

		if( flags.contains( GridFlags.CANNOT_CARRY_COMPRESSED ) )
		{
			this.blockDense.add( gridNode );
		}

		this.repath();
	}

	@Override
	public final void onSplit( IGridStorage storageB )
	{

	}

	@Override
	public final void onJoin( IGridStorage storageB )
	{

	}

	@Override
	public final void populateGridStorage( IGridStorage storage )
	{

	}

	private void recalcController()
	{
		this.recalculateControllerNextTick = false;
		ControllerState old = this.controllerState;

		if( this.controllers.isEmpty() )
		{
			this.controllerState = ControllerState.NO_CONTROLLER;
		}
		else
		{
			IGridNode startingNode = this.controllers.iterator().next().getGridNode( ForgeDirection.UNKNOWN );
			if( startingNode == null )
			{
				this.controllerState = ControllerState.CONTROLLER_CONFLICT;
				return;
			}

			DimensionalCoord dc = startingNode.getGridBlock().getLocation();
			ControllerValidator cv = new ControllerValidator( dc.x, dc.y, dc.z );

			startingNode.beginVisit( cv );

			if( cv.isValid && cv.found == this.controllers.size() )
			{
				this.controllerState = ControllerState.CONTROLLER_ONLINE;
			}
			else
			{
				this.controllerState = ControllerState.CONTROLLER_CONFLICT;
			}
		}

		if( old != this.controllerState )
		{
			this.myGrid.postEvent( new MENetworkControllerChange() );
		}
	}

	private int calculateRequiredChannels()
	{
		int depth = 0;
		this.semiOpen.clear();

		for( IGridNode nodes : this.requireChannels )
		{
			if( !this.semiOpen.contains( nodes ) )
			{
				IGridBlock gb = nodes.getGridBlock();
				EnumSet<GridFlags> flags = gb.getFlags();

				if( flags.contains( GridFlags.COMPRESSED_CHANNEL ) && !this.blockDense.isEmpty() )
				{
					return 9;
				}

				depth++;

				if( flags.contains( GridFlags.MULTIBLOCK ) )
				{
					IGridMultiblock gmb = (IGridMultiblock) gb;
					Iterator<IGridNode> i = gmb.getMultiblockNodes();
					while( i.hasNext() )
					{
						this.semiOpen.add( (IPathItem) i.next() );
					}
				}
			}
		}

		return depth;
	}

	private void achievementPost()
	{
		if( this.lastChannels != this.channelsInUse && AEConfig.instance.isFeatureEnabled( AEFeature.Channels ) )
		{
			Achievements currentBracket = this.getAchievementBracket( this.channelsInUse );
			Achievements lastBracket = this.getAchievementBracket( this.lastChannels );
			if( currentBracket != lastBracket && currentBracket != null )
			{
				Set<Integer> players = new HashSet<Integer>();
				for( IGridNode n : this.requireChannels )
				{
					players.add( n.getPlayerID() );
				}

				for( int id : players )
				{
					Platform.addStat( id, currentBracket.getAchievement() );
				}
			}
		}
		this.lastChannels = this.channelsInUse;
	}

	private Achievements getAchievementBracket( int ch )
	{
		if( ch < 8 )
		{
			return null;
		}

		if( ch < 128 )
		{
			return Achievements.Networking1;
		}

		if( ch < 2048 )
		{
			return Achievements.Networking2;
		}

		return Achievements.Networking3;
	}

	@MENetworkEventSubscribe
	void updateNodReq( MENetworkChannelChanged ev )
	{
		IGridNode gridNode = ev.node;

		if( gridNode.getGridBlock().getFlags().contains( GridFlags.REQUIRE_CHANNEL ) )
		{
			this.requireChannels.add( gridNode );
		}
		else
		{
			this.requireChannels.remove( gridNode );
		}

		this.repath();
	}

	@Override
	public final boolean isNetworkBooting()
	{
		return !this.active.isEmpty() && !this.booting;
	}

	@Override
	public final ControllerState getControllerState()
	{
		return this.controllerState;
	}

	@Override
	public final void repath()
	{
		// clean up...
		this.active.clear();

		this.channelsByBlocks = 0;
		this.updateNetwork = true;
	}
}
