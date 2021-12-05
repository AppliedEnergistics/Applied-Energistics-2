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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.Platform;

public class PatternProviderReturnInventory extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 9;

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

    public PatternProviderReturnInventory(Runnable listener) {
        super(listener, NUMBER_OF_SLOTS);
    }

    /**
     * Return true if something could be injected into the network.
     */
    public boolean injectIntoNetwork(MEStorage storage, IActionSource src) {
        var didSomething = false;
        injectingIntoNetwork = true;

        try {
            for (int i = 0; i < stacks.length; ++i) {
                GenericStack stack = stacks[i];
                if (stack != null) {
                    long sizeBefore = stack.amount();
                    var inserted = storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, src);
                    if (inserted >= stack.amount()) {
                        stacks[i] = null;
                    } else {
                        stacks[i] = new GenericStack(stack.what(), stack.amount() - inserted);
                    }

                    if (GenericStack.getStackSizeOrZero(stacks[i]) != sizeBefore) {
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
            if (stack != null && stack.what() instanceof AEItemKey itemKey) {
                drops.add(itemKey.toStack((int) Math.min(Integer.MAX_VALUE, stack.amount())));
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
            if (stacks[slot] != null && stacks[slot].what() instanceof AEItemKey what) {
                return what.toStack((int) Math.min(Integer.MAX_VALUE, stacks[slot].amount()));
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

            var genericStack = GenericStack.fromItemStack(stack);
            if (genericStack != null) {
                var inserted = insert(slot, genericStack.what(), genericStack.amount(),
                        simulate ? Actionable.SIMULATE : Actionable.MODULATE);
                if (inserted > 0) {
                    return Platform.copyStackWithSize(stack, stack.getCount() - (int) inserted);
                }
            }

            return stack;
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
            if (stacks[tank] != null && stacks[tank].what() instanceof AEFluidKey what) {
                return what.toStack((int) Math.min(Integer.MAX_VALUE, stacks[tank].amount()));
            }
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 4 * AEFluidKey.AMOUNT_BUCKET;
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

            var what = AEFluidKey.of(resource);
            if (what == null) {
                return 0;
            }

            long filled = 0;
            for (int i = 0; i < stacks.length && resource.getAmount() - filled > 0; ++i) {
                filled += insert(i, what, resource.getAmount() - filled, Actionable.of(action));
            }
            return (int) filled;
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
