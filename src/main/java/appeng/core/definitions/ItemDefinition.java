/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core.definitions;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.storage.data.AEItemKey;
import appeng.api.storage.data.AEKey;
import appeng.util.Platform;

public class ItemDefinition<T extends Item> implements ItemLike {
    private final ResourceLocation id;
    private final T item;

    public ItemDefinition(ResourceLocation id, T item) {
        Objects.requireNonNull(id, "id");
        this.id = id;
        this.item = item;
    }

    @Nonnull
    public ResourceLocation id() {
        return this.id;
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(final int stackSize) {
        return new ItemStack(item, stackSize);
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     *
     * @return true if the item stack is a matching item.
     */
    public final boolean isSameAs(final ItemStack comparableStack) {
        return Platform.itemComparisons().isEqualItemType(comparableStack, this.stack());
    }

    /**
     * @return True if this item is represented by the given key.
     */
    public final boolean isSameAs(AEKey key) {
        if (key instanceof AEItemKey itemKey) {
            return item == itemKey.getItem();
        }
        return false;
    }

    @Override
    public T asItem() {
        return item;
    }
}
