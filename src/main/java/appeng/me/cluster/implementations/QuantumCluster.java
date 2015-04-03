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

package appeng.me.cluster.implementations;


import java.util.Iterator;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import appeng.api.AEApi;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.exceptions.FailedConnection;
import appeng.api.features.ILocatable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.WorldCoord;
import appeng.me.cache.helpers.ConnectionWrapper;
import appeng.me.cluster.IAECluster;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.iterators.ChainedIterator;


public class QuantumCluster implements ILocatable, IAECluster
{

	final public WorldCoord min;
	final public WorldCoord max;
	public boolean isDestroyed = false;
	public boolean updateStatus = true;
	public TileQuantumBridge[] Ring;
	boolean registered = false;
	ConnectionWrapper connection;
	private long thisSide;
	private long otherSide;
	private TileQuantumBridge center;

	public QuantumCluster( WorldCoord _min, WorldCoord _max )
	{
		this.min = _min;
		this.max = _max;
		this.Ring = new TileQuantumBridge[8];
	}

	@SubscribeEvent
	public void onUnload( WorldEvent.Unload e )
	{
		if( this.center.getWorldObj() == e.world )
		{
			this.updateStatus = false;
			this.destroy();
		}
	}

	@Override
	public void updateStatus( boolean updateGrid )
	{
		long qe;

		qe = this.center.getQEFrequency();

		if( this.thisSide != qe && this.thisSide != -qe )
		{
			if( qe != 0 )
			{
				if( this.thisSide != 0 )
					MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );

				if( this.canUseNode( -qe ) )
				{
					this.otherSide = qe;
					this.thisSide = -qe;
				}
				else if( this.canUseNode( qe ) )
				{
					this.thisSide = qe;
					this.otherSide = -qe;
				}

				MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Register ) );
			}
			else
			{
				MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );

				this.otherSide = 0;
				this.thisSide = 0;
			}
		}

		ILocatable myOtherSide = this.otherSide == 0 ? null : AEApi.instance().registries().locatable().getLocatableBy( this.otherSide );

		boolean shutdown = false;

		if( myOtherSide instanceof QuantumCluster )
		{
			QuantumCluster sideA = this;
			QuantumCluster sideB = (QuantumCluster) myOtherSide;

			if( sideA.isActive() && sideB.isActive() )
			{
				if( this.connection != null && this.connection.connection != null )
				{
					IGridNode a = this.connection.connection.a();
					IGridNode b = this.connection.connection.b();
					IGridNode sa = sideA.getNode();
					IGridNode sb = sideB.getNode();
					if( ( a == sa || b == sa ) && ( a == sb || b == sb ) )
						return;
				}

				try
				{
					if( sideA.connection != null )
					{
						if( sideA.connection.connection != null )
						{
							sideA.connection.connection.destroy();
							sideA.connection = new ConnectionWrapper( null );
						}
					}

					if( sideB.connection != null )
					{
						if( sideB.connection.connection != null )
						{
							sideB.connection.connection.destroy();
							sideB.connection = new ConnectionWrapper( null );
						}
					}

					sideA.connection = sideB.connection = new ConnectionWrapper( AEApi.instance().createGridConnection( sideA.getNode(), sideB.getNode() ) );
				}
				catch( FailedConnection e )
				{
					// :(
				}
			}
			else
				shutdown = true;
		}
		else
			shutdown = true;

		if( shutdown && this.connection != null )
		{
			if( this.connection.connection != null )
			{
				this.connection.connection.destroy();
				this.connection.connection = null;
				this.connection = new ConnectionWrapper( null );
			}
		}
	}

	public boolean canUseNode( long qe )
	{
		QuantumCluster qc = (QuantumCluster) AEApi.instance().registries().locatable().getLocatableBy( qe );
		if( qc != null )
		{
			World theWorld = qc.center.getWorldObj();
			if( !qc.isDestroyed )
			{
				Chunk c = theWorld.getChunkFromBlockCoords( qc.center.xCoord, qc.center.zCoord );
				if( c.isChunkLoaded )
				{
					int id = theWorld.provider.dimensionId;
					World cur = DimensionManager.getWorld( id );

					TileEntity te = theWorld.getTileEntity( qc.center.xCoord, qc.center.yCoord, qc.center.zCoord );
					return te != qc.center || theWorld != cur;
				}
			}
		}
		return true;
	}

	private boolean isActive()
	{
		if( this.isDestroyed || !this.registered )
			return false;

		return this.center.isPowered() && this.hasQES();
	}

	private IGridNode getNode()
	{
		return this.center.getGridNode( ForgeDirection.UNKNOWN );
	}

	public boolean hasQES()
	{
		return this.thisSide != 0;
	}

	@Override
	public void destroy()
	{
		if( this.isDestroyed )
			return;
		this.isDestroyed = true;

		if( this.registered )
		{
			MinecraftForge.EVENT_BUS.unregister( this );
			this.registered = false;
		}

		if( this.thisSide != 0 )
		{
			this.updateStatus( true );
			MinecraftForge.EVENT_BUS.post( new LocatableEventAnnounce( this, LocatableEvent.Unregister ) );
		}

		this.center.updateStatus( null, (byte) -1, this.updateStatus );

		for( TileQuantumBridge r : this.Ring )
		{
			r.updateStatus( null, (byte) -1, this.updateStatus );
		}

		this.center = null;
		this.Ring = new TileQuantumBridge[8];
	}

	@Override
	public Iterator<IGridHost> getTiles()
	{
		return new ChainedIterator<IGridHost>( this.Ring[0], this.Ring[1], this.Ring[2], this.Ring[3], this.Ring[4], this.Ring[5], this.Ring[6], this.Ring[7], this.center );
	}

	public boolean isCorner( TileQuantumBridge tileQuantumBridge )
	{
		return this.Ring[0] == tileQuantumBridge || this.Ring[2] == tileQuantumBridge || this.Ring[4] == tileQuantumBridge || this.Ring[6] == tileQuantumBridge;
	}

	@Override
	public long getLocatableSerial()
	{
		return this.thisSide;
	}

	public TileQuantumBridge getCenter()
	{
		return this.center;
	}

	public void setCenter( TileQuantumBridge c )
	{
		this.registered = true;
		MinecraftForge.EVENT_BUS.register( this );
		this.center = c;
	}
}
