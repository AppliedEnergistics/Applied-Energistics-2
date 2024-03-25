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

package appeng.items.contents;

import java.util.Set;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEKeyType;
import appeng.util.ConfigInventory;

public final class CellConfig {
    private CellConfig() {
    }

    public static ConfigInventory create(Set<AEKeyType> supportedTypes, ItemStack is, int size) {
        Preconditions.checkArgument(size >= 1 && size <= 63,
                "Config inventory must have between 1 and 63 slots inclusive.");
        var holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(size).supportedTypes(supportedTypes).changeListener(holder::save)
                .build();
        holder.load();
        return holder.inv;
    }

    public static ConfigInventory create(Set<AEKeyType> supportedTypes, ItemStack is) {
        var holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(63).supportedTypes(supportedTypes).changeListener(holder::save)
                .build();
        holder.load();
        return holder.inv;
    }

    public static ConfigInventory create(ItemStack is) {
        var holder = new Holder(is);
        holder.inv = ConfigInventory.configTypes(63).changeListener(holder::save).build();
        holder.load();
        return holder.inv;
    }

    private static class Holder {
        private final ItemStack stack;
        private ConfigInventory inv;

        public Holder(ItemStack stack) {
            this.stack = stack;
        }

        public void load() {
            if (stack.hasTag()) {
                inv.readFromChildTag(stack.getOrCreateTag(), "list");
            }
        }

        public void save() {
            inv.writeToChildTag(stack.getOrCreateTag(), "list");
        }
    }
}
