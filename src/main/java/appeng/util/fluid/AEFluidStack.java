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

import java.util.Objects;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.items.misc.WrappedFluidStack;
import appeng.util.Platform;
import appeng.util.item.AEStack;

public final class AEFluidStack extends AEStack<IAEFluidStack> implements IAEFluidStack, Comparable<AEFluidStack> {
    private static final String NBT_STACKSIZE = "cnt";
    private static final String NBT_REQUESTABLE = "req";
    private static final String NBT_CRAFTABLE = "craft";
    private static final String NBT_FLUID_ID = "f";
    private static final String NBT_FLUID_TAG = "ft";

    private final FluidVariant fluid;

    private AEFluidStack(final AEFluidStack fluidStack) {
        this.fluid = fluidStack.fluid;
        this.setStackSize(fluidStack.getStackSize());

        // priority = is.priority;
        this.setCraftable(fluidStack.isCraftable());
        this.setCountRequestable(fluidStack.getCountRequestable());
    }

    private AEFluidStack(@Nonnull FluidVariant fluid, long amount) {
        Preconditions.checkArgument(!fluid.isBlank(), "!fluid.isBlank()");
        this.fluid = fluid;
        this.setStackSize(amount);
        this.setCraftable(false);
        this.setCountRequestable(0);
    }

    public static AEFluidStack of(FluidVariant fluid, long amount) {
        Objects.requireNonNull(fluid, "fluid");
        if (fluid.isBlank()) {
            return null;
        }

        return new AEFluidStack(fluid, amount);
    }

    public static IAEFluidStack fromNBT(final CompoundTag data) {

        ResourceLocation fluidId = new ResourceLocation(data.getString(NBT_FLUID_ID));
        Fluid fluid = Registry.FLUID.get(fluidId);
        CompoundTag tag = null;
        if (data.contains(NBT_FLUID_TAG, Tag.TAG_COMPOUND)) {
            tag = data.getCompound(NBT_FLUID_TAG);
        }

        var variant = FluidVariant.of(fluid, tag);
        if (variant.isBlank()) {
            return null;
        }

        long amount = data.getLong(NBT_STACKSIZE);

        AEFluidStack fluidStack = new AEFluidStack(variant, amount);
        fluidStack.setCountRequestable(data.getLong(NBT_REQUESTABLE));
        fluidStack.setCraftable(data.getBoolean(NBT_CRAFTABLE));
        return fluidStack;
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        data.putString(NBT_FLUID_ID, Registry.FLUID.getKey(fluid.getFluid()).toString());
        if (fluid.getNbt() != null) {
            data.put(NBT_FLUID_TAG, fluid.getNbt());
        }
        data.putLong(NBT_STACKSIZE, this.getStackSize());
        data.putLong(NBT_REQUESTABLE, this.getCountRequestable());
        data.putBoolean(NBT_CRAFTABLE, this.isCraftable());
    }

    @Override
    public boolean fuzzyEquals(IAEStack other, final FuzzyMode mode) {
        if (other instanceof AEFluidStack fluidStack) {
            return this.fluid == fluidStack.getFluid();
        } else {
            return false;
        }
    }

    @Override
    public IAEFluidStack copy() {
        return new AEFluidStack(this);
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.fluids();
    }

    @Override
    public int compareTo(final AEFluidStack other) {
        if (this.fluid != other.fluid) {
            return Integer.compare(
                    Registry.FLUID.getId(fluid.getFluid()),
                    Registry.FLUID.getId(other.fluid.getFluid()));
        }

        if (Platform.itemComparisons().isNbtTagEqual(fluid.getNbt(), other.fluid.getNbt())) {
            return 0;
        }

        return this.fluid.hashCode() - other.fluid.hashCode();
    }

    @Override
    public int hashCode() {
        return fluid.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        return other != null && other.getClass() == AEFluidStack.class && ((AEFluidStack) other).fluid.equals(fluid);
    }

    @Override
    public String toString() {
        return this.getStackSize() + "x" + fluid;
    }

    @Override
    public boolean hasTagCompound() {
        return this.fluid.hasNbt();
    }

    public ResourceAmount<FluidVariant> getResourceAmount() {
        return new ResourceAmount<>(fluid, getStackSize());
    }

    @Override
    public FluidVariant getFluid() {
        return this.fluid;
    }

    @Override
    public ItemStack wrap() {
        return WrappedFluidStack.wrap(this);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        var is = wrap();
        WrappedFluidStack item = (WrappedFluidStack) is.getItem();
        item.setAmount(is, 0L);
        return is;
    }

    public static IAEFluidStack fromPacket(final FriendlyByteBuf buffer) {
        var fluid = FluidVariant.fromPacket(buffer);
        boolean isCraftable = buffer.readBoolean();
        long stackSize = buffer.readVarLong();
        long countRequestable = buffer.readVarLong();

        final AEFluidStack stack = new AEFluidStack(fluid, stackSize);
        stack.setCountRequestable(countRequestable);
        stack.setCraftable(isCraftable);
        return stack;
    }

    @Override
    public void writeToPacket(final FriendlyByteBuf buffer) {
        fluid.toPacket(buffer);
        buffer.writeBoolean(this.isCraftable());
        buffer.writeVarLong(this.getStackSize());
        buffer.writeVarLong(this.getCountRequestable());
    }
}
