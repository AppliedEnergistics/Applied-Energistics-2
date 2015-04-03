package appeng.api.definitions;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.google.common.base.Optional;


public interface IItemDefinition extends IComparableDefinition
{
	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Optional<Item> maybeItem();

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	Optional<ItemStack> maybeStack( int stackSize );
}
