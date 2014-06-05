package appeng.crafting;

import net.minecraft.world.World;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;

public interface ICraftingHost
{

	/**
	 * Get Crafting cache for the host.
	 */
	IGrid getGrid();

	/**
	 * required for crafting calculations.
	 * 
	 * @return world the host is located in
	 */
	World getWorld();

	/**
	 * get source of moving items around.
	 * 
	 * @return {@link BaseActionSource} of host.
	 */
	BaseActionSource getActionSrc();

}
