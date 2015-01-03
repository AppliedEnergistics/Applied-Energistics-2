/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 AlgorithmX2
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package appeng.api.util;


import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IComparableDefinition;
import appeng.api.definitions.IItemDefinition;
import appeng.api.definitions.ITileDefinition;


/**
 * Gives easy access to different part of the various, items/blocks/materials in AE.
 *
 * @deprecated use {@link ITileDefinition} and its sub-classes
 */
@Deprecated
public interface AEItemDefinition
{
	/**
	 * @return the {@link Block} Implementation if applicable
	 *
	 * @deprecated use {@link IBlockDefinition#maybeBlock()}
	 */
	@Deprecated
	@Nullable
	Block block();

	/**
	 * @return the {@link Item} Implementation if applicable
	 *
	 * @deprecated use {@link IItemDefinition#maybeItem()}
	 */
	@Deprecated
	@Nullable
	Item item();

	/**
	 * @return the {@link TileEntity} Class if applicable.
	 *
	 * @deprecated use {@link ITileDefinition#maybeEntity()}
	 */
	@Deprecated
	@Nullable
	Class<? extends TileEntity> entity();

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 *
	 * @deprecated use {@link IItemDefinition#maybeStack(int)}
	 */
	@Deprecated
	@Nullable
	ItemStack stack( int stackSize );

	/**
	 * Compare {@link ItemStack} with this
	 *
	 * @param comparableItem compared item
	 *
	 * @return true if the item stack is a matching item.
	 *
	 * @deprecated use {@link IComparableDefinition#isSameAs(ItemStack)}
	 */
	@Deprecated
	boolean sameAsStack( ItemStack comparableItem );

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
	 * @deprecated use {@link IComparableDefinition#isSameAs(IBlockAccess, int, int, int)} }
	 */
	@Deprecated
	boolean sameAsBlock( IBlockAccess world, int x, int y, int z );
}
