package appeng.me.storage;

import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackList;

/**
 * Convenient base class for wrapping another {@link appeng.api.storage.IMEInventory} and forwarding
 * <strong>all</strong> methods to the base inventory.
 * <p/>
 * If no delegate is set, it will act like a {@link NullInventory}.
 */
public class DelegatingMEInventory<T extends IAEStack> implements IMEInventory<T> {
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
    public T injectItems(T input, Actionable type, IActionSource src) {
        return delegate.injectItems(input, type, src);
    }

    @Override
    public T extractItems(T request, Actionable mode, IActionSource src) {
        return delegate.extractItems(request, mode, src);
    }

    @Override
    public IAEStackList<T> getAvailableStacks(IAEStackList<T> out) {
        return delegate.getAvailableStacks(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return delegate.getChannel();
    }
}
