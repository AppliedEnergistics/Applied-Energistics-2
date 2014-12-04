package appeng.api.networking.crafting;

import appeng.api.networking.events.MENetworkCraftingPatternChange;

/**
 * Allows a IGridHost to provide crafting patterns to the network, post a {@link MENetworkCraftingPatternChange} to tell
 * AE2 to update.
 */
public interface ICraftingProvider extends ICraftingMedium
{

	/**
	 * called when the network is looking for possible crafting jobs.
	 * 
	 * @param craftingTracker crafting helper
	 */
	void provideCrafting(ICraftingProviderHelper craftingTracker);

}
