/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 AlgorithmX2
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

package appeng.util.item;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public final class AEItemStackRegistry {
    private static final WeakHashMap<AESharedItemStack, WeakReference<AESharedItemStack>> REGISTRY = new WeakHashMap<>();

    private AEItemStackRegistry() {
    }

    static synchronized AESharedItemStack getRegisteredStack(final @Nonnull ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            throw new IllegalArgumentException("stack cannot be empty");
        }

        int oldStackSize = itemStack.getCount();
        itemStack.setCount(1);

        AESharedItemStack search = new AESharedItemStack(itemStack);
        WeakReference<AESharedItemStack> weak = REGISTRY.get(search);
        AESharedItemStack ret = null;

        if (weak != null) {
            ret = weak.get();
        }

        if (ret == null) {
            ret = new AESharedItemStack(itemStack.copy());
            REGISTRY.put(ret, new WeakReference<>(ret));
        }
        itemStack.setCount(oldStackSize);

        return ret;
    }
}
