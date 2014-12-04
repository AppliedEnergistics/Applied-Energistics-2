package appeng.api.features;

import net.minecraft.item.ItemStack;

/**
 * Provider for special comparisons. when an item is encountered AE Will request
 * if the comparison function handles the item, by trying to request a
 * IItemComparison class.
 */
public interface IItemComparisonProvider
{

	/**
	 * should return a new IItemComparison, or return null if it doesn't handle
	 * the supplied item.
	 * 
	 * @param is item
	 * @return IItemComparison, or null
	 */
	IItemComparison getComparison(ItemStack is);

	/**
	 * Simple test for support ( AE generally skips this and calls the above function. )
	 * 
	 * @param stack item
	 * @return true, if getComparison will return a valid IItemComparison Object
	 */
	public boolean canHandle(ItemStack stack);

}