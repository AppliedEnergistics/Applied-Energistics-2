package appeng.me.storage;

import java.util.function.Supplier;

import appeng.api.storage.ILinkStatus;
import appeng.api.storage.MEStorage;

/**
 * A delegating ME inventory that falls back to an empty inventory if the {@link ILinkStatus link status} indicates that
 * the terminal is not connected.
 */
public class LinkStatusRespectingInventory extends DelegatingMEInventory {
    private final Supplier<ILinkStatus> linkStatusSupplier;

    public LinkStatusRespectingInventory(MEStorage delegate, Supplier<ILinkStatus> linkStatusSupplier) {
        super(delegate);
        this.linkStatusSupplier = linkStatusSupplier;
    }

    @Override
    protected MEStorage getDelegate() {
        if (linkStatusSupplier.get().connected()) {
            return super.getDelegate();
        }
        return NullInventory.of();
    }
}
