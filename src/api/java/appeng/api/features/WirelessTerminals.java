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

package appeng.api.features;

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Allows mods to register their items as wireless terminals, which can serve as hosts for a terminal screen. A
 * {@link IWirelessTerminalHandler} needs to be provided for each item to allow the terminal screen to interact with the
 * item.
 */
@ThreadSafe
public final class WirelessTerminals {

    private static final Map<Item, IWirelessTerminalHandler> registry = new IdentityHashMap<>();

    volatile static Opener opener;

    /**
     * Registers a wireless terminal handler for a given item.
     */
    public synchronized static void register(ItemLike itemLike, IWirelessTerminalHandler handler) {
        Preconditions.checkNotNull(itemLike, "itemLike");
        Preconditions.checkNotNull(itemLike.asItem(), "itemLike.asItem()");
        Preconditions.checkNotNull(handler, "handler");
        var item = itemLike.asItem();
        Preconditions.checkState(!registry.containsKey(item), "Handler for item %s is already registered.", item);
        registry.put(item, handler);
    }

    /**
     * Gets the wireless terminal handler for a given item or null, if no handler is registered.
     */
    @Nullable
    public synchronized static IWirelessTerminalHandler get(ItemLike itemLike) {
        Preconditions.checkNotNull(itemLike, "itemLike");
        Preconditions.checkNotNull(itemLike.asItem(), "itemLike.asItem()");
        return registry.get(itemLike.asItem());
    }

    /**
     * Opens the wireless terminal screen for a given wireless terminal item that is in the player's hand.
     */
    public static void openTerminal(ItemStack item, Player player, InteractionHand hand) {
        Preconditions.checkState(opener != null, "AE has not finished initialization yet.");
        opener.open(item, player, hand);
    }

    /**
     * Opens the wireless terminal screen for a wireless terminal item that is in a specific inventory slot.
     */
    public static void openTerminal(ItemStack item, Player player, int inventorySlot) {
        Preconditions.checkState(opener != null, "AE has not finished initialization yet.");
        opener.open(item, player, inventorySlot);
    }

    interface Opener {
        void open(ItemStack item, Player player, InteractionHand hand);

        void open(ItemStack item, Player player, int inventorySlot);
    }

}
