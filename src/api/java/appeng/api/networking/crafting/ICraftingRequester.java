package appeng.api.networking.crafting;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;

import com.google.common.collect.ImmutableSet;

public interface ICraftingRequester extends IActionHost
{

	/**
	 * called when the host is added to the grid, and should return all crafting links it poses so they can be connected
	 * with the cpu that hosts the job.
	 * 
	 * @return set of jobs, or an empty list.
	 */
	ImmutableSet<ICraftingLink> getRequestedJobs();

	/**
	 * items are injected into the requester as they are completed, any items that cannot be taken, or are unwanted can
	 * be returned.
	 * 
	 * @param items item
	 * @param mode action mode
	 * @return unwanted item
	 */
	IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode);

	/**
	 * called when the job changes from in progress, to either complete, or canceled.
	 * 
	 * after this call the crafting link is "dead" and should be discarded.
	 */
	void jobStateChange(ICraftingLink link);

}
