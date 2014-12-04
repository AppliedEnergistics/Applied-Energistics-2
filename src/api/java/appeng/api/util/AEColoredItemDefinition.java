package appeng.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface AEColoredItemDefinition
{

	/**
	 * @return the {@link Block} Implementation if applicable
	 */
	Block block(AEColor color);

	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Item item(AEColor color);

	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Class<? extends TileEntity> entity(AEColor color);

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	ItemStack stack(AEColor color, int stackSize);

	/**
	 * @param stackSize
	 *            - stack size of the result.
	 * @return an array of all colors.
	 */
	ItemStack[] allStacks(int stackSize);

	/**
	 * Compare {@link ItemStack} with this {@link AEItemDefinition}
	 *
	 * @param color compared color of item
	 * @param comparableItem compared item
	 * @return true if the item stack is a matching item.
	 */
	boolean sameAs(AEColor color, ItemStack comparableItem);

}
