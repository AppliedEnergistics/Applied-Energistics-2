package appeng.api.definitions;


import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.IBlockAccess;

import com.google.common.base.Optional;


public interface IBlockDefinition extends IItemDefinition
{
	/**
	 * @return the {@link Block} implementation if applicable
	 */
	Optional<Block> maybeBlock();

	/**
	 * @return the {@link ItemBlock} implementation if applicable
	 */
	Optional<ItemBlock> maybeItemBlock();

	/**
	 * Compare Block with world.
	 *
	 * @param world world of block
	 * @param x     x pos of block
	 * @param y     y pos of block
	 * @param z     z pos of block
	 *
	 * @return if the block is placed in the world at the specific location.
	 */
	boolean isSameAs( IBlockAccess world, int x, int y, int z );
}
