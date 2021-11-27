package appeng.me.storage;

import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.util.IVariantConversion;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class MonitoringStorageAdapter<V extends TransferVariant<?>>
        extends StorageAdapter<V> implements MEMonitorStorage, ITickingMonitor, IHandlerAdapter<Storage<V>> {
    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;
    private final Map<IMEMonitorListener, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final InventoryCache cache;

    public MonitoringStorageAdapter(IVariantConversion<V> conversion, Storage<V> storage, boolean showExtractableOnly) {
        super(conversion, storage);
        this.cache = new InventoryCache(showExtractableOnly);
    }

    @Override
    public TickRateModulation onTick() {
        var changes = this.cache.update();
        if (!changes.isEmpty()) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        this.cache.getAvailableKeys(out);
    }

    @Override
    public void setActionSource(IActionSource source) {
        this.source = source;
    }

    @Override
    public void addListener(final IMEMonitorListener l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorListener l) {
        this.listeners.remove(l);
    }

    private void postDifference(Set<AEKey> a) {
        var i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            var l = i.next();
            var key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, a, this.source);
            } else {
                i.remove();
            }
        }
    }

    private class InventoryCache {
        private KeyCounter frontBuffer = new KeyCounter();
        private KeyCounter backBuffer = new KeyCounter();
        private final boolean extractableOnly;

        public InventoryCache(boolean extractableOnly) {
            this.extractableOnly = extractableOnly;
        }

        public Set<AEKey> update() {
            // Flip back & front buffer and start building a new list
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;
            frontBuffer.reset();

            // Rebuild the front buffer
            var storage = getStorage();
            if (storage != null) {
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
                        frontBuffer.add(getConversion().getKey(view.getResource()), amount);
                    }
                }
            }

            // Diff the front-buffer against the backbuffer
            var changes = new KeyCounter();
            for (var entry : frontBuffer) {
                var old = backBuffer.get(entry.getKey());
                if (old == 0 || old != entry.getLongValue()) {
                    changes.add(entry.getKey(), entry.getLongValue()); // new or changed entry
                }
            }
            // Account for removals
            for (var oldEntry : backBuffer) {
                if (frontBuffer.get(oldEntry.getKey()) == 0) {
                    changes.add(oldEntry.getKey(), -oldEntry.getLongValue());
                }
            }

            return changes.keySet();
        }

        public void getAvailableKeys(KeyCounter out) {
            out.addAll(frontBuffer);
        }
    }
}
