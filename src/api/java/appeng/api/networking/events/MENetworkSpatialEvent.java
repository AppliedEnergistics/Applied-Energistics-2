package appeng.api.networking.events;

import appeng.api.networking.IGridHost;

/**
 * An event that is posted whenever a spatial IO is actived, called for IGridCache
 */
public class MENetworkSpatialEvent extends MENetworkEvent
{
	public final IGridHost host;
	public final double spatialEnergyUsage;

	/**
	 * @param SpatialIO   ( instance of the SpatialIO block )
	 * @param EnergyUsage ( the amount of energy that the SpatialIO uses)
	 */
	public MENetworkSpatialEvent(IGridHost SpatialIO, double EnergyUsage)
	{
		host = SpatialIO;
		spatialEnergyUsage = EnergyUsage;
	}
}
