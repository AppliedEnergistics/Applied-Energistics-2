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

import java.util.Map;

import com.google.common.collect.Iterables;

import net.minecraft.nbt.ListTag;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;

public class ListCraftingInventory implements ICraftingInventory {
    public final KeyCounter list = new KeyCounter();

    private final ChangeListener listener;

    @FunctionalInterface
    public interface ChangeListener {
        void onChange(AEKey key, long delta);
    }

    public ListCraftingInventory(ChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void insert(AEKey what, long amount, Actionable mode) {
        if (mode == Actionable.MODULATE) {
            list.add(what, amount);
            listener.onChange(what, -amount);
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode) {
        var extracted = Math.min(list.get(what), amount);
        if (mode == Actionable.MODULATE) {
            list.remove(what, extracted);
            list.removeZeros();
            listener.onChange(what, extracted);
        }
        return extracted;
    }

    @Override
    public Iterable<AEKey> findFuzzyTemplates(AEKey what) {
        return Iterables.transform(list.findFuzzy(what, FuzzyMode.IGNORE_ALL), Map.Entry::getKey);
    }

    public void clear() {
        for (var stack : list) {
            listener.onChange(stack.getKey(), stack.getLongValue());
        }
        list.clear();
    }

    public void readFromNBT(ListTag data) {
        list.clear();

        if (data != null) {
            for (int i = 0; i < data.size(); ++i) {
                var compound = data.getCompound(i);
                var key = AEKey.fromTagGeneric(compound);
                if (key != null) {
                    var amount = compound.getLong("#");
                    insert(key, amount, Actionable.MODULATE);
                }
            }
        }
    }

    public ListTag writeToNBT() {
        ListTag tag = new ListTag();

        for (var entry : list) {
            var key = entry.getKey();
            var amount = entry.getLongValue();

            var entryTag = key.toTagGeneric();
            entryTag.putLong("#", amount);
            tag.add(entryTag);
        }

        return tag;
    }
}
