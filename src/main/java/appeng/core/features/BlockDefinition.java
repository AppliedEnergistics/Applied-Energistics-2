/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ObjectArrays;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;
import appeng.block.AEBaseBlock;


public class BlockDefinition extends ItemDefinition implements IBlockDefinition
{
	private final Block block;
	private final boolean enabled;

	public BlockDefinition( Block block, ActivityState state )
	{
		super( constructItemFromBlock( block ), state );

		Preconditions.checkNotNull( block );
		Preconditions.checkNotNull( state );

		this.block = block;
		this.enabled = state == ActivityState.Enabled;
	}

	@Override
	public final Optional<Block> maybeBlock()
	{
		return Optional.of( this.block );
	}

	@Override
	public final Optional<ItemBlock> maybeItemBlock()
	{
		if( this.enabled )
		{
			return Optional.of( new ItemBlock( this.block ) );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public final Optional<ItemStack> maybeStack( int stackSize )
	{
		if( this.enabled )
		{
			return Optional.of( new ItemStack( this.block ) );
		}
		else
		{
			return Optional.absent();
		}
	}

	@Override
	public final boolean isSameAs( IBlockAccess world, int x, int y, int z )
	{
		return this.enabled && world.getBlock( x, y, z ) == this.block;
	}

	/**
	 * Create an {@link ItemBlock} from a {@link Block} to register it later as {@link Item}
	 *
	 * @param block
	 * @return
	 */
	private static Item constructItemFromBlock( Block block )
	{
		final Class<? extends ItemBlock> itemclass = getItemBlockConstructor( block );
		return constructItemBlock( block, itemclass );
	}

	/**
	 * Returns the cosntructor to use.
	 *
	 * Either {@link ItemBlock} or in case of an {@link AEBaseBlock} the class returned by
	 * AEBaseBlock.getItemBlockClass().
	 *
	 * @param block the block used to determine the used constructor.
	 * @return a {@link Class} extending ItemBlock
	 */
	private static Class<? extends ItemBlock> getItemBlockConstructor( Block block )
	{
		if ( block instanceof AEBaseBlock )
		{
			final AEBaseBlock aeBaseBlock = ( AEBaseBlock ) block;
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
	 * @return an {@link Item} for the block. Actually always a sub type of {@link ItemBlock}
	 */
	private static Item constructItemBlock( Block block, Class<? extends ItemBlock> itemclass )
	{
		try
		{
			Object[] itemCtorArgs = new Object[] {};
			Class<?>[] ctorArgClasses = new Class<?>[itemCtorArgs.length + 1];
			ctorArgClasses[0] = Block.class;
			for ( int idx = 1; idx < ctorArgClasses.length; idx++ )
			{
				ctorArgClasses[idx] = itemCtorArgs[idx - 1].getClass();
			}

			Constructor<? extends ItemBlock> itemCtor = itemclass.getConstructor( ctorArgClasses );
			return itemCtor.newInstance( ObjectArrays.concat( block, itemCtorArgs ) );
		}
		catch ( Throwable t )
		{
			return null;
		}
	}
}
