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

package appeng.blockentity.networking;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.api.util.AECableType;
import appeng.block.networking.EnergyCellBlock;
import appeng.blockentity.grid.AENetworkBlockEntity;
import appeng.util.SettingsFrom;

public class EnergyCellBlockEntity extends AENetworkBlockEntity implements IAEPowerStorage {

    private static final double MAX_STORED = 200000.0;

    private double internalCurrentPower = 0.0;
    private double internalMaxPower = MAX_STORED;

    private byte currentMeta = -1;

    public EnergyCellBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .addService(IAEPowerStorage.class, this);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void onReady() {
        super.onReady();
        final int value = this.level.getBlockState(this.worldPosition).getValue(EnergyCellBlock.ENERGY_STORAGE);
        this.currentMeta = (byte) value;
        this.updateStateForPowerLevel();
    }

    /**
     * Given a fill factor, return the storage level (0-7) used for the state of the block. This is also used for
     * determining the item model.
     */
    public static int getStorageLevelFromFillFactor(double fillFactor) {
        byte factor = (byte) (8.0 * fillFactor);

        return Mth.clamp(factor, 0, EnergyCellBlock.MAX_FULLNESS);
    }

    /**
     * Updates the block state of this cell so that it matches the power level.
     */
    private void updateStateForPowerLevel() {
        if (this.notLoaded() || this.isRemoved()) {
            return;
        }

        int storageLevel = getStorageLevelFromFillFactor(this.internalCurrentPower / this.getInternalMaxPower());

        if (this.currentMeta != storageLevel) {
            this.currentMeta = (byte) storageLevel;
            this.level.setBlockAndUpdate(this.worldPosition,
                    this.level.getBlockState(this.worldPosition).setValue(EnergyCellBlock.ENERGY_STORAGE,
                            storageLevel));
        }
    }

    private void onAmountChanged() {
        setChanged();
        updateStateForPowerLevel();
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putDouble("internalCurrentPower", this.internalCurrentPower);
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.internalCurrentPower = data.getDouble("internalCurrentPower");
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.internalCurrentPower = input.getDouble("internalCurrentPower");
        }
    }

    @Override
    public void exportSettings(SettingsFrom from, CompoundTag data, @Nullable Player player) {
        super.exportSettings(from, data, player);

        if (from == SettingsFrom.DISMANTLE_ITEM && this.internalCurrentPower > 0.0001) {
            data.putDouble("internalCurrentPower", this.internalCurrentPower);
            data.putDouble("internalMaxPower", this.getInternalMaxPower()); // used for tool tip.
        }
    }

    @Override
    public final double injectAEPower(double amt, Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            final double fakeBattery = this.internalCurrentPower + amt;
            if (fakeBattery > this.getInternalMaxPower()) {
                return fakeBattery - this.getInternalMaxPower();
            }

            return 0;
        }

        if (this.internalCurrentPower < 0.01 && amt > 0.01) {
            this.getMainNode().getNode().getGrid()
                    .postEvent(new GridPowerStorageStateChanged(this, PowerEventType.PROVIDE_POWER));
        }

        this.internalCurrentPower += amt;
        if (this.internalCurrentPower > this.getInternalMaxPower()) {
            amt = this.internalCurrentPower - this.getInternalMaxPower();
            this.internalCurrentPower = this.getInternalMaxPower();

            this.onAmountChanged();
            return amt;
        }

        this.onAmountChanged();
        return 0;
    }

    @Override
    public double getAEMaxPower() {
        return this.getInternalMaxPower();
    }

    @Override
    public double getAECurrentPower() {
        return this.internalCurrentPower;
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        return pm.divide(this.extractAEPower(pm.multiply(amt), mode));
    }

    @Override
    public int getPriority() {
        return 200;
    }

    private double extractAEPower(double amt, Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            if (this.internalCurrentPower > amt) {
                return amt;
            }
            return this.internalCurrentPower;
        }

        final boolean wasFull = this.internalCurrentPower >= this.getInternalMaxPower() - 0.001;

        if (wasFull && amt > 0.001) {
            getMainNode().ifPresent(
                    grid -> grid.postEvent(new GridPowerStorageStateChanged(this, PowerEventType.REQUEST_POWER)));
        }

        if (this.internalCurrentPower > amt) {
            this.internalCurrentPower -= amt;

            this.onAmountChanged();
            return amt;
        }

        amt = this.internalCurrentPower;
        this.internalCurrentPower = 0;

        this.onAmountChanged();
        return amt;
    }

    private double getInternalMaxPower() {
        return this.internalMaxPower;
    }

    void setInternalMaxPower(double internalMaxPower) {
        this.internalMaxPower = internalMaxPower;
    }

}
