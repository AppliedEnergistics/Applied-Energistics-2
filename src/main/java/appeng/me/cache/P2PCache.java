package appeng.me.cache;

import java.util.HashMap;

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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class P2PCache implements IGridCache
{

	final private HashMap<Long, PartP2PTunnel> inputs = new HashMap();
	final private Multimap<Long, PartP2PTunnel> outputs = LinkedHashMultimap.create();
	final private TunnelCollection NullColl = new TunnelCollection<PartP2PTunnel>( null, null );

	final IGrid myGrid;

	public P2PCache(IGrid g) {
		myGrid = g;
	}

	@MENetworkEventSubscribe
	public void bootComplete(MENetworkBootingStatusChange bootStatus)
	{
		ITickManager tm = myGrid.getCache( ITickManager.class );
		for (PartP2PTunnel me : inputs.values())
		{
			if ( me instanceof PartP2PTunnelME )
				tm.wakeDevice( me.getGridNode() );
		}
	}

	@MENetworkEventSubscribe
	public void bootComplete(MENetworkPowerStatusChange power)
	{
		ITickManager tm = myGrid.getCache( ITickManager.class );
		for (PartP2PTunnel me : inputs.values())
		{
			if ( me instanceof PartP2PTunnelME )
				tm.wakeDevice( me.getGridNode() );
		}
	}

	@Override
	public void onUpdateTick()
	{

	}

	public void updateFreq(PartP2PTunnel t, long NewFreq)
	{
		if ( outputs.containsValue( t ) )
			outputs.remove( t.freq, t );

		if ( inputs.containsValue( t ) )
			inputs.remove( t.freq );

		t.freq = NewFreq;

		if ( t.output )
			outputs.put( t.freq, t );
		else
			inputs.put( t.freq, t );

		// AELog.info( "update-" + (t.output ? "output: " : "input: ") + t.freq
		// );
		updateTunnel( t.freq, t.output, true );
		updateTunnel( t.freq, !t.output, true );
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
		{
			if ( machine instanceof PartP2PTunnelME )
			{
				if ( !node.hasFlag( GridFlags.REQUIRE_CHANNEL ) )
					return;
			}

			PartP2PTunnel t = (PartP2PTunnel) machine;
			// AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq
			// );

			if ( t.output )
				outputs.put( t.freq, t );
			else
				inputs.put( t.freq, t );

			updateTunnel( t.freq, !t.output, false );
		}
	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
		{
			if ( machine instanceof PartP2PTunnelME )
			{
				if ( !node.hasFlag( GridFlags.REQUIRE_CHANNEL ) )
					return;
			}

			PartP2PTunnel t = (PartP2PTunnel) machine;
			// AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq
			// );

			if ( t.output )
				outputs.remove( t.freq, t );
			else
				inputs.remove( t.freq );

			updateTunnel( t.freq, !t.output, false );
		}
	}

	private void updateTunnel(long freq, boolean updateOutputs, boolean configChange)
	{
		for (PartP2PTunnel p : outputs.get( freq ))
		{
			if ( configChange )
				p.onTunnelConfigChange();
			p.onTunnelNetworkChange();
		}

		PartP2PTunnel in = inputs.get( freq );
		if ( in != null )
		{
			if ( configChange )
				in.onTunnelConfigChange();
			in.onTunnelNetworkChange();
		}
	}

	public TunnelCollection<PartP2PTunnel> getOutputs(long freq, Class<? extends PartP2PTunnel> c)
	{
		PartP2PTunnel in = inputs.get( freq );
		if ( in == null )
			return NullColl;

		TunnelCollection<PartP2PTunnel> out = inputs.get( freq ).getCollection( outputs.get( freq ), c );
		if ( out == null )
			return NullColl;

		return out;
	}

	public PartP2PTunnel getInput(long freq)
	{
		return inputs.get( freq );
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
