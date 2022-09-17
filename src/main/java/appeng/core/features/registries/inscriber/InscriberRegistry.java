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

package appeng.core.features.registries.inscriber;


import appeng.api.features.IInscriberRecipe;
import appeng.api.features.IInscriberRecipeBuilder;
import appeng.api.features.IInscriberRegistry;
import appeng.api.features.InscriberProcessType;
import com.google.common.base.Preconditions;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;


/**
 * @author thatsIch
 * @version rv3
 * @since rv2
 */
public final class InscriberRegistry implements IInscriberRegistry {
    private final Set<IInscriberRecipe> recipes;
    private final Set<ItemStack> optionals;
    private final Set<ItemStack> inputs;

    public InscriberRegistry() {
        this.inputs = new HashSet<>();
        this.optionals = new HashSet<>();
        this.recipes = new HashSet<>();
    }

    @Nonnull
    @Override
    public Collection<IInscriberRecipe> getRecipes() {
        return Collections.unmodifiableCollection(this.recipes);
    }

    @Nonnull
    @Override
    public Set<ItemStack> getOptionals() {
        return this.optionals;
    }

    @Nonnull
    @Override
    public Set<ItemStack> getInputs() {
        return this.inputs;
    }

    @Nonnull
    @Override
    public IInscriberRecipeBuilder builder() {
        return new Builder();
    }

    @Override
    public boolean addRecipe(final IInscriberRecipe recipe) {
        Preconditions.checkNotNull(recipe, "Tried to add (null) as inscriber recipe to the registry.");

        if (this.recipes.add(recipe)) {
            recipe.getTopOptional().ifPresent(this.optionals::add);
            recipe.getBottomOptional().ifPresent(this.optionals::add);

            this.inputs.addAll(recipe.getInputs());

            return true;
        }

        return false;
    }

    @Override
    public boolean removeRecipe(final IInscriberRecipe toBeRemovedRecipe) {
        Preconditions.checkNotNull(toBeRemovedRecipe, "Tried to remove (null) from the registry.");

        boolean changed = false;

        for (final Iterator<IInscriberRecipe> iterator = this.recipes.iterator(); iterator.hasNext(); ) {
            final IInscriberRecipe recipe = iterator.next();
            if (recipe.equals(toBeRemovedRecipe)) {
                changed = true;
                iterator.remove();
            }
        }

        return changed;
    }

    /**
     * Internal {@link IInscriberRecipeBuilder} implementation.
     * Needs to be adapted to represent a correct {@link IInscriberRecipe}
     */
    private static final class Builder implements IInscriberRecipeBuilder {
        private List<ItemStack> inputs;
        private ItemStack output;
        private ItemStack topOptional;
        private ItemStack bottomOptional;
        private InscriberProcessType type;

        @Nonnull
        @Override
        public Builder withInputs(@Nonnull final Collection<ItemStack> inputs) {
            Preconditions.checkNotNull(inputs);
            Preconditions.checkArgument(!inputs.isEmpty());

            this.inputs = new ArrayList<>(inputs.size());
            this.inputs.addAll(inputs);

            return this;
        }

        @Nonnull
        @Override
        public Builder withOutput(@Nonnull final ItemStack output) {
            Preconditions.checkNotNull(output);
            Preconditions.checkArgument(!output.isEmpty());

            this.output = output;

            return this;
        }

        @Nonnull
        @Override
        public Builder withTopOptional(@Nonnull final ItemStack topOptional) {
            Preconditions.checkNotNull(topOptional);
            Preconditions.checkArgument(!topOptional.isEmpty());

            this.topOptional = topOptional;

            return this;
        }

        @Nonnull
        @Override
        public Builder withBottomOptional(@Nonnull final ItemStack bottomOptional) {
            Preconditions.checkNotNull(bottomOptional);
            Preconditions.checkArgument(!bottomOptional.isEmpty());

            this.bottomOptional = bottomOptional;

            return this;
        }

        @Nonnull
        @Override
        public Builder withProcessType(@Nonnull final InscriberProcessType type) {
            Preconditions.checkNotNull(type);

            this.type = type;

            return this;
        }

        @Nonnull
        @Override
        public IInscriberRecipe build() {
            Preconditions.checkState(this.inputs != null, "Input must be defined.");
            Preconditions.checkState(!this.inputs.isEmpty(), "Input must have a size.");
            Preconditions.checkState(!this.output.isEmpty(), "Output cannot be empty.");
            Preconditions.checkState(!this.topOptional.isEmpty() || !this.bottomOptional.isEmpty(), "One optional must be defined.");
            Preconditions.checkState(this.type != null, "Process type must be defined.");

            return new InscriberRecipe(this.inputs, this.output, this.topOptional, this.bottomOptional, this.type);
        }
    }
}
