package appeng.util.crafting.mock;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.ItemList;

public class MockedMEMonitor implements IMEMonitor<IAEItemStack> {
    public final IItemList<IAEItemStack> list = new ItemList();

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        return list;
    }

    @Override
    public IItemList<IAEItemStack> getStorageList() {
        return list;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
        // Always allow extraction because it will always be one of the available stacks
        return request.copy(); // no clue if it should be copied or not
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public IStorageChannel<IAEItemStack> getChannel() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public AccessRestriction getAccess() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean isPrioritized(IAEItemStack input) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean canAccept(IAEItemStack input) {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public int getSlot() {
        throw new UnsupportedOperationException("mock");
    }

    @Override
    public boolean validForPass(int i) {
        throw new UnsupportedOperationException("mock");
    }
}
