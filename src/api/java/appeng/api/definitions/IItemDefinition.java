package appeng.api.definitions;


import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;


public interface IItemDefinition extends IComparableDefinition
{
	/**
	 * @return the unique name of the definition which will be used to register the underlying structure. Will never be null
	 */
	@Nonnull
	String identifier();

	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Optional<Item> maybeItem();

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	Optional<ItemStack> maybeStack( int stackSize );
}
