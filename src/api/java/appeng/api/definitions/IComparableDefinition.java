package appeng.api.definitions;


import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;


/**
 * Interface to compare a definition with an itemstack or a block
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public interface IComparableDefinition
{
	/**
	 * Compare {@link ItemStack} with this
	 *
	 * @param comparableStack compared item
	 *
	 * @return true if the item stack is a matching item.
	 */
	boolean isSameAs( ItemStack comparableStack );

	/**
	 * Compare Block with world.
	 *
	 * @param world world of block
	 * @param x     x pos of block
	 * @param y     y pos of block
	 * @param z     z pos of block
	 *
	 * @return if the block is placed in the world at the specific location.
	 *
	 * @deprecated moved to {@link IBlockDefinition}. Is removed in the next major release rv3
	 */
	@Deprecated
	boolean isSameAs( IBlockAccess world, int x, int y, int z );
}
