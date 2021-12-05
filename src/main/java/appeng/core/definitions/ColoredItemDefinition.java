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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import appeng.api.util.AEColor;

public final class ColoredItemDefinition {

    private final ItemLike[] items = new ItemLike[AEColor.values().length];
    private final ResourceLocation[] ids = new ResourceLocation[AEColor.values().length];

    void add(AEColor v, ResourceLocation id, ItemLike is) {
        this.ids[v.ordinal()] = id;
        this.items[v.ordinal()] = is;
    }

    public ResourceLocation id(final AEColor color) {
        return ids[color.ordinal()];
    }

    public Item item(final AEColor color) {
        final ItemLike is = this.items[color.ordinal()];

        if (is == null) {
            return null;
        }

        return is.asItem();
    }

    public ItemStack stack(final AEColor color) {
        return stack(color, 1);
    }

    public ItemStack stack(final AEColor color, final int stackSize) {
        final ItemLike is = this.items[color.ordinal()];

        if (is == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(is, stackSize);
    }

}
