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

/**
 * Combines several ME storages that each handle only a given key-space.
 */
public class CompositeStorage implements MEStorage, ITickingMonitor {
    private final InventoryCache cache;

    private Map<AEKeyType, MEStorage> storages;

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
        return storage != null ? storage.insert(what, amount, mode, source) : 0;
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
        boolean changed = this.cache.update();
        if (changed) {
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
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
    }
}
