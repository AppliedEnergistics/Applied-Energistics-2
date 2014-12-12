package appeng.api.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

/**
 * Gives easy access to different part of the various, items/blocks/materials in AE.
 */
public interface AEItemDefinition
{

	/**
	 * @return the {@link Block} Implementation if applicable
	 */
	Block block();

	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Item item();

	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Class<? extends TileEntity> entity();

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	ItemStack stack(int stackSize);

	/**
	 * Compare {@link ItemStack} with this {@link AEItemDefinition}
	 * 
	 * @param comparableItem compared item
	 * @return true if the item stack is a matching item.
	 */
	boolean sameAsStack(ItemStack comparableItem);

	/**
	 * Compare Block with world.
	 * 
	 * @param world world of block
	 * @param x x pos of block
	 * @param y y pos of block
	 * @param z z pos of block
	 * 
	 * @return if the block is placed in the world at the specific location.
	 */
	boolean sameAsBlock(IBlockAccess world, int x, int y, int z);
}
