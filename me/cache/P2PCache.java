package appeng.me.cache;

import java.util.HashMap;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.core.AELog;
import appeng.me.cache.helpers.TunnelCollection;
import appeng.parts.p2p.PartP2PTunnel;

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

	public void updateFreq(PartP2PTunnel t, long NewFreq)
	{
		outputs.remove( t.freq, t );
		inputs.remove( t.freq );

		t.freq = NewFreq;

		if ( t.output )
			outputs.put( t.freq, t );
		else
			inputs.put( t.freq, t );

		AELog.info( "update-" + (t.output ? "output: " : "input: ") + t.freq );
		updateTunnel( t.freq, t.output );
		updateTunnel( t.freq, !t.output );
	}

	@Override
	public void addNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
		{
			PartP2PTunnel t = (PartP2PTunnel) machine;
			AELog.info( "add-" + (t.output ? "output: " : "input: ") + t.freq );

			if ( t.output )
				outputs.put( t.freq, t );
			else
				inputs.put( t.freq, t );

			updateTunnel( t.freq, !t.output );
		}
	}

	@Override
	public void removeNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
		{
			PartP2PTunnel t = (PartP2PTunnel) machine;
			AELog.info( "rmv-" + (t.output ? "output: " : "input: ") + t.freq );

			if ( t.output )
				outputs.remove( t.freq, t );
			else
				inputs.remove( t.freq );

			updateTunnel( t.freq, !t.output );
		}
	}

	private void updateTunnel(long freq, boolean updateOutputs)
	{
		if ( updateOutputs )
		{
			for (PartP2PTunnel p : outputs.values())
				p.onChange();
		}
		else
		{
			for (PartP2PTunnel p : inputs.values())
				p.onChange();
		}
	}

	public TunnelCollection<PartP2PTunnel> getOutputs(long freq, Class<? extends PartP2PTunnel> c)
	{
		PartP2PTunnel in = inputs.get( freq );
		if ( in == null )
			return NullColl;

		return inputs.get( freq ).getCollection( outputs.get( freq ) );
	}

	public PartP2PTunnel getInput(long freq)
	{
		return inputs.get( freq );
	}

	@Override
	public void onUpdateTick(IGrid grid)
	{

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
