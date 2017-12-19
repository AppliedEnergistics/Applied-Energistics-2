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

import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.data.IAEItemStack;
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
	 * But the general intention is about converting an {@link ItemStack} into the corresponding {@link IAEItemStack}.
	 * Another valid case might be to use it instead of {@link IAEStack#copy()}, but this might not be supported by all
	 * types.
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
	 * use energy from energy, to remove request items from cell, at the request of src.
	 *
	 * @param energy to be drained energy source
	 * @param cell cell of requested items
	 * @param request requested items
	 * @param src action source
	 *
	 * @return items that successfully extracted.
	 */
	@Nullable
	T poweredExtraction( @Nonnull IEnergySource energy, @Nonnull IMEInventory<T> cell, @Nonnull T request, @Nonnull IActionSource src );

	/**
	 * use energy from energy, to inject input items into cell, at the request of src
	 *
	 * @param energy to be added energy source
	 * @param cell injected cell
	 * @param input to be injected items
	 * @param src action source
	 *
	 * @return items that failed to insert.
	 */
	@Nullable
	T poweredInsert( @Nonnull IEnergySource energy, @Nonnull IMEInventory<T> cell, @Nonnull T input, @Nonnull IActionSource src );

}
