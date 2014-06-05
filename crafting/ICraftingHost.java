package appeng.crafting;

import net.minecraft.world.World;
import appeng.me.cache.CraftingCache;

public interface ICraftingHost
{

	/**
	 * Get Crasfting cache for the host.
	 */
	CraftingCache getCraftingCache();

	/**
	 * required for crafting calculations.
	 * 
	 * @return world the host is located in
	 */
	World getWorld();

}
