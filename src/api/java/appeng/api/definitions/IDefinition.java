package appeng.api.definitions;


import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;


public interface IDefinition
{
	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	ItemStack stack(int stackSize);

	/**
	 * Compare {@link ItemStack} with this
	 *
	 * @param comparableStack compared item
	 * @return true if the item stack is a matching item.
	 */
	boolean sameAsStack( ItemStack comparableStack );

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
	boolean sameAsBlock( IBlockAccess world, int x, int y, int z );
}
