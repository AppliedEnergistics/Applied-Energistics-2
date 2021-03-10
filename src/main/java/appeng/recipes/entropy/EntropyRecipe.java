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
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.core.AppEng;
import appeng.items.tools.powered.EntropyManipulatorItem;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements IRecipe<IInventory> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");

    public static final IRecipeType<EntropyRecipe> TYPE = IRecipeType.register(TYPE_ID.toString());

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final EntropyMode mode;

    @Nullable
    private final Block inputBlock;
    @Nullable
    private final CompoundNBT inputBlockProperties;
    @Nonnull
    private final List<StateMatcher> inputBlockMatchers;

    @Nullable
    private final Fluid inputFluid;
    @Nullable
    private final CompoundNBT inputFluidProperties;
    @Nonnull
    private final List<StateMatcher> inputFluidMatchers;

    @Nullable
    private final Block outputBlock;
    private List<StateApplier> outputBlockAppliers;
    @Nullable
    private final CompoundNBT outputBlockProperties;
    private final boolean outputBlockKeep;
    @Nullable
    private final Fluid outputFluid;
    @Nullable
    private final CompoundNBT outputFluidProperties;
    private final boolean outputFluidKeep;

    @Nonnull
    private final List<ItemStack> drops;

    public EntropyRecipe(ResourceLocation id, EntropyMode mode, Block inputBlock, CompoundNBT inputBlockProperties,
            Fluid inputFluid, CompoundNBT inputFluidProperties, Block outputBlock, CompoundNBT outputBlockNbt,
            boolean outputBlockKeep, Fluid outputFluid, CompoundNBT outputFluidNbt, boolean outputFluidKeep,
            List<ItemStack> drops) {
        Preconditions.checkArgument(id != null);
        Preconditions.checkArgument(mode != null);
        Preconditions.checkArgument(drops == null || !drops.isEmpty(),
                "drops needs to be either null or a non empty list");

        this.id = id;
        this.mode = mode;

        this.inputBlock = inputBlock;
        this.inputBlockProperties = inputBlockProperties;
        this.inputBlockMatchers = createMatchers(inputBlockProperties);

        this.inputFluid = inputFluid;
        this.inputFluidProperties = inputFluidProperties;
        this.inputFluidMatchers = createMatchers(inputFluidProperties);

        this.outputBlock = outputBlock;
        this.outputBlockProperties = outputBlockNbt;
        this.outputBlockAppliers = createAppliers(outputBlockNbt);
        this.outputBlockKeep = outputBlockKeep;

        this.outputFluid = outputFluid;
        this.outputFluidProperties = outputFluidNbt;
        this.outputFluidKeep = outputFluidKeep;

        this.drops = drops != null ? drops : Collections.emptyList();
    }

    private List<StateApplier> createAppliers(CompoundNBT outputBlockNbt) {
        if (this.getOutputBlock() == null) {
            return Collections.emptyList();
        }

        List<StateApplier> list = new ArrayList<>();

        StateContainer<Block, BlockState> base = this.getOutputBlock().getStateContainer();

        for (String key : outputBlockNbt.keySet()) {
            Property baseProperty = base.getProperty(key);
            String value = outputBlockNbt.getString(key);
            Comparable propertyValue = (Comparable) baseProperty.parseValue(value).orElse(null);

            list.add(new StateApplier(baseProperty, propertyValue));
        }

        return list;
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    @Override
    public IRecipeType<?> getType() {
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
    public Block getInputBlock() {
        return this.inputBlock;
    }

    @Nullable
    public CompoundNBT getInputBlockProperties() {
        return inputBlockProperties;
    }

    @Nullable
    public Fluid getInputFluid() {
        return this.inputFluid;
    }

    @Nullable
    public CompoundNBT getInputFluidProperties() {
        return inputFluidProperties;
    }

    @Nullable
    public Block getOutputBlock() {
        return this.outputBlock;
    }

    @Nullable
    public CompoundNBT getOutputBlockProperties() {
        return outputBlockProperties;
    }

    public boolean getOutputBlockKeep() {
        return this.outputBlockKeep;
    }

    @Nullable
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BlockState getOutputBlockState(BlockState originalBlockState) {
        if (this.getOutputBlock() == null) {
            return null;
        }

        BlockState state = getOutputBlock().getDefaultState();

        if (this.outputBlockKeep) {
            for (Property property : originalBlockState.getProperties()) {
                if (state.hasProperty(property)) {
                    state = state.with(property, originalBlockState.get(property));
                }
            }
        }

        for (StateApplier entry : this.outputBlockAppliers) {
            state = (BlockState) entry.apply(state);
        }

//        if (this.outputBlockProperties != null && !this.outputBlockProperties.isEmpty()) {
//            StateContainer<Block, BlockState> base = this.getOutputBlock().getStateContainer();
//
//            for (String key : this.outputBlockProperties.keySet()) {
//                String value = this.outputBlockProperties.getString(key);
//                Property baseProperty = base.getProperty(key);
//                Comparable propertyValue = (Comparable) baseProperty.parseValue(value).orElse(null);
//
//                state = state.with(baseProperty, propertyValue);
//            }
//        }

        return state;
    }

    @Nullable
    public Fluid getOutputFluid() {
        return this.outputFluid;
    }

    @Nullable
    public CompoundNBT getOutputFluidProperties() {
        return outputFluidProperties;
    }

    public boolean getOutputFluidKeep() {
        return this.outputFluidKeep;
    }

    @Nullable
    public FluidState getOutputFluidState() {
        if (this.getOutputFluid() == null) {
            return null;
        }

        FluidState state = getOutputFluid().getDefaultState();

        if (this.outputFluidProperties != null && !this.outputFluidProperties.isEmpty()) {
            StateContainer<Fluid, FluidState> base = state.getFluid().getStateContainer();

            for (String key : this.outputFluidProperties.keySet()) {
                String value = this.outputFluidProperties.getString(key);
                Property baseProperty = base.getProperty(key);
                Comparable propertyValue = (Comparable) baseProperty.parseValue(value).orElse(null);

                state = state.with(baseProperty, propertyValue);
            }
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

        if (fluidState.getFluid() != this.getInputFluid() && this.getInputFluid() != null) {
            return false;
        }

        boolean isValid = true;

        if (fluidState.getFluid() == this.getInputFluid()) {
            isValid = this.inputFluidMatchers.stream().map(m -> {
                return m.matches(fluidState.getFluid().getStateContainer(), fluidState);
            }).allMatch(Boolean::valueOf);
        }

        return isValid;
    }

    /**
     * Creates matchers from the passed NBT structure.
     * 
     * @see StateMatcher
     * @see SingleValueMatcher
     * @see MultipleValuesMatcher
     * @see RangeValueMatcher
     * 
     * @param nbt
     * @return
     */
    private static List<StateMatcher> createMatchers(CompoundNBT nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return Collections.emptyList();
        }
        List<StateMatcher> matchers = new ArrayList<>();

        for (String key : nbt.keySet()) {
            CompoundNBT entry = nbt.getCompound(key);

            if (entry.contains("value")) {
                String value = entry.get("value").getString();
                matchers.add(new SingleValueMatcher(key, value));
            } else if (entry.contains("values")) {
                List<String> values = entry.getList("values", 8).stream().map(e -> e.toString())
                        .collect(Collectors.toList());
                matchers.add(new MultipleValuesMatcher(key, values));
            } else if (entry.contains("range")) {
                String min = entry.getCompound("range").getString("min");
                String max = entry.getCompound("range").getString("max");
                matchers.add(new RangeValueMatcher(key, min, max));
            }

        }

        return matchers;
    }

}
