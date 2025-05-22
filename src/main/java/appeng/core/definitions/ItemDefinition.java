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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.helpers.ItemComparisonHelper;

public class ItemDefinition<T extends Item> implements ItemLike {
    private final ResourceLocation id;
    private final String englishName;
    private final T item;

    public ItemDefinition(String englishName, ResourceLocation id, T item) {
        Objects.requireNonNull(id, "id");
        this.id = id;
        this.englishName = englishName;
        this.item = item;
    }

    public String getEnglishName() {
        return englishName;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public ItemStack stack() {
        return stack(1);
    }

    public ItemStack stack(int stackSize) {
        return new ItemStack(item, stackSize);
    }

    public GenericStack genericStack(long stackSize) {
        return new GenericStack(AEItemKey.of(item), stackSize);
    }

    /**
     * Compare {@link ItemStack} with this
     *
     * @param comparableStack compared item
     *
     * @return true if the item stack is a matching item.
     */
    public final boolean isSameAs(ItemStack comparableStack) {
        return ItemComparisonHelper.isEqualItemType(comparableStack, this.stack());
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
