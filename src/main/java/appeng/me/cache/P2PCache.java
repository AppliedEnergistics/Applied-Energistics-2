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

package appeng.me.cache;


import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkBootingStatusChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.ITickManager;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.parts.p2p.PartP2PTunnelME;


public class P2PCache implements IGridCache
{

	final IGrid myGrid;
	private final Map<Long, PartP2PTunnel> inputs = new HashMap<Long, PartP2PTunnel>();
	private final Multimap<Long, PartP2PTunnel> outputs = LinkedHashMultimap.create();
	private final TunnelCollection NullColl = new TunnelCollection<PartP2PTunnel>( null, null );

	public P2PCache( IGrid g )
	{
		this.myGrid = g;
	}

	@MENetworkEventSubscribe
	public void bootComplete( MENetworkBootingStatusChange bootStatus )
	{
		ITickManager tm = this.myGrid.getCache( ITickManager.class );
		for( PartP2PTunnel me : this.inputs.values() )
		{
			if( me instanceof PartP2PTunnelME )
			{
				tm.wakeDevice( me.getGridNode() );
			}
		}
	}

	@MENetworkEventSubscribe
	public void bootComplete( MENetworkPowerStatusChange power )
	{
		ITickManager tm = this.myGrid.getCache( ITickManager.class );
		for( PartP2PTunnel me : this.inputs.values() )
		{
			if( me instanceof PartP2PTunnelME )
			{
				tm.wakeDevice( me.getGridNode() );
			}
		}
	}

	@Override
	public void onUpdateTick()
	{

	}

	@Override
	public void removeNode( IGridNode node, IGridHost machine )
	{
		if( machine instanceof PartP2PTunnel )
		{
			if( machine instanceof PartP2PTunnelME )
			{
				if( !node.hasFlag( GridFlags.REQUIRE_CHANNEL ) )
				{
					return;
				}
			}

			PartP2PTunnel t = (PartP2PTunnel) machine;
			// AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq
			// );

			if( t.output )
			{
				this.outputs.remove( t.freq, t );
			}
			else
			{
				this.inputs.remove( t.freq );
			}

			this.updateTunnel( t.freq, !t.output, false );
		}
	}

	@Override
	public void addNode( IGridNode node, IGridHost machine )
	{
		if( machine instanceof PartP2PTunnel )
		{
			if( machine instanceof PartP2PTunnelME )
			{
				if( !node.hasFlag( GridFlags.REQUIRE_CHANNEL ) )
				{
					return;
				}
			}

			PartP2PTunnel t = (PartP2PTunnel) machine;
			// AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq
			// );

			if( t.output )
			{
				this.outputs.put( t.freq, t );
			}
			else
			{
				this.inputs.put( t.freq, t );
			}

			this.updateTunnel( t.freq, !t.output, false );
		}
	}

	@Override
	public void onSplit( IGridStorage storageB )
	{

	}

	@Override
	public void onJoin( IGridStorage storageB )
	{

	}

	@Override
	public void populateGridStorage( IGridStorage storage )
	{

	}

	private void updateTunnel( long freq, boolean updateOutputs, boolean configChange )
	{
		for( PartP2PTunnel p : this.outputs.get( freq ) )
		{
			if( configChange )
			{
				p.onTunnelConfigChange();
			}
			p.onTunnelNetworkChange();
		}

		PartP2PTunnel in = this.inputs.get( freq );
		if( in != null )
		{
			if( configChange )
			{
				in.onTunnelConfigChange();
			}
			in.onTunnelNetworkChange();
		}
	}

	public void updateFreq( PartP2PTunnel t, long NewFreq )
	{
		if( this.outputs.containsValue( t ) )
		{
			this.outputs.remove( t.freq, t );
		}

		if( this.inputs.containsValue( t ) )
		{
			this.inputs.remove( t.freq );
		}

		t.freq = NewFreq;

		if( t.output )
		{
			this.outputs.put( t.freq, t );
		}
		else
		{
			this.inputs.put( t.freq, t );
		}

		// AELog.info( "update-" + (t.output ? "output: " : "input: ") + t.freq
		// );
		this.updateTunnel( t.freq, t.output, true );
		this.updateTunnel( t.freq, !t.output, true );
	}

	public TunnelCollection<PartP2PTunnel> getOutputs( long freq, Class<? extends PartP2PTunnel> c )
	{
		PartP2PTunnel in = this.inputs.get( freq );
		if( in == null )
		{
			return this.NullColl;
		}

		TunnelCollection<PartP2PTunnel> out = this.inputs.get( freq ).getCollection( this.outputs.get( freq ), c );
		if( out == null )
		{
			return this.NullColl;
		}

		return out;
	}

	public PartP2PTunnel getInput( long freq )
	{
		return this.inputs.get( freq );
	}
}
