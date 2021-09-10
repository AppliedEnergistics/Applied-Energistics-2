/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import java.util.Collection;
import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.Upgrades;
import appeng.api.config.YesNo;
import appeng.api.implementations.blockentities.ISegmentedInventory;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherNode;
import appeng.api.networking.events.GridCraftingPatternChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.blockentity.inventory.AppEngInternalAEInventory;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.ItemLevelEmitterMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;
import appeng.util.inv.InvOperation;

public class ItemLevelEmitterPart extends UpgradeablePart implements IStackWatcherHost,
        ICraftingWatcherNode, IMEMonitorHandlerReceiver<IAEItemStack>, ICraftingProvider {

    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID,
            "part/item_level_emitter_status_has_channel");

    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    private static final int FLAG_ON = 4;

    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory(this, 1);

    private boolean prevState = false;

    private long lastReportedValue = 0;
    private long reportingValue = 0;

    private IStackWatcher stackWatcher;
    private ICraftingWatcher craftingWatcher;

    public ItemLevelEmitterPart(final ItemStack is) {
        super(is);

        getMainNode()
                .addService(ICraftingWatcherNode.class, this)
                .addService(ICraftingProvider.class, this)
                .addService(IStackWatcherHost.class, this);

        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
    }

    @Override
    protected int getUpgradeSlots() {
        return 1;
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(final long v) {
        this.reportingValue = v;
        this.updateState();
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        updateState();
    }

    private void updateState() {
        final boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            final BlockEntity te = this.getHost().getBlockEntity();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos().relative(this.getSide()));
        }
    }

    // TODO: Make private again
    public boolean isLevelEmitterOn() {
        if (isRemote()) {
            return (this.getClientFlags() & FLAG_ON) == FLAG_ON;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (getInstalledUpgrades(Upgrades.CRAFTING) > 0) {
            var grid = getMainNode().getGrid();
            if (grid != null) {
                return grid.getCraftingService().isRequesting(this.config.getAEStackInSlot(0));
            }

            return this.prevState;
        }

        final boolean flipState = this.getConfigManager()
                .getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState ? this.reportingValue >= this.lastReportedValue + 1
                : this.reportingValue < this.lastReportedValue + 1;
    }

    @Override
    protected int calculateClientFlags() {
        return super.calculateClientFlags() | (this.prevState ? FLAG_ON : 0);
    }

    @Override
    public void updateWatcher(final ICraftingWatcher newWatcher) {
        this.craftingWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onRequestChange(final ICraftingService craftingGrid, final IAEStack<?> what) {
        this.updateState();
    }

    // update the system...
    private void configureWatchers() {
        final IAEItemStack myStack = this.config.getAEStackInSlot(0);

        if (this.stackWatcher != null) {
            this.stackWatcher.reset();
        }

        if (this.craftingWatcher != null) {
            this.craftingWatcher.reset();
        }

        getMainNode().ifPresent((grid, node) -> {
            grid.postEvent(new GridCraftingPatternChange(this, node));
        });

        if (this.getInstalledUpgrades(Upgrades.CRAFTING) > 0) {
            if (this.craftingWatcher != null && myStack != null) {
                this.craftingWatcher.add(myStack);
            }

            return;
        }

        getMainNode().ifPresent(grid -> {
            if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0 || myStack == null) {
                grid.getStorageService()
                        .getInventory(StorageChannels.items())
                        .addListener(this, grid);
            } else {
                grid.getStorageService()
                        .getInventory(StorageChannels.items())
                        .removeListener(this);

                if (this.stackWatcher != null) {
                    this.stackWatcher.add(myStack);
                }
            }

            this.updateReportingValue(grid.getStorageService()
                    .getInventory(StorageChannels.items()));
        });
    }

    private void updateReportingValue(final IMEMonitor<IAEItemStack> monitor) {
        final IAEItemStack myStack = this.config.getAEStackInSlot(0);

        if (myStack == null) {
            this.lastReportedValue = 0;
            for (final IAEItemStack st : monitor.getStorageList()) {
                this.lastReportedValue += st.getStackSize();
            }
        } else if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
            this.lastReportedValue = 0;
            final FuzzyMode fzMode = this.getConfigManager().getSetting(Settings.FUZZY_MODE);
            final Collection<IAEItemStack> fuzzyList = monitor.getStorageList().findFuzzy(myStack, fzMode);
            for (final IAEItemStack st : fuzzyList) {
                this.lastReportedValue += st.getStackSize();
            }
        } else {
            final IAEItemStack r = monitor.getStorageList().findPrecise(myStack);
            if (r == null) {
                this.lastReportedValue = 0;
            } else {
                this.lastReportedValue = r.getStackSize();
            }
        }

        this.updateState();
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.stackWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(final IItemList o, final IAEStack fullStack, final IAEStack diffStack,
            final IActionSource src, final IStorageChannel chan) {
        if (chan == StorageChannels.items()
                && fullStack.equals(this.config.getAEStackInSlot(0))
                && this.getInstalledUpgrades(Upgrades.FUZZY) == 0) {
            this.lastReportedValue = fullStack.getStackSize();
            this.updateState();
        }
    }

    @Override
    public boolean isValid(final Object effectiveGrid) {
        return effectiveGrid != null && this.getMainNode().getGrid() == effectiveGrid;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEItemStack> monitor, final Iterable<IAEItemStack> change,
            final IActionSource actionSource) {
        this.updateReportingValue((IMEMonitor<IAEItemStack>) monitor);
    }

    @Override
    public void onListUpdate() {
        getMainNode().ifPresent(grid -> {
            this.updateReportingValue(grid.getStorageService()
                    .getInventory(StorageChannels.items()));
        });
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    public int isProvidingStrongPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public void animateTick(final Level level, final BlockPos pos, final Random r) {
        if (this.isLevelEmitterOn()) {
            final Direction d = this.getSide();

            final double d0 = d.getStepX() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d1 = d.getStepY() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d2 = d.getStepZ() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

            level.addParticle(DustParticleOptions.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1,
                    0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public AECableType getDesiredConnectionType() {
        return AECableType.SMART; // TODO: This was previously in an unused method getCableConnectionType intended for
                                  // external connections, check if this visual change is desirable
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public boolean onPartActivate(final Player player, final InteractionHand hand, final Vec3 pos) {
        if (!isRemote()) {
            MenuOpener.open(ItemLevelEmitterMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.configureWatchers();
    }

    @Override
    public void onChangeInventory(final IItemHandler inv, final int slot, final InvOperation mc,
            final ItemStack removedStack, final ItemStack newStack) {
        if (inv == this.config) {
            this.configureWatchers();
        }

        super.onChangeInventory(inv, slot, mc, removedStack, newStack);
    }

    @Override
    public void upgradesChanged() {
        this.configureWatchers();
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void readFromNBT(final CompoundTag data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
        this.config.readFromNBT(data, "config");
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putLong("lastReportedValue", this.lastReportedValue);
        data.putLong("reportingValue", this.reportingValue);
        data.putBoolean("prevState", this.prevState);
        this.config.writeToNBT(data, "config");
    }

    @Override
    public IItemHandler getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.CONFIG)) {
            return config;
        } else {
            return super.getSubInventory(id);
        }
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        if (this.getInstalledUpgrades(Upgrades.CRAFTING) > 0
                && this.getConfigManager().getSetting(Settings.CRAFT_VIA_REDSTONE) == YesNo.YES) {
            final IAEItemStack what = this.config.getAEStackInSlot(0);
            if (what != null) {
                craftingTracker.setEmitable(what);
            }
        }
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
