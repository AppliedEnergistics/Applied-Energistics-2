package appeng.decorative.stair;


import javax.annotation.Nonnull;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;


/**
 * @author thatsIch
 * @version rv3 - 29.06.2015
 * @since rv3 29.06.2015
 */
public class StairBlockDefinition implements IBlockDefinition
{
	public StairBlockDefinition()
	{
	}

	@Override
	public Optional<Block> maybeBlock()
	{
		return null;
	}

	@Override
	public Optional<ItemBlock> maybeItemBlock()
	{
		return null;
	}

	@Override
	public boolean isSameAs( IBlockAccess world, BlockPos pos )
	{
		return false;
	}

	@Nonnull
	@Override
	public String identifier()
	{
		return null;
	}

	@Override
	public Optional<Item> maybeItem()
	{
		return null;
	}

	@Override
	public Optional<ItemStack> maybeStack( int stackSize )
	{
		return null;
	}

	@Override
	public boolean isSameAs( ItemStack comparableStack )
	{
		return false;
	}
}
