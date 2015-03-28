package appeng.api.definitions;


import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

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
}
