package appeng.me.cache;

import java.util.Collection;
import java.util.HashMap;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.parts.misc.PartP2PTunnel;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class P2PCache implements IGridCache
{

	private HashMap<Long, PartP2PTunnel> inputs = new HashMap();
	private Multimap<Long, PartP2PTunnel> Tunnels = LinkedHashMultimap.create();

	final IGrid myGrid;

	public P2PCache(IGrid g) {
		myGrid = g;
	}

	@Override
	public void addNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
			reset( grid );
	}

	@Override
	public void removeNode(IGrid grid, IGridNode node, IGridHost machine)
	{
		if ( machine instanceof PartP2PTunnel )
			reset( grid );
	}

	public void reset(IGrid grid)
	{

		Tunnels.clear();
		inputs.clear();

		for (IGridNode n : grid.getMachines( PartP2PTunnel.class ))
		{
			PartP2PTunnel p = (PartP2PTunnel) n.getMachine();
			if ( p.freq > 0 )
			{
				if ( p.output )
					Tunnels.put( p.freq, p );
				else if ( inputs.containsKey( p.freq ) )
					p.output = true;
				else
					inputs.put( p.freq, p );
			}
		}

		for (PartP2PTunnel p : inputs.values())
			p.onChange();

		for (PartP2PTunnel p : Tunnels.values())
			p.onChange();
	}

	public Collection<PartP2PTunnel> getOutputs(long freq)
	{
		return Tunnels.get( freq );
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
