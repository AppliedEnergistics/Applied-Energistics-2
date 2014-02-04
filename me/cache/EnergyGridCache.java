package appeng.me.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergyGridProvider;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.me.GridNode;

public class EnergyGridCache implements IEnergyGrid
{

	/**
	 * estimated power available.
	 */
	int availableTicksSinceUpdate = 0;
	double globalAvailablePower = 0;

	/**
	 * idle draw.
	 */
	double drainPerTick = 0;

	final double AvgLength = 40.0;

	double avgDrainPerTick = 0;
	double avgInjectionPerTick = 0;

	double tickDrainPerTick = 0;
	double tickInjectionPerTick = 0;

	// Double[] totalDrainPastTicks = new Double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
	// 0.0, 0.0, 0.0, 0.0, 0.0 };
	// Double[] totalInjectionPastTicks = new Double[] { 0.0, 0.0, 0.0, 0.0,
	// 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

	/**
	 * power status
	 */
	boolean publicHasPower = false;
	boolean hasPower = true;
	long ticksSinceHasPowerChange = 900;

	/**
	 * excess power in the system.
	 */
	double prev_extra = 0;
	double extra = 0;

	IAEPowerStorage lastProvider;
	Set<IAEPowerStorage> providers = new LinkedHashSet();

	IAEPowerStorage lastRequestor;
	Set<IAEPowerStorage> requesters = new LinkedHashSet();

	private IAEPowerStorage getFirstRequestor()
	{
		if ( lastRequestor == null )
		{
			Iterator<IAEPowerStorage> i = requesters.iterator();
			lastRequestor = i.hasNext() ? i.next() : null;
		}

		return lastRequestor;
	}

	private IAEPowerStorage getFirstProvider()
	{
		if ( lastProvider == null )
		{
			Iterator<IAEPowerStorage> i = providers.iterator();
			lastProvider = i.hasNext() ? i.next() : null;
		}

		return lastProvider;
	}

	Set<IEnergyGridProvider> gproviders = new LinkedHashSet();

	final IGrid myGrid;

	public EnergyGridCache(IGrid g) {
		myGrid = g;
	}

	@MENetworkEventSubscribe
	public void EnergyNodeChanges(MENetworkPowerIdleChange ev)
	{
		// update power usage based on event.
		GridNode node = (GridNode) ev.node;
		IGridBlock gb = node.getGridBlock();

		double newDraw = gb.getIdlePowerUsage();
		double diffDraw = newDraw - node.previousDraw;
		node.previousDraw = newDraw;

		drainPerTick += diffDraw;
	}

	@MENetworkEventSubscribe
	public void EnergyNodeChanges(MENetworkPowerStorage ev)
	{
		if ( ev.storage.isAEPublicPowerStorage() )
		{
			switch (ev.type)
			{
			case PROVIDE_POWER:
				if ( ev.storage.getPowerFlow() != AccessRestriction.WRITE )
					providers.add( ev.storage );
				break;
			case REQUEST_POWER:
				if ( ev.storage.getPowerFlow() != AccessRestriction.READ )
					requesters.add( ev.storage );
				break;
			}
		}
		else
		{
			(new RuntimeException( "Attempt to ask the IEnergyGrid to charge a non public energy store." )).printStackTrace();
		}
	}

	@Override
	public double getEnergyDemand(double maxRequired)
	{
		double required = 0;
		Iterator<IAEPowerStorage> it = requesters.iterator();
		while (required < maxRequired && it.hasNext())
		{
			IAEPowerStorage node = it.next();
			if ( node.getPowerFlow() != AccessRestriction.READ )
				required += Math.max( 0.0, node.getAEMaxPower() - node.getAECurrentPower() );
		}
		return required;
	}

	@Override
	public double injectPower(double i, Actionable mode)
	{
		i += extra;

		if ( mode == Actionable.SIMULATE )
		{
			Iterator<IAEPowerStorage> it = requesters.iterator();
			while (i > 0 && it.hasNext())
			{
				IAEPowerStorage node = it.next();
				i = node.injectAEPower( i, Actionable.SIMULATE );
			}
		}
		else
		{
			tickInjectionPerTick += i;
			// totalInjectionPastTicks[0] += i;

			while (i > 0 && !requesters.isEmpty())
			{
				IAEPowerStorage node = getFirstRequestor();

				i = node.injectAEPower( i, Actionable.MODULATE );
				if ( i > 0 )
				{
					requesters.remove( node );
					lastRequestor = null;
				}
			}

			extra = i;
		}

		return i;
	}

	Set<IEnergyGrid> seen = new HashSet();

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm)
	{
		seen.clear();
		return pm.divide( extractAEPower( pm.multiply( amt ), mode, seen ) );
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof IEnergyGridProvider )
			gproviders.add( (IEnergyGridProvider) machine );

		// idle draw...
		GridNode gnode = (GridNode) node;
		IGridBlock gb = gnode.getGridBlock();
		gnode.previousDraw = gb.getIdlePowerUsage();
		drainPerTick += gnode.previousDraw;

		// power storage
		if ( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if ( ps.isAEPublicPowerStorage() )
			{
				double max = ps.getAEMaxPower();
				double current = ps.getAECurrentPower();

				if ( current > 0 && ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					globalAvailablePower += ((IAEPowerStorage) machine).getAECurrentPower();
					providers.add( ps );
				}

				if ( current < max && ps.getPowerFlow() != AccessRestriction.READ )
					requesters.add( ps );
			}
		}

		myGrid.postEventTo( node, new MENetworkPowerStatusChange() );
	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof IEnergyGridProvider )
			gproviders.remove( machine );

		// idle draw.
		GridNode gnode = (GridNode) node;
		drainPerTick -= gnode.previousDraw;

		// power storage.
		if ( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if ( ps.getPowerFlow() != AccessRestriction.WRITE )
				globalAvailablePower -= ps.getAECurrentPower();

			if ( lastProvider == machine )
				lastProvider = null;

			if ( lastRequestor == machine )
				lastRequestor = null;

			providers.remove( machine );
			requesters.remove( machine );
		}
	}

	@Override
	public void onUpdateTick()
	{
		avgDrainPerTick *= (AvgLength - 1) / AvgLength;
		avgInjectionPerTick *= (AvgLength - 1) / AvgLength;

		avgDrainPerTick += tickDrainPerTick / AvgLength;
		avgInjectionPerTick += tickInjectionPerTick / AvgLength;

		tickDrainPerTick = 0;
		tickInjectionPerTick = 0;

		// next tick is here...
		// for (int x = 0; x < totalDrainPastTicks.length - 1; x++)
		// totalDrainPastTicks[x + 1] = totalDrainPastTicks[x];
		// totalDrainPastTicks[0] = 0.0;

		// for (int x = 0; x < totalInjectionPastTicks.length - 1; x++)
		// totalInjectionPastTicks[x + 1] = totalInjectionPastTicks[x];
		// totalInjectionPastTicks[0] = 0.0;

		// power information.
		double drained = extractAEPower( getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG );
		boolean currentlyHasPower = drained >= drainPerTick - 0.1;

		// ticks since change..
		if ( currentlyHasPower == hasPower )
			ticksSinceHasPowerChange++;
		else
			ticksSinceHasPowerChange = 0;

		// update status..
		hasPower = currentlyHasPower;

		// update public status, this buffers power ups for 30 ticks.
		if ( hasPower && ticksSinceHasPowerChange > 30 )
			publicPowerState( true, myGrid );
		else if ( !hasPower )
			publicPowerState( false, myGrid );

		availableTicksSinceUpdate++;
		if ( extra > 32 )
			injectPower( 0.0, Actionable.MODULATE );
	}

	private void publicPowerState(boolean newState, IGrid grid)
	{
		if ( publicHasPower == newState )
			return;

		publicHasPower = newState;
		grid.postEvent( new MENetworkPowerStatusChange() );
	}

	/**
	 * refresh current stored power.
	 */
	public void refreshPower()
	{
		availableTicksSinceUpdate = 0;
		globalAvailablePower = 0;
		for (IAEPowerStorage p : providers)
			globalAvailablePower += p.getAECurrentPower();
	}

	@Override
	public double getStoredPower()
	{
		if ( availableTicksSinceUpdate > 90 )
			refreshPower();

		return globalAvailablePower;
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, Set<IEnergyGrid> seen)
	{
		if ( !seen.add( this ) )
			return 0;

		double extractedPower = extra;

		if ( mode == Actionable.SIMULATE )
		{
			extractedPower += simulateExtract( extractedPower, amt );

			if ( extractedPower < amt )
			{
				Iterator<IEnergyGridProvider> i = gproviders.iterator();
				while (extractedPower < amt && i.hasNext())
					extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
			}

			return extractedPower;
		}
		else
			extractedPower += doExtract( extractedPower, amt );

		// got more then we wanted?
		if ( extractedPower > amt )
		{
			extra = extractedPower - amt;
			globalAvailablePower -= amt;

			return amt;
		}

		if ( extractedPower < amt )
		{
			Iterator<IEnergyGridProvider> i = gproviders.iterator();
			while (extractedPower < amt && i.hasNext())
				extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
		}

		// go less or the correct amount?
		globalAvailablePower -= extractedPower;
		return extractedPower;
	}

	private double doExtract(double extractedPower, double amt)
	{
		extra = 0;

		while (extractedPower < amt && !providers.isEmpty())
		{
			IAEPowerStorage node = getFirstProvider();

			double req = amt - extractedPower;
			double newPower = node.extractAEPower( req, Actionable.MODULATE, PowerMultiplier.ONE );
			extractedPower += newPower;

			if ( newPower < req )
			{
				providers.remove( node );
				lastProvider = null;
			}
		}

		tickDrainPerTick += extractedPower;
		// totalDrainPastTicks[0] += extractedPower;
		return extractedPower;
	}

	private double simulateExtract(double extractedPower, double amt)
	{
		Iterator<IAEPowerStorage> it = providers.iterator();

		while (extractedPower < amt && it.hasNext())
		{
			IAEPowerStorage node = it.next();

			double req = amt - extractedPower;
			double newPower = node.extractAEPower( req, Actionable.SIMULATE, PowerMultiplier.ONE );
			extractedPower += newPower;
		}

		return extractedPower;
	}

	@Override
	public boolean isNetworkPowered()
	{
		return publicHasPower;
	}

	@Override
	public double getIdlePowerUsage()
	{
		return drainPerTick;
	}

	@Override
	public double getAvgPowerUsage()
	{
		return avgDrainPerTick;/*
								 * double out = 0;
								 * 
								 * for (double x : totalDrainPastTicks) out +=
								 * x;
								 * 
								 * return out / totalDrainPastTicks.length;
								 */
	}

	@Override
	public double getAvgPowerInjection()
	{
		return avgInjectionPerTick;/*
									 * double out = 0;
									 * 
									 * for (double x : totalInjectionPastTicks)
									 * out += x;
									 * 
									 * return out /
									 * totalInjectionPastTicks.length;
									 */
	}

	@Override
	public void onSplit(IGridStorage storageB)
	{
		extra /= 2;
		storageB.dataObject().setDouble( "extraEnergy", extra );
	}

	@Override
	public void onJoin(IGridStorage storageB)
	{
		extra += storageB.dataObject().getDouble( "extraEnergy" );
	}

	@Override
	public void populateGridStorage(IGridStorage storage)
	{
		storage.dataObject().setDouble( "extraEnergy", this.extra );
	}

}
