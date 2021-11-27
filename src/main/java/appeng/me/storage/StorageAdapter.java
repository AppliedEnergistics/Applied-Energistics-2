package appeng.me.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEKey;
import appeng.util.IVariantConversion;
import appeng.util.Platform;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Adapts platform storage to {@link MEStorage} without monitoring capabilities.
 */
public class StorageAdapter<V extends TransferVariant<?>> implements MEStorage {
    private final IVariantConversion<V> conversion;
    @Nullable
    private Storage<V> storage;

    public StorageAdapter(IVariantConversion<V> conversion, @Nullable Storage<V> storage) {
        this.conversion = conversion;
        this.storage = storage;
    }

    @Nullable
    public Storage<V> getStorage() {
        return storage;
    }

    public IVariantConversion<V> getConversion() {
        return conversion;
    }

    public void setStorage(Storage<V> newHandler) {
        this.storage = Objects.requireNonNull(newHandler);
    }

    /**
     * Called after successful inject or extract, use to schedule a cache rebuild (storage bus), or rebuild it directly
     * (interface).
     */
    protected void onInjectOrExtract() {
    }

    @Override
    public long insert(AEKey what, long amount, Actionable type, IActionSource src) {
        if (this.storage == null) {
            return 0;
        }

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
        if (this.storage == null) {
            return 0;
        }

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
}
