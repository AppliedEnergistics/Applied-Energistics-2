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


import appeng.api.definitions.IItemDefinition;
import appeng.util.Platform;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;


public class ItemDefinition implements IItemDefinition {
    private final String identifier;
    private final Optional<Item> item;

    public ItemDefinition(String registryName, Item item) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(registryName), "registryName");
        this.identifier = registryName;
        this.item = Optional.ofNullable(item);
    }

    @Nonnull
    @Override
    public String identifier() {
        return this.identifier;
    }

    @Override
    public final Optional<Item> maybeItem() {
        return this.item;
    }

    @Override
    public Optional<ItemStack> maybeStack(final int stackSize) {
        return this.item.map(item -> new ItemStack(item, stackSize));
    }

    @Override
    public boolean isEnabled() {
        return this.item.isPresent();
    }

    @Override
    public final boolean isSameAs(final ItemStack comparableStack) {
        return this.isEnabled() && Platform.itemComparisons().isEqualItemType(comparableStack, this.maybeStack(1).get());
    }

}
