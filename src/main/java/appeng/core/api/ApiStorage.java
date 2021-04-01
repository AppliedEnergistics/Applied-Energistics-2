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

package appeng.core.api;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.FluidExtractable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageHelper;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.crafting.CraftingLink;
import appeng.fluids.items.FluidDummyItem;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.FluidList;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;

public class ApiStorage implements IStorageHelper {

    private final ClassToInstanceMap<IStorageChannel<?>> channels;

    public ApiStorage() {
        this.channels = MutableClassToInstanceMap.create();
        this.registerStorageChannel(IItemStorageChannel.class, new ItemStorageChannel());
        this.registerStorageChannel(IFluidStorageChannel.class, new FluidStorageChannel());
    }

    @Override
    public <T extends IAEStack<T>, C extends IStorageChannel<T>> void registerStorageChannel(Class<C> channel,
            C factory) {
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(factory);
        Preconditions.checkArgument(channel.isInstance(factory));
        Preconditions.checkArgument(!this.channels.containsKey(channel));

        this.channels.putInstance(channel, factory);
    }

    @Override
    public <T extends IAEStack<T>, C extends IStorageChannel<T>> C getStorageChannel(Class<C> channel) {
        Preconditions.checkNotNull(channel);

        final C type = this.channels.getInstance(channel);

        Preconditions.checkNotNull(type);

        return type;
    }

    @Override
    public Collection<IStorageChannel<? extends IAEStack<?>>> storageChannels() {
        return Collections.unmodifiableCollection(this.channels.values());
    }

    @Override
    public ICraftingLink loadCraftingLink(final CompoundNBT data, final ICraftingRequester req) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(req);

        return new CraftingLink(data, req);
    }

    @Override
    public <T extends IAEStack<T>> T poweredInsert(IEnergySource energy, IMEInventory<T> inv, T input,
            IActionSource src, Actionable mode) {
        return Platform.poweredInsert(energy, inv, input, src, mode);
    }

    @Override
    public <T extends IAEStack<T>> T poweredExtraction(IEnergySource energy, IMEInventory<T> inv, T request,
            IActionSource src, Actionable mode) {
        return Platform.poweredExtraction(energy, inv, request, src, mode);
    }

    @Override
    public void postChanges(IStorageGrid gs, ItemStack removedCell, ItemStack addedCell, IActionSource src) {
        Preconditions.checkNotNull(gs);
        Preconditions.checkNotNull(removedCell);
        Preconditions.checkNotNull(addedCell);
        Preconditions.checkNotNull(src);

        Platform.postChanges(gs, removedCell, addedCell, src);
    }

    private static final class ItemStorageChannel implements IItemStorageChannel {

        @Override
        public IItemList<IAEItemStack> createList() {
            return new ItemList();
        }

        @Override
        public IAEItemStack createStack(Object input) {
            Preconditions.checkNotNull(input);

            if (input instanceof ItemStack) {
                return AEItemStack.fromItemStack((ItemStack) input);
            }

            return null;
        }

        @Override
        public IAEItemStack createFromNBT(CompoundNBT nbt) {
            Preconditions.checkNotNull(nbt);
            return AEItemStack.fromNBT(nbt);
        }

        @Override
        public IAEItemStack readFromPacket(PacketBuffer input) {
            Preconditions.checkNotNull(input);

            return AEItemStack.fromPacket(input);
        }
    }

    private static final class FluidStorageChannel implements IFluidStorageChannel {

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

            if (input instanceof FluidVolume) {
                return AEFluidStack.fromFluidVolume((FluidVolume) input, RoundingMode.DOWN);
            }
            if (input instanceof ItemStack) {
                final ItemStack is = (ItemStack) input;
                if (is.getItem() instanceof FluidDummyItem) {
                    return AEFluidStack.fromFluidVolume(((FluidDummyItem) is.getItem()).getFluidStack(is),
                            RoundingMode.DOWN);
                } else {
                    FluidExtractable fluidExtractable = FluidAttributes.EXTRACTABLE.get(is);
                    FluidVolume fluidVolume = fluidExtractable.attemptAnyExtraction(FluidAmount.MAX_VALUE,
                            Simulation.ACTION);
                    if (!fluidVolume.isEmpty()) {
                        return AEFluidStack.fromFluidVolume(fluidVolume, RoundingMode.DOWN);
                    }
                }
            }

            return null;
        }

        @Override
        public IAEFluidStack readFromPacket(PacketBuffer input) {
            Preconditions.checkNotNull(input);

            return AEFluidStack.fromPacket(input);
        }

        @Override
        public IAEFluidStack createFromNBT(CompoundNBT nbt) {
            Preconditions.checkNotNull(nbt);
            return AEFluidStack.fromNBT(nbt);
        }
    }
}
