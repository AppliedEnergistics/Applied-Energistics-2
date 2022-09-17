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

package appeng.core.features.registries.grinder;


import appeng.api.features.IGrinderRecipe;
import net.minecraft.item.ItemStack;

import java.util.Optional;


public class AppEngGrinderRecipe implements IGrinderRecipe {

    private final ItemStack in;
    private final ItemStack out;

    private final float optionalChance;
    private final Optional<ItemStack> optionalOutput;

    private final float optionalChance2;
    private final Optional<ItemStack> optionalOutput2;

    private final int turns;

    AppEngGrinderRecipe(final ItemStack input, final ItemStack output, final int cost) {
        this(input, output, null, null, 0, 0, cost);
    }

    AppEngGrinderRecipe(final ItemStack input, final ItemStack output, final ItemStack optional, final float chance, final int cost) {
        this(input, output, optional, null, chance, 0, cost);
    }

    AppEngGrinderRecipe(final ItemStack input, final ItemStack output, final ItemStack optional1, final ItemStack optional2, final float chance1, final float chance2, final int cost) {
        this.in = input;
        this.out = output;

        this.optionalOutput = Optional.ofNullable(optional1);
        this.optionalChance = chance1;

        this.optionalOutput2 = Optional.ofNullable(optional2);
        this.optionalChance2 = chance2;

        this.turns = cost;
    }

    @Override
    public ItemStack getInput() {
        return this.in;
    }

    @Override
    public ItemStack getOutput() {
        return this.out;
    }

    @Override
    public Optional<ItemStack> getOptionalOutput() {
        return this.optionalOutput;
    }

    @Override
    public Optional<ItemStack> getSecondOptionalOutput() {
        return this.optionalOutput2;
    }

    @Override
    public float getOptionalChance() {
        return this.optionalChance;
    }

    @Override
    public float getSecondOptionalChance() {
        return this.optionalChance2;
    }

    @Override
    public int getRequiredTurns() {
        return this.turns;
    }
}
