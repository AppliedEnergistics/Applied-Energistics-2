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

package appeng.core.features;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import appeng.api.definitions.IItemDefinition;
import appeng.util.Platform;

public class ItemDefinition implements IItemDefinition {
    private final ResourceLocation id;
    private final Item item;

    public ItemDefinition(ResourceLocation id, Item item) {
        Preconditions.checkNotNull(id, "id");
        this.id = id;
        this.item = item;
    }

    @Nonnull
    @Override
    public ResourceLocation id() {
        return this.id;
    }

    @Override
    public final Item item() {
        return this.item;
    }

    @Override
    public ItemStack stack(final int stackSize) {
        return new ItemStack(item, stackSize);
    }

    @Override
    public final boolean isSameAs(final ItemStack comparableStack) {
        return Platform.itemComparisons().isEqualItemType(comparableStack, this.stack(1));
    }

}
