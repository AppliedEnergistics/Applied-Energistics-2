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

package appeng.api.implementations.parts;


import appeng.api.networking.IGridHost;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.INetworkToolAgent;


/**
 * The Storage monitor is a {@link IPart} located on the sides of a IPartHost
 */
public interface IPartStorageMonitor extends IPartMonitor, IPart, IGridHost, INetworkToolAgent
{

	/**
	 * @return the item being displayed on the storage monitor, in AEStack Form, can be either a IAEItemStack or an
	 * IAEFluidStack the quantity is important remember to use getStackSize() on the IAEStack, and not on the
	 * FluidStack/ItemStack acquired from it.
	 */
	IAEStack<?> getDisplayed();

	/**
	 * @return the current locked state of the Storage Monitor
	 */
	boolean isLocked();
}