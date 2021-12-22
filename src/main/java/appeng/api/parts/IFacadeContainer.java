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

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Used Internally.
 *
 * not intended for implementation.
 */
public interface IFacadeContainer {

    /**
     * Checks if the {@link IFacadePart} can be added to the given side.
     *
     * @return true if the facade can be successfully added.
     */
    boolean canAddFacade(IFacadePart a);

    /**
     * Attempts to add the {@link IFacadePart} to the given side.
     *
     * @return true if the facade as successfully added.
     */
    boolean addFacade(IFacadePart a);

    /**
     * Removed the facade on the given side, or does nothing.
     */
    void removeFacade(IPartHost host, Direction side);

    /**
     * @return the {@link IFacadePart} for a given side, or null.
     */
    @Nullable
    IFacadePart getFacade(Direction s);

    /**
     * write nbt data
     *
     * @param data to be written data
     */
    void writeToNBT(CompoundTag data);

    /**
     * read from stream
     *
     * @param data to be read data
     *
     * @return true if it was readable
     */
    boolean readFromStream(FriendlyByteBuf data);

    /**
     * read from NBT
     *
     * @param data to be read data
     */
    void readFromNBT(CompoundTag data);

    /**
     * write to stream
     *
     * @param data to be written data
     */
    void writeToStream(FriendlyByteBuf data);

    /**
     * @return true if there are no facades.
     */
    boolean isEmpty();
}
