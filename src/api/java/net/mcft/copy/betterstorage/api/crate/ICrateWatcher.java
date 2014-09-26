package net.mcft.copy.betterstorage.api.crate;

import net.minecraft.item.ItemStack;

public interface ICrateWatcher {
	
	/** Called when any items in the crate storage get
	 *  changed. The stack will represent which items were
	 *  added or removed. Stack size is negative if items
	 *  were removed. */
	public void onCrateItemsModified(ItemStack stack);
	
}
