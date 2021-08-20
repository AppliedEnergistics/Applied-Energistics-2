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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.crafting.execution.GenericStackHelper;

public class PatternProviderReturnInventory extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 5;

    // TODO: how do we expose this for foreign storage channels?
    private final Participant participant = new Participant();
    private final ItemStorage itemStorage = new ItemStorage();
    private final FluidStorage fluidStorage = new FluidStorage();

    public PatternProviderReturnInventory(Listener listener) {
        super(listener, NUMBER_OF_SLOTS);
    }

    /**
     * Return true if something could be injected into the network.
     */
    public boolean injectIntoNetwork(IStorageMonitorable network, IActionSource src) {
        var didSomething = false;

        for (int i = 0; i < stacks.length; ++i) {
            if (stacks[i] != null) {
                long sizeBefore = stacks[i].getStackSize();
                stacks[i] = GenericStackHelper.injectMonitorable(network, stacks[i], Actionable.MODULATE, src);

                if (IAEStack.getStackSizeOrZero(stacks[i]) != sizeBefore) {
                    didSomething = true;
                }
            }
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

    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    public FluidStorage getFluidStorage() {
        return fluidStorage;
    }

    private class Participant extends SnapshotParticipant<IAEStack[]> {
        @Override
        protected IAEStack[] createSnapshot() {
            var snapshot = new IAEStack[stacks.length];
            for (int i = 0; i < stacks.length; ++i) {
                snapshot[i] = stacks[i] == null ? null : IAEStack.copy(stacks[i]);
            }
            return snapshot;
        }

        @Override
        protected void readSnapshot(IAEStack[] snapshot) {
            System.arraycopy(snapshot, 0, stacks, 0, stacks.length);
        }

        @Override
        protected void onFinalCommit() {
            onChange();
        }
    }

    private abstract class AbstractStorage<V extends TransferVariant<?>, T extends IAEStack>
            implements InsertionOnlyStorage<V> {
        private final IStorageChannel<T> channel;

        protected AbstractStorage(IStorageChannel<T> channel) {
            this.channel = channel;
        }

        @Override
        public long insert(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            long totalInserted = 0;

            for (int slot = 0; slot < stacks.length; ++slot) {
                if (stacks[slot] == null) {
                    long inserted = Math.min(maxAmount - totalInserted, getCapacity());

                    if (inserted > 0) {
                        participant.updateSnapshots(transaction);
                        stacks[slot] = createStack(resource, inserted);
                        totalInserted += inserted;
                    }
                } else if (stacks[slot].getChannel() == channel) {
                    var fs = stacks[slot].cast(channel);

                    if (variantMatches(fs, resource)) {
                        long inserted = Math.min(maxAmount - totalInserted, getCapacity() - fs.getStackSize());

                        if (inserted > 0) {
                            participant.updateSnapshots(transaction);
                            fs.incStackSize(inserted);
                            totalInserted += inserted;
                        }
                    }
                }
            }

            return totalInserted;
        }

        @Override
        public Iterator<StorageView<V>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }

        public abstract long getCapacity();

        public abstract boolean variantMatches(T stack, V variant);

        public abstract T createStack(V variant, long amount);
    }

    private class FluidStorage extends AbstractStorage<FluidVariant, IAEFluidStack> {
        protected FluidStorage() {
            super(StorageChannels.fluids());
        }

        public long getCapacity() {
            return 4 * FluidConstants.BUCKET;
        }

        @Override
        public boolean variantMatches(IAEFluidStack stack, FluidVariant variant) {
            return stack.getFluid().equals(variant);
        }

        @Override
        public IAEFluidStack createStack(FluidVariant variant, long amount) {
            return IAEFluidStack.of(variant, amount);
        }
    }

    private class ItemStorage extends AbstractStorage<ItemVariant, IAEItemStack> {
        protected ItemStorage() {
            super(StorageChannels.items());
        }

        @Override
        public long getCapacity() {
            return 64;
        }

        @Override
        public boolean variantMatches(IAEItemStack stack, ItemVariant variant) {
            return variant.matches(stack.getDefinition());
        }

        @Override
        public IAEItemStack createStack(ItemVariant variant, long amount) {
            return IAEItemStack.of(variant.toStack((int) amount));
        }
    }
}
