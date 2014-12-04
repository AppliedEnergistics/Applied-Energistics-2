package appeng.api.networking.events;

import appeng.api.networking.IGrid;

/**
 * Part of AE's Event Bus.
 * 
 * Posted via {@link IGrid}.postEvent or {@link IGrid}.postEventTo
 */
public class MENetworkEvent
{

	private int visited = 0;
	private boolean canceled = false;

	/**
	 * Call to prevent AE from posting the event to any further objects.
	 */
	public void cancel()
	{
		canceled = true;
	}

	/**
	 * called by AE after each object is called to cancel any future calls.
	 * 
	 * @return true to cancel future calls
	 */
	public boolean isCanceled()
	{
		return canceled;
	}

	/**
	 * the number of objects that were visited by the event.
	 * 
	 * @return number of visitors
	 */
	public int getVisitedObjects()
	{
		return visited;
	}

	/**
	 * Called by AE after iterating the event subscribers.
	 * 
	 * @param v current number of visitors
	 */
	public void setVisitedObjects(int v)
	{
		visited = v;
	}
}
