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

package appeng.api.storage.data;


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;


public interface IAEStack<StackType extends IAEStack>
{

	/**
	 * add two stacks together
	 *
	 * @param is added item
	 */
	void add( StackType is );

	/**
	 * number of items in the stack.
	 *
	 * @return basically ItemStack.stackSize
	 */
	long getStackSize();

	/**
	 * changes the number of items in the stack.
	 *
	 * @param stackSize , ItemStack.stackSize = N
	 */
	StackType setStackSize( long stackSize );

	/**
	 * Same as getStackSize, but for requestable items. ( LP )
	 *
	 * @return basically itemStack.stackSize but for requestable items.
	 */
	long getCountRequestable();

	/**
	 * Same as setStackSize, but for requestable items. ( LP )
	 *
	 * @return basically itemStack.stackSize = N but for setStackSize items.
	 */
	StackType setCountRequestable( long countRequestable );

	/**
	 * true, if the item can be crafted.
	 *
	 * @return true, if it can be crafted.
	 */
	boolean isCraftable();

	/**
	 * change weather the item can be crafted.
	 *
	 * @param isCraftable can item be crafted
	 */
	StackType setCraftable( boolean isCraftable );

	/**
	 * clears, requestable, craftable, and stack sizes.
	 */
	StackType reset();

	/**
	 * returns true, if the item can be crafted, requested, or extracted.
	 *
	 * @return isThisRecordMeaningful
	 */
	boolean isMeaningful();

	/**
	 * Adds more to the stack size...
	 *
	 * @param i additional stack size
	 */
	void incStackSize( long i );

	/**
	 * removes some from the stack size.
	 */
	void decStackSize( long i );

	/**
	 * adds items to the requestable
	 *
	 * @param i increased amount of requested items
	 */
	void incCountRequestable( long i );

	/**
	 * removes items from the requestable
	 *
	 * @param i decreased amount of requested items
	 */
	void decCountRequestable( long i );

	/**
	 * write to a NBTTagCompound.
	 *
	 * @param i to be written data
	 */
	void writeToNBT( NBTTagCompound i );

	/**
	 * Compare stacks using precise logic.
	 *
	 * a IAEItemStack to another AEItemStack or a ItemStack.
	 *
	 * or
	 *
	 * IAEFluidStack, FluidStack
	 *
	 * @param obj compared object
	 *
	 * @return true if they are the same.
	 */
	@Override
	boolean equals( Object obj );

	/**
	 * compare stacks using fuzzy logic
	 *
	 * a IAEItemStack to another AEItemStack or a ItemStack.
	 *
	 * @param st   stacks
	 * @param mode used fuzzy mode
	 *
	 * @return true if two stacks are equal based on AE Fuzzy Comparison.
	 */
	boolean fuzzyComparison( Object st, FuzzyMode mode );

	/**
	 * Slower for disk saving, but smaller/more efficient for packets.
	 *
	 * @param data to be written data
	 *
	 * @throws IOException
	 */
	void writeToPacket( ByteBuf data ) throws IOException;

	/**
	 * Clone the Item / Fluid Stack
	 *
	 * @return a new Stack, which is copied from the original.
	 */
	StackType copy();

	/**
	 * create an empty stack.
	 *
	 * @return a new stack, which represents an empty copy of the original.
	 */
	StackType empty();

	/**
	 * obtain the NBT Data for the item.
	 *
	 * @return nbt data
	 */
	IAETagCompound getTagCompound();

	/**
	 * @return true if the stack is a {@link IAEItemStack}
	 */
	boolean isItem();

	/**
	 * @return true if the stack is a {@link IAEFluidStack}
	 */
	boolean isFluid();

	/**
	 * @return ITEM or FLUID
	 */
	StorageChannel getChannel();
}
