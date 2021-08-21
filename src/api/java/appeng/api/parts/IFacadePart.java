/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 - 2015 AlgorithmX2
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

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used Internally.
 *
 * not intended for implementation.
 */
public interface IFacadePart {

    /**
     * used to save the part.
     */
    ItemStack getItemStack();

    /**
     * used to collide, and pick the part
     *
     * @param ch         collision helper
     * @param itemEntity collision with an item entity?
     */
    void getBoxes(IPartCollisionHelper ch, boolean itemEntity);

    /**
     * @return The side the facade is attached to.
     */
    @Nonnull
    Direction getSide();

    Item getItem();

    boolean notAEFacade();

    /**
     * The item that this facade masquerades as.
     */
    ItemStack getTextureItem();

    /**
     * @return The block state used for rendering.
     */
    BlockState getBlockState();

}
