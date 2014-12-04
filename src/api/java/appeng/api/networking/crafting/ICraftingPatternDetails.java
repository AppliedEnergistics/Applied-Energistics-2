package appeng.api.networking.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.storage.data.IAEItemStack;

/**
 * do not implement provided by {@link ICraftingPatternItem}
 * 
 * caching this instance will increase performance of validation and checks.
 */
public interface ICraftingPatternDetails
{

	/**
	 * @return source item.
	 */
	ItemStack getPattern();

	/**
	 * @param slotIndex specific slot index
	 * @param itemStack item in slot
	 * @param world crafting world
	 * 
	 * @return if an item can be used in the specific slot for this pattern.
	 */
	boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world);

	/**
	 * @return if this pattern is a crafting pattern ( work bench )
	 */
	boolean isCraftable();

	/**
	 * @return a list of the inputs, will include nulls.
	 */
	IAEItemStack[] getInputs();

	/**
	 * @return a list of the inputs, will be clean
	 */
	IAEItemStack[] getCondensedInputs();

	/**
	 * @return a list of the outputs, will be clean
	 */
	IAEItemStack[] getCondensedOutputs();

	/**
	 * @return a list of the outputs, will include nulls.
	 */
	IAEItemStack[] getOutputs();

	/**
	 * @return if this pattern is enabled to support substitutions.
	 */
	boolean canSubstitute();

	/**
	 * Allow using this instance of the pattern details to preform the crafting action with performance enhancements.
	 * 
	 * @param craftingInv inventory
	 * @param world crafting world
	 * @return the crafted ( work bench ) item.
	 */
	ItemStack getOutput(InventoryCrafting craftingInv, World world);

	/**
	 * Set the priority the of this pattern.
	 * 
	 * @param priority priority of pattern
	 */
	void setPriority(int priority);

	/**
	 * Get the priority of this pattern
	 *
	 * @return the priority of this pattern
	 */
	int getPriority();
}
