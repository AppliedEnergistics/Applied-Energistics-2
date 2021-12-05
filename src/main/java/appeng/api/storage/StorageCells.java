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

package appeng.api.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.storage.cells.ICellGuiHandler;
import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;

/**
 * Storage Cell Registry, used for specially implemented cells, if you just want to make a item act like a cell, or new
 * cell with different bytes, you should probably consider implementing {@link IBasicCellItem} on your item instead.
 */
@ThreadSafe
public final class StorageCells {

    private static final List<ICellHandler> handlers = new ArrayList<>();
    private static final List<ICellGuiHandler> guiHandlers = new ArrayList<>();

    private StorageCells() {
    }

    /**
     * Register a new handler.
     * <p>
     * Never be call before {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent} was handled by AE2. Will
     * throw an exception otherwise.
     *
     * @param handler cell handler
     */
    public static synchronized void addCellHandler(@Nonnull ICellHandler handler) {
        Objects.requireNonNull(handler, "Called before FMLCommonSetupEvent.");
        Preconditions.checkArgument(!handlers.contains(handler),
                "Tried to register the same handler instance twice.");

        handlers.add(handler);
    }

    /**
     * Register a new handler
     *
     * @param handler cell gui handler
     */
    public static synchronized void addCellGuiHandler(@Nonnull ICellGuiHandler handler) {
        guiHandlers.add(handler);
    }

    /**
     * return true, if you can get a InventoryHandler for the item passed.
     *
     * @param is to be checked item
     * @return true if the provided item, can be handled by a handler in AE, ( AE May choose to skip this and just get
     *         the handler instead. )
     */
    public static synchronized boolean isCellHandled(ItemStack is) {
        if (is.isEmpty()) {
            return false;
        }
        for (final ICellHandler ch : handlers) {
            if (ch.isCell(is)) {
                return true;
            }
        }
        return false;
    }

    /**
     * get the handler, for the requested item.
     *
     * @param is to be checked item
     * @return the handler registered for this item type.
     */
    @Nullable
    public static synchronized ICellHandler getHandler(ItemStack is) {
        if (is.isEmpty()) {
            return null;
        }
        for (final ICellHandler ch : handlers) {
            if (ch.isCell(is)) {
                return ch;
            }
        }
        return null;
    }

    /**
     * get the handler, for the requested channel.
     *
     * @param is ItemStack
     * @return the handler registered for this channel.
     */
    @Nullable
    public static synchronized ICellGuiHandler getGuiHandler(ItemStack is) {
        ICellGuiHandler fallBack = null;

        for (final ICellGuiHandler ch : guiHandlers) {
            if (ch.isSpecializedFor(is)) {
                return ch;
            }

            if (fallBack == null) {
                fallBack = ch;
            }
        }
        return fallBack;
    }

    /**
     * Returns the inventory for the provided storage cell item by querying all registered handlers.
     *
     * @param is   item with inventory handler
     * @param host can be null. If provided, the host is responsible for persisting the cell content.
     * @return The cell inventory, or null if there isn't one.
     */
    @Nullable
    public static synchronized StorageCell getCellInventory(ItemStack is, @Nullable ISaveProvider host) {
        if (is.isEmpty()) {
            return null;
        }
        for (var ch : handlers) {
            var inventory = ch.getCellInventory(is, host);
            if (inventory != null) {
                return inventory;
            }
        }
        return null;
    }

}
