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

import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.energy.IEnergyWatcher;
import appeng.api.networking.energy.IEnergyWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.EnergyLevelEmitterMenu;
import appeng.parts.PartModel;
import appeng.util.Platform;

public class EnergyLevelEmitterPart extends UpgradeablePart implements IEnergyWatcherHost {

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

    private boolean prevState = false;

    private long lastReportedValue = 0;
    private long reportingValue = 0;

    private IEnergyWatcher energyWatcher;

    public EnergyLevelEmitterPart(final ItemStack is) {
        super(is);

        getMainNode()
                .addService(IEnergyWatcherHost.class, this);

        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.NO);
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(final long v) {
        this.reportingValue = v;
        this.configureWatchers();
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

        final boolean flipState = this.getConfigManager()
                .getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState ? this.reportingValue >= this.lastReportedValue + 1
                : this.reportingValue < this.lastReportedValue + 1;
    }

    @Override
    protected int calculateClientFlags() {
        return super.calculateClientFlags() | (this.prevState ? FLAG_ON : 0);
    }

    // update the system...
    private void configureWatchers() {
        if (this.energyWatcher != null) {
            this.energyWatcher.reset();
        }

        if (this.energyWatcher != null) {
            this.energyWatcher.add(this.reportingValue);
        }

        getMainNode().ifPresent(grid -> {
            // update to power...
            this.lastReportedValue = (long) grid.getEnergyService().getStoredPower();
            this.updateState();
        });

        return;
    }

    private void updateReportingValue(final IMEMonitor<IAEItemStack> monitor) {
        this.updateState();
    }

    @Override
    public void updateWatcher(final IEnergyWatcher newWatcher) {
        this.energyWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onThresholdPass(final IEnergyService energyGrid) {
        this.lastReportedValue = (long) energyGrid.getStoredPower();
        this.updateState();
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
            MenuOpener.open(EnergyLevelEmitterMenu.TYPE, player, MenuLocator.forPart(this));
        }
        return true;
    }

    @Override
    public void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.configureWatchers();
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
    }

    @Override
    public void writeToNBT(final CompoundTag data) {
        super.writeToNBT(data);
        data.putLong("lastReportedValue", this.lastReportedValue);
        data.putLong("reportingValue", this.reportingValue);
        data.putBoolean("prevState", this.prevState);
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
