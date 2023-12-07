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

import appeng.core.AppEng;
import appeng.init.InitRecipeTypes;
import appeng.items.tools.powered.EntropyManipulatorItem;
import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements Recipe<Container> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");

    public static final RecipeType<EntropyRecipe> TYPE = InitRecipeTypes.register(TYPE_ID.toString());

    private final EntropyMode mode;

    @Nullable
    private final Block inputBlock;

    private final Map<String, PropertyValueMatcher> inputBlockMatchers;

    @Nullable
    private final Fluid inputFluid;

    private final Map<String, PropertyValueMatcher> inputFluidMatchers;

    @Nullable
    private final Block outputBlock;

    private final Map<String, String> outputBlockStateAppliers;
    private final boolean outputBlockKeep;
    @Nullable
    private final Fluid outputFluid;

    private final Map<String, String> outputFluidStateAppliers;
    private final boolean outputFluidKeep;

    private final List<ItemStack> drops;

    public EntropyRecipe(EntropyMode mode, Optional<Block> optionalInputBlock, Map<String, PropertyValueMatcher> inputBlockMatchers,
                         Optional<Fluid> optionalInputFluid, Map<String, PropertyValueMatcher> inputFluidMatchers, Optional<Block> optionalOutputBlock,
                         Map<String, String> outputBlockStateAppliers, boolean outputBlockKeep, Optional<Fluid> optionalOutputFluid,
                         Map<String, String> outputFluidStateAppliers, boolean outputFluidKeep, List<ItemStack> drops) {
        Preconditions.checkArgument(optionalInputBlock.isPresent() || optionalInputFluid.isPresent(),
                "One of inputBlock or inputFluid must not be null");

        this.mode = Objects.requireNonNull(mode, "mode must not be null");

        this.inputBlock = optionalInputBlock.orElse(null);
        this.inputBlockMatchers = Objects.requireNonNull(inputBlockMatchers, "inputBlockMatchers must be not null");
        if (this.inputBlock != null) {
            PropertyUtils.validatePropertyMatchers(inputBlock.getStateDefinition(), inputBlockMatchers);
        }

        this.inputFluid = optionalInputFluid.orElse(null);
        this.inputFluidMatchers = Objects.requireNonNull(inputFluidMatchers, "inputFluidMatchers must be not null");
        if (this.inputFluid != null) {
            PropertyUtils.validatePropertyMatchers(inputFluid.getStateDefinition(), inputFluidMatchers);
        }

        this.outputBlock = optionalOutputBlock.orElse(null);
        this.outputBlockStateAppliers = Objects.requireNonNull(outputBlockStateAppliers,
                "outputBlockStateAppliers must be not null");
        this.outputBlockKeep = outputBlockKeep;

        this.outputFluid = optionalOutputFluid.orElse(null);
        this.outputFluidStateAppliers = Objects.requireNonNull(outputFluidStateAppliers,
                "outputFluidStateAppliers must be not null");
        this.outputFluidKeep = outputFluidKeep;

        this.drops = Objects.requireNonNull(drops, "drops must not be null");
    }

    @Override
    public boolean matches(Container inv, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container inv, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
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

    public EntropyMode getMode() {
        return this.mode;
    }

    public Optional<Block> getInputBlock() {
        return Optional.ofNullable(this.inputBlock);
    }

    public Optional<Fluid> getInputFluid() {
        return Optional.ofNullable(this.inputFluid);
    }

    public Optional<Block> getOutputBlock() {
        return Optional.ofNullable(this.outputBlock);
    }

    public boolean getOutputBlockKeep() {
        return this.outputBlockKeep;
    }

    @Nullable
    public BlockState getOutputBlockState(BlockState originalBlockState) {
        if (outputBlock == null) {
            return null;
        }

        BlockState state = outputBlock.defaultBlockState();

        if (this.outputBlockKeep) {
            for (Property<?> property : originalBlockState.getProperties()) {
                state = copyProperty(originalBlockState, state, property);
            }
        }

        var stateDefinition = originalBlockState.getBlock().getStateDefinition();
        state = PropertyUtils.applyProperties(stateDefinition, state, outputBlockStateAppliers);

        return state;
    }

    public Optional<Fluid> getOutputFluid() {
        return Optional.ofNullable(this.outputFluid);
    }

    public boolean getOutputFluidKeep() {
        return this.outputFluidKeep;
    }

    @Nullable
    public FluidState getOutputFluidState(FluidState originalFluidState) {
        if (outputFluid == null) {
            return null;
        }

        FluidState state = outputFluid.defaultFluidState();

        if (this.outputFluidKeep) {
            for (Property<?> property : originalFluidState.getProperties()) {
                state = copyProperty(originalFluidState, state, property);
            }
        }

        var stateDefinition = state.getType().getStateDefinition();
        state = PropertyUtils.applyProperties(stateDefinition, state, outputBlockStateAppliers);

        return state;
    }

    public List<ItemStack> getDrops() {
        return this.drops;
    }

    public boolean matches(EntropyMode mode, BlockState blockState, FluidState fluidState) {
        if (this.getMode() != mode) {
            return false;
        }

        if (inputBlock != null) {
            if (blockState.getBlock() != inputBlock) {
                return false;
            }
            var stateDefinition = inputBlock.getStateDefinition();
            if (!PropertyUtils.doPropertiesMatch(stateDefinition, blockState, inputBlockMatchers)) {
                return false;
            }
        }

        if (inputFluid != null) {
            if (fluidState.getType() != inputFluid) {
                return false;
            }
            var stateDefinition = inputFluid.getStateDefinition();
            if (!PropertyUtils.doPropertiesMatch(stateDefinition, fluidState, inputFluidMatchers)) {
                return false;
            }
        }

        return true;
    }

    Map<String, PropertyValueMatcher> getInputBlockMatchers() {
        return inputBlockMatchers;
    }

    Map<String, PropertyValueMatcher> getInputFluidMatchers() {
        return inputFluidMatchers;
    }

    Map<String, String> getOutputBlockStateAppliers() {
        return outputBlockStateAppliers;
    }

    Map<String, String> getOutputFluidStateAppliers() {
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
