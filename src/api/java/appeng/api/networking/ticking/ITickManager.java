package appeng.api.networking.ticking;

import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridNode;

/**
 * 
 * The network tick manager.
 * 
 */
public interface ITickManager extends IGridCache
{

	/**
	 * immediately sets the node to tick, only valid if your node is marked as "Alertable" in its TickingRequest
	 * 
	 * Sleeping Devices Still Alertable, when your tile is alerted its new status is determined by the result of its
	 * tick.
	 * 
	 * @param node gridnode
	 */
	boolean alertDevice(IGridNode node);

	/**
	 * 
	 * disables ticking for your device.
	 * 
	 * @param node gridnode
	 * 
	 * @return if the call was successful.
	 */
	boolean sleepDevice(IGridNode node);

	/**
	 * 
	 * enables ticking for your device, undoes a sleepDevice call.
	 * 
	 * @param node gridnode
	 * 
	 * @return if the call was successful.
	 */
	boolean wakeDevice(IGridNode node);

}
