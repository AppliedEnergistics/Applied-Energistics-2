package appeng.me.storage;

import java.util.*;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;
import appeng.util.IVariantConversion;
import appeng.util.Platform;

public abstract class StorageAdapter<V extends TransferVariant<?>, T extends IAEStack>
        implements IMEInventory<T>, IBaseMonitor<T>, ITickingMonitor, IHandlerAdapter<Storage<V>> {
    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;
    private final Map<IMEMonitorHandlerReceiver<T>, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final IVariantConversion<V, T> conversion;
    private Storage<V> storage;
    private final InventoryCache cache;

    public StorageAdapter(IVariantConversion<V, T> conversion, Storage<V> storage, boolean showExtractableOnly) {
        this.conversion = conversion;
        this.storage = storage;
        this.cache = new InventoryCache(showExtractableOnly);
    }

    @Override
    public void setHandler(Storage<V> newHandler) {
        this.storage = newHandler;
    }

    /**
     * Called after successful inject or extract, use to schedule a cache rebuild (storage bus), or rebuild it directly
     * (interface).
     */
    protected abstract void onInjectOrExtract();

    @Override
    public T injectItems(T input, Actionable type, IActionSource src) {

        try (var tx = Platform.openOrJoinTx()) {
            var filled = this.storage.insert(conversion.getVariant(input), input.getStackSize(), tx);

            if (filled == 0) {
                return IAEStack.copy(input);
            }

            if (type == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            if (filled >= input.getStackSize()) {
                return null;
            }

            return IAEStack.copy(input, input.getStackSize() - filled);
        }

    }

    @Override
    public T extractItems(T request, Actionable mode, IActionSource src) {

        try (var tx = Platform.openOrJoinTx()) {

            var drained = this.storage.extract(conversion.getVariant(request), request.getStackSize(), tx);

            if (drained <= 0) {
                return null;
            }

            if (mode == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            return IAEStack.copy(request, drained);
        }

    }

    @Override
    public TickRateModulation onTick() {
        List<T> changes = this.cache.update();
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public IAEStackList<T> getAvailableItems(IAEStackList<T> out) {
        return this.cache.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return conversion.getChannel();
    }

    @Override
    public void setActionSource(IActionSource source) {
        this.source = source;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<T> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<T> l) {
        this.listeners.remove(l);
    }

    private void postDifference(Iterable<T> a) {
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.listeners.entrySet()
                .iterator();
        while (i.hasNext()) {
            final Map.Entry<IMEMonitorHandlerReceiver<T>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<T> key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, a, this.source);
            } else {
                i.remove();
            }
        }
    }

    private class InventoryCache {
        private IAEStackList<T> frontBuffer = conversion.getChannel().createList();
        private IAEStackList<T> backBuffer = conversion.getChannel().createList();
        private final boolean extractableOnly;

        public InventoryCache(boolean extractableOnly) {
            this.extractableOnly = extractableOnly;
        }

        public List<T> update() {
            // Flip back & front buffer and start building a new list
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;
            frontBuffer.resetStatus();

            // Rebuild the front buffer
            try (var tx = Transaction.openOuter()) {
                for (var view : storage.iterable(tx)) {
                    if (view.isResourceBlank()) {
                        continue;
                    }

                    // Skip resources that cannot be extracted if that filter was enabled
                    if (extractableOnly) {
                        // Use an inner TX to prevent two tanks that can be extracted from only mutually exclusively
                        // from not being influenced by our extraction test here.
                        try (var innerTx = tx.openNested()) {
                            var extracted = view.extract(view.getResource(), 1, innerTx);
                            // If somehow extracting the minimal amount doesn't work, check if everything could be
                            // extracted because the tank might have a minimum (or fixed) allowed extraction amount.
                            if (extracted == 0) {
                                extracted = view.extract(view.getResource(), view.getAmount(), innerTx);
                            }
                            if (extracted == 0) {
                                // We weren't able to simulate extraction of any fluid, so skip this one
                                continue;
                            }
                        }
                    }

                    long amount = Math.min(view.getAmount(), MAX_REPORTED_AMOUNT);
                    frontBuffer.addStorage(conversion.createStack(view.getResource(), amount));
                }
            }

            // Diff the front-buffer against the backbuffer
            var changes = new ArrayList<T>();
            for (var stack : frontBuffer) {
                var old = backBuffer.findPrecise(stack);
                if (old == null) {
                    changes.add(IAEStack.copy(stack)); // new entry
                } else if (old.getStackSize() != stack.getStackSize()) {
                    var change = IAEStack.copy(stack);
                    change.decStackSize(old.getStackSize());
                    changes.add(change); // changed amount
                }
            }
            // Account for removals
            for (var oldStack : backBuffer) {
                if (frontBuffer.findPrecise(oldStack) == null) {
                    changes.add(IAEStack.copy(oldStack, -oldStack.getStackSize()));
                }
            }

            return changes;
        }

        public IAEStackList<T> getAvailableItems(IAEStackList<T> out) {
            for (var stack : frontBuffer) {
                out.addStorage(stack);
            }
            return out;
        }
    }
}
