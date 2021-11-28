package appeng.me.storage;

import java.util.Objects;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.core.localization.GuiText;
import appeng.util.IVariantConversion;
import appeng.util.Platform;

/**
 * Adapts platform storage to {@link MEStorage} without monitoring capabilities.
 */
public class StorageAdapter<V extends TransferVariant<?>> implements MEStorage {
    /**
     * Clamp reported values to avoid overflows when amounts get too close to Long.MAX_VALUE.
     */
    private static final long MAX_REPORTED_AMOUNT = 1L << 42;
    private final IVariantConversion<V> conversion;
    private boolean extractableOnly;
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

    public void setExtractableOnly(boolean extractableOnly) {
        this.extractableOnly = extractableOnly;
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

    @Override
    public void getAvailableStacks(KeyCounter out) {
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
                    out.add(conversion.getKey(view.getResource()), amount);
                }
            }
        }
    }

    @Override
    public Component getDescription() {
        return GuiText.ExternalStorage.text(conversion.getKeySpace().getDescription());
    }
}
