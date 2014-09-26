package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * @author PowerCrystals
 *
 * Defines a fertilizer item for use in the Fertilizer.
 */
public interface IFactoryFertilizer
{
	/**
	 * @return The ID of this fertilizer item.
	 */
	Item getFertilizer();
	
	/**
	 * @return The type of fertilizer this is.
	 */
	FertilizerType getFertilizerType(ItemStack stack);
	
	/**
	 * Called when a fertilization is successful. If you set the ItemStack size to 0, it will be deleted by the fertilizer.
	 * 
	 * @param fertilizer The ItemStack used to fertilize.
	 */
	void consume(ItemStack fertilizer);
}
