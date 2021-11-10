/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 TeamAppliedEnergistics
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

package appeng.api.features;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * A registry for items that can be linked to a specific network using for example the security station's user
 * interface.
 * <p/>
 * This can be used by items like wireless terminals to encode the network security key in their NBT. This security key
 * can then be used to locate the grid for that security key later, when the item wants to interact with the grid.
 */
@ThreadSafe
public final class GridLinkables {

    private static final Map<Item, IGridLinkableHandler> registry = new IdentityHashMap<>();

    private GridLinkables() {
    }

    /**
     * Register a handler to link or unlink stacks of a given item with a network.
     *
     * @param itemLike The type of item to register a handler for.
     * @param handler  The handler that handles linking and unlinking for the item stacks.
     */
    public synchronized static void register(ItemLike itemLike, IGridLinkableHandler handler) {
        Objects.requireNonNull(itemLike, "itemLike");
        Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");
        Objects.requireNonNull(handler, "handler");
        var item = itemLike.asItem();
        Preconditions.checkState(!registry.containsKey(item), "Handler for %s already registered", item);
        registry.put(item, handler);
    }

    /**
     * Gets the registered handler for a given item.
     */
    @Nullable
    public static synchronized IGridLinkableHandler get(ItemLike itemLike) {
        Objects.requireNonNull(itemLike, "itemLike");
        Objects.requireNonNull(itemLike.asItem(), "itemLike.asItem()");
        return registry.get(itemLike.asItem());
    }

}
