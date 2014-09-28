package appeng.me.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

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
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPostCacheConstruction;
import appeng.api.networking.events.MENetworkPowerIdleChange;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkPowerStorage;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.me.energy.EnergyThreshold;
import appeng.me.energy.EnergyWatcher;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class EnergyGridCache implements IEnergyGrid
{

	/**
	 * estimated power available.
	 */
	int availableTicksSinceUpdate = 0;
	double globalAvailablePower = 0;
	double globalMaxPower = 0;

	/**
	 * idle draw.
	 */
	double drainPerTick = 0;

	final double AvgLength = 40.0;

	double avgDrainPerTick = 0;
	double avgInjectionPerTick = 0;

	double tickDrainPerTick = 0;
	double tickInjectionPerTick = 0;

	/**
	 * power status
	 */
	boolean publicHasPower = false;
	boolean hasPower = true;
	long ticksSinceHasPowerChange = 900;

	/**
	 * excess power in the system.
	 */
	double extra = 0;

	IAEPowerStorage lastProvider;
	final Set<IAEPowerStorage> providers = new LinkedHashSet();

	IAEPowerStorage lastRequester;
	final Set<IAEPowerStorage> requesters = new LinkedHashSet();

	final public TreeSet<EnergyThreshold> interests = new TreeSet<EnergyThreshold>();
	final private HashMap<IGridNode, IEnergyWatcher> watchers = new HashMap<IGridNode, IEnergyWatcher>();

	final private Set<IEnergyGrid> localSeen = new HashSet();

	private double buffer()
	{
		return providers.isEmpty() ? 1000.0 : 0.0;
	}

	private IAEPowerStorage getFirstRequester()
	{
		if ( lastRequester == null )
		{
			Iterator<IAEPowerStorage> i = requesters.iterator();
			lastRequester = i.hasNext() ? i.next() : null;
		}

		return lastRequester;
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

	final Multiset<IEnergyGridProvider> energyGridProviders = HashMultiset.create();

	final IGrid myGrid;
	PathGridCache pgc;

	public EnergyGridCache(IGrid g) {
		myGrid = g;
	}

	@MENetworkEventSubscribe
	public void postInit(MENetworkPostCacheConstruction pcc)
	{
		pgc = myGrid.getCache( IPathingGrid.class );
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
		localSeen.clear();
		return getEnergyDemand( maxRequired, localSeen );
	}

	@Override
	public double getEnergyDemand(double maxRequired, Set<IEnergyGrid> seen)
	{
		if ( !seen.add( this ) )
			return 0;

		double required = buffer() - extra;

		Iterator<IAEPowerStorage> it = requesters.iterator();
		while (required < maxRequired && it.hasNext())
		{
			IAEPowerStorage node = it.next();
			if ( node.getPowerFlow() != AccessRestriction.READ )
				required += Math.max( 0.0, node.getAEMaxPower() - node.getAECurrentPower() );
		}

		Iterator<IEnergyGridProvider> ix = energyGridProviders.iterator();
		while (required < maxRequired && ix.hasNext())
		{
			IEnergyGridProvider node = ix.next();
			required += node.getEnergyDemand( maxRequired - required, seen );
		}

		return required;
	}

	@Override
	public double injectPower(double amt, Actionable mode)
	{
		localSeen.clear();
		return injectAEPower( amt, mode, localSeen );
	}

	@Override
	public double injectAEPower(double amt, Actionable mode, Set<IEnergyGrid> seen)
	{
		if ( !seen.add( this ) )
			return 0;

		double ignore = extra;
		amt += extra;

		if ( mode == Actionable.SIMULATE )
		{
			Iterator<IAEPowerStorage> it = requesters.iterator();
			while (amt > 0 && it.hasNext())
			{
				IAEPowerStorage node = it.next();
				amt = node.injectAEPower( amt, Actionable.SIMULATE );
			}

			Iterator<IEnergyGridProvider> i = energyGridProviders.iterator();
			while (amt > 0 && i.hasNext())
				amt = i.next().injectAEPower( amt, mode, seen );
		}
		else
		{
			tickInjectionPerTick += amt - ignore;
			// totalInjectionPastTicks[0] += i;

			while (amt > 0 && !requesters.isEmpty())
			{
				IAEPowerStorage node = getFirstRequester();

				amt = node.injectAEPower( amt, Actionable.MODULATE );
				if ( amt > 0 )
				{
					requesters.remove( node );
					lastRequester = null;
				}
			}

			Iterator<IEnergyGridProvider> i = energyGridProviders.iterator();
			while (amt > 0 && i.hasNext())
			{
				IEnergyGridProvider what = i.next();
				Set<IEnergyGrid> listCopy = new HashSet<IEnergyGrid>();
				listCopy.addAll( seen );

				double cannotHold = what.injectAEPower( amt, Actionable.SIMULATE, listCopy );
				what.injectAEPower( amt - cannotHold, mode, seen );

				amt = cannotHold;
			}

			extra = amt;
		}

		return Math.max( 0.0, amt - buffer() );
	}

	@Override
	public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm)
	{
		localSeen.clear();
		return pm.divide( extractAEPower( pm.multiply( amt ), mode, localSeen ) );
	}

	@Override
	public void addNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof IEnergyGridProvider )
			energyGridProviders.add( (IEnergyGridProvider) machine );

		// idle draw...
		GridNode gridNode = (GridNode) node;
		IGridBlock gb = gridNode.getGridBlock();
		gridNode.previousDraw = gb.getIdlePowerUsage();
		drainPerTick += gridNode.previousDraw;

		// power storage
		if ( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if ( ps.isAEPublicPowerStorage() )
			{
				double max = ps.getAEMaxPower();
				double current = ps.getAECurrentPower();

				if ( ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					globalMaxPower += ps.getAEMaxPower();
				}

				if ( current > 0 && ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					globalAvailablePower += current;
					providers.add( ps );
				}

				if ( current < max && ps.getPowerFlow() != AccessRestriction.READ )
					requesters.add( ps );
			}
		}

		if ( machine instanceof IEnergyWatcherHost )
		{
			IEnergyWatcherHost swh = (IEnergyWatcherHost) machine;
			EnergyWatcher iw = new EnergyWatcher( this, (IEnergyWatcherHost) swh );
			watchers.put( node, iw );
			swh.updateWatcher( iw );
		}

		myGrid.postEventTo( node, new MENetworkPowerStatusChange() );
	}

	@Override
	public void removeNode(IGridNode node, IGridHost machine)
	{
		if ( machine instanceof IEnergyGridProvider )
			energyGridProviders.remove( machine );

		// idle draw.
		GridNode gridNode = (GridNode) node;
		drainPerTick -= gridNode.previousDraw;

		// power storage.
		if ( machine instanceof IAEPowerStorage )
		{
			IAEPowerStorage ps = (IAEPowerStorage) machine;
			if ( ps.isAEPublicPowerStorage() )
			{
				if ( ps.getPowerFlow() != AccessRestriction.WRITE )
				{
					globalMaxPower -= ps.getAEMaxPower();
					globalAvailablePower -= ps.getAECurrentPower();
				}

				if ( lastProvider == machine )
					lastProvider = null;

				if ( lastRequester == machine )
					lastRequester = null;

				providers.remove( machine );
				requesters.remove( machine );
			}
		}

		if ( machine instanceof IStackWatcherHost )
		{
			IEnergyWatcher myWatcher = watchers.get( machine );
			if ( myWatcher != null )
			{
				myWatcher.clear();
				watchers.remove( machine );
			}
		}

	}

	double lastStoredPower = -1;

	@Override
	public void onUpdateTick()
	{
		if ( !interests.isEmpty() )
		{
			double oldPower = lastStoredPower;
			lastStoredPower = getStoredPower();

			EnergyThreshold low = new EnergyThreshold( Math.min( oldPower, lastStoredPower ), null );
			EnergyThreshold high = new EnergyThreshold( Math.max( oldPower, lastStoredPower ), null );
			for (EnergyThreshold th : interests.subSet( low, true, high, true ))
			{
				((EnergyWatcher) th.watcher).post( this );
			}
		}

		avgDrainPerTick *= (AvgLength - 1) / AvgLength;
		avgInjectionPerTick *= (AvgLength - 1) / AvgLength;

		avgDrainPerTick += tickDrainPerTick / AvgLength;
		avgInjectionPerTick += tickInjectionPerTick / AvgLength;

		tickDrainPerTick = 0;
		tickInjectionPerTick = 0;

		// power information.
		boolean currentlyHasPower = false;

		if ( drainPerTick > 0.0001 )
		{
			double drained = extractAEPower( getIdlePowerUsage(), Actionable.MODULATE, PowerMultiplier.CONFIG );
			currentlyHasPower = drained >= drainPerTick - 0.001;
		}
		else
		{
			currentlyHasPower = extractAEPower( 0.1, Actionable.SIMULATE, PowerMultiplier.CONFIG ) > 0;
		}

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
	}

	private void publicPowerState(boolean newState, IGrid grid)
	{
		if ( publicHasPower == newState )
			return;

		publicHasPower = newState;
		((Grid) myGrid).setImportantFlag( 0, publicHasPower );
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

		return Math.max( 0.0, globalAvailablePower );
	}

	@Override
	public double getMaxStoredPower()
	{
		return globalMaxPower;
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
				Iterator<IEnergyGridProvider> i = energyGridProviders.iterator();
				while (extractedPower < amt && i.hasNext())
					extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
			}

			return extractedPower;
		}
		else
		{
			extra = 0;
			extractedPower = doExtract( extractedPower, amt );
		}

		// got more then we wanted?
		if ( extractedPower > amt )
		{
			extra = extractedPower - amt;
			globalAvailablePower -= amt;

			tickDrainPerTick += amt;
			return amt;
		}

		if ( extractedPower < amt )
		{
			Iterator<IEnergyGridProvider> i = energyGridProviders.iterator();
			while (extractedPower < amt && i.hasNext())
				extractedPower += i.next().extractAEPower( amt - extractedPower, mode, seen );
		}

		// go less or the correct amount?
		globalAvailablePower -= extractedPower;
		tickDrainPerTick += extractedPower;
		return extractedPower;
	}

	private double doExtract(double extractedPower, double amt)
	{
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
		return drainPerTick + pgc.channelPowerUsage;
	}

	@Override
	public double getAvgPowerUsage()
	{
		return avgDrainPerTick;
	}

	@Override
	public double getAvgPowerInjection()
	{
		return avgInjectionPerTick;
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
