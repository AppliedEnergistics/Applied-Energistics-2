package appeng.api.networking.energy;

public interface IEnergyWatcherHost
{

	/**
	 * provides the IEnergyWatcher for this host, for the current network, is called when the hot changes networks. You
	 * do not need to clear your old watcher, its already been removed by the time this gets called.
	 * 
	 * @param newWatcher new watcher
	 */
	void updateWatcher(IEnergyWatcher newWatcher);

	/**
	 * Called when a threshold is crossed.
	 * 
	 * @param energyGrid grid
	 */
	void onThresholdPass(IEnergyGrid energyGrid);

}
