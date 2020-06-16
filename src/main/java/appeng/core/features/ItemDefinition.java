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

import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;
import appeng.api.features.AEFeature;
import appeng.util.Platform;

public class ItemDefinition implements IItemDefinition {
    private final String identifier;
    private final Item item;
    private final Set<AEFeature> features;

    public ItemDefinition(String registryName, Item item, Set<AEFeature> features) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(registryName), "registryName");
        this.identifier = registryName;
        this.item = item;
        this.features = ImmutableSet.copyOf(features);
    }

    @Nonnull
    @Override
    public String identifier() {
        return this.identifier;
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
    public Set<AEFeature> features() {
        return features;
    }

    @Override
    public final boolean isSameAs(final ItemStack comparableStack) {
        return Platform.itemComparisons().isEqualItemType(comparableStack, this.stack(1));
    }

}
