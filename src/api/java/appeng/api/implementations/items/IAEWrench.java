package appeng.api.implementations.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Implemented on AE's wrench(s) as a substitute for if BC's API is not
 * available.
 */
public interface IAEWrench
{

	/**
	 * Check if the wrench can be used.
	 * 
	 * @param player wrenching player
	 * @param x x pos of wrenched block
	 * @param y y pos of wrenched block
	 * @param z z pos of wrenched block
	 *
	 * @return true if wrench can be used
	 */
	boolean canWrench(ItemStack wrench, EntityPlayer player, int x, int y, int z);

}
