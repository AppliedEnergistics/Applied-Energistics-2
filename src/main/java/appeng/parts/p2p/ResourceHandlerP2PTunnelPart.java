package appeng.parts.p2p;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.transfer.EmptyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.parts.IPartItem;
import appeng.api.stacks.AEKeyType;
import appeng.util.InsertionOnlyResourceHandler;

public abstract class ResourceHandlerP2PTunnelPart<P extends ResourceHandlerP2PTunnelPart<P, T>, T extends Resource>
        extends CapabilityP2PTunnelPart<P, ResourceHandler<T>> {

    private final T emptyResource;
    private final AEKeyType keyType;

    public ResourceHandlerP2PTunnelPart(IPartItem<?> partItem,
            BlockCapability<ResourceHandler<T>, Direction> capability,
            T emptyResource,
            AEKeyType keyType) {
        super(partItem, capability);
        this.inputHandler = new InputStorage();
        this.outputHandler = new OutputStorage();
        this.emptyHandler = EmptyResourceHandler.instance();
        this.emptyResource = emptyResource;
        this.keyType = keyType;
    }

    private class InputStorage extends InsertionOnlyResourceHandler<T> {
        public InputStorage() {
            super(emptyResource);
        }

        @Override
        public int insert(T resource, int maxAmount, TransactionContext tx) {
            TransferPreconditions.checkNonEmptyNonNegative(resource, maxAmount);
            int total = 0;

            var outputs = getOutputs();
            final int outputTunnels = outputs.size();
            final int amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            final int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : outputs) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    final ResourceHandler<T> output = capabilityGuard.get();
                    final int toSend = amountPerOutput + overflow;

                    final int received = output.insert(resource, toSend, tx);

                    overflow = toSend - received;
                    total += received;
                }
            }

            deductTransportCost(total, keyType, tx);
            return total;
        }
    }

    private class OutputStorage implements ResourceHandler<T> {
        @Override
        public int extract(T resource, int maxAmount, TransactionContext tx) {
            try (CapabilityGuard input = getInputCapability()) {
                int extracted = input.get().extract(resource, maxAmount, tx);
                deductTransportCost(extracted, keyType, tx);
                return extracted;
            }
        }

        @Override
        public int extract(int index, T resource, int amount, TransactionContext tx) {
            try (CapabilityGuard input = getInputCapability()) {
                int extracted = input.get().extract(index, resource, amount, tx);
                deductTransportCost(extracted, keyType, tx);
                return extracted;
            }
        }

        @Override
        public int size() {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().size();
            }
        }

        @Override
        public T getResource(int index) {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().getResource(index);
            }
        }

        @Override
        public long getAmountAsLong(int index) {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().getAmountAsLong(index);
            }
        }

        @Override
        public long getCapacityAsLong(int index, T resource) {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().getCapacityAsLong(index, resource);
            }
        }

        @Override
        public boolean isValid(int index, T resource) {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().isValid(index, resource);
            }
        }

        @Override
        public int insert(int index, T resource, int amount, TransactionContext transaction) {
            return 0; // This only allows extraction
        }

        @Override
        public int insert(T resource, int amount, TransactionContext transaction) {
            return 0; // This only allows extraction
        }
    }
}
