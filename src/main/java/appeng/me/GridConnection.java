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

package appeng.me;


import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.FailedConnection;
import appeng.api.exceptions.NullNodeConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.me.pathfinding.IPathItem;
import appeng.util.Platform;
import appeng.util.ReadOnlyCollection;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Arrays;
import java.util.EnumSet;


public class GridConnection implements IGridConnection, IPathItem
{

	private static final String EXISTING_CONNECTION_MESSAGE = "Connection between node [machine=%s, %s] and [machine=%s, %s] on [%s] already exists.";

	private static final MENetworkChannelsChanged EVENT = new MENetworkChannelsChanged();
	private int channelData = 0;
	private Object visitorIterationNumber = null;
	private GridNode sideA;
	private ForgeDirection fromAtoB;
	private GridNode sideB;

	public GridConnection( final IGridNode aNode, final IGridNode bNode, final ForgeDirection fromAtoB ) throws FailedConnection
	{

		final GridNode a = (GridNode) aNode;
		final GridNode b = (GridNode) bNode;

		if( Platform.securityCheck( a, b ) )
		{
			if( AEConfig.instance.isFeatureEnabled( AEFeature.LogSecurityAudits ) )
			{
				final DimensionalCoord aCoordinates = a.getGridBlock().getLocation();
				final DimensionalCoord bCoordinates = b.getGridBlock().getLocation();

				AELog.info( "Security audit 1 failed at [%s] belonging to player [id=%d]", aCoordinates.toString(), a.getPlayerID() );
				AELog.info( "Security audit 2 failed at [%s] belonging to player [id=%d]", bCoordinates.toString(), b.getPlayerID() );
			}

			throw new SecurityConnectionException();
		}

		if( a == null || b == null )
		{
			throw new NullNodeConnectionException();
		}

		if( a.hasConnection( b ) || b.hasConnection( a ) )
		{
			final String aMachineClass = a.getGridBlock().getMachine().getClass().getSimpleName();
			final String bMachineClass = b.getGridBlock().getMachine().getClass().getSimpleName();
			final String aCoordinates = a.getGridBlock().getLocation().toString();
			final String bCoordinates = b.getGridBlock().getLocation().toString();

			throw new ExistingConnectionException( String.format( EXISTING_CONNECTION_MESSAGE, aMachineClass, aCoordinates, bMachineClass, bCoordinates, fromAtoB ) );
		}

		this.sideA = a;
		this.fromAtoB = fromAtoB;
		this.sideB = b;

		if( b.getMyGrid() == null )
		{
			b.setGrid( a.getInternalGrid() );
		}
		else
		{
			if( a.getMyGrid() == null )
			{
				final GridPropagator gp = new GridPropagator( b.getInternalGrid() );
				a.beginVisit( gp );
			}
			else if( b.getMyGrid() == null )
			{
				final GridPropagator gp = new GridPropagator( a.getInternalGrid() );
				b.beginVisit( gp );
			}
			else if( this.isNetworkABetter( a, b ) )
			{
				final GridPropagator gp = new GridPropagator( a.getInternalGrid() );
				b.beginVisit( gp );
			}
			else
			{
				final GridPropagator gp = new GridPropagator( b.getInternalGrid() );
				a.beginVisit( gp );
			}
		}

		// a connection was destroyed RE-PATH!!
		final IPathingGrid p = this.sideA.getInternalGrid().getCache( IPathingGrid.class );
		p.repath();

		this.sideA.addConnection( this );
		this.sideB.addConnection( this );
	}

	private boolean isNetworkABetter( final GridNode a, final GridNode b )
	{
		return a.getMyGrid().getPriority() > b.getMyGrid().getPriority() || a.getMyGrid().size() > b.getMyGrid().size();
	}

	@Override
	public IGridNode getOtherSide( final IGridNode gridNode )
	{
		if( gridNode == this.sideA )
		{
			return this.sideB;
		}
		if( gridNode == this.sideB )
		{
			return this.sideA;
		}

		throw new GridException( "Invalid Side of Connection" );
	}

	@Override
	public ForgeDirection getDirection( final IGridNode side )
	{
		if( this.fromAtoB == ForgeDirection.UNKNOWN )
		{
			return this.fromAtoB;
		}

		if( this.sideA == side )
		{
			return this.fromAtoB;
		}
		else
		{
			return this.fromAtoB.getOpposite();
		}
	}

	@Override
	public void destroy()
	{
		// a connection was destroyed RE-PATH!!
		final IPathingGrid p = this.sideA.getInternalGrid().getCache( IPathingGrid.class );
		p.repath();

		this.sideA.removeConnection( this );
		this.sideB.removeConnection( this );

		this.sideA.validateGrid();
		this.sideB.validateGrid();
	}

	@Override
	public IGridNode a()
	{
		return this.sideA;
	}

	@Override
	public IGridNode b()
	{
		return this.sideB;
	}

	@Override
	public boolean hasDirection()
	{
		return this.fromAtoB != ForgeDirection.UNKNOWN;
	}

	@Override
	public int getUsedChannels()
	{
		return ( this.channelData >> 8 ) & 0xff;
	}

	@Override
	public IPathItem getControllerRoute()
	{
		if( this.sideA.getFlags().contains( GridFlags.CANNOT_CARRY ) )
		{
			return null;
		}
		return this.sideA;
	}

	@Override
	public void setControllerRoute( final IPathItem fast, final boolean zeroOut )
	{
		if( zeroOut )
		{
			this.channelData &= ~0xff;
		}

		if( this.sideB == fast )
		{
			final GridNode tmp = this.sideA;
			this.sideA = this.sideB;
			this.sideB = tmp;
			this.fromAtoB = this.fromAtoB.getOpposite();
		}
	}

	@Override
	public boolean canSupportMoreChannels()
	{
		return this.getLastUsedChannels() < 32; // max, PERIOD.
	}

	@Override
	public IReadOnlyCollection<IPathItem> getPossibleOptions()
	{
		return new ReadOnlyCollection<IPathItem>( Arrays.asList( (IPathItem) this.a(), (IPathItem) this.b() ) );
	}

	@Override
	public void incrementChannelCount( final int usedChannels )
	{
		this.channelData += usedChannels;
	}

	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return EnumSet.noneOf( GridFlags.class );
	}

	@Override
	public void finalizeChannels()
	{
		if( this.getUsedChannels() != this.getLastUsedChannels() )
		{
			this.channelData &= 0xff;
			this.channelData |= this.channelData << 8;

			if( this.sideA.getInternalGrid() != null )
			{
				this.sideA.getInternalGrid().postEventTo( this.sideA, EVENT );
			}

			if( this.sideB.getInternalGrid() != null )
			{
				this.sideB.getInternalGrid().postEventTo( this.sideB, EVENT );
			}
		}
	}

	private int getLastUsedChannels()
	{
		return this.channelData & 0xff;
	}

	Object getVisitorIterationNumber()
	{
		return this.visitorIterationNumber;
	}

	void setVisitorIterationNumber( final Object visitorIterationNumber )
	{
		this.visitorIterationNumber = visitorIterationNumber;
	}
}
