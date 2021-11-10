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

import net.minecraft.world.item.ItemStack;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.util.ConfigInventory;

public final class CellConfig {
    private CellConfig() {
    }

    public static <T extends AEKey> ConfigInventory<T> create(IStorageChannel<T> channel, ItemStack is) {
        var holder = new Holder<T>(is);
        holder.inv = ConfigInventory.configTypes(channel, 63, holder::save);
        holder.load();
        return holder.inv;
    }

    private static class Holder<T extends AEKey> {
        private final ItemStack stack;
        private ConfigInventory<T> inv;

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
