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

package appeng.api.storage;


import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;


public interface IStorageChannel<T extends IAEStack<T>>
{

	/**
	 * Can be used as factor for transferring stacks of a channel.
	 * 
	 * E.g. used by IO Ports to transfer 1000 mB, not 1 mB to match the
	 * item channel transferring a full bucket per operation.
	 * 
	 * @return
	 */
	default int transferFactor()
	{
		return 1;
	}

	/**
	 * The number of units (eg item count, or millibuckets) that can be stored per byte in a storage cell.
	 * Standard value for items is 8, and for fluids it's 8000
	 *
	 * @return number of units
	 */
	default int getUnitsPerByte()
	{
		return 8;
	}

	/**
	 * Create a new {@link IItemList} of the specific type.
	 * 
	 * @return
	 */
	@Nonnull
	IItemList<T> createList();

	/**
	 * Create a new {@link IAEStack} subtype of the specific object.
	 * 
	 * The parameter is unbound to allow a slightly more flexible approach.
	 * But the general intention is about converting an {@link ItemStack} or {@link FluidStack} into the corresponding
	 * {@link IAEStack}.
	 * Another valid case might be to use it instead of {@link IAEStack#copy()}, but this might not be supported by all
	 * types.
	 * IAEStacks that use custom items for {@link IAEStack#asItemStackRepresentation()} must also be able to convert
	 * these.
	 * 
	 * @param input The object to turn into an {@link IAEStack}
	 * @return The converted stack or null
	 */
	@Nullable
	T createStack( @Nonnull Object input );

	/**
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 */
	@Nullable
	T readFromPacket( @Nonnull ByteBuf input ) throws IOException;

	/**
	 * create from nbt data
	 * 
	 * @param nbt
	 * @return
	 */
	@Nullable
	T createFromNBT( @Nonnull NBTTagCompound nbt );
}
