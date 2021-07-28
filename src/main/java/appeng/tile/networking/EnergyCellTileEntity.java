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

package appeng.tile.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
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
import appeng.tile.grid.AENetworkTileEntity;
import appeng.util.SettingsFrom;

public class EnergyCellTileEntity extends AENetworkTileEntity implements IAEPowerStorage {

    private static final double MAX_STORED = 200000.0;

    private double internalCurrentPower = 0.0;
    private double internalMaxPower = MAX_STORED;

    private byte currentMeta = -1;

    public EnergyCellTileEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState blockState) {
        super(tileEntityTypeIn, pos, blockState);
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
        this.changePowerLevel();
    }

    /**
     * Given a fill factor, return the storage level (0-7) used for the state of the block. This is also used for
     * determining the item model.
     */
    public static int getStorageLevelFromFillFactor(double fillFactor) {
        byte factor = (byte) (8.0 * fillFactor);

        return Mth.clamp(factor, 0, EnergyCellBlock.MAX_FULLNESS);
    }

    private void changePowerLevel() {
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

    @Override
    public CompoundTag save(final CompoundTag data) {
        super.save(data);
        data.putDouble("internalCurrentPower", this.internalCurrentPower);
        return data;
    }

    @Override
    public void load(final CompoundTag data) {
        super.load(data);
        this.internalCurrentPower = data.getDouble("internalCurrentPower");
    }

    @Override
    public boolean canBeRotated() {
        return false;
    }

    @Override
    public void uploadSettings(final SettingsFrom from, final CompoundTag compound) {
        if (from == SettingsFrom.DISMANTLE_ITEM) {
            this.internalCurrentPower = compound.getDouble("internalCurrentPower");
        }
    }

    @Override
    public CompoundTag downloadSettings(final SettingsFrom from) {
        if (from == SettingsFrom.DISMANTLE_ITEM) {
            final CompoundTag tag = new CompoundTag();
            tag.putDouble("internalCurrentPower", this.internalCurrentPower);
            tag.putDouble("internalMaxPower", this.getInternalMaxPower()); // used for tool tip.
            return tag;
        }
        return null;
    }

    @Override
    public final double injectAEPower(double amt, final Actionable mode) {
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

            this.changePowerLevel();
            return amt;
        }

        this.changePowerLevel();
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
    public final double extractAEPower(final double amt, final Actionable mode, final PowerMultiplier pm) {
        return pm.divide(this.extractAEPower(pm.multiply(amt), mode));
    }

    @Override
    public int getPriority() {
        return 200;
    }

    private double extractAEPower(double amt, final Actionable mode) {
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

            this.changePowerLevel();
            return amt;
        }

        amt = this.internalCurrentPower;
        this.internalCurrentPower = 0;

        this.changePowerLevel();
        return amt;
    }

    private double getInternalMaxPower() {
        return this.internalMaxPower;
    }

    void setInternalMaxPower(final double internalMaxPower) {
        this.internalMaxPower = internalMaxPower;
    }

}
