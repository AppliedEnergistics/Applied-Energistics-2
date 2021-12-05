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

import appeng.helpers.externalstorage.GenericStackInv;
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
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEMonitorStorage;
import appeng.util.IVariantConversion;

public class PatternProviderReturnInventory extends GenericStackInv {
    public static int NUMBER_OF_SLOTS = 9;

    /**
     * Used to prevent injection through the handlers when we are pushing items out in the network. Otherwise, a storage
     * bus on the pattern provider could potentially void items.
     */
    private boolean injectingIntoNetwork = false;
    // TODO: how do we expose this for foreign storage channels?
    private final Participant participant = new Participant();
    private final Storage<ItemVariant> itemStorage = new GenericStorage<>(IVariantConversion.ITEM);
    private final Storage<FluidVariant> fluidStorage = new GenericStorage<>(IVariantConversion.FLUID);

    public PatternProviderReturnInventory(Runnable listener) {
        super(listener, NUMBER_OF_SLOTS);
    }

    /**
     * Return true if something could be injected into the network.
     */
    public boolean injectIntoNetwork(MEMonitorStorage storage, IActionSource src) {
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

    public Storage<ItemVariant> getItemStorage() {
        return itemStorage;
    }

    public Storage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

    private class Participant extends SnapshotParticipant<GenericStack[]> {
        @Override
        protected GenericStack[] createSnapshot() {
            var snapshot = new GenericStack[stacks.length];
            System.arraycopy(stacks, 0, snapshot, 0, stacks.length);
            return snapshot;
        }

        @Override
        protected void readSnapshot(GenericStack[] snapshot) {
            System.arraycopy(snapshot, 0, stacks, 0, stacks.length);
        }

        @Override
        protected void onFinalCommit() {
            onChange();
        }
    }

    private class GenericStorage<V extends TransferVariant<?>> implements InsertionOnlyStorage<V> {
        private final IVariantConversion<V> conversion;

        protected GenericStorage(IVariantConversion<V> conversion) {
            this.conversion = conversion;
        }

        @Override
        public long insert(V resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            if (injectingIntoNetwork) {
                // We are pushing out items already, prevent changing the stacks in unexpected ways.
                return 0;
            }
            long totalInserted = 0;

            for (int slot = 0; slot < stacks.length; ++slot) {
                if (stacks[slot] == null) {
                    long inserted = Math.min(maxAmount - totalInserted, conversion.getBaseSlotSize(resource));

                    if (inserted > 0) {
                        participant.updateSnapshots(transaction);
                        stacks[slot] = new GenericStack(conversion.getKey(resource), inserted);
                        totalInserted += inserted;
                    }
                } else if (conversion.variantMatches(stacks[slot].what(), resource)) {
                    var stack = stacks[slot];
                    long inserted = Math.min(maxAmount - totalInserted,
                            conversion.getBaseSlotSize(resource) - stack.amount());

                    if (inserted > 0) {
                        participant.updateSnapshots(transaction);
                        stacks[slot] = new GenericStack(stack.what(), stack.amount() + inserted);
                        totalInserted += inserted;
                    }
                }
            }

            return totalInserted;
        }

        @Override
        public Iterator<StorageView<V>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }
    }
}
