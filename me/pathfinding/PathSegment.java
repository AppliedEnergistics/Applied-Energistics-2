package appeng.me.pathfinding;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;

public class PathSegment
{

	public boolean isDead;

	static class RouteComplete extends Exception
	{

		private static final long serialVersionUID = 810456465120286110L;

	};

	public PathSegment(List open, Set semiopen, Set closed) {
		this.open = open;
		this.semiopen = semiopen;
		this.closed = closed;
		isDead = false;
	}

	List<IPathItem> open;
	Set<IPathItem> semiopen;
	Set<IPathItem> closed;

	public boolean step()
	{
		List<IPathItem> oldOpen = open;
		open = new LinkedList();

		for (IPathItem i : oldOpen)
		{
			for (IPathItem pi : i.getPossibleOptions())
			{
				EnumSet<GridFlags> flags = pi.getFlags();

				if ( !closed.contains( pi ) )
				{
					pi.setControllerRoute( i, true );

					if ( flags.contains( GridFlags.REQURE_CHANNEL ) )
					{
						// close the semi open.
						if ( !semiopen.contains( pi ) )
						{
							boolean worked = false;

							if ( flags.contains( GridFlags.DENSE_CHANNEL ) )
								worked = useDenseChannel( pi );
							else
								worked = useChannel( pi );

							if ( worked && flags.contains( GridFlags.MULTIBLOCK ) )
							{
								Iterator<IGridNode> oni = ((IGridMultiblock) ((IGridNode) pi).getGridBlock()).getMultiblockNodes();
								while (oni.hasNext())
								{
									IGridNode otherNodes = oni.next();
									if ( otherNodes != pi )
										semiopen.add( (IPathItem) otherNodes );
								}
							}
						}
						else
						{
							pi.incrementChannelCount( 1 ); // give a channel.
							semiopen.remove( pi );
						}
					}

					closed.add( pi );
					open.add( pi );
				}
			}
		}

		return open.isEmpty();
	}

	private boolean useChannel(IPathItem start)
	{
		IPathItem pi = start;
		while (pi != null)
		{
			if ( !pi.canSupportMoreChannels() )
				return false;

			pi = pi.getControllerRoute();
		}

		pi = start;
		while (pi != null)
		{
			pi.incrementChannelCount( 1 );
			pi = pi.getControllerRoute();
		}

		return true;
	}

	private boolean useDenseChannel(IPathItem start)
	{
		IPathItem pi = start;
		while (pi != null)
		{
			if ( !pi.canSupportMoreChannels() || pi.getFlags().contains( GridFlags.CANNOT_CARRY_DENSE ) )
				return false;

			pi = pi.getControllerRoute();
		}

		pi = start;
		while (pi != null)
		{
			pi.incrementChannelCount( 1 );
			pi = pi.getControllerRoute();
		}

		return true;
	}

}
