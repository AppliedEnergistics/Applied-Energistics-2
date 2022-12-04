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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class EntropyRecipeBuilder {
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
    private List<ItemStack> drops = Collections.emptyList();

    public static EntropyRecipeBuilder cool(ResourceLocation id) {
        return new EntropyRecipeBuilder().setId(id).setMode(EntropyMode.COOL);
    }

    public static EntropyRecipeBuilder heat(ResourceLocation id) {
        return new EntropyRecipeBuilder().setId(id).setMode(EntropyMode.HEAT);
    }

    public EntropyRecipeBuilder setId(ResourceLocation id) {
        Preconditions.checkArgument(id != null);
        this.id = id;
        return this;
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

    public EntropyRecipeBuilder addBlockStateMatcher(StateMatcher matcher) {
        Preconditions.checkState(this.inputBlock != null,
                "Can only add appliers when an input block is present.");

        if (this.inputBlockMatchers.isEmpty()) {
            this.inputBlockMatchers = new ArrayList<>();
        }

        this.inputBlockMatchers.add(matcher);

        return this;
    }

    public EntropyRecipeBuilder addFluidStateMatcher(StateMatcher matcher) {
        Preconditions.checkState(this.inputFluid != null,
                "Can only add appliers when an input fluid is present.");

        if (this.inputFluidMatchers.isEmpty()) {
            this.inputFluidMatchers = new ArrayList<>();
        }

        this.inputFluidMatchers.add(matcher);

        return this;
    }

    public EntropyRecipeBuilder addBlockStateAppliers(StateApplier<?> applier) {
        Preconditions.checkState(this.outputBlock != null,
                "Can only add appliers when an output block is present.");

        if (this.outputBlockStateAppliers.isEmpty()) {
            this.outputBlockStateAppliers = new ArrayList<>();
        }

        this.outputBlockStateAppliers.add(applier);

        return this;
    }

    public EntropyRecipeBuilder addFluidStateAppliers(StateApplier<?> applier) {
        Preconditions.checkState(this.outputFluid != null,
                "Can only add appliers when an output fluid is present.");

        if (this.outputFluidStateAppliers.isEmpty()) {
            this.outputFluidStateAppliers = new ArrayList<>();
        }

        this.outputFluidStateAppliers.add(applier);

        return this;
    }

    public EntropyRecipe build() {
        Preconditions.checkState(id != null);
        Preconditions.checkState(mode != null);
        Preconditions.checkState(inputBlock != null || inputFluid != null,
                "Either inputBlock or inputFluid needs to be not null");

        return new EntropyRecipe(id, mode, inputBlock, inputBlockMatchers, inputFluid, inputFluidMatchers, outputBlock,
                outputBlockStateAppliers, outputBlockKeep, outputFluid, outputFluidStateAppliers, outputFluidKeep,
                drops);
    }

    public void save(Consumer<FinishedRecipe> consumer) {
        consumer.accept(new Result());
    }

    private class Result implements FinishedRecipe {
        @Override
        public void serializeRecipeData(JsonObject json) {
            EntropyRecipeSerializer.INSTANCE.toJson(build(), json);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return EntropyRecipeSerializer.INSTANCE;
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return null;
        }
    }

}
