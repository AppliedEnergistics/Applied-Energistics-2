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
import javax.annotation.Nullable;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.Api;
import appeng.fluids.items.FluidDummyItem;
import appeng.util.Platform;
import appeng.util.item.AEStack;

import java.math.RoundingMode;

public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack> {
    private static final String NBT_STACKSIZE = "cnt";
    private static final String NBT_REQUESTABLE = "req";
    private static final String NBT_CRAFTABLE = "craft";
    private static final String NBT_FLUID_ID = "f";
    private static final String NBT_FLUID_TAG = "ft";

    private final FluidKey fluid;
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

    private AEFluidStack(@Nonnull FluidKey fluid, long amount, @Nullable CompoundTag tag) {
        this.fluid = Preconditions.checkNotNull(fluid);
        this.setStackSize(amount);
        this.setCraftable(false);
        this.setCountRequestable(0);
        this.tagCompound = tag;
    }

    public static AEFluidStack fromFluidVolume(final FluidVolume input, RoundingMode roundingMode) {
        if (input.isEmpty()) {
            return null;
        }

        FluidKey fluid = input.getFluidKey();
        if (fluid == null) {
            throw new IllegalArgumentException("Fluid is null.");
        }

        CompoundTag tag = input.toTag();
        if (tag.isEmpty()) {
            tag = null;
        }

        long amount = input.amount().asLong(1000, roundingMode);

        return new AEFluidStack(fluid, amount, tag);
    }

    public static IAEFluidStack fromNBT(final CompoundTag data) {
        CompoundTag fluidId = data.getCompound(NBT_FLUID_ID);
        FluidKey fluid = FluidKey.fromTag(fluidId);
        if (fluid == FluidKeys.EMPTY) {
            return null;
        }

        CompoundTag tag = null;
        if (data.contains(NBT_FLUID_TAG, NbtType.COMPOUND)) {
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
        data.put(NBT_FLUID_ID, this.fluid.toTag());
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
            return this.fluid.entry.getId().compareTo(other.fluid.entry.getId());
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
        } else if (other instanceof FluidVolume) {
            final FluidVolume is = (FluidVolume) other;
            return is.getFluidKey() == this.fluid
                    && Platform.itemComparisons().isNbtTagEqual(this.tagCompound, is.toTag());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getStackSize() + "x" + this.getFluidStack().getFluidKey().entry.getId() + " " + this.tagCompound;
    }

    @Override
    public boolean hasTagCompound() {
        return this.tagCompound != null;
    }

    @Override
    public FluidVolume getFluidStack() {
        FluidAmount amount = FluidAmount.of(this.getStackSize(), 1000);
        return this.fluid.readVolume(tagCompound).withAmount(amount);
    }

    @Override
    public FluidAmount getAmount() {
        return FluidAmount.of(getStackSize(), 1000);
    }

    @Override
    public FluidKey getFluid() {
        return this.fluid;
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        ItemStack is = Api.instance().definitions().items().dummyFluidItem().maybeStack(1).orElse(ItemStack.EMPTY);
        if (!is.isEmpty()) {
            FluidDummyItem item = (FluidDummyItem) is.getItem();
            item.setFluidStack(is, this.getFluidStack());
            return is;
        }
        return ItemStack.EMPTY;
    }

    public static IAEFluidStack fromPacket(final PacketByteBuf buffer) {
        final boolean isCraftable = buffer.readBoolean();

        FluidKey fluid = FluidKey.fromTag(buffer.readCompoundTag());
        CompoundTag compoundTag = buffer.readCompoundTag();
        final long amount = buffer.readVarLong();
        final long countRequestable = buffer.readVarLong();

        final AEFluidStack fluidStack = new AEFluidStack(fluid, amount, compoundTag);
        fluidStack.setCountRequestable(countRequestable);
        fluidStack.setCraftable(isCraftable);
        return fluidStack;
    }

    @Override
    public void writeToPacket(final PacketByteBuf buffer) {
        buffer.writeBoolean(this.isCraftable());
        buffer.writeCompoundTag(fluid.toTag());
        buffer.writeCompoundTag(tagCompound);
        buffer.writeVarLong(this.getStackSize());
        buffer.writeVarLong(this.getCountRequestable());
    }
}
