package appeng.me.storage;

import java.util.Map;
import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.core.localization.GuiText;
import appeng.helpers.iface.PatternProviderLogic;

/**
 * Combines several ME storages that each handle only a given key-space.
 */
public class CompositeStorage implements MEStorage, ITickingMonitor {
    private final InventoryCache cache;

    private Map<AEKeyType, MEStorage> storages;

    private boolean forceCacheRebuild;

    public CompositeStorage(Map<AEKeyType, MEStorage> storages) {
        this.storages = storages;
        this.cache = new InventoryCache();
    }

    public void setStorages(Map<AEKeyType, MEStorage> storages) {
        this.storages = Objects.requireNonNull(storages);
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

        // If a pattern provider in blocking mode successfully inserts its input into this storage bus,
        // and we do not currently report that item as being in this storage, we have to refresh
        // the cache the next time it's queried. Otherwise, the pattern provider would not correctly
        // detect the pattern input to already be stored here.
        if (inserted > 0
                && !forceCacheRebuild
                && isPatternProviderInBlockingMode(source)
                && !cache.contains(what)) {
            forceCacheRebuild = true;
        }

        return inserted;
    }

    private static boolean isPatternProviderInBlockingMode(IActionSource source) {
        if (source.machine().isEmpty()) {
            return false;
        }
        var machineNode = source.machine().get().getActionableNode();
        if (machineNode != null && machineNode.getOwner() instanceof PatternProviderLogic logic) {
            return logic.isBlocking();
        }
        return false;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        var storage = storages.get(what.getType());
        return storage != null ? storage.extract(what, amount, mode, source) : 0;
    }

    /**
     * Describes the types of storage represented by this object.
     */
    @Override
    public Component getDescription() {
        var types = new TextComponent("");
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

    private class InventoryCache {
        private KeyCounter frontBuffer = new KeyCounter();
        private KeyCounter backBuffer = new KeyCounter();

        public boolean update() {
            // Flip back & front buffer and start building a new list
            var tmp = backBuffer;
            backBuffer = frontBuffer;
            frontBuffer = tmp;
            frontBuffer.reset();

            // Rebuild the front buffer
            for (var storage : storages.values()) {
                storage.getAvailableStacks(frontBuffer);
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

        public boolean contains(AEKey what) {
            return frontBuffer.get(what) > 0;
        }
    }
}
