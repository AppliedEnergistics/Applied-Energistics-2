package appeng.me.storage;

import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

/**
 * Delegates all calls to a {@link MEStorage} returned by a supplier such that the underlying storage can change
 * dynamically.
 */
public final class SupplierStorage implements MEStorage {
    private final Supplier<@Nullable MEStorage> supplier;

    public SupplierStorage(Supplier<MEStorage> supplier) {
        this.supplier = supplier;
    }

    public boolean isPresent() {
        return supplier.get() != null;
    }

    private MEStorage getDelegate() {
        return Objects.requireNonNullElseGet(supplier.get(), NullInventory::of);
    }

    @Override
    public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
        return getDelegate().isPreferredStorageFor(what, source);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return getDelegate().insert(what, amount, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        return getDelegate().extract(what, amount, mode, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        getDelegate().getAvailableStacks(out);
    }

    @Override
    public Component getDescription() {
        return getDelegate().getDescription();
    }

    @Override
    public KeyCounter getAvailableStacks() {
        return getDelegate().getAvailableStacks();
    }
}
