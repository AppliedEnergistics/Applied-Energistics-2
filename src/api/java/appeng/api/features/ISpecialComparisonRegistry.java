package appeng.api.features;

import net.minecraft.item.ItemStack;

/**
 * A Registry of any special comparison handlers for AE To use.
 * 
 */
public interface ISpecialComparisonRegistry
{

	/**
	 * return TheHandler or null.
	 * 
	 * @param stack item
	 * @return a handler it found for a specific item
	 */
	IItemComparison getSpecialComparison(ItemStack stack);

	/**
	 * Register a new special comparison function with AE.
	 * 
	 * @param prov comparison provider
	 */
	public void addComparisonProvider(IItemComparisonProvider prov);

}