package appeng.api.features;

import net.minecraft.item.ItemStack;
import appeng.api.config.TunnelType;

/**
 * A Registry for how p2p Tunnels are attuned
 */
public interface IP2PTunnelRegistry
{

	/**
	 * Allows third parties to register items from their mod as potential
	 * attunements for AE's P2P Tunnels
	 * 
	 * @param trigger
	 *            - the item which triggers attunement
	 * @param type
	 *            - the type of tunnel
	 */
	public abstract void addNewAttunement(ItemStack trigger, TunnelType type);

	/**
	 * returns null if no attunement can be found.
	 * 
	 * @param trigger attunement trigger
	 * @return null if no attunement can be found or attunement
	 */
	TunnelType getTunnelTypeByItem(ItemStack trigger);

}