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

public class PathGridCache implements IPathingGrid
{

	boolean recalculateControllerNextTick = true;
	boolean updateNetwork = true;
	boolean booting = false;

	final LinkedList<PathSegment> active = new LinkedList();

	ControllerState controllerState = ControllerState.NO_CONTROLLER;

	int instance = Integer.MIN_VALUE;

	int ticksUntilReady = 20;
	public int channelsInUse = 0;
	int lastChannels = 0;

	final Set<TileController> controllers = new HashSet();
	final Set<IGridNode> requireChannels = new HashSet();
	final Set<IGridNode> blockDense = new HashSet();

	final IGrid myGrid;
	private HashSet<IPathItem> semiOpen = new HashSet();
	private HashSet<IPathItem> closedList = new HashSet();

	public int channelsByBlocks = 0;
	public double channelPowerUsage = 0.0;

	public PathGridCache(IGrid g)
	{
		myGrid = g;
	}

	@Override
	public void onUpdateTick()
	{
		if ( recalculateControllerNextTick )
		{
			recalcController();
		}

		if ( updateNetwork )
		{
			if ( !booting )
				myGrid.postEvent( new MENetworkBootingStatusChange() );

			booting = true;
			updateNetwork = false;
			instance++;
			channelsInUse = 0;

			if ( !AEConfig.instance.isFeatureEnabled( AEFeature.Channels ) )
			{
				int used = calculateRequiredChannels();

				int nodes = myGrid.getNodes().size();
				ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				channelsByBlocks = nodes * used;
				channelPowerUsage = (double) channelsByBlocks / 128.0;

				myGrid.getPivot().beginVisit( new AdHocChannelUpdater( used ) );
			}
			else if ( controllerState == ControllerState.NO_CONTROLLER )
			{
				int requiredChannels = calculateRequiredChannels();
				int used = requiredChannels;
				if ( requiredChannels > 8 )
					used = 0;

				int nodes = myGrid.getNodes().size();
				channelsInUse = used;

				ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				channelsByBlocks = nodes * used;
				channelPowerUsage = (double) channelsByBlocks / 128.0;

				myGrid.getPivot().beginVisit( new AdHocChannelUpdater( used ) );
			}
			else if ( controllerState == ControllerState.CONTROLLER_CONFLICT )
			{
				ticksUntilReady = 20;
				myGrid.getPivot().beginVisit( new AdHocChannelUpdater( 0 ) );
			}
			else
			{
				int nodes = myGrid.getNodes().size();
				ticksUntilReady = 20 + Math.max( 0, nodes / 100 - 20 );
				closedList = new HashSet();
				semiOpen = new HashSet();

				// myGrid.getPivot().beginVisit( new AdHocChannelUpdater( 0 )
				// );
				for (IGridNode node : myGrid.getMachines( TileController.class ))
				{
					closedList.add( (IPathItem) node );
					for (IGridConnection gcc : node.getConnections())
					{
						GridConnection gc = (GridConnection) gcc;
						if ( !(gc.getOtherSide( node ).getMachine() instanceof TileController) )
						{
							List open = new LinkedList();
							closedList.add( gc );
							open.add( gc );
							gc.setControllerRoute( (GridNode) node, true );
							active.add( new PathSegment( this, open, semiOpen, closedList ) );
						}
					}
				}
			}
		}

		if ( !active.isEmpty() || ticksUntilReady > 0 )
		{
			Iterator<PathSegment> i = active.iterator();
			while (i.hasNext())
			{
				PathSegment pat = i.next();
				if ( pat.step() )
				{
					pat.isDead = true;
					i.remove();
				}
			}

			ticksUntilReady--;

			if ( active.isEmpty() && ticksUntilReady <= 0 )
			{
				if ( controllerState == ControllerState.CONTROLLER_ONLINE )
				{
					for (TileController tc : controllers)
					{
						tc.getGridNode( ForgeDirection.UNKNOWN ).beginVisit( new ControllerChannelUpdater() );
						break;
					}
				}

				// check for achievements
				achievementPost();

				booting = false;
				channelPowerUsage = (double) channelsByBlocks / 128.0;
				myGrid.postEvent( new MENetworkBootingStatusChange() );
			}
		}
	}

	private void achievementPost()
	{
		if ( lastChannels != channelsInUse && AEConfig.instance.isFeatureEnabled( AEFeature.Channels ) )
		{
			Achievements currentBracket = getAchievementBracket( channelsInUse );
			Achievements lastBracket = getAchievementBracket( lastChannels );
			if ( currentBracket != lastBracket && currentBracket != null )
			{
				Set<Integer> players = new HashSet();
				for (IGridNode n : requireChannels)
					players.add( n.getPlayerID() );

				for (int id : players)
				{
					Platform.addStat( id, currentBracket.getAchievement() );
				}
			}
		}
		lastChannels = channelsInUse;
	}

	private Achievements getAchievementBracket(int ch)
	{
		if ( ch < 8 )
			return null;

		if ( ch < 128 )
			return Achievements.Networking1;

		if ( ch < 2048 )
			return Achievements.Networking2;

		return Achievements.Networking3;
	}

	private int calculateRequiredChannels()
	{
		int depth = 0;
		semiOpen.clear();

		for (IGridNode nodes : requireChannels)
		{
			if ( !semiOpen.contains( nodes ) )
			{
				IGridBlock gb = nodes.getGridBlock();
				EnumSet<GridFlags> flags = gb.getFlags();

				if ( flags.contains( GridFlags.COMPRESSED_CHANNEL ) && !blockDense.isEmpty() )
					return 9;

				depth++;

				if ( flags.contains( GridFlags.MULTIBLOCK ) )
				{
					IGridMultiblock gmb = (IGridMultiblock) gb;
					Iterator<IGridNode> i = gmb.getMultiblockNodes();
					while (i.hasNext())
						semiOpen.add( (IPathItem) i.next() );
				}
			}
		}

		return depth;
	}

	@Override
	public void repath()
	{
		// clean up...
		active.clear();

		channelsByBlocks = 0;
		updateNetwork = true;
	}

	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileController )
		{
			controllers.remove( machine );
			recalculateControllerNextTick = true;
		}

		EnumSet<GridFlags> flags = gridNode.getGridBlock().getFlags();

		if ( flags.contains( GridFlags.REQUIRE_CHANNEL ) )
			requireChannels.remove( gridNode );

		if ( flags.contains( GridFlags.CANNOT_CARRY_COMPRESSED ) )
			blockDense.remove( gridNode );

		repath();
	}

	@Override
	public void addNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileController )
		{
			controllers.add( (TileController) machine );
			recalculateControllerNextTick = true;
		}

		EnumSet<GridFlags> flags = gridNode.getGridBlock().getFlags();

		if ( flags.contains( GridFlags.REQUIRE_CHANNEL ) )
			requireChannels.add( gridNode );

		if ( flags.contains( GridFlags.CANNOT_CARRY_COMPRESSED ) )
			blockDense.add( gridNode );

		repath();
	}

	@MENetworkEventSubscribe
	void updateNodReq(MENetworkChannelChanged ev)
	{
		IGridNode gridNode = ev.node;

		if ( gridNode.getGridBlock().getFlags().contains( GridFlags.REQUIRE_CHANNEL ) )
			requireChannels.add( gridNode );
		else
			requireChannels.remove( gridNode );

		repath();
	}

	private void recalcController()
	{
		recalculateControllerNextTick = false;
		ControllerState old = controllerState;

		if ( controllers.isEmpty() )
		{
			controllerState = ControllerState.NO_CONTROLLER;
		}
		else
		{
			IGridNode startingNode = controllers.iterator().next().getGridNode( ForgeDirection.UNKNOWN );
			if ( startingNode == null )
			{
				controllerState = ControllerState.CONTROLLER_CONFLICT;
				return;
			}

			DimensionalCoord dc = startingNode.getGridBlock().getLocation();
			ControllerValidator cv = new ControllerValidator( dc.x, dc.y, dc.z );

			startingNode.beginVisit( cv );

			if ( cv.isValid && cv.found == controllers.size() )
				controllerState = ControllerState.CONTROLLER_ONLINE;
			else
				controllerState = ControllerState.CONTROLLER_CONFLICT;
		}

		if ( old != controllerState )
		{
			myGrid.postEvent( new MENetworkControllerChange() );
		}
	}

	@Override
	public ControllerState getControllerState()
	{
		return controllerState;
	}

	@Override
	public boolean isNetworkBooting()
	{
		return !active.isEmpty() && booting == false;
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
