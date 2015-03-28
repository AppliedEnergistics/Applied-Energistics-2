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


public interface AEColoredItemDefinition
{

	/**
	 * @return the {@link Block} Implementation if applicable
	 */
	Block block( AEColor color );

	/**
	 * @return the {@link Item} Implementation if applicable
	 */
	Item item( AEColor color );

	/**
	 * @return the {@link TileEntity} Class if applicable.
	 */
	Class<? extends TileEntity> entity( AEColor color );

	/**
	 * @return an {@link ItemStack} with specified quantity of this item.
	 */
	ItemStack stack( AEColor color, int stackSize );

	/**
	 * @param stackSize - stack size of the result.
	 *
	 * @return an array of all colors.
	 */
	ItemStack[] allStacks( int stackSize );

	/**
	 * Compare {@link ItemStack} with this
	 *
	 * @param color          compared color of item
	 * @param comparableItem compared item
	 *
	 * @return true if the item stack is a matching item.
	 */
	boolean sameAs( AEColor color, ItemStack comparableItem );
}
