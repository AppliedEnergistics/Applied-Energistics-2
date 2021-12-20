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

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import appeng.api.util.AEColor;

public final class ColoredItemDefinition<T extends Item> {

    private final Map<AEColor, ItemDefinition<T>> items = new EnumMap<>(AEColor.class);
    private final Map<AEColor, ResourceLocation> ids = new EnumMap<>(AEColor.class);

    void add(AEColor v, ResourceLocation id, ItemDefinition<T> is) {
        this.ids.put(v, id);
        this.items.put(v, is);
    }

    public ResourceLocation id(AEColor color) {
        return ids.get(color);
    }

    public T item(final AEColor color) {
        return this.items.get(color).asItem();
    }

    public ItemStack stack(final AEColor color) {
        return stack(color, 1);
    }

    public ItemStack stack(AEColor color, final int stackSize) {
        var item = item(color);

        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, stackSize);
    }

}
