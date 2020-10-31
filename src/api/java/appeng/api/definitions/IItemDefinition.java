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

package appeng.api.definitions;

import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;

import appeng.api.features.AEFeature;

public interface IItemDefinition extends IComparableDefinition, IItemProvider {
    /**
     * @return the unique name of the definition which will be used to register the underlying structure. Will never be
     *         null
     */
    @Nonnull
    String identifier();

    /**
     * @return the {@link Item} Implementation if applicable
     */
    default Optional<Item> maybeItem() {
        return Optional.of(item());
    }

    Item item();

    /**
     * @return an {@link ItemStack} with specified quantity of this item.
     */
    default Optional<ItemStack> maybeStack(int stackSize) {
        return Optional.of(stack(stackSize));
    }

    ItemStack stack(int stackSize);

    /**
     * @return an immutable set of the features of this item
     */
    Set<AEFeature> features();

    @Override
    default Item asItem() {
        return item();
    }

}
