package appeng.api.implementations.items;

import net.minecraft.item.ItemStack;

import java.util.Set;

/**
 * Lets you specify the name of the group of items this falls under.
 */
public interface IItemGroup
{

	/**
	 * returning null, is the same as not implementing the interface at all.
	 * 
	 * @param is item
	 * @return an unlocalized string to use for the items group name.
	 */
	String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is);

}
