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

package appeng.me.pathfinding;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridMultiblock;
import appeng.api.networking.IGridNode;
import appeng.me.cache.PathGridCache;

public class PathSegment
{

	public boolean isDead;

	final PathGridCache pgc;

	public PathSegment(PathGridCache myPGC, List<IPathItem> open, Set<IPathItem> semiOpen, Set<IPathItem> closed)
	{
		this.open = open;
		this.semiOpen = semiOpen;
		this.closed = closed;
		pgc = myPGC;
		isDead = false;
	}

	List<IPathItem> open;
	final Set<IPathItem> semiOpen;
	final Set<IPathItem> closed;

	public boolean step()
	{
		List<IPathItem> oldOpen = open;
		open = new LinkedList<IPathItem>();

		for (IPathItem i : oldOpen)
		{
			for (IPathItem pi : i.getPossibleOptions())
			{
				EnumSet<GridFlags> flags = pi.getFlags();

				if ( !closed.contains( pi ) )
				{
					pi.setControllerRoute( i, true );

					if ( flags.contains( GridFlags.REQUIRE_CHANNEL ) )
					{
						// close the semi open.
						if ( !semiOpen.contains( pi ) )
						{
							boolean worked;

							if ( flags.contains( GridFlags.COMPRESSED_CHANNEL ) )
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
										semiOpen.add( (IPathItem) otherNodes );
								}
							}
						}
						else
						{
							pi.incrementChannelCount( 1 ); // give a channel.
							semiOpen.remove( pi );
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
			pgc.channelsByBlocks++;
			pi.incrementChannelCount( 1 );
			pi = pi.getControllerRoute();
		}

		pgc.channelsInUse++;
		return true;
	}

	private boolean useDenseChannel(IPathItem start)
	{
		IPathItem pi = start;
		while (pi != null)
		{
			if ( !pi.canSupportMoreChannels() || pi.getFlags().contains( GridFlags.CANNOT_CARRY_COMPRESSED ) )
				return false;

			pi = pi.getControllerRoute();
		}

		pi = start;
		while (pi != null)
		{
			pgc.channelsByBlocks++;
			pi.incrementChannelCount( 1 );
			pi = pi.getControllerRoute();
		}

		pgc.channelsInUse++;
		return true;
	}

}
