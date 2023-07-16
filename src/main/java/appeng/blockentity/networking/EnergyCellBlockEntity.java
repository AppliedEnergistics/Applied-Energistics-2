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

import org.jetbrains.annotations.Nullable;

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
import appeng.hooks.ticking.TickHandler;
import appeng.me.energy.StoredEnergyAmount;
import appeng.util.SettingsFrom;

public class EnergyCellBlockEntity extends AENetworkBlockEntity implements IAEPowerStorage {

    private final StoredEnergyAmount stored;
    private byte currentDisplayLevel;
    private boolean neighborChangePending;

    public EnergyCellBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.getMainNode()
                .setIdlePowerUsage(0)
                .addService(IAEPowerStorage.class, this);

        var cellBlock = (EnergyCellBlock) getBlockState().getBlock();
        this.stored = new StoredEnergyAmount(0, cellBlock.getMaxPower(), this::emitPowerEvent);
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.COVERED;
    }

    @Override
    public void onReady() {
        super.onReady();
        final int value = this.level.getBlockState(this.worldPosition).getValue(EnergyCellBlock.ENERGY_STORAGE);
        this.currentDisplayLevel = (byte) value;
        this.updateStateForPowerLevel();
    }

    /**
     * Given a fill factor, return the storage level used for the state of the block. This is also used for determining
     * the item model.
     */
    public static int getStorageLevelFromFillFactor(double fillFactor) {
        return (int) Math.floor(EnergyCellBlock.MAX_FULLNESS * Mth.clamp(fillFactor + 0.01, 0, 1));
    }

    /**
     * Updates the block state of this cell so that it matches the power level.
     */
    private void updateStateForPowerLevel() {
        if (this.isRemoved()) {
            return;
        }

        int storageLevel = getStorageLevelFromFillFactor(this.stored.getAmount() / this.stored.getMaximum());

        if (this.currentDisplayLevel != storageLevel) {
            this.currentDisplayLevel = (byte) storageLevel;
            this.level.setBlockAndUpdate(this.worldPosition,
                    this.level.getBlockState(this.worldPosition).setValue(EnergyCellBlock.ENERGY_STORAGE,
                            storageLevel));
        }
    }

    private void onAmountChanged() {
        // Delay the notification since this happens while energy is being extracted/injected from the grid
        // During injection/extraction, the grid should not be modified
        if (!neighborChangePending) {
            neighborChangePending = true;
            TickHandler.instance().addCallable(level, () -> {
                if (!isRemoved() && neighborChangePending) {
                    neighborChangePending = false;
                    updateStateForPowerLevel();
                    setChanged();
                }
            });
        }
    }

    @Override
    public void saveAdditional(CompoundTag data) {
        super.saveAdditional(data);
        data.putDouble("internalCurrentPower", this.stored.getAmount());
    }

    @Override
    public void loadTag(CompoundTag data) {
        super.loadTag(data);
        this.stored.setStored(data.getDouble("internalCurrentPower"));
    }

    @Override
    public void importSettings(SettingsFrom mode, CompoundTag input, @Nullable Player player) {
        super.importSettings(mode, input, player);

        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            this.stored.setStored(input.getDouble("internalCurrentPower"));
        }
    }

    @Override
    public void exportSettings(SettingsFrom from, CompoundTag data, @Nullable Player player) {
        super.exportSettings(from, data, player);

        if (from == SettingsFrom.DISMANTLE_ITEM && this.stored.getAmount() > 0) {
            data.putDouble("internalCurrentPower", this.stored.getAmount());
            data.putDouble("internalMaxPower", this.stored.getMaximum()); // used for tool tip.
        }
    }

    @Override
    public final double injectAEPower(double amt, Actionable mode) {
        var inserted = this.stored.insert(amt, mode == Actionable.MODULATE);
        if (mode == Actionable.MODULATE && inserted > 0) {
            this.onAmountChanged();
        }
        return amt - inserted;
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        return pm.divide(this.extractAEPower(pm.multiply(amt), mode));
    }

    private double extractAEPower(double amt, Actionable mode) {
        var extracted = this.stored.extract(amt, mode == Actionable.MODULATE);
        if (mode == Actionable.MODULATE && extracted > 0) {
            this.onAmountChanged();
        }
        return extracted;
    }

    @Override
    public double getAEMaxPower() {
        return this.stored.getMaximum();
    }

    @Override
    public double getAECurrentPower() {
        return this.stored.getAmount();
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
    public int getPriority() {
        return ((EnergyCellBlock) getBlockState().getBlock()).getPriority();
    }

    private void emitPowerEvent(PowerEventType type) {
        getMainNode().ifPresent(
                grid -> grid.postEvent(new GridPowerStorageStateChanged(this, type)));
    }
}
