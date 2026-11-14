package appeng.me.storage;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;

/**
 * Combines several ME storages that each handle only a given key-space.
 */
public class CompositeStorage implements MEStorage, ITickingMonitor {
    private final InventoryCache cache;

    private Map<AEKeyType, MEStorage> storages;

    private boolean forceCacheRebuild = true;

    public CompositeStorage(Map<AEKeyType, MEStorage> storages) {
        this.storages = storages;
        this.cache = new InventoryCache();
    }

    public void setStorages(Map<AEKeyType, MEStorage> storages) {
        this.storages = Objects.requireNonNull(storages);
        this.forceCacheRebuild = true;
    }

    @Override
    public void invalidateCache() {
        this.forceCacheRebuild = true;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        var storage = storages.get(what.getType());
        return storage != null && storage.isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        var storage = storages.get(what.getType());
        var inserted = storage != null ? storage.insert(what, amount, mode, source) : 0;

        if (inserted > 0 && mode == Actionable.MODULATE) {
            forceCacheRebuild = true;
        }

        return inserted;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var storage = storages.get(what.getType());
        if (storage == null) {
            return 0;
        }

        // Querying the external inventory is expensive, and network extractions can visit every storage bus.
        // Skip keys the external inventory did not contain when it was last scanned, unless the cache is
        // known to be outdated.
        if (!forceCacheRebuild && cache.isKnownAbsent(what)) {
            return 0;
        }

        var extracted = storage.extract(what, amount, mode, source);

        if (extracted > 0 && mode == Actionable.MODULATE) {
            forceCacheRebuild = true;
        }

        return extracted;
    }

    /**
     * Describes the types of storage represented by this object.
     */
    @Override
    public Component getDescription() {
        var types = Component.literal("");
        boolean first = true;
        for (var keyType : storages.keySet()) {
            if (!first) {
                types.append(", ");
            } else {
                first = false;
            }
            types.append(keyType.getDescription());
        }

        return GuiText.ExternalStorage.text(types);
    }

    @Override
    public TickRateModulation onTick() {
        forceCacheRebuild = false;
        boolean changed = this.cache.update();
        if (changed) {
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        if (forceCacheRebuild) {
            forceCacheRebuild = false;
            cache.update();
        }
        this.cache.getAvailableKeys(out);
    }

    @Override
    public int getEstimatedStackCount() {
        int total = 0;
        for (MEStorage value : storages.values()) {
            total += value.getEstimatedStackCount();
        }
        return total;
    }

    private class InventoryCache {
        private KeyCounter frontBuffer = new KeyCounter();
        private KeyCounter backBuffer = new KeyCounter();
        private final Set<AEKey> unextractableKeys = new HashSet<>();
        private final Set<AEKeyType> completelyScannedTypes = new HashSet<>();

        public boolean update() {
            // Flip back & front buffer and start building a new list
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;
            frontBuffer.reset();
            unextractableKeys.clear();
            completelyScannedTypes.clear();

            // Rebuild the front buffer
            for (var entry : storages.entrySet()) {
                if (entry.getValue() instanceof ExternalStorageFacade facade) {
                    if (facade.getAvailableStacks(frontBuffer, unextractableKeys)) {
                        completelyScannedTypes.add(entry.getKey());
                    }
                } else {
                    entry.getValue().getAvailableStacks(frontBuffer);
                }
            }

            boolean changed = false;
            // Diff the front-buffer against the backbuffer
            for (var entry : frontBuffer) {
                var old = backBuffer.get(entry.getKey());
                if (old == 0 || old != entry.getLongValue()) {
                    changed = true;
                }
            }
            // Account for removals
            for (var oldEntry : backBuffer) {
                if (frontBuffer.get(oldEntry.getKey()) == 0) {
                    changed = true;
                }
            }

            frontBuffer.removeZeros();

            return changed;
        }

        public void getAvailableKeys(KeyCounter out) {
            out.addAll(frontBuffer);
        }

        /**
         * @return True if the last scan covered every key of this type in the external inventory, and the given key was
         *         not among them, even as a key that could not be extracted.
         */
        public boolean isKnownAbsent(AEKey what) {
            return completelyScannedTypes.contains(what.getType())
                    && !contains(what)
                    && !unextractableKeys.contains(what);
        }

        public boolean contains(AEKey what) {
            return frontBuffer.get(what) > 0;
        }
    }
}
