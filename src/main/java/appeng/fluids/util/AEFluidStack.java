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

package appeng.fluids.util;

import javax.annotation.Nonnull;

import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.items.FluidDummyItem;
import appeng.util.Platform;
import appeng.util.item.AEStack;

public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack> {
    private static final String NBT_STACKSIZE = "cnt";
    private static final String NBT_REQUESTABLE = "req";
    private static final String NBT_CRAFTABLE = "craft";
    private static final String NBT_FLUIDSTACK = "fs";

    private final Fluid fluid;
    private CompoundNBT tagCompound;

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

    private AEFluidStack(@Nonnull final FluidStack fluidStack) {
        this.fluid = fluidStack.getFluid();

        if (this.fluid == null) {
            throw new IllegalArgumentException("Fluid is null.");
        }

        this.setStackSize(fluidStack.getAmount());
        this.setCraftable(false);
        this.setCountRequestable(0);

        if (fluidStack.getTag() != null) {
            this.tagCompound = fluidStack.getTag().copy();
        }
    }

    public static AEFluidStack fromFluidStack(final FluidStack input) {
        if (input == null) {
            return null;
        }

        return new AEFluidStack(input);
    }

    public static IAEFluidStack fromNBT(final CompoundNBT data) {
        final FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(data.getCompound(NBT_FLUIDSTACK));

        if (fluidStack == null) {
            return null;
        }

        final AEFluidStack fluid = AEFluidStack.fromFluidStack(fluidStack);
        fluid.setStackSize(data.getLong(NBT_STACKSIZE));
        fluid.setCountRequestable(data.getLong(NBT_REQUESTABLE));
        fluid.setCraftable(data.getBoolean(NBT_CRAFTABLE));

        if (fluid.hasTagCompound()) {
            fluid.tagCompound = fluid.tagCompound.copy();
        }

        return fluid;
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
    public void writeToNBT(final CompoundNBT data) {
        data.getCompound(NBT_FLUIDSTACK).putString("FluidName", this.fluid.getRegistryName().toString());
        data.getCompound(NBT_FLUIDSTACK).putInt("Amount", 0);
        if (this.hasTagCompound()) {
            data.getCompound(NBT_FLUIDSTACK).put("Tag", this.tagCompound);
        } else {
            data.getCompound(NBT_FLUIDSTACK).remove("Tag");
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
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return true;
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IFluidStorageChannel.class);
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
        result = prime * result + ((this.fluid == null) ? 0 : this.fluid.hashCode());
        result = prime * result + ((this.tagCompound == null) ? 0 : this.tagCompound.hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof AEFluidStack) {
            final AEFluidStack is = (AEFluidStack) other;
            return is.fluid == this.fluid && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.tagCompound);
        } else if (other instanceof FluidStack) {
            final FluidStack is = (FluidStack) other;
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
        ItemStack is = AEApi.instance().definitions().items().dummyFluidItem().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!is.isEmpty()) {
            FluidDummyItem item = (FluidDummyItem) is.getItem();
            item.setFluidStack(is, this.getFluidStack());
            return is;
        }
        return ItemStack.EMPTY;
    }

    public static IAEFluidStack fromPacket(final PacketBuffer buffer) {
        final boolean isCraftable = buffer.readBoolean();
        final FluidStack fluidStack = buffer.readFluidStack();

        final long stackSize = buffer.readVarLong();
        final long countRequestable = buffer.readVarLong();

        if (fluidStack == null) {
            return null;
        }

        final AEFluidStack fluid = AEFluidStack.fromFluidStack(fluidStack);
        fluid.setStackSize(stackSize);
        fluid.setCountRequestable(countRequestable);
        fluid.setCraftable(isCraftable);
        return fluid;
    }

    @Override
    public void writeToPacket(final PacketBuffer buffer) {
        buffer.writeBoolean(this.isCraftable());
        buffer.writeFluidStack(this.getFluidStack());
        buffer.writeVarLong(this.getStackSize());
        buffer.writeVarLong(this.getCountRequestable());
    }
}
