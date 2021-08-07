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

package appeng.util.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.core.definitions.AEItems;
import appeng.items.misc.FluidDummyItem;
import appeng.util.Platform;
import appeng.util.item.AEStack;

public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack> {
    private static final String NBT_STACKSIZE = "cnt";
    private static final String NBT_REQUESTABLE = "req";
    private static final String NBT_CRAFTABLE = "craft";
    private static final String NBT_FLUID_ID = "f";
    private static final String NBT_FLUID_TAG = "ft";

    private final Fluid fluid;
    private CompoundTag tagCompound;

    private AEFluidStack(final AEFluidStack fluidStack) {
        this.fluid = fluidStack.fluid;
        this.setStackSize(fluidStack.getStackSize());

        // priority = is.priority;
        this.setCraftable(fluidStack.isCraftable());
        this.setCountRequestable(fluidStack.getCountRequestable());

        if (fluidStack.hasTagCompound()) {
            this.tagCompound = fluidStack.tagCompound.copy();
        }
    }

    private AEFluidStack(@Nonnull Fluid fluid, long amount, @Nullable CompoundTag tag) {
        if (fluid == Fluids.EMPTY) {
            System.out.println();
        }
        this.fluid = Preconditions.checkNotNull(fluid);
        this.setStackSize(amount);
        this.setCraftable(false);
        this.setCountRequestable(0);
        this.tagCompound = tag;
    }

    public static AEFluidStack fromFluidStack(final FluidStack input) {
        if (input.isEmpty()) {
            return null;
        }

        Fluid fluid = input.getFluid();
        if (fluid == null) {
            throw new IllegalArgumentException("Fluid is null.");
        }

        long amount = input.getAmount();
        CompoundTag tag = null;
        if (input.getTag() != null) {
            tag = input.getTag().copy();
        }
        return new AEFluidStack(fluid, amount, tag);
    }

    public static IAEFluidStack fromNBT(final CompoundTag data) {
        ResourceLocation fluidId = new ResourceLocation(data.getString(NBT_FLUID_ID));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
        if (fluid == null || fluid == Fluids.EMPTY) {
            return null;
        }

        CompoundTag tag = null;
        if (data.contains(NBT_FLUID_TAG, Constants.NBT.TAG_COMPOUND)) {
            tag = data.getCompound(NBT_FLUID_TAG);
        }

        long amount = data.getLong(NBT_STACKSIZE);

        AEFluidStack fluidStack = new AEFluidStack(fluid, amount, tag);
        fluidStack.setCountRequestable(data.getLong(NBT_REQUESTABLE));
        fluidStack.setCraftable(data.getBoolean(NBT_CRAFTABLE));
        return fluidStack;
    }

    @Override
    public void add(final IAEFluidStack option) {
        if (option == null) {
            return;
        }
        this.incStackSize(option.getStackSize());
        this.setCountRequestable(this.getCountRequestable() + option.getCountRequestable());
        this.setCraftable(this.isCraftable() || option.isCraftable());
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        data.putString(NBT_FLUID_ID, this.fluid.getRegistryName().toString());
        if (this.hasTagCompound()) {
            data.put(NBT_FLUID_TAG, this.tagCompound);
        }
        data.putLong(NBT_STACKSIZE, this.getStackSize());
        data.putLong(NBT_REQUESTABLE, this.getCountRequestable());
        data.putBoolean(NBT_CRAFTABLE, this.isCraftable());
    }

    @Override
    public boolean fuzzyComparison(final IAEFluidStack other, final FuzzyMode mode) {
        return this.fluid == other.getFluid();
    }

    @Override
    public IAEFluidStack copy() {
        return new AEFluidStack(this);
    }

    @Override
    public IAEFluidStack empty() {
        final IAEFluidStack dup = this.copy();
        dup.reset();
        return dup;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return Api.instance().storage().getStorageChannel(IFluidStorageChannel.class);
    }

    @Override
    public int compareTo(final AEFluidStack other) {
        if (this.fluid != other.fluid) {
            return this.fluid.getRegistryName().compareTo(other.fluid.getRegistryName());
        }

        if (Platform.itemComparisons().isNbtTagEqual(this.tagCompound, other.tagCompound)) {
            return 0;
        }

        return this.tagCompound.hashCode() - other.tagCompound.hashCode();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.fluid == null ? 0 : this.fluid.hashCode());
        result = prime * result + (this.tagCompound == null ? 0 : this.tagCompound.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AEFluidStack is) {
            return is.fluid == this.fluid && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.tagCompound);
        } else if (other instanceof FluidStack is) {
            return is.getFluid() == this.fluid
                    && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.getTag());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getStackSize() + "x" + this.getFluidStack().getFluid().getRegistryName() + " " + this.tagCompound;
    }

    @Override
    public boolean hasTagCompound() {
        return this.tagCompound != null;
    }

    @Override
    public FluidStack getFluidStack() {
        final int amount = (int) Math.min(Integer.MAX_VALUE, this.getStackSize());
        final FluidStack is = new FluidStack(this.fluid, amount, this.tagCompound);

        return is;
    }

    @Override
    public Fluid getFluid() {
        return this.fluid;
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        ItemStack is = AEItems.DUMMY_FLUID_ITEM.stack();
        if (!is.isEmpty()) {
            FluidDummyItem item = (FluidDummyItem) is.getItem();
            item.setFluidStack(is, this.getFluidStack());
            return is;
        }
        return ItemStack.EMPTY;
    }

    public static IAEFluidStack fromPacket(final FriendlyByteBuf buffer) {
        final boolean isCraftable = buffer.readBoolean();
        Fluid fluid = buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS);
        CompoundTag tag = buffer.readNbt();

        final long stackSize = buffer.readVarLong();
        final long countRequestable = buffer.readVarLong();

        final AEFluidStack stack = new AEFluidStack(fluid, stackSize, tag);
        stack.setCountRequestable(countRequestable);
        stack.setCraftable(isCraftable);
        return stack;
    }

    @Override
    public void writeToPacket(final FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.isCraftable());
        // Cannot use writeFluidStack here because for FluidStacks with amount==0, it
        // will not write the fluid
        buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, fluid);
        buffer.writeNbt(getFluidStack().getTag());
        buffer.writeVarLong(this.getStackSize());
        buffer.writeVarLong(this.getCountRequestable());
    }
}
