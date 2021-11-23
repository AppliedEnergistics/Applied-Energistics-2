package appeng.me.storage;

import java.util.*;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.util.IVariantConversion;
import appeng.util.Platform;

public abstract class StorageAdapter<V extends TransferVariant<?>>
        implements MEMonitorStorage, ITickingMonitor, IHandlerAdapter<Storage<V>> {
    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;
    private final Map<IMEMonitorListener, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final IVariantConversion<V> conversion;
    private Storage<V> storage;
    private final InventoryCache cache;

    public StorageAdapter(IVariantConversion<V> conversion, Storage<V> storage, boolean showExtractableOnly) {
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
    public long insert(AEKey what, long amount, Actionable type, IActionSource src) {
        var variant = conversion.getVariant(what);
        if (variant.isBlank()) {
            return 0;
        }

        try (var tx = Platform.openOrJoinTx()) {
            var inserted = this.storage.insert(variant, amount, tx);

            if (inserted > 0 && type == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            return inserted;
        }
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var variant = conversion.getVariant(what);
        if (variant.isBlank()) {
            return 0;
        }

        try (var tx = Platform.openOrJoinTx()) {
            var extracted = this.storage.extract(variant, amount, tx);

            if (extracted > 0 && mode == Actionable.MODULATE) {
                tx.commit();
                this.onInjectOrExtract();
            }

            return extracted;
        }
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
                    frontBuffer.add(conversion.getKey(view.getResource()), amount);
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
