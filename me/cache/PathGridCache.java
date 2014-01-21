package appeng.me.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraftforge.common.ForgeDirection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkControllerChange;
import appeng.api.networking.pathing.ControllerState;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.pathfinding.AdHocChannelUpdater;
import appeng.me.pathfinding.ControllerChannelUpdater;
import appeng.me.pathfinding.ControllerValidator;
import appeng.me.pathfinding.IPathItem;
import appeng.me.pathfinding.PathSegment;
import appeng.tile.networking.TileController;

public class PathGridCache implements IPathingGrid
{

	boolean recalculateControllerNextTick = true;
	boolean updateNetwork = true;
	boolean booting = false;

	LinkedList<PathSegment> active = new LinkedList();

	ControllerState controllerState = ControllerState.NO_CONTROLLER;

	int instance = Integer.MIN_VALUE;

	int ticksUntilReady = 20;
	Set<TileController> controllers = new HashSet();
	Set<IGridNode> requireChannels = new HashSet();

	final IGrid myGrid;
	private HashSet<IPathItem> semiOpen = new HashSet();
	private HashSet<IPathItem> closedList = new HashSet();

	public PathGridCache(IGrid g) {
		myGrid = g;
	}

	@Override
	public void onUpdateTick(IGrid grid)
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

			if ( controllerState == ControllerState.NO_CONTROLLER )
			{
				int requiredChannels = calculateRequiredChanels();
				int used = requiredChannels;
				if ( requiredChannels > 8 )
					used = 0;

				int nodes = myGrid.getNodes().size();
				ticksUntilReady = 20 + (nodes / 10);

				myGrid.getPivot().beginVisition( new AdHocChannelUpdater( used ) );
			}
			else if ( controllerState == ControllerState.CONTROLLER_CONFLICT )
			{
				ticksUntilReady = 20;
				myGrid.getPivot().beginVisition( new AdHocChannelUpdater( 0 ) );
			}
			else
			{
				int nodes = myGrid.getNodes().size();
				ticksUntilReady = 20 + (nodes / 10);
				closedList = new HashSet();
				semiOpen = new HashSet();

				// myGrid.getPivot().beginVisition( new AdHocChannelUpdater( 0 ) );
				for (IGridNode node : grid.getMachines( TileController.class ))
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
							active.add( new PathSegment( open, semiOpen, closedList ) );
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
						tc.getGridNode( ForgeDirection.UNKNOWN ).beginVisition( new ControllerChannelUpdater() );
						break;
					}
				}

				booting = false;
				myGrid.postEvent( new MENetworkBootingStatusChange() );
			}
		}
	}

	private int calculateRequiredChanels()
	{
		int depth = 0;
		semiOpen.clear();

		for (IGridNode nodes : requireChannels)
		{
			if ( !semiOpen.contains( nodes ) )
			{
				depth++;

				IGridBlock gb = nodes.getGridBlock();
				if ( gb.getFlags().contains( GridFlags.MULTIBLOCK ) )
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

		updateNetwork = true;
	}

	@Override
	public void removeNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileController )
		{
			controllers.remove( machine );
			recalculateControllerNextTick = true;
		}

		if ( gridNode.getGridBlock().getFlags().contains( GridFlags.REQURE_CHANNEL ) )
			requireChannels.remove( gridNode );

		repath();
	}

	@Override
	public void addNode(IGrid grid, IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileController )
		{
			controllers.add( (TileController) machine );
			recalculateControllerNextTick = true;
		}

		if ( gridNode.getGridBlock().getFlags().contains( GridFlags.REQURE_CHANNEL ) )
			requireChannels.add( gridNode );

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

			startingNode.beginVisition( cv );

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
