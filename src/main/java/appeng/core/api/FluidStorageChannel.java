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

package appeng.core.api;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AppEng;
import appeng.items.misc.FluidDummyItem;
import appeng.util.fluid.AEFluidStack;
import appeng.util.fluid.FluidList;

public final class FluidStorageChannel implements IFluidStorageChannel {

    public static final IFluidStorageChannel INSTANCE = new FluidStorageChannel();

    private FluidStorageChannel() {
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return AppEng.makeId("fluid");
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
    public IItemList<IAEFluidStack> createList() {
        return new FluidList();
    }

    @Override
    public IAEFluidStack createStack(Object input) {
        Preconditions.checkNotNull(input);

        if (input instanceof FluidStack) {
            return AEFluidStack.fromFluidStack((FluidStack) input);
        }
        if (input instanceof ItemStack is) {
            if (is.getItem() instanceof FluidDummyItem) {
                return AEFluidStack.fromFluidStack(((FluidDummyItem) is.getItem()).getFluidStack(is));
            } else {
                return AEFluidStack.fromFluidStack(FluidUtil.getFluidContained(is).orElse(null));
            }
        }

        return null;
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
}
