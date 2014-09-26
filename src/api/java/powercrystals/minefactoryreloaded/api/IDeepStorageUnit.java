package powercrystals.minefactoryreloaded.api;

import net.minecraft.item.ItemStack;

public interface IDeepStorageUnit
{
	/**
	 * @return A populated ItemStack with stackSize for the full amount of materials in the DSU.
	 * May have a stackSize > getMaxStackSize(). May have a stackSize of 0 (indicating locked contents).
	 */
	ItemStack getStoredItemType();
	
	/**
	 * Sets the total amount of the item currently being stored, or zero if all items are to be removed.
	 */
	void setStoredItemCount(int amount);
	
	/**
	 * Sets the type of the stored item and initializes the number of stored items to amount.
	 * Will overwrite any existing stored items.
	 */
	void setStoredItemType(ItemStack type, int amount);
	
	/**
	 * @return The maximum number of items the DSU can hold.
	 * May change based on the current type stored.
	 */
	int getMaxStoredCount();
}
