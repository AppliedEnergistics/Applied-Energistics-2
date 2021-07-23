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

package appeng.tile.powersink;

import java.util.EnumSet;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged.PowerEventType;
import appeng.capabilities.Capabilities;
import appeng.helpers.ForgeEnergyAdapter;
import appeng.tile.AEBaseInvTileEntity;

public abstract class AEBasePoweredTileEntity extends AEBaseInvTileEntity
        implements IAEPowerStorage, IExternalPowerSink {

    // values that determine general function, are set by inheriting classes if
    // needed. These should generally remain static.
    private double internalMaxPower = 10000;
    private boolean internalPublicPowerStorage = false;
    private AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;
    // the current power buffer.
    private double internalCurrentPower = 0;
    private static final Set<Direction> ALL_SIDES = ImmutableSet.copyOf(EnumSet.allOf(Direction.class));
    private Set<Direction> internalPowerSides = ALL_SIDES;
    private final IEnergyStorage forgeEnergyAdapter;
    // Cache the optional to not continuously re-allocate it or the supplier
    private final LazyOptional<IEnergyStorage> forgeEnergyAdapterOptional;

    public AEBasePoweredTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        this.forgeEnergyAdapter = new ForgeEnergyAdapter(this);
        this.forgeEnergyAdapterOptional = LazyOptional.of(() -> forgeEnergyAdapter);
    }

    protected final Set<Direction> getPowerSides() {
        return this.internalPowerSides;
    }

    protected void setPowerSides(final Set<Direction> sides) {
        this.internalPowerSides = ImmutableSet.copyOf(sides);
    }

    @Override
    public CompoundNBT save(final CompoundNBT data) {
        super.save(data);
        data.putDouble("internalCurrentPower", this.getInternalCurrentPower());
        return data;
    }

    @Override
    public void load(BlockState blockState, final CompoundNBT data) {
        super.load(blockState, data);
        this.setInternalCurrentPower(data.getDouble("internalCurrentPower"));
    }

    @Override
    public final double getExternalPowerDemand(final PowerUnits externalUnit, final double maxPowerRequired) {
        return PowerUnits.AE.convertTo(externalUnit,
                Math.max(0.0, this.getFunnelPowerDemand(externalUnit.convertTo(PowerUnits.AE, maxPowerRequired))));
    }

    protected double getFunnelPowerDemand(final double maxRequired) {
        return this.getInternalMaxPower() - this.getInternalCurrentPower();
    }

    @Override
    public final double injectExternalPower(final PowerUnits input, final double amt, Actionable mode) {
        return PowerUnits.AE.convertTo(input, this.funnelPowerIntoStorage(input.convertTo(PowerUnits.AE, amt), mode));
    }

    protected double funnelPowerIntoStorage(final double power, final Actionable mode) {
        return this.injectAEPower(power, mode);
    }

    @Override
    public final double injectAEPower(double amt, final Actionable mode) {
        if (amt < 0.000001) {
            return 0;
        }

        final double required = this.getAEMaxPower() - this.getAECurrentPower();
        final double insertable = Math.min(required, amt);

        if (mode == Actionable.MODULATE) {
            if (this.getInternalCurrentPower() < 0.01 && insertable > 0.01) {
                this.PowerEvent(PowerEventType.PROVIDE_POWER);
            }

            this.setInternalCurrentPower(this.getInternalCurrentPower() + insertable);
        }

        return amt - insertable;
    }

    protected void PowerEvent(final PowerEventType x) {
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
    public final double extractAEPower(final double amt, final Actionable mode, final PowerMultiplier multiplier) {
        return multiplier.divide(this.extractAEPower(multiplier.multiply(amt), mode));
    }

    protected double extractAEPower(double amt, final Actionable mode) {
        if (mode == Actionable.SIMULATE) {
            if (this.getInternalCurrentPower() > amt) {
                return amt;
            }
            return this.getInternalCurrentPower();
        }

        final boolean wasFull = this.getInternalCurrentPower() >= this.getInternalMaxPower() - 0.001;
        if (wasFull && amt > 0.001) {
            this.PowerEvent(PowerEventType.REQUEST_POWER);
        }

        if (this.getInternalCurrentPower() > amt) {
            this.setInternalCurrentPower(this.getInternalCurrentPower() - amt);
            return amt;
        }

        amt = this.getInternalCurrentPower();
        this.setInternalCurrentPower(0);
        return amt;
    }

    public double getInternalCurrentPower() {
        return this.internalCurrentPower;
    }

    public void setInternalCurrentPower(final double internalCurrentPower) {
        this.internalCurrentPower = internalCurrentPower;
    }

    public double getInternalMaxPower() {
        return this.internalMaxPower;
    }

    public void setInternalMaxPower(final double internalMaxPower) {
        this.internalMaxPower = internalMaxPower;
    }

    private boolean isInternalPublicPowerStorage() {
        return this.internalPublicPowerStorage;
    }

    public void setInternalPublicPowerStorage(final boolean internalPublicPowerStorage) {
        this.internalPublicPowerStorage = internalPublicPowerStorage;
    }

    private AccessRestriction getInternalPowerFlow() {
        return this.internalPowerFlow;
    }

    public void setInternalPowerFlow(final AccessRestriction internalPowerFlow) {
        this.internalPowerFlow = internalPowerFlow;
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability) {

        if (capability == Capabilities.FORGE_ENERGY && this.getPowerSides().equals(ALL_SIDES)) {
            return (LazyOptional<T>) this.forgeEnergyAdapterOptional;
        }

        return super.getCapability(capability);

    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == Capabilities.FORGE_ENERGY && this.getPowerSides().contains(facing)) {
            return (LazyOptional<T>) this.forgeEnergyAdapterOptional;
        }
        return super.getCapability(capability, facing);
    }

}
