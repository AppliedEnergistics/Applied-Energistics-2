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


import net.minecraft.nbt.NBTTagCompound;

import appeng.api.features.IItemComparison;


/**
 * Don't cast this... either compare with it, or copy it.
 *
 * Don't Implement.
 */
public interface IAETagCompound
{

	/**
	 * @return a copy ( the copy will not be a IAETagCompound, it will be a NBTTagCompound )
	 */
	NBTTagCompound getNBTTagCompoundCopy();

	/**
	 * compare to other NBTTagCompounds or IAETagCompounds
	 *
	 * @param a compared object
	 *
	 * @return true, if they are the same.
	 */
	@Override
	boolean equals( Object a );

	/**
	 * @return the special comparison for this tag
	 */
	IItemComparison getSpecialComparison();
}