/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.parts.automation;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.AEKey;
import appeng.helpers.IConfigInvHost;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.util.ConfigInventory;

/**
 * Abstract level emitter logic for storage-based level emitters (item and fluid).
 */
public abstract class AbstractStorageLevelEmitterPart<T extends AEKey> extends AbstractLevelEmitterPart
        implements IConfigInvHost {
    private final ConfigInventory<T> config = ConfigInventory.configTypes(getChannel(), 1, this::configureWatchers);
    private IStackWatcher stackWatcher;
    private ICraftingWatcher craftingWatcher;

    private final IMEMonitorListener<T> handlerReceiver = new IMEMonitorListener<>() {
        @Override
        public boolean isValid(Object effectiveGrid) {
            return effectiveGrid != null && getMainNode().getGrid() == effectiveGrid;
        }

        @Override
        public void postChange(IMEMonitor<T> monitor, Iterable<T> change, IActionSource actionSource) {
            updateReportingValue(monitor);
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
        public <U extends AEKey> void onStackChange(U what, long amount) {
            if (what.equals(getConfiguredKey()) && getInstalledUpgrades(Upgrades.FUZZY) == 0) {
                lastReportedValue = amount;
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
        public void onRequestChange(ICraftingService craftingGrid, AEKey what) {
            updateState();
        }
    };

    private final ICraftingProvider craftingProvider = craftingTracker -> {
        if (getInstalledUpgrades(Upgrades.CRAFTING) > 0
                && getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES) {
            if (getConfiguredKey() != null) {
                craftingTracker.setEmitable(getConfiguredKey());
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
    private T getConfiguredKey() {
        return config.getKey(0);
    }

    protected abstract IStorageChannel<T> getChannel();

    protected abstract MenuType<?> getMenuType();

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
            return getConfiguredKey() != null && grid.getCraftingService().isRequesting(getConfiguredKey());
        }

        return false;
    }

    @Override
    protected void configureWatchers() {
        var myStack = getConfiguredKey();

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

    private void updateReportingValue(IMEMonitor<T> monitor) {
        var myStack = getConfiguredKey();

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (var st : monitor.getCachedAvailableStacks()) {
                this.lastReportedValue += st.getLongValue();
            }
        } else if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.lastReportedValue = 0;
            final FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            var fuzzyList = monitor.getCachedAvailableStacks().findFuzzy(myStack, fzMode);
            for (var st : fuzzyList) {
                this.lastReportedValue += st.getLongValue();
            }
        } else {
            this.lastReportedValue = monitor.getCachedAvailableStacks().get(myStack);
        }

        this.updateState();
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        config.readFromChildTag(data, "config");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        config.writeToChildTag(data, "config");
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(getMenuType(), player, MenuLocator.forPart(this));
        }
        return true;
    }

    public ConfigInventory<T> getConfig() {
        return config;
    }
}
