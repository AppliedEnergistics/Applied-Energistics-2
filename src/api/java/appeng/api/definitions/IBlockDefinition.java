package appeng.api.definitions;


import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockPos;
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
	 * @param pos location
	 *
	 * @return if the block is placed in the world at the specific location.
	 */
	boolean isSameAs( IBlockAccess world, BlockPos pos );
}
