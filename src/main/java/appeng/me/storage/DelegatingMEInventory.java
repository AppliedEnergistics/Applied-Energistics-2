package appeng.me.storage;

import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;

/**
 * Convenient base class for wrapping another {@link appeng.api.storage.IMEInventory} and forwarding
 * <strong>all</strong> methods to the base inventory.
 * <p/>
 * If no delegate is set, it will act like a {@link NullInventory}.
 */
public class DelegatingMEInventory<T extends AEKey> implements IMEInventory<T> {
    private IMEInventory<T> delegate;

    public DelegatingMEInventory(IMEInventory<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    protected IMEInventory<T> getDelegate() {
        return delegate;
    }

    protected void setDelegate(IMEInventory<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPreferredStorageFor(T input, IActionSource source) {
        return delegate.isPreferredStorageFor(input, source);
    }

    @Override
    public long insert(T what, long amount, Actionable mode, IActionSource source) {
        return delegate.insert(what, amount, mode, source);
    }

    @Override
    public long extract(T what, long amount, Actionable mode, IActionSource source) {
        return delegate.extract(what, amount, mode, source);
    }

    @Override
    public void getAvailableStacks(KeyCounter<T> out) {
        delegate.getAvailableStacks(out);
    }

    @Override
    public KeyCounter<T> getAvailableStacks() {
        return delegate.getAvailableStacks();
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return delegate.getChannel();
    }
}
