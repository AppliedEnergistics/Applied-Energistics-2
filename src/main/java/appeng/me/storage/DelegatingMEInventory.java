package appeng.me.storage;

import java.util.Objects;

import net.minecraft.network.chat.Component;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;

/**
 * Convenient base class for wrapping another {@link MEStorage} and forwarding <strong>all</strong> methods to the base
 * inventory.
 * <p/>
 * If no delegate is set, it will act like a {@link NullInventory}.
 */
public class DelegatingMEInventory implements MEStorage {
    private MEStorage delegate;

    public DelegatingMEInventory(MEStorage delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    protected MEStorage getDelegate() {
        return delegate;
    }

    protected void setDelegate(MEStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean isPreferredStorageFor(AEKey input, IActionSource source) {
        return getDelegate().isPreferredStorageFor(input, source);
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
    public KeyCounter getAvailableStacks() {
        return getDelegate().getAvailableStacks();
    }

    @Override
    public Component getDescription() {
        return getDelegate().getDescription();
    }
}
