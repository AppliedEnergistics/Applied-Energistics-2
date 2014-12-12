package appeng.api.networking.crafting;

public interface ICraftingCallback
{

	/**
	 * this call back is synchronized with the world you passed.
	 * 
	 * @param job
	 *            - final job
	 */
	public void calculationComplete(ICraftingJob job);

}
