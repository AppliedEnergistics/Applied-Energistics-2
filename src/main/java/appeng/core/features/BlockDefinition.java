/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.features;


import java.lang.reflect.Constructor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;


public class BlockDefinition extends ItemDefinition implements IBlockDefinition
{
	private static final ItemBlockTransformer ITEMBLOCK_TRANSFORMER = new ItemBlockTransformer();
	private final Optional<Block> block;

	public BlockDefinition( final String identifier, Block block, ActivityState state )
	{
		super( identifier, constructItemFromBlock( block ), state );

		Preconditions.checkNotNull( block );
		Preconditions.checkNotNull( state );

		if( state == ActivityState.Enabled )
		{
			this.block = Optional.of( block );
		}
		else
		{
			this.block = Optional.absent();
		}
	}

	/**
	 * Create an {@link ItemBlock} from a {@link Block} to register it later as {@link Item}
	 *
	 * @param block source block
	 *
	 * @return item from block
	 */
	private static Item constructItemFromBlock( Block block )
	{
		final Class<? extends ItemBlock> itemclass = getItemBlockConstructor( block );
		return constructItemBlock( block, itemclass );
	}

	/**
	 * Returns the constructor to use.
	 *
	 * Either {@link ItemBlock} or in case of an {@link AEBaseBlock} the class returned by
	 * AEBaseBlock.getItemBlockClass().
	 *
	 * @param block the block used to determine the used constructor.
	 *
	 * @return a {@link Class} extending ItemBlock
	 */
	private static Class<? extends ItemBlock> getItemBlockConstructor( Block block )
	{
		if( block instanceof AEBaseBlock )
		{
			final AEBaseBlock aeBaseBlock = (AEBaseBlock) block;
			return aeBaseBlock.getItemBlockClass();
		}

		return ItemBlock.class;
	}

	/**
	 * Actually construct an instance of {@link Item} with the block and earlier determined constructor.
	 *
	 * Shamelessly stolen from the forge magic.
	 *
	 * TODO: throw an exception instead of returning null? As this could cause issue later on.
	 *
	 * @param block the block to create the {@link ItemBlock} from
	 * @param itemclass the class used to construct it.
	 *
	 * @return an {@link Item} for the block. Actually always a sub type of {@link ItemBlock}
	 */
	private static Item constructItemBlock( Block block, Class<? extends ItemBlock> itemclass )
	{
		try
		{
			Object[] itemCtorArgs = {};
			Class<?>[] ctorArgClasses = new Class<?>[itemCtorArgs.length + 1];
			ctorArgClasses[0] = Block.class;
			for( int idx = 1; idx < ctorArgClasses.length; idx++ )
			{
				ctorArgClasses[idx] = itemCtorArgs[idx - 1].getClass();
			}

			Constructor<? extends ItemBlock> itemCtor = itemclass.getConstructor( ctorArgClasses );
			return itemCtor.newInstance( ObjectArrays.concat( block, itemCtorArgs ) );
		}
		catch( Throwable t )
		{
			return null;
		}
	}

	@Override
	public final Optional<Block> maybeBlock()
	{
		return this.block;
	}

	@Override
	public final Optional<ItemBlock> maybeItemBlock()
	{
		return this.block.transform( ITEMBLOCK_TRANSFORMER );
	}

	@Override
	public final Optional<ItemStack> maybeStack( int stackSize )
	{
		return this.block.transform( new ItemStackTransformer( stackSize ) );
	}

	@Override
	public final boolean isSameAs( IBlockAccess world, BlockPos pos )
	{
		return this.isEnabled() && world.getBlockState( pos ).getBlock() == this.block.get();
	}

	private static class ItemBlockTransformer implements Function<Block, ItemBlock>
	{
		@Override
		public ItemBlock apply( Block input )
		{
			return new ItemBlock( input );
		}
	}

	private static class ItemStackTransformer implements Function<Block, ItemStack>
	{
		private final int stackSize;

		public ItemStackTransformer( int stackSize )
		{
			Preconditions.checkArgument( stackSize > 0 );

			this.stackSize = stackSize;
		}

		@Override
		public ItemStack apply( Block input )
		{
			return new ItemStack( input, this.stackSize );
		}
	}
}
