package appeng.parts.p2p;

import java.util.Collections;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.PowerUnits;

/**
 * Base class for P2P tunnels that work with {@code Storage<T>}.
 */
public abstract class StorageP2PTunnelPart<P extends StorageP2PTunnelPart<P, T>, T extends TransferVariant<?>>
        extends CapabilityP2PTunnelPart<P, Storage<T>> {
    public StorageP2PTunnelPart(ItemStack is, BlockApiLookup<Storage<T>, Direction> api) {
        super(is, api);
        this.inputHandler = new InputStorage();
        this.outputHandler = new OutputStorage();
        this.emptyHandler = Storage.empty();
    }

    private class InputStorage implements InsertionOnlyStorage<T> {
        @Override
        public long insert(T resource, long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            long total = 0;

            final int outputTunnels = getOutputs().size();
            final long amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            final long amountPerOutput = amount / outputTunnels;
            long overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    final Storage<T> output = capabilityGuard.get();
                    final long toSend = amountPerOutput + overflow;

                    final long received = output.insert(resource, toSend, transaction);

                    overflow = toSend - received;
                    total += received;
                }
            }

            queueTunnelDrain(PowerUnits.TR, total, transaction);

            return total;
        }

        @Override
        public Iterator<StorageView<T>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }
    }

    private class OutputStorage implements ExtractionOnlyStorage<T> {
        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            try (CapabilityGuard input = getInputCapability()) {
                long extracted = input.get().extract(resource, maxAmount, transaction);

                queueTunnelDrain(PowerUnits.TR, extracted, transaction);

                return extracted;
            }
        }

        @Override
        public Iterator<StorageView<T>> iterator(TransactionContext transaction) {
            try (CapabilityGuard input = getInputCapability()) {
                return Iterators.transform(
                        input.get().iterator(transaction),
                        PowerDrainingStorageView::new);
            }
        }
    }

    /**
     * Queues power drain when resources are extracted through this.
     */
    private class PowerDrainingStorageView implements StorageView<T> {
        private final StorageView<T> delegate;

        public PowerDrainingStorageView(StorageView<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            long extracted = delegate.extract(resource, maxAmount, transaction);

            queueTunnelDrain(PowerUnits.TR, extracted, transaction);

            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return delegate.isResourceBlank();
        }

        @Override
        public T getResource() {
            return delegate.getResource();
        }

        @Override
        public long getAmount() {
            return delegate.getAmount();
        }

        @Override
        public long getCapacity() {
            return delegate.getCapacity();
        }
    }
}
