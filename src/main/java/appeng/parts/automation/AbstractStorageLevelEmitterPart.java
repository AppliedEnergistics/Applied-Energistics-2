package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

import appeng.api.config.*;
import appeng.api.networking.crafting.*;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/**
 * Abstract level emitter logic for storage-based level emitters (item and fluid).
 */
public abstract class AbstractStorageLevelEmitterPart<T extends IAEStack> extends AbstractLevelEmitterPart {
    private IStackWatcher stackWatcher;
    private ICraftingWatcher craftingWatcher;

    private final IMEMonitorHandlerReceiver<T> handlerReceiver = new IMEMonitorHandlerReceiver<T>() {
        @Override
        public boolean isValid(Object effectiveGrid) {
            return effectiveGrid != null && getMainNode().getGrid() == effectiveGrid;
        }

        @Override
        public void postChange(IBaseMonitor<T> monitor, Iterable<T> change, IActionSource actionSource) {
            updateReportingValue((IMEMonitor<T>) monitor);
        }

        @Override
        public void onListUpdate() {
            getMainNode().ifPresent(grid -> {
                updateReportingValue(grid.getStorageService().getInventory(getChannel()));
            });
        }
    };
    private final IStackWatcherNode stackWatcherNode = new IStackWatcherNode() {
        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            stackWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public <U extends IAEStack> void onStackChange(IItemList<U> o, IAEStack fullStack, IAEStack diffStack,
                IActionSource src, IStorageChannel<U> chan) {
            if (fullStack.equals(getConfiguredStack())
                    && getInstalledUpgrades(Upgrades.FUZZY) == 0) {
                lastReportedValue = fullStack.getStackSize();
                updateState();
            }
        }
    };
    private final ICraftingWatcherNode craftingWatcherNode = new ICraftingWatcherNode() {
        @Override
        public void updateWatcher(ICraftingWatcher newWatcher) {
            craftingWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public void onRequestChange(ICraftingService craftingGrid, IAEStack what) {
            updateState();
        }
    };

    private final ICraftingProvider craftingProvider = craftingTracker -> {
        if (getInstalledUpgrades(Upgrades.CRAFTING) > 0
                && getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES) {
            if (getConfiguredStack() != null) {
                craftingTracker.setEmitable(getConfiguredStack());
            }
        }
    };

    public AbstractStorageLevelEmitterPart(ItemStack is, boolean allowFuzzy) {
        super(is);

        getMainNode().addService(IStackWatcherNode.class, stackWatcherNode);
        getMainNode().addService(ICraftingWatcherNode.class, craftingWatcherNode);
        getMainNode().addService(ICraftingProvider.class, craftingProvider);

        this.getConfigManager().registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        if (allowFuzzy) {
            this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        }
    }

    @Nullable
    protected abstract T getConfiguredStack();

    protected abstract IStorageChannel<T> getChannel();

    @Override
    protected final int getUpgradeSlots() {
        return 1;
    }

    @Override
    public final void upgradesChanged() {
        this.configureWatchers();
    }

    @Override
    protected boolean hasDirectOutput() {
        return getInstalledUpgrades(Upgrades.CRAFTING) > 0;
    }

    @Override
    protected boolean getDirectOutput() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            return getConfiguredStack() != null && grid.getCraftingService().isRequesting(getConfiguredStack());
        }

        return false;
    }

    @Override
    protected void configureWatchers() {
        var myStack = getConfiguredStack();

        if (this.stackWatcher != null) {
            this.stackWatcher.reset();
        }

        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }

        getMainNode().ifPresent((grid, node) -> {
            grid.postEvent(new GridCraftingPatternChange(craftingProvider, node));
        });

        if (this.getInstalledUpgrades(Upgrades.CRAFTING) > 0) {
            if (this.craftingWatcher != null && myStack != null) {
                this.craftingWatcher.add(myStack);
            }
        } else {
            getMainNode().ifPresent(grid -> {
                var monitor = grid.getStorageService().getInventory(getChannel());

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0 || myStack == null) {
                    monitor.addListener(handlerReceiver, grid);
                } else {
                    monitor.removeListener(handlerReceiver);

                    if (this.stackWatcher != null) {
                        this.stackWatcher.add(myStack);
                    }
                }

                this.updateReportingValue(monitor);
            });
        }

        updateState();
    }

    private void updateReportingValue(final IMEMonitor<T> monitor) {
        var myStack = getConfiguredStack();

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (var st : monitor.getStorageList()) {
                this.lastReportedValue += st.getStackSize();
            }
        } else if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.lastReportedValue = 0;
            final FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            var fuzzyList = monitor.getStorageList().findFuzzy(myStack, fzMode);
            for (var st : fuzzyList) {
                this.lastReportedValue += st.getStackSize();
            }
        } else {
            var r = monitor.getStorageList().findPrecise(myStack);
            if (r == null) {
                this.lastReportedValue = 0;
            } else {
                this.lastReportedValue = r.getStackSize();
            }
        }

        this.updateState();
    }
}
