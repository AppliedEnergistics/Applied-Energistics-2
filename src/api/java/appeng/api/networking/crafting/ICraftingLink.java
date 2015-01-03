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

package appeng.api.networking.crafting;


import net.minecraft.nbt.NBTTagCompound;


public interface ICraftingLink
{

	/**
	 * @return true if the job was canceled.
	 */
	boolean isCanceled();

	/**
	 * @return true if the job was completed.
	 */
	boolean isDone();

	/**
	 * cancels the job.
	 */
	void cancel();

	/**
	 * @return true if this link was generated without a requesting machine, such as a player generated request.
	 */
	boolean isStandalone();

	/**
	 * write the link to an NBT Tag
	 *
	 * @param tag to be written data
	 */
	void writeToNBT( NBTTagCompound tag );

	/**
	 * @return the crafting ID for this link.
	 */
	String getCraftingID();
}
