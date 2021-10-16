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

package appeng.util.fluid;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;

import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.util.inv.IAEFluidInventory;

public class AEFluidInventory implements IAEFluidTank {

    /**
     * While this may seem redundant, it helps since this class heavily mixes AE fluids stacks, which use null to
     * represent "nothing", and Minecraft's FluidStack, which uses #isEmpty() to represent nothing.
     */
    private static final IAEFluidStack EMPTY_AE_FLUIDSTACK = null;

    private final IAEFluidStack[] fluids;
    private final List<View> storageViews;
    private final IAEFluidInventory handler;
    private final long capacity;

    public AEFluidInventory(final IAEFluidInventory handler, final int slots, final long capacity) {
        this.fluids = new IAEFluidStack[slots];
        this.storageViews = new ArrayList<>();
        for (int i = 0; i < slots; ++i) {
            this.storageViews.add(new View(i));
        }
        this.handler = handler;
        this.capacity = capacity;
    }

    public AEFluidInventory(final IAEFluidInventory handler, final int slots) {
        this(handler, slots, Integer.MAX_VALUE);
    }

    @Override
    public long getTankCapacity(int tankIndex) {
        return tankIndex >= 0 && tankIndex < this.fluids.length ? capacity : 0;
    }

    @Override
    public void setFluidInSlot(final int slot, final IAEFluidStack fluid) {
        if (slot >= 0 && slot < this.getSlots()) {
            if (Objects.equals(this.fluids[slot], fluid)) {
                if (fluid != EMPTY_AE_FLUIDSTACK && fluid.getStackSize() != this.fluids[slot].getStackSize()) {
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                    this.onContentChanged(slot);
                }
            } else {
                if (fluid == EMPTY_AE_FLUIDSTACK) {
                    this.fluids[slot] = EMPTY_AE_FLUIDSTACK;
                } else {
                    this.fluids[slot] = fluid.copy();
                    this.fluids[slot].setStackSize(Math.min(fluid.getStackSize(), this.capacity));
                }

                this.onContentChanged(slot);
            }
        }
    }

    private void onContentChanged(final int slot) {
        if (this.handler != null && !this.handler.isRemote()) {
            this.handler.onFluidInventoryChanged(this, slot);
        }
    }

    @Override
    public IAEFluidStack getFluidInSlot(final int slot) {
        if (slot >= 0 && slot < this.getSlots()) {
            return this.fluids[slot];
        }
        return EMPTY_AE_FLUIDSTACK;
    }

    @Override
    public int getSlots() {
        return this.fluids.length;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long totalInserted = 0;

        // First iteration matches the resource, second iteration inserts into empty slots.
        for (View view : storageViews) {
            if (!view.isResourceBlank()) {
                totalInserted += view.insert(resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        for (View view : storageViews) {
            if (view.isResourceBlank()) {
                totalInserted += view.insert(resource, maxAmount - totalInserted, transaction);
                if (totalInserted >= maxAmount) {
                    break;
                }
            }
        }

        return totalInserted;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long totalExtracted = 0;

        for (View view : storageViews) {
            totalExtracted += view.extract(resource, maxAmount - totalExtracted, transaction);
        }

        return totalExtracted;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Iterator<StorageView<FluidVariant>> iterator(TransactionContext transaction) {
        return (Iterator) storageViews.iterator();
    }

    @Override
    public long fill(int slot, IAEFluidStack stack, boolean doFill) {
        try (Transaction tx = Transaction.openOuter()) {
            long result = storageViews.get(slot).insert(stack.getFluid(), stack.getStackSize(), tx);
            if (doFill)
                tx.commit();
            return result;
        }
    }

    @Override
    public long drain(int slot, IAEFluidStack stack, boolean doDrain) {
        try (Transaction tx = Transaction.openOuter()) {
            long result = storageViews.get(slot).extract(stack.getFluid(), stack.getStackSize(), tx);
            if (doDrain)
                tx.commit();
            return result;
        }
    }

    public void writeToNBT(final CompoundTag data, final String name) {
        final CompoundTag c = new CompoundTag();
        this.writeToNBT(c);
        data.put(name, c);
    }

    private void writeToNBT(final CompoundTag target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final CompoundTag c = new CompoundTag();

                if (this.fluids[x] != EMPTY_AE_FLUIDSTACK) {
                    this.fluids[x].writeToNBT(c);
                }

                target.put("#" + x, c);
            } catch (final Exception ignored) {
            }
        }
    }

    public void readFromNBT(final CompoundTag data, final String name) {
        final CompoundTag c = data.getCompound(name);
        if (!c.isEmpty()) {
            this.readFromNBT(c);
        }
    }

    private void readFromNBT(final CompoundTag target) {
        for (int x = 0; x < this.fluids.length; x++) {
            try {
                final CompoundTag c = target.getCompound("#" + x);

                if (!c.isEmpty()) {
                    this.fluids[x] = AEFluidStack.fromNBT(c);
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    // SnapshotParticipant doesn't allow null snapshots so we can't just copy the IAEFluidStack.
    private class View extends SnapshotParticipant<ResourceAmount<FluidVariant>> implements StorageView<FluidVariant> {
        private final int slotIndex;

        private View(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (!getResource().equals(resource))
                return 0;

            long actuallyExtracted = Math.min(getAmount(), maxAmount);

            if (actuallyExtracted > 0) {
                updateSnapshots(transaction);
                fluids[slotIndex].decStackSize(actuallyExtracted);
                if (fluids[slotIndex].getStackSize() == 0) {
                    fluids[slotIndex] = EMPTY_AE_FLUIDSTACK;
                }
            }

            return actuallyExtracted;
        }

        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (isResourceBlank() || fluids[slotIndex].getFluid().equals(resource)) {
                long inserted = Math.min(maxAmount, capacity - getAmount());

                if (inserted > 0) {
                    updateSnapshots(transaction);
                    if (isResourceBlank()) {
                        fluids[slotIndex] = AEFluidStack.of(resource, inserted);
                    } else {
                        fluids[slotIndex].incStackSize(inserted);
                    }
                    return inserted;
                }
            }

            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public FluidVariant getResource() {
            return fluids[slotIndex] == EMPTY_AE_FLUIDSTACK ? FluidVariant.blank() : fluids[slotIndex].getFluid();
        }

        @Override
        public long getAmount() {
            return fluids[slotIndex] == EMPTY_AE_FLUIDSTACK ? 0 : fluids[slotIndex].getStackSize();
        }

        @Override
        public long getCapacity() {
            return capacity;
        }

        @Override
        protected ResourceAmount<FluidVariant> createSnapshot() {
            return new ResourceAmount<>(getResource(), getAmount());
        }

        @Override
        protected void readSnapshot(ResourceAmount<FluidVariant> snapshot) {
            if (snapshot.resource().isBlank() || snapshot.amount() == 0) {
                fluids[slotIndex] = EMPTY_AE_FLUIDSTACK;
            } else {
                fluids[slotIndex] = AEFluidStack.of(snapshot.resource(), snapshot.amount());
            }
        }

        @Override
        protected void onFinalCommit() {
            onContentChanged(slotIndex);
        }
    }

}
