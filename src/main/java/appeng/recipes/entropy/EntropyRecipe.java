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

import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import appeng.core.AppEng;
import appeng.items.tools.powered.EntropyManipulatorItem;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements Recipe<Container> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");

    public static final RecipeType<EntropyRecipe> TYPE = RecipeType.register(TYPE_ID.toString());

    @Nonnull
    private final net.minecraft.resources.ResourceLocation id;
    @Nonnull
    private final EntropyMode mode;

    @Nullable
    private final Block inputBlock;
    @Nonnull
    private final List<StateMatcher> inputBlockMatchers;

    @Nullable
    private final Fluid inputFluid;
    @Nonnull
    private final List<StateMatcher> inputFluidMatchers;

    @Nullable
    private final Block outputBlock;
    @Nonnull
    private final List<StateApplier<?>> outputBlockStateAppliers;
    private final boolean outputBlockKeep;
    @Nullable
    private final net.minecraft.world.level.material.Fluid outputFluid;
    @Nonnull
    private final List<StateApplier<?>> outputFluidStateAppliers;
    private final boolean outputFluidKeep;

    @Nonnull
    private final List<ItemStack> drops;

    public EntropyRecipe(net.minecraft.resources.ResourceLocation id, EntropyMode mode, Block inputBlock, List<StateMatcher> inputBlockMatchers,
                         net.minecraft.world.level.material.Fluid inputFluid, List<StateMatcher> inputFluidMatchers, Block outputBlock,
                         List<StateApplier<?>> outputBlockStateAppliers, boolean outputBlockKeep, net.minecraft.world.level.material.Fluid outputFluid,
                         List<StateApplier<?>> outputFluidStateAppliers, boolean outputFluidKeep, List<ItemStack> drops) {
        Preconditions.checkArgument(inputBlock != null || inputFluid != null,
                "One of inputBlock or inputFluid must not be null");

        this.id = Objects.requireNonNull(id, "id must not be null");
        this.mode = Objects.requireNonNull(mode, "mode must not be null");

        this.inputBlock = inputBlock;
        this.inputBlockMatchers = Objects.requireNonNull(inputBlockMatchers, "inputBlockMatchers must be not null");

        this.inputFluid = inputFluid;
        this.inputFluidMatchers = Objects.requireNonNull(inputFluidMatchers, "inputFluidMatchers must be not null");

        this.outputBlock = outputBlock;
        this.outputBlockStateAppliers = Objects.requireNonNull(outputBlockStateAppliers,
                "outputBlockStateAppliers must be not null");
        this.outputBlockKeep = outputBlockKeep;

        this.outputFluid = outputFluid;
        this.outputFluidStateAppliers = Objects.requireNonNull(outputFluidStateAppliers,
                "outputFluidStateAppliers must be not null");
        this.outputFluidKeep = outputFluidKeep;

        this.drops = Objects.requireNonNull(drops, "drops must not be null");
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public net.minecraft.resources.ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Nonnull
    public EntropyMode getMode() {
        return this.mode;
    }

    @Nullable
    public net.minecraft.world.level.block.Block getInputBlock() {
        return this.inputBlock;
    }

    @Nullable
    public net.minecraft.world.level.material.Fluid getInputFluid() {
        return this.inputFluid;
    }

    @Nullable
    public Block getOutputBlock() {
        return this.outputBlock;
    }

    public boolean getOutputBlockKeep() {
        return this.outputBlockKeep;
    }

    @Nullable
    public BlockState getOutputBlockState(BlockState originalBlockState) {
        if (this.getOutputBlock() == null) {
            return null;
        }

        net.minecraft.world.level.block.state.BlockState state = getOutputBlock().defaultBlockState();

        if (this.outputBlockKeep) {
            for (Property<?> property : originalBlockState.getProperties()) {
                state = copyProperty(originalBlockState, state, property);
            }
        }

        for (StateApplier<?> entry : this.outputBlockStateAppliers) {
            state = entry.apply(state);
        }

        return state;
    }

    @Nullable
    public net.minecraft.world.level.material.Fluid getOutputFluid() {
        return this.outputFluid;
    }

    public boolean getOutputFluidKeep() {
        return this.outputFluidKeep;
    }

    @Nullable
    public net.minecraft.world.level.material.FluidState getOutputFluidState(net.minecraft.world.level.material.FluidState originalFluidState) {
        if (this.getOutputFluid() == null) {
            return null;
        }

        FluidState state = getOutputFluid().defaultFluidState();

        if (this.outputFluidKeep) {
            for (Property<?> property : originalFluidState.getProperties()) {
                state = copyProperty(originalFluidState, state, property);
            }
        }

        for (StateApplier<?> entry : this.outputFluidStateAppliers) {
            state = entry.apply(state);
        }

        return state;
    }

    @Nonnull
    public List<ItemStack> getDrops() {
        return this.drops;
    }

    public boolean matches(EntropyMode mode, BlockState blockState, FluidState fluidState) {
        if (this.getMode() != mode) {
            return false;
        }

        if (blockState.getBlock() != this.getInputBlock() && this.getInputBlock() != null) {
            return false;
        }

        if (fluidState.getType() != this.getInputFluid() && this.getInputFluid() != null) {
            return false;
        }

        boolean isValid = true;

        if (fluidState.getType() == this.getInputFluid()) {
            isValid = this.inputFluidMatchers.stream().allMatch(m -> m.matches(fluidState));
        }

        return isValid;
    }

    @Nonnull
    List<StateMatcher> getInputBlockMatchers() {
        return inputBlockMatchers;
    }

    @Nonnull
    List<StateMatcher> getInputFluidMatchers() {
        return inputFluidMatchers;
    }

    @Nonnull
    List<StateApplier<?>> getOutputBlockStateAppliers() {
        return outputBlockStateAppliers;
    }

    @Nonnull
    List<StateApplier<?>> getOutputFluidStateAppliers() {
        return outputFluidStateAppliers;
    }

    /**
     * Copies a property from one stateholder to another (if that stateholder also has that property).
     */
    private static <T extends Comparable<T>, SH extends StateHolder<?, SH>> SH copyProperty(SH from, SH to,
                                                                                            Property<T> property) {
        if (to.hasProperty(property)) {
            return to.setValue(property, from.getValue(property));
        } else {
            return to;
        }
    }

}
