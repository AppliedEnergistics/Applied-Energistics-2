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
