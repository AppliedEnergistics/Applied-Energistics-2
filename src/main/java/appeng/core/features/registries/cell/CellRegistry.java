/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.core.features.registries.cell;


import appeng.api.storage.*;
import appeng.api.storage.data.IAEStack;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class CellRegistry implements ICellRegistry {

    private final List<ICellHandler> handlers;
    private final List<ICellGuiHandler> guiHandlers;

    public CellRegistry() {
        this.handlers = new ArrayList<>();
        this.guiHandlers = new ArrayList<>();
    }

    @Override
    public void addCellHandler(final ICellHandler handler) {
        Preconditions.checkNotNull(handler, "Called before FMLInitializationEvent.");
        Preconditions.checkArgument(!this.handlers.contains(handler), "Tried to register the same handler instance twice.");

        this.handlers.add(handler);

        // Verify that the first entry is always our own handler.
        Verify.verify(this.handlers.get(0) instanceof BasicCellHandler);
    }

    @Override
    public boolean isCellHandled(final ItemStack is) {
        if (is.isEmpty()) {
            return false;
        }
        for (final ICellHandler ch : this.handlers) {
            if (ch.isCell(is)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ICellHandler getHandler(final ItemStack is) {
        if (is.isEmpty()) {
            return null;
        }
        for (final ICellHandler ch : this.handlers) {
            if (ch.isCell(is)) {
                return ch;
            }
        }
        return null;
    }

    @Override
    public <T extends IAEStack<T>> ICellInventoryHandler<T> getCellInventory(final ItemStack is, final ISaveProvider container, final IStorageChannel<T> chan) {
        if (is.isEmpty()) {
            return null;
        }
        for (final ICellHandler ch : this.handlers) {
            if (ch.isCell(is)) {
                return ch.getCellInventory(is, container, chan);
            }
        }
        return null;
    }

    @Override
    public void addCellGuiHandler(ICellGuiHandler handler) {
        this.guiHandlers.add(handler);
    }

    @Override
    public <T extends IAEStack<T>> ICellGuiHandler getGuiHandler(final IStorageChannel<T> channel, final ItemStack is) {
        ICellGuiHandler fallBack = null;

        for (final ICellGuiHandler ch : this.guiHandlers) {
            if (ch.isHandlerFor(channel)) {
                if (ch.isSpecializedFor(is)) {
                    return ch;
                }

                if (fallBack == null) {
                    fallBack = ch;
                }
            }
        }
        return fallBack;
    }
}
