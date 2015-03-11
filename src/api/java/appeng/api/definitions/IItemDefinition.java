package appeng.api.definitions;


import net.minecraft.item.Item;


public interface IItemDefinition extends IDefinition
{
	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Item item();
}
