/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.recipes.entropy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

class EntropyRecipeBuilder {
    private ResourceLocation id;
    private EntropyMode mode;

    private Block inputBlock;
    private List<StateMatcher> inputBlockMatchers = Collections.emptyList();

    private Fluid inputFluid;
    private List<StateMatcher> inputFluidMatchers = Collections.emptyList();

    private Block outputBlock;
    private List<StateApplier<?>> outputBlockStateAppliers = Collections.emptyList();
    private boolean outputBlockKeep;
    private Fluid outputFluid;
    private List<StateApplier<?>> outputFluidStateAppliers = Collections.emptyList();
    private boolean outputFluidKeep;
    private List<ItemStack> drops;

    void setId(ResourceLocation id) {
        Preconditions.checkArgument(id != null);
        this.id = id;
    }

    void setMode(EntropyMode mode) {
        Preconditions.checkArgument(mode != null);
        this.mode = mode;
    }

    void setInputBlock(Block inputBlock) {
        Preconditions.checkArgument(inputBlock != null);
        this.inputBlock = inputBlock;
    }

    void setInputFluid(Fluid inputFluid) {
        Preconditions.checkArgument(inputFluid != null);
        this.inputFluid = inputFluid;
    }

    void setOutputBlock(Block outputBlock) {
        Preconditions.checkArgument(outputBlock != null);
        this.outputBlock = outputBlock;
    }

    void setOutputBlockKeep(boolean outputBlockKeep) {
        this.outputBlockKeep = outputBlockKeep;
    }

    void setOutputFluid(Fluid outputFluid) {
        Preconditions.checkArgument(outputFluid != null);
        this.outputFluid = outputFluid;
    }

    void setOutputFluidKeep(boolean outputFluidKeep) {
        this.outputFluidKeep = outputFluidKeep;
    }

    void setDrops(List<ItemStack> drops) {
        Preconditions.checkArgument(drops == null || !drops.isEmpty(),
                "drops needs to be either null or a non empty list");

        this.drops = drops;
    }

    void addBlockStateMatcher(StateMatcher matcher) {
        Preconditions.checkArgument(this.inputBlock != null,
                "Can only add appliers when an input block is present.");

        if (this.inputBlockMatchers.isEmpty()) {
            this.inputBlockMatchers = new ArrayList<>();
        }

        this.inputBlockMatchers.add(matcher);

    }

    void addFluidStateMatcher(StateMatcher matcher) {
        Preconditions.checkArgument(this.inputFluid != null,
                "Can only add appliers when an input fluid is present.");

        if (this.inputFluidMatchers.isEmpty()) {
            this.inputFluidMatchers = new ArrayList<>();
        }

        this.inputFluidMatchers.add(matcher);

    }

    void addBlockStateAppliers(StateApplier<?> applier) {
        Preconditions.checkArgument(this.outputBlock != null,
                "Can only add appliers when an output block is present.");

        if (this.outputBlockStateAppliers.isEmpty()) {
            this.outputBlockStateAppliers = new ArrayList<>();
        }

        this.outputBlockStateAppliers.add(applier);

    }

    void addFluidStateAppliers(StateApplier<?> applier) {
        Preconditions.checkArgument(this.outputFluid != null,
                "Can only add appliers when an output fluid is present.");

        if (this.outputFluidStateAppliers.isEmpty()) {
            this.outputFluidStateAppliers = new ArrayList<>();
        }

        this.outputFluidStateAppliers.add(applier);

    }

    EntropyRecipe build() {
        Preconditions.checkState(id != null);
        Preconditions.checkState(mode != null);
        Preconditions.checkState(drops == null || !drops.isEmpty(),
                "drops needs to be either null or a non empty list");

        return new EntropyRecipe(id, mode, inputBlock, inputBlockMatchers, inputFluid, inputFluidMatchers, outputBlock,
                outputBlockStateAppliers, outputBlockKeep, outputFluid, outputFluidStateAppliers, outputFluidKeep,
                drops);
    }

}
