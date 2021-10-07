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

package appeng.helpers.iface;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.inventories.BaseInternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;
import appeng.util.fluid.AEFluidStack;
import appeng.util.item.AEItemStack;

public class PatternProviderReturnInventory extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 5;

    /**
     * Used to prevent injection through the handlers when we are pushing items out in the network. Otherwise, a storage
     * bus on the pattern provider could potentially void items.
     */
    private boolean injectingIntoNetwork = false;
    // TODO: how do we expose this for foreign storage channels?
    private final IItemHandler itemHandler = new ItemHandler();
    private final LazyOptional<IItemHandler> itemHandlerOpt = LazyOptional.of(() -> itemHandler);
    private final IFluidHandler fluidHandler = new FluidHandler();
    private final LazyOptional<IFluidHandler> fluidHandlerOpt = LazyOptional.of(() -> fluidHandler);

    public PatternProviderReturnInventory(Listener listener) {
        super(listener, NUMBER_OF_SLOTS);
    }

    /**
     * Return true if something could be injected into the network.
     */
    public boolean injectIntoNetwork(IStorageMonitorable network, IActionSource src) {
        var didSomething = false;
        injectingIntoNetwork = true;

        try {
            for (int i = 0; i < stacks.length; ++i) {
                if (stacks[i] != null) {
                    long sizeBefore = stacks[i].getStackSize();
                    stacks[i] = GenericStackHelper.injectMonitorable(network, stacks[i], Actionable.MODULATE, src);

                    if (IAEStack.getStackSizeOrZero(stacks[i]) != sizeBefore) {
                        didSomething = true;
                    }
                }
            }
        } finally {
            injectingIntoNetwork = false;
        }

        return didSomething;
    }

    public void addDrops(List<ItemStack> drops) {
        for (var stack : stacks) {
            if (stack != null && stack.getChannel() == StorageChannels.items()) {
                drops.add(stack.cast(StorageChannels.items()).createItemStack());
            }
        }
    }

    public <T> LazyOptional<T> getCapability(Capability<T> capability) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerOpt.cast();
        } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return fluidHandlerOpt.cast();
        } else {
            return LazyOptional.empty();
        }
    }

    private class ItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            if (stacks[slot] != null && stacks[slot].getChannel() == StorageChannels.items()) {
                return stacks[slot].cast(StorageChannels.items()).createItemStack();
            }
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            if (injectingIntoNetwork) {
                // We are pushing out items already, prevent changing the stacks in unexpected ways.
                return stack;
            }

            // Can't insert if something other than items is there.
            if (stacks[slot] != null && stacks[slot].getChannel() != StorageChannels.items()) {
                return stack;
            }
            // Otherwise this is just a "regular" insert.
            return new BaseInternalInventory() {
                @Override
                public int size() {
                    return 1;
                }

                @Override
                public ItemStack getStackInSlot(int slotIndex) {
                    return ItemHandler.this.getStackInSlot(slot);
                }

                @Override
                public void setItemDirect(int slotIndex, @Nonnull ItemStack stack) {
                    setStack(slot, AEItemStack.fromItemStack(stack));
                }
            }.insertItem(0, stack, simulate);
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }
    }

    private class FluidHandler implements IFluidHandler {

        @Override
        public int getTanks() {
            return size();
        }

        @Nonnull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (stacks[tank] != null && stacks[tank].getChannel() == StorageChannels.fluids()) {
                return stacks[tank].cast(StorageChannels.fluids()).getFluidStack();
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 4 * FluidAttributes.BUCKET_VOLUME;
        }

        @Override
        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (injectingIntoNetwork) {
                // We are pushing out items already, prevent changing the stacks in unexpected ways.
                return 0;
            }

            int filled = 0;
            for (int i = 0; i < stacks.length && resource.getAmount() - filled > 0; ++i) {
                int remainingCapacity = 0;
                if (stacks[i] == null) {
                    remainingCapacity = getTankCapacity(0);
                } else if (stacks[i].getChannel() == StorageChannels.fluids()) {
                    var fs = stacks[i].cast(StorageChannels.fluids());
                    // noinspection EqualsBetweenInconvertibleTypes
                    if (fs.equals(resource)) {
                        remainingCapacity = getTankCapacity(0) - (int) fs.getStackSize();
                    }
                }

                if (remainingCapacity > 0) {
                    int slotInserted = Math.min(resource.getAmount() - filled, remainingCapacity);

                    if (slotInserted > 0) {
                        filled += slotInserted;

                        if (action.execute()) {
                            var newStack = AEFluidStack.fromFluidStack(resource);
                            newStack.setStackSize(getTankCapacity(0) - remainingCapacity + slotInserted);
                            setStack(i, newStack);
                        }
                    }
                }
            }
            return filled;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
