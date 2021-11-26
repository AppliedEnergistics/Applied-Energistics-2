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

import java.util.List;
import java.util.Set;

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
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherNode;
import appeng.api.storage.AEKeySpace;
import appeng.api.storage.IMEMonitorListener;
import appeng.api.storage.MEMonitorStorage;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
import appeng.helpers.IConfigInvHost;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.util.ConfigInventory;

/**
 * Abstract level emitter logic for storage-based level emitters (item and fluid).
 */
public abstract class AbstractStorageLevelEmitterPart extends AbstractLevelEmitterPart
        implements IConfigInvHost, ICraftingProvider {
    private final ConfigInventory config = ConfigInventory.configTypes(1, this::configureWatchers);
    private IStackWatcher stackWatcher;
    private ICraftingWatcher craftingWatcher;

    private final IMEMonitorListener handlerReceiver = new IMEMonitorListener() {
        @Override
        public boolean isValid(Object effectiveGrid) {
            return effectiveGrid != null && getMainNode().getGrid() == effectiveGrid;
        }

        @Override
        public void postChange(MEMonitorStorage monitor, Iterable<AEKey> change, IActionSource actionSource) {
            updateReportingValue(monitor);
        }

        @Override
        public void onListUpdate() {
            getMainNode().ifPresent(grid -> {
                updateReportingValue(grid.getStorageService().getInventory());
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
        public void onStackChange(AEKey what, long amount) {
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

    public AbstractStorageLevelEmitterPart(ItemStack is, boolean allowFuzzy) {
        super(is);

        getMainNode().addService(IStackWatcherNode.class, stackWatcherNode);
        getMainNode().addService(ICraftingWatcherNode.class, craftingWatcherNode);
        getMainNode().addService(ICraftingProvider.class, this);

        this.getConfigManager().registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        if (allowFuzzy) {
            this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        }
    }

    @Nullable
    private AEKey getConfiguredKey() {
        return config.getKey(0);
    }

    protected abstract AEKeySpace getChannel();

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
    public List<IPatternDetails> getAvailablePatterns() {
        return List.of();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        return false;
    }

    @Override
    public boolean isBusy() {
        return true;
    }

    @Override
    public Set<AEKey> getEmitableItems() {
        if (getInstalledUpgrades(Upgrades.CRAFTING) > 0
                && getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES) {
            if (getConfiguredKey() != null) {
                return Set.of(getConfiguredKey());
            }
        }
        return Set.of();
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

        ICraftingProvider.requestUpdate(getMainNode());

        if (this.getInstalledUpgrades(Upgrades.CRAFTING) > 0) {
            if (this.craftingWatcher != null && myStack != null) {
                this.craftingWatcher.add(myStack);
            }
        } else {
            getMainNode().ifPresent(grid -> {
                var monitor = grid.getStorageService().getInventory();

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

    private void updateReportingValue(MEMonitorStorage monitor) {
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
        if (!isClientSide()) {
            MenuOpener.open(getMenuType(), player, MenuLocator.forPart(this));
        }
        return true;
    }

    public ConfigInventory getConfig() {
        return config;
    }
}
