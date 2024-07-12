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

package appeng.blockentity.powersink;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.blockentity.AEBaseInvBlockEntity;
import appeng.helpers.ForgeEnergyAdapter;
import appeng.me.energy.StoredEnergyAmount;

public abstract class AEBasePoweredBlockEntity extends AEBaseInvBlockEntity
        implements IAEPowerStorage, IExternalPowerSink {
    private boolean internalPublicPowerStorage = false;
    private AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;
    // the current power buffer.
    private final StoredEnergyAmount stored = new StoredEnergyAmount(0, 10000, this::emitPowerStateEvent);
    private static final Set<Direction> ALL_SIDES = ImmutableSet.copyOf(EnumSet.allOf(Direction.class));
    private Set<Direction> internalPowerSides = ALL_SIDES;
    private final IEnergyStorage forgeEnergyAdapter;
    // Cache the optional to not continuously re-allocate it or the supplier

    public AEBasePoweredBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
        super(blockEntityType, pos, blockState);
        this.forgeEnergyAdapter = new ForgeEnergyAdapter(this);
    }

    protected final Set<Direction> getPowerSides() {
        return this.internalPowerSides;
    }

    protected void setPowerSides(Set<Direction> sides) {
        this.internalPowerSides = ImmutableSet.copyOf(sides);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.putDouble("internalCurrentPower", this.getInternalCurrentPower());
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        this.setInternalCurrentPower(data.getDouble("internalCurrentPower"));
    }

    @Override
    public final double getExternalPowerDemand(PowerUnit externalUnit, double maxPowerRequired) {
        return PowerUnit.AE.convertTo(externalUnit,
                Math.max(0.0, this.getFunnelPowerDemand(externalUnit.convertTo(PowerUnit.AE, maxPowerRequired))));
    }

    protected double getFunnelPowerDemand(double maxRequired) {
        return this.getInternalMaxPower() - this.getInternalCurrentPower();
    }

    @Override
    public final double injectExternalPower(PowerUnit input, double amt, Actionable mode) {
        return PowerUnit.AE.convertTo(input, this.funnelPowerIntoStorage(input.convertTo(PowerUnit.AE, amt), mode));
    }

    protected double funnelPowerIntoStorage(double power, Actionable mode) {
        return this.injectAEPower(power, mode);
    }

    @Override
    public final double injectAEPower(double amt, Actionable mode) {
        return amt - stored.insert(amt, mode == Actionable.MODULATE);
    }

    protected void emitPowerStateEvent(PowerEventType x) {
        // nothing.
    }

    @Override
    public final double getAEMaxPower() {
        return this.getInternalMaxPower();
    }

    @Override
    public final double getAECurrentPower() {
        return this.getInternalCurrentPower();
    }

    @Override
    public final boolean isAEPublicPowerStorage() {
        return this.isInternalPublicPowerStorage();
    }

    @Override
    public final AccessRestriction getPowerFlow() {
        return this.getInternalPowerFlow();
    }

    @Override
    public final double extractAEPower(double amt, Actionable mode, PowerMultiplier multiplier) {
        return multiplier.divide(this.extractAEPower(multiplier.multiply(amt), mode));
    }

    protected double extractAEPower(double amt, Actionable mode) {
        return this.stored.extract(amt, mode == Actionable.MODULATE);
    }

    public double getInternalCurrentPower() {
        return this.stored.getAmount();
    }

    public void setInternalCurrentPower(double internalCurrentPower) {
        this.stored.setStored(internalCurrentPower);
    }

    public double getInternalMaxPower() {
        return stored.getMaximum();
    }

    public void setInternalMaxPower(double internalMaxPower) {
        this.stored.setMaximum(internalMaxPower);
    }

    private boolean isInternalPublicPowerStorage() {
        return this.internalPublicPowerStorage;
    }

    public void setInternalPublicPowerStorage(boolean internalPublicPowerStorage) {
        this.internalPublicPowerStorage = internalPublicPowerStorage;
    }

    private AccessRestriction getInternalPowerFlow() {
        return this.internalPowerFlow;
    }

    public void setInternalPowerFlow(AccessRestriction internalPowerFlow) {
        this.internalPowerFlow = internalPowerFlow;
    }

    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction side) {
        if (side == null && getPowerSides().equals(ALL_SIDES)) {
            return forgeEnergyAdapter;
        } else if (side != null && getPowerSides().contains(side)) {
            return forgeEnergyAdapter;
        } else {
            return null;
        }
    }

}
