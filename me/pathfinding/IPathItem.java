package appeng.me.pathfinding;

import java.util.EnumSet;

import appeng.api.networking.GridFlags;
import appeng.api.util.IReadOnlyCollection;

public interface IPathItem
{

	IPathItem getControllerRoute();

	void setControllerRoute(IPathItem fast, boolean zeroOut);

	/**
	 * used to determine if the finder can continue.
	 */
	boolean canSupportMoreChannels();

	/**
	 * find possible choices for other pathing.
	 */
	IReadOnlyCollection<IPathItem> getPossibleOptions();

	/**
	 * add one to the channel count, this is mostly for cables.
	 */
	void incrementChannelCount(int usedChannels);

	/**
	 * get the grid flags for this IPathItem.
	 * 
	 * @return the flag set.
	 */
	public EnumSet<GridFlags> getFlags();

	/**
	 * channels are done, wrap it up.
	 * 
	 * @return
	 */
	void finalizeChannels();

}
