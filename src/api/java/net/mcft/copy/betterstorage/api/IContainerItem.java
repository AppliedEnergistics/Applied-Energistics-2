package net.mcft.copy.betterstorage.api;

import net.minecraft.item.ItemStack;

/** When implemented for an Item, signalizes that it can store or
 *  is storing other items. This is useful for other container items to
 *  keep themselves from accepting them, resulting in infinite storage. */
public interface IContainerItem {
	
	/** Returns the contents of this container item. <br>
	 *  May return null if not supported. */
	public ItemStack[] getContainerItemContents(ItemStack container);
	
	/** Returns if this item can be stored in another container item. */
	public boolean canBeStoredInContainerItem(ItemStack item);
	
}
