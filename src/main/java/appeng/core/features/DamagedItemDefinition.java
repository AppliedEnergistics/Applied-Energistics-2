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
import com.google.common.base.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Optional;


public final class DamagedItemDefinition implements IItemDefinition {
    private final String identifier;
    private final Optional<IStackSrc> source;

    public DamagedItemDefinition(@Nonnull final String identifier, @Nonnull final IStackSrc source) {
        this.identifier = Preconditions.checkNotNull(identifier);
        Preconditions.checkNotNull(source);

        if (source.isEnabled()) {
            this.source = Optional.of(source);
        } else {
            this.source = Optional.empty();
        }
    }

    @Nonnull
    @Override
    public String identifier() {
        return this.identifier;
    }

    @Override
    public Optional<Item> maybeItem() {
        return this.source.map(IStackSrc::getItem);
    }

    @Override
    public Optional<ItemStack> maybeStack(final int stackSize) {
        return this.source.map(input -> input.stack(stackSize));
    }

    @Override
    public boolean isEnabled() {
        return this.source.isPresent();
    }

    @Override
    public boolean isSameAs(final ItemStack comparableStack) {
        if (comparableStack.isEmpty()) {
            return false;
        }

        return this.isEnabled() && comparableStack.getItem() == this.source.get().getItem() && comparableStack.getItemDamage() == this.source.get().getDamage();
    }

}
