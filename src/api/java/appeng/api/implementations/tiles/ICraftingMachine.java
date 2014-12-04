package appeng.api.implementations.tiles;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.networking.crafting.ICraftingPatternDetails;

public interface ICraftingMachine
{

	/**
	 * inserts a crafting plan, and the necessary items into the crafting machine.
	 * 
	 * @param patternDetails details of pattern
	 * @param table crafting table
	 * @param ejectionDirection ejection direction
	 * 
	 * @return if it was accepted, all or nothing.
	 */
	boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table, ForgeDirection ejectionDirection);

	/**
	 * check if the crafting machine is accepting pushes via pushPattern, if this is false, all calls to push will fail,
	 * you can try inserting into the inventory instead.
	 * 
	 * @return true, if pushPattern can complete, if its false push will always be false.
	 */
	boolean acceptsPlans();

}
