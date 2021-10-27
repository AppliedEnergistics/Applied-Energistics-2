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

package appeng.api.storage;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStackList;
import appeng.core.AppEng;
import appeng.items.misc.WrappedFluidStack;
import appeng.util.fluid.AEFluidStack;
import appeng.util.fluid.FluidList;

public final class FluidStorageChannel implements IStorageChannel<IAEFluidStack> {

    private static final ResourceLocation ID = AppEng.makeId("fluid");

    static final FluidStorageChannel INSTANCE = new FluidStorageChannel();

    private FluidStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int transferFactor() {
        return 125;
    }

    @Override
    public int getUnitsPerByte() {
        return 8000;
    }

    @Override
    public IAEStackList<IAEFluidStack> createList() {
        return new FluidList();
    }

    @Override
    public IAEFluidStack createStack(ItemStack is) {
        Preconditions.checkNotNull(is, "is");

        if (WrappedFluidStack.isWrapped(is)) {
            return WrappedFluidStack.unwrap(is);
        } else {
            FluidStack input = FluidUtil.getFluidContained(is).orElse(null);
            if (input == null) {
                return null;
            }
            return IAEFluidStack.of(input);
        }
    }

    @Override
    public IAEFluidStack readFromPacket(FriendlyByteBuf input) {
        Preconditions.checkNotNull(input);

        return AEFluidStack.fromPacket(input);
    }

    @Override
    public IAEFluidStack createFromNBT(CompoundTag nbt) {
        Preconditions.checkNotNull(nbt);
        return AEFluidStack.fromNBT(nbt);
    }

    @Override
    public IAEFluidStack copy(IAEFluidStack stack) {
        return stack.copy();
    }
}
