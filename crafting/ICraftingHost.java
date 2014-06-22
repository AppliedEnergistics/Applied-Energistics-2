package appeng.crafting;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;

public interface ICraftingHost
{

	/**
	 * Get Crafting cache for the host.
	 */
	IGrid getGrid();

	/**
	 * get source of moving items around.
	 * 
	 * @return {@link BaseActionSource} of host.
	 */
	BaseActionSource getActionSrc();

}
