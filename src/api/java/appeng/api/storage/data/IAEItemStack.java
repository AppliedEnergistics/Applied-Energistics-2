package appeng.api.storage.data;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An alternate version of ItemStack for AE to keep tabs on things easier, and to support larger storage. stackSizes of
 * getItemStack will be capped.
 * 
 * You may hold on to these if you want, just make sure you let go of them when your not using them.
 * 
 * Don't Implement.
 * 
 * Construct with Util.createItemStack( ItemStack )
 */
public interface IAEItemStack extends IAEStack<IAEItemStack>
{

	/**
	 * creates a standard MC ItemStack for the item.
	 * 
	 * @return new ItemStack
	 */
	public ItemStack getItemStack();

	/**
	 * create a AE Item clone
	 * 
	 * @return the copy
	 */
	@Override
	public IAEItemStack copy();

	/**
	 * is there NBT Data for this item?
	 * 
	 * @return if there is
	 */
	boolean hasTagCompound();

	/**
	 * Combines two IAEItemStacks via addition.
	 * 
	 * @param option
	 *            to add to the current one.
	 */
	@Override
	void add(IAEItemStack option);

	/**
	 * quick way to get access to the MC Item Definition.
	 * 
	 * @return item definition
	 */
	Item getItem();

	/**
	 * @return the items damage value
	 */
	int getItemDamage();

	/**
	 * Compare the Ore Dictionary ID for this to another item.
	 */
	boolean sameOre(IAEItemStack is);

	/**
	 * compare the item/damage/nbt of the stack.
	 * 
	 * @param otherStack to be compared item
	 * @return true if it is the same type (same item, damage, nbt)
	 */
	boolean isSameType(IAEItemStack otherStack);

	/**
	 * compare the item/damage/nbt of the stack.
	 * 
	 * @param stored to be compared item
	 * @return true if it is the same type (same item, damage, nbt)
	 */
	boolean isSameType(ItemStack stored);
}