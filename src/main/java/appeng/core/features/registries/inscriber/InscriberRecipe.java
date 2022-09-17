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

package appeng.core.features.registries.inscriber;


import appeng.api.features.IInscriberRecipe;
import appeng.api.features.InscriberProcessType;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


/**
 * Basic inscriber recipe
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public class InscriberRecipe implements IInscriberRecipe {
    @Nonnull
    private final List<ItemStack> inputs;

    @Nonnull
    private final ItemStack output;

    @Nonnull
    private final Optional<ItemStack> maybeTop;

    @Nonnull
    private final Optional<ItemStack> maybeBot;

    @Nonnull
    private final InscriberProcessType type;

    InscriberRecipe(@Nonnull final Collection<ItemStack> inputs, @Nonnull final ItemStack output, @Nullable final ItemStack top, @Nullable final ItemStack bot, @Nonnull final InscriberProcessType type) {
        this.inputs = new ArrayList<>(inputs.size());
        this.inputs.addAll(inputs);

        this.output = output;
        this.maybeTop = Optional.ofNullable(top);
        this.maybeBot = Optional.ofNullable(bot);

        this.type = type;
    }

    @Nonnull
    @Override
    public final List<ItemStack> getInputs() {
        return this.inputs;
    }

    @Nonnull
    @Override
    public final ItemStack getOutput() {
        return this.output;
    }

    @Nonnull
    @Override
    public final Optional<ItemStack> getTopOptional() {
        return this.maybeTop;
    }

    @Nonnull
    @Override
    public final Optional<ItemStack> getBottomOptional() {
        return this.maybeBot;
    }

    @Nonnull
    @Override
    public final InscriberProcessType getProcessType() {
        return this.type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IInscriberRecipe)) {
            return false;
        }

        final IInscriberRecipe that = (IInscriberRecipe) o;

        if (!this.inputs.equals(that.getInputs())) {
            return false;
        }
        if (!this.output.equals(that.getOutput())) {
            return false;
        }
        if (!this.maybeTop.equals(that.getTopOptional())) {
            return false;
        }
        if (!this.maybeBot.equals(that.getBottomOptional())) {
            return false;
        }
        return this.type == that.getProcessType();
    }

    @Override
    public int hashCode() {
        int result = this.inputs.hashCode();
        result = 31 * result + this.output.hashCode();
        result = 31 * result + this.maybeTop.hashCode();
        result = 31 * result + this.maybeBot.hashCode();
        result = 31 * result + this.type.hashCode();
        return result;
    }
}
