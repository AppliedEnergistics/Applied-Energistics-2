package appeng.me.energy;

import appeng.api.networking.energy.IEnergyWatcher;
import appeng.util.ItemSorters;

public class EnergyThreshold implements Comparable<EnergyThreshold>
{

	public final double Limit;
	public final IEnergyWatcher watcher;
	final int hash;

	public EnergyThreshold(double lim, IEnergyWatcher wat) {
		Limit = lim;
		watcher = wat;

		if ( watcher != null )
			hash = watcher.hashCode() ^ ((Double) lim).hashCode();
		else
			hash = ((Double) lim).hashCode();
	}

	@Override
	public int hashCode()
	{
		return hash;
	}

	@Override
	public int compareTo(EnergyThreshold o)
	{
		return ItemSorters.compareDouble( Limit, o.Limit );
	}

}
