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

package appeng.crafting.inv;

import java.util.Collection;

import javax.annotation.Nullable;

import net.minecraft.nbt.ListTag;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedStackList;
import appeng.crafting.execution.GenericStackHelper;

public class ListCraftingInventory implements ICraftingInventory {
    public final MixedStackList list = new MixedStackList();

    public void postChange(IAEStack template, long delta) {
    }

    @Override
    public void injectItems(IAEStack input, Actionable mode) {
        if (mode == Actionable.MODULATE && input != null) {
            list.addStorage(input);
            postChange(input, -input.getStackSize());
        }
    }

    @Nullable
    @Override
    public IAEStack extractItems(IAEStack input, Actionable mode) {
        IAEStack precise = list.findPrecise(input);
        if (precise == null)
            return null;
        long extracted = Math.min(precise.getStackSize(), input.getStackSize());
        if (mode == Actionable.MODULATE) {
            precise.decStackSize(extracted);
            postChange(input, extracted);
        }
        return IAEStack.copy(input, extracted);
    }

    @Override
    public Collection<IAEStack> findFuzzyTemplates(IAEStack input) {
        return list.findFuzzy(input, FuzzyMode.IGNORE_ALL);
    }

    public void clear() {
        for (IAEStack stack : list) {
            postChange(stack, stack.getStackSize());
        }
        list.resetStatus();
    }

    public void readFromNBT(ListTag data) {
        list.resetStatus();

        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                injectItems(GenericStackHelper.readGenericStack(data.getCompound(i)), Actionable.MODULATE);
            }
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (IAEStack stack : list) {
            tag.add(GenericStackHelper.writeGenericStack(stack));
        }

        return tag;
    }
}
