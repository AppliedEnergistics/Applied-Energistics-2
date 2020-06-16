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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;
import appeng.api.features.AEFeature;

public final class DamagedItemDefinition implements IItemDefinition {
    private final String identifier;
    private final IStackSrc source;

    public DamagedItemDefinition(@Nonnull final String identifier, @Nonnull final IStackSrc source) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.source = Preconditions.checkNotNull(source);
    }

    @Override
    public Item item() {
        return source.getItem();
    }

    @Override
    public ItemStack stack(int stackSize) {
        return source.stack(stackSize);
    }

    @Nonnull
    @Override
    public String identifier() {
        return this.identifier;
    }

    @Override
    public Optional<Item> maybeItem() {
        return Optional.of(this.source.getItem());
    }

    @Override
    public Optional<ItemStack> maybeStack(final int stackSize) {
        return Optional.of(this.source.stack(stackSize));
    }

    @Override
    public Set<AEFeature> features() {
        return Collections.emptySet();
    }

    @Override
    public boolean isSameAs(final ItemStack comparableStack) {
        if (comparableStack.isEmpty()) {
            return false;
        }

        return comparableStack.getItem() == this.source.getItem();
    }

}
