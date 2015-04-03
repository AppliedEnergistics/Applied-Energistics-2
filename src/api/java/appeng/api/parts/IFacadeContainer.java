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

package appeng.api.parts;


import java.io.IOException;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;


/**
 * Used Internally.
 *
 * not intended for implementation.
 */
public interface IFacadeContainer
{

	/**
	 * Attempts to add the {@link IFacadePart} to the given side.
	 *
	 * @return true if the facade as successfully added.
	 */
	boolean addFacade( IFacadePart a );

	/**
	 * Removed the facade on the given side, or does nothing.
	 */
	void removeFacade( IPartHost host, ForgeDirection side );

	/**
	 * @return the {@link IFacadePart} for a given side, or null.
	 */
	IFacadePart getFacade( ForgeDirection s );

	/**
	 * rotate the facades left.
	 */
	void rotateLeft();

	/**
	 * write nbt data
	 *
	 * @param data to be written data
	 */
	void writeToNBT( NBTTagCompound data );

	/**
	 * read from stream
	 *
	 * @param data to be read data
	 *
	 * @return true if it was readable
	 *
	 * @throws IOException
	 */
	boolean readFromStream( ByteBuf data ) throws IOException;

	/**
	 * read from NBT
	 *
	 * @param data to be read data
	 */
	void readFromNBT( NBTTagCompound data );

	/**
	 * write to stream
	 *
	 * @param data to be written data
	 *
	 * @throws IOException
	 */
	void writeToStream( ByteBuf data ) throws IOException;

	/**
	 * @return true if there are no facades.
	 */
	boolean isEmpty();
}
