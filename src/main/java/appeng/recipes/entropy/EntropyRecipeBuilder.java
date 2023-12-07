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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class EntropyRecipeBuilder {
    private EntropyMode mode;
    private Block inputBlock;
    private final Map<String, PropertyValueMatcher> inputBlockMatchers = new HashMap<>();

    private Fluid inputFluid;
    private final Map<String, PropertyValueMatcher> inputFluidMatchers = new HashMap<>();

    private Block outputBlock;
    private final Map<String, String> outputBlockStateAppliers = new HashMap<>();
    private boolean outputBlockKeep;
    private Fluid outputFluid;
    private final Map<String, String> outputFluidStateAppliers = new HashMap<>();
    private boolean outputFluidKeep;
    private List<ItemStack> drops = Collections.emptyList();

    public static EntropyRecipeBuilder cool() {
        return new EntropyRecipeBuilder().setMode(EntropyMode.COOL);
    }

    public static EntropyRecipeBuilder heat() {
        return new EntropyRecipeBuilder().setMode(EntropyMode.HEAT);
    }

    public EntropyRecipeBuilder setMode(EntropyMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        return this;
    }

    public EntropyRecipeBuilder setInputBlock(Block inputBlock) {
        this.inputBlock = Objects.requireNonNull(inputBlock, "inputBlock must not be null");
        return this;
    }

    public EntropyRecipeBuilder setInputFluid(Fluid inputFluid) {
        this.inputFluid = Objects.requireNonNull(inputFluid, "inputFluid must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputBlock(Block outputBlock) {
        this.outputBlock = Objects.requireNonNull(outputBlock, "outputBlock must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputBlockKeep(boolean outputBlockKeep) {
        this.outputBlockKeep = outputBlockKeep;
        return this;
    }

    public EntropyRecipeBuilder setOutputFluid(Fluid outputFluid) {
        this.outputFluid = Objects.requireNonNull(outputFluid, "outputFluid must not be null");
        return this;
    }

    public EntropyRecipeBuilder setOutputFluidKeep(boolean outputFluidKeep) {
        this.outputFluidKeep = outputFluidKeep;
        return this;
    }

    public EntropyRecipeBuilder setDrops(List<ItemStack> drops) {
        Preconditions.checkArgument(!drops.isEmpty(), "drops needs to be a non empty list when set");

        this.drops = drops;
        return this;
    }

    public EntropyRecipeBuilder setDrops(ItemStack... drops) {
        return setDrops(Arrays.asList(drops));
    }

    public EntropyRecipeBuilder addBlockStateMatcher(String property, PropertyValueMatcher valueMatcher) {
        Preconditions.checkState(this.inputBlock != null,
                "Can only add appliers when an input block is present.");

        this.inputBlockMatchers.put(property, valueMatcher);

        return this;
    }

    public EntropyRecipeBuilder setBlockStateMatchers(Map<String, PropertyValueMatcher> properties) {
        Preconditions.checkState(this.inputBlock != null,
                "Can only add appliers when an input block is present.");

        this.inputBlockMatchers.clear();
        this.inputBlockMatchers.putAll(properties);

        return this;
    }

    public EntropyRecipeBuilder addFluidStateMatcher(String property, PropertyValueMatcher valueMatcher) {
        Preconditions.checkState(this.inputFluid != null,
                "Can only add appliers when an input fluid is present.");

        this.inputFluidMatchers.put(property, valueMatcher);

        return this;
    }

    public EntropyRecipeBuilder setFluidStateMatchers(Map<String, PropertyValueMatcher> properties) {
        Preconditions.checkState(this.inputFluid != null,
                "Can only add appliers when an input fluid is present.");

        this.inputFluidMatchers.clear();
        this.inputFluidMatchers.putAll(properties);

        return this;
    }

    public EntropyRecipeBuilder addBlockStateAppliers(String property, String value) {
        Preconditions.checkState(this.outputBlock != null,
                "Can only add appliers when an output block is present.");

        this.outputBlockStateAppliers.put(property, value);

        return this;
    }

    public EntropyRecipeBuilder setBlockStateAppliers(Map<String, String> properties) {
        Preconditions.checkState(this.outputBlock != null,
                "Can only add appliers when an output block is present.");

        this.outputBlockStateAppliers.clear();
        this.outputBlockStateAppliers.putAll(properties);

        return this;
    }

    public EntropyRecipeBuilder addFluidStateAppliers(String property, String value) {
        Preconditions.checkState(this.outputFluid != null,
                "Can only add appliers when an output fluid is present.");

        this.outputFluidStateAppliers.put(property, value);

        return this;
    }

    public EntropyRecipeBuilder setFluidStateAppliers(Map<String, String> properties) {
        Preconditions.checkState(this.outputFluid != null,
                "Can only add appliers when an output fluid is present.");

        this.outputFluidStateAppliers.clear();
        this.outputFluidStateAppliers.putAll(properties);

        return this;
    }

    public EntropyRecipe build() {
        Preconditions.checkState(mode != null);
        Preconditions.checkState(inputBlock != null || inputFluid != null,
                "Either inputBlock or inputFluid needs to be not null");

        return new EntropyRecipe(mode, Optional.ofNullable(inputBlock), inputBlockMatchers, Optional.ofNullable(inputFluid), inputFluidMatchers, Optional.ofNullable(outputBlock),
                outputBlockStateAppliers, outputBlockKeep, Optional.ofNullable(outputFluid), outputFluidStateAppliers, outputFluidKeep,
                drops);
    }

    public void save(RecipeOutput consumer, ResourceLocation id) {
        consumer.accept(id, build(), null);
    }

}
