/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.helpers.iface;

import javax.annotation.Nullable;

import net.minecraft.nbt.ListTag;

import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;

public class GenericStackInv {
    protected final IAEStack[] stacks;
    private final Listener listener;

    public GenericStackInv(@Nullable Listener listener, int size) {
        this.stacks = new IAEStack[size];
        this.listener = listener;
    }

    public int size() {
        return stacks.length;
    }

    public boolean isEmpty() {
        for (var stack : stacks) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public IAEStack getStack(int slot) {
        return stacks[slot];
    }

    public void setStack(int slot, @Nullable IAEStack stack) {
        stacks[slot] = stack;
        onChange();
    }

    protected void onChange() {
        if (listener != null) {
            listener.onChange();
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (var stack : stacks) {
            tag.add(GenericStackHelper.writeGenericStack(stack));
        }

        return tag;
    }

    public void readFromNBT(ListTag tag) {
        for (int i = 0; i < tag.size(); ++i) {
            stacks[i] = GenericStackHelper.readGenericStack(tag.getCompound(i));
        }
    }

    public interface Listener {
        void onChange();
    }
}
