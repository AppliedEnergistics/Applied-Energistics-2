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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.IGrid;
import appeng.api.networking.IStackWatcher;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.storage.IStorageWatcherNode;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.helpers.IConfigInvHost;
import appeng.hooks.ticking.TickHandler;
import appeng.items.parts.PartModels;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.StorageLevelEmitterMenu;
import appeng.menu.locator.MenuLocators;
import appeng.parts.PartModel;
import appeng.util.ConfigInventory;

/**
 * Abstract level emitter logic for storage-based level emitters (item and fluid).
 */
public class StorageLevelEmitterPart extends AbstractLevelEmitterPart
        implements IConfigInvHost, ICraftingProvider {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/level_emitter_status_has_channel");
    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    private final ConfigInventory config = ConfigInventory.configTypes(1, this::configureWatchers);
    private IStackWatcher storageWatcher;
    private IStackWatcher craftingWatcher;
    private long lastUpdateTick = -1;

    private final IStorageWatcherNode stackWatcherNode = new IStorageWatcherNode() {
        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            storageWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public void onStackChange(AEKey what, long amount) {
            if (what.equals(getConfiguredKey()) && !isUpgradedWith(AEItems.FUZZY_CARD)) {
                lastReportedValue = amount;
                updateState();
            } else { // either fuzzy upgrade or null filter
                long currentTick = TickHandler.instance().getCurrentTick();
                if (currentTick != lastUpdateTick) {
                    lastUpdateTick = currentTick;
                    updateReportingValue(getGridNode().getGrid());
                }
            }
        }
    };
    private final ICraftingWatcherNode craftingWatcherNode = new ICraftingWatcherNode() {
        @Override
        public void updateWatcher(IStackWatcher newWatcher) {
            craftingWatcher = newWatcher;
            configureWatchers();
        }

        @Override
        public void onRequestChange(ICraftingService craftingGrid, AEKey what) {
            updateState();
        }
    };

    public StorageLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);

        getMainNode().addService(IStorageWatcherNode.class, stackWatcherNode);
        getMainNode().addService(ICraftingWatcherNode.class, craftingWatcherNode);
        getMainNode().addService(ICraftingProvider.class, this);

        getConfigManager().registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
        getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Nullable
    private AEKey getConfiguredKey() {
        return config.getKey(0);
    }

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
        return isUpgradedWith(AEItems.CRAFTING_CARD);
    }

    @Override
    protected boolean getDirectOutput() {
        var grid = getMainNode().getGrid();
        if (grid != null) {
            if (getConfiguredKey() != null) {
                return grid.getCraftingService().isRequesting(getConfiguredKey());
            } else {
                return grid.getCraftingService().isRequestingAny();
            }
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
        if (isUpgradedWith(AEItems.CRAFTING_CARD)
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

        if (this.storageWatcher != null) {
            this.storageWatcher.reset();
        }

        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }

        ICraftingProvider.requestUpdate(getMainNode());

        if (isUpgradedWith(AEItems.CRAFTING_CARD)) {
            if (this.craftingWatcher != null) {
                if (myStack == null) {
                    this.craftingWatcher.setWatchAll(true);
                } else {
                    this.craftingWatcher.add(myStack);
                }
            }
        } else {
            if (this.storageWatcher != null) {
                if (isUpgradedWith(AEItems.FUZZY_CARD) || myStack == null) {
                    this.storageWatcher.setWatchAll(true);
                } else {
                    this.storageWatcher.add(myStack);
                }
            }

            getMainNode().ifPresent(this::updateReportingValue);
        }

        updateState();
    }

    private void updateReportingValue(IGrid grid) {
        var stacks = grid.getStorageService().getCachedInventory();
        var myStack = getConfiguredKey();

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (var st : stacks) {
                this.lastReportedValue += st.getLongValue();
            }
        } else if (isUpgradedWith(AEItems.FUZZY_CARD)) {
            this.lastReportedValue = 0;
            final FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            var fuzzyList = stacks.findFuzzy(myStack, fzMode);
            for (var st : fuzzyList) {
                this.lastReportedValue += st.getLongValue();
            }
        } else {
            this.lastReportedValue = stacks.get(myStack);
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
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if (!isClientSide()) {
            MenuOpener.open(StorageLevelEmitterMenu.TYPE, player, MenuLocators.forPart(this));
        }
        return true;
    }

    public ConfigInventory getConfig() {
        return config;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
        }
    }
}
