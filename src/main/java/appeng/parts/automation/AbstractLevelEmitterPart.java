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

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Setting;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNodeListener;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.util.Platform;
import appeng.util.SettingsFrom;

public abstract class AbstractLevelEmitterPart extends UpgradeablePart {
    private static final int FLAG_ON = 4;

    private boolean prevState;
    protected long lastReportedValue;
    private long reportingValue;

    public AbstractLevelEmitterPart(IPartItem<?> partItem) {
        super(partItem);

        // Level emitters do not require a channel to function
        getMainNode().setFlags();

        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    protected abstract void configureWatchers();

    protected abstract boolean hasDirectOutput();

    protected abstract boolean getDirectOutput();

    @Override
    protected final void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (getMainNode().hasGridBooted()) {
            updateState();
        }
    }

    protected void updateState() {
        final boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            final BlockEntity te = this.getHost().getBlockEntity();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos());
            Platform.notifyBlocksOfNeighbors(te.getLevel(), te.getBlockPos().relative(this.getSide()));
        }
    }

    public final long getReportingValue() {
        return this.reportingValue;
    }

    public final void setReportingValue(long v) {
        this.reportingValue = v;
        this.updateState();
    }

    @Override
    public final int isProvidingStrongPower() {
        return prevState ? 15 : 0;
    }

    @Override
    public final int isProvidingWeakPower() {
        return prevState ? 15 : 0;
    }

    @Override
    public final void animateTick(Level level, BlockPos pos, Random r) {
        if (this.isLevelEmitterOn()) {
            final Direction d = this.getSide();

            final double d0 = d.getStepX() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d1 = d.getStepY() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d2 = d.getStepZ() * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

            level.addParticle(DustParticleOptions.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1,
                    0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    protected boolean isLevelEmitterOn() {
        if (isClientSide()) {
            return (this.getClientFlags() & FLAG_ON) == FLAG_ON;
        }

        if (!this.getMainNode().isActive()) {
            return false;
        }

        if (hasDirectOutput()) {
            return getDirectOutput();
        }

        final boolean flipState = this.getConfigManager()
                .getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState ? this.reportingValue >= this.lastReportedValue + 1
                : this.reportingValue < this.lastReportedValue + 1;
    }

    @Override
    protected final int calculateClientFlags() {
        return super.calculateClientFlags() | (prevState ? FLAG_ON : 0);
    }

    @Override
    public final boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putLong("lastReportedValue", this.lastReportedValue);
        data.putLong("reportingValue", this.reportingValue);
        data.putBoolean("prevState", this.prevState);
    }

    @Override
    public final float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public final void getBoxes(IPartCollisionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    public final AECableType getDesiredConnectionType() {
        return AECableType.SMART; // TODO: This was previously in an unused method getCableConnectionType intended for
        // external connections, check if this visual change is desirable
    }

    @Override
    public final void onSettingChanged(IConfigManager manager, Setting<?> setting) {
        this.configureWatchers();
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);
        reportingValue = input.getLong("reportingValue");
    }

    @Override
    public void exportSettings(SettingsFrom mode, CompoundTag output) {
        super.exportSettings(mode, output);

        if (mode == SettingsFrom.MEMORY_CARD) {
            output.putLong("reportingValue", reportingValue);
        }
    }
}
