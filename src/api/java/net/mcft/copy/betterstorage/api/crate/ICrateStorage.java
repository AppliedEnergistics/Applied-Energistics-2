package net.mcft.copy.betterstorage.api.crate;

import net.minecraft.item.ItemStack;

public interface ICrateStorage {
	
	// The "identifier" item stacks are used to look up specific
	// types of items within the crate. Item type, damage and NBT
	// data are used to identify items, so they have to match.
	
	// You can see which exact items are stored within the crate
	// using the getContents() and getRandomStacks() methods and
	// use the stacks returned to extract these items. Do not
	// modify stacks returned by these methods.
	
	// Storage crates are supposed to be random and less efficient,
	// as a downside to their ability to expand and store lots of
	// items. Therefore, where possible / it makes sense, it's
	// recommended to use getRandomStacks(). Look at a few items
	// every tick (or a different interval) and give up if none
	// matched the requirements. This encourages players to keep
	// the number of different items within a crate low.
	
	// If this is not possible, it's recommended to balance things
	// by for example increasing resource or energy cost, perhaps
	// depending on the number of unique items.
	
	
	/** Returns the crate's identifier. If it's the same as another
	 *  crate's identifier, they're part of the same crate pile and
	 *  therefore share the same inventory. */
	public Object getCrateIdentifier();
	
	
	/** Returns the number of slots in this crate pile. */
	public int getCapacity();
	
	/** Returns the number of slots that are occupied by items. This is
	 *  also the number of stacks that will be returned by getRandomStacks(). */
	public int getOccupiedSlots();
	
	// You can calculate the number of free slots by subtracting the
	// capacity by the number of occupied slots. Note that even if this
	// is 0, there might still be space for items in occupied slots.
	
	/** Returns the number of different unique items. This is also
	 *  the number of items that will be returned by getContents(). */
	public int getUniqueItems();
	
	
	/** Returns all items in the crate pile. The stacks may have
	 *  stack sizes above their usual limit. */
	public Iterable<ItemStack> getContents();
	
	/** Returns a randomized stream of stacks from the crate pile. This
	 *  works best if used to look at a few items. If the intention is to
	 *  look at the whole inventory, getContents() should be used instead. */
	public Iterable<ItemStack> getRandomStacks();
	
	
	/** Returns the number of items of this specific type. */
	public int getItemCount(ItemStack identifier);
	
	/** Returns the space left for items of this specific type. */
	public int getSpaceForItem(ItemStack identifier);
	
	
	/** Tries to insert an item stack into the crate. Returns null
	 *  if all items were inserted successfully, or an item stack
	 *  of whatever items could not be inserted.
	 *  The stack may have a stack size above its usual limit. */
	public ItemStack insertItems(ItemStack stack);
	
	/** Tries to extract the specified type and amount of items.
	 *  Returns null if no items could be extracted, or an item
	 *  stack if some, or all of them could be extracted successfully. */
	public ItemStack extractItems(ItemStack identifier, int amount);
	
	
	/** Registers a crate watcher. Its onCrateItemsModified method
	 *  will be called when any items are changed. */
	public void registerCrateWatcher(ICrateWatcher watcher);
	
	/** Unregisters a crate watcher. */
	public void unregisterCrateWatcher(ICrateWatcher watcher);
	
}
