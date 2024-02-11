package appeng.me.storage;

import java.util.function.Supplier;

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
public class SupplierStorage implements MEStorage {
    private final Supplier<MEStorage> supplier;

    public SupplierStorage(Supplier<MEStorage> supplier) {
        this.supplier = supplier;
    }

    private MEStorage getDelegate() {
        return supplier.get();
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

    public static void checkPreconditions(AEKey what, long amount, Actionable mode, IActionSource source) {
        MEStorage.checkPreconditions(what, amount, mode, source);
    }
}
