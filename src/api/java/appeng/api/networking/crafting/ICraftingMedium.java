package appeng.api.networking.crafting;

import net.minecraft.inventory.InventoryCrafting;

/**
 * A place to send Items for crafting purposes, this is considered part of AE's External crafting system.
 */
public interface ICraftingMedium
{

	/**
	 * instruct a medium to create the item represented by the pattern+details, the items on the table, and where if
	 * possible the output should be directed.
	 * 
	 * @param patternDetails details
	 * @param table crafting table
	 * @return if the pattern was successfully pushed.
	 */
	boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table);

	/**
	 * @return if this is false, the crafting engine will refuse to send new jobs to this medium.
	 */
	boolean isBusy();

}
