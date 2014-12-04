package appeng.api.networking.crafting;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

public interface ICraftingJob
{

	/**
	 * @return if this job is a simulation, simulations cannot be submitted and only represent 1 possible future
	 *         crafting job with fake items.
	 */
	boolean isSimulation();

	/**
	 * @return total number of bytes to process this job.
	 */
	long getByteTotal();

	/**
	 * Populates the plan list with stack size, and requestable values that represent the stored, and crafting job
	 * contents respectively.
	 * 
	 * @param plan plan
	 */
	void populatePlan(IItemList<IAEItemStack> plan);

	/**
	 * @return the final output of the job.
	 */
	IAEItemStack getOutput();

}
