/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.helpers;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.StorageChannels;
import appeng.items.contents.CellConfig;

/**
 * Implements a cell config inventory that uses the item-stack serialization provided by a storage channel to save a
 * filter.
 */
public class FluidCellConfig extends CellConfig {
    public FluidCellConfig(ItemStack is) {
        super(is);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        var configuredStack = StorageChannels.fluids().createStack(stack);
        if (configuredStack == null) {
            // Was unable to convert the given item stack to the target storage channel
            return stack;
        }

        return super.insertItem(slot, configuredStack.asItemStackRepresentation(), simulate);
    }

    @Override
    public void setItemDirect(int slot, @Nonnull ItemStack stack) {
        if (!stack.isEmpty()) {
            var configuredStack = StorageChannels.fluids().createStack(stack);
            if (configuredStack == null) {
                // Was unable to convert the given item stack to the target storage channel
                return;
            }
            stack = configuredStack.asItemStackRepresentation();
        }

        super.setItemDirect(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            var configuredStack = StorageChannels.fluids().createStack(stack);
            if (configuredStack == null) {
                // Was unable to convert the given item stack to the target storage channel
                return false;
            }
            stack = configuredStack.asItemStackRepresentation();
        }
        return super.isItemValid(slot, stack);
    }

}
