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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnits;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkPowerStorage.PowerEventType;
import appeng.capabilities.Capabilities;
import appeng.integration.Integrations;
import appeng.integration.abstraction.IC2PowerSink;
import appeng.tile.AEBaseInvTile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.EnumSet;


public abstract class AEBasePoweredTile extends AEBaseInvTile implements IAEPowerStorage, IExternalPowerSink {

    // values that determine general function, are set by inheriting classes if
    // needed. These should generally remain static.
    private double internalMaxPower = 10000;
    private boolean internalPublicPowerStorage = false;
    private AccessRestriction internalPowerFlow = AccessRestriction.READ_WRITE;
    // the current power buffer.
    private double internalCurrentPower = 0;
    private EnumSet<EnumFacing> internalPowerSides = EnumSet.allOf(EnumFacing.class);
    private final IEnergyStorage forgeEnergyAdapter;
    private Object teslaEnergyAdapter;
    private GTCEEnergyAdapter gtceEnergyAdapter = null;
    private final IC2PowerSink ic2Sink;

    public AEBasePoweredTile() {
        this.forgeEnergyAdapter = new ForgeEnergyAdapter(this);
        if (Capabilities.TESLA_CONSUMER != null) {
            this.teslaEnergyAdapter = new TeslaEnergyAdapter(this);
        }
        if (Capabilities.GTCE_ENERGY != null) {
            this.gtceEnergyAdapter = new GTCEEnergyAdapter(this);
        }

        this.ic2Sink = Integrations.ic2().createPowerSink(this, this);
        this.ic2Sink.setValidFaces(this.internalPowerSides);
    }

    protected EnumSet<EnumFacing> getPowerSides() {
        return this.internalPowerSides.clone();
    }

    protected void setPowerSides(final EnumSet<EnumFacing> sides) {
        this.internalPowerSides = sides;
        this.ic2Sink.setValidFaces(sides);
        // trigger re-calc!
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setDouble("internalCurrentPower", this.getInternalCurrentPower());
        return data;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.setInternalCurrentPower(data.getDouble("internalCurrentPower"));
    }

    @Override
    public final double getExternalPowerDemand(final PowerUnits externalUnit, final double maxPowerRequired) {
        return PowerUnits.AE.convertTo(externalUnit, Math.max(0.0, this.getFunnelPowerDemand(externalUnit.convertTo(PowerUnits.AE, maxPowerRequired))));
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

    @Override
    public void onReady() {
        super.onReady();

        this.ic2Sink.onLoad();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        this.ic2Sink.onChunkUnload();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        this.ic2Sink.invalidate();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == Capabilities.FORGE_ENERGY) {
            if (this.getPowerSides().contains(facing)) {
                return true;
            }
        } else if (capability == Capabilities.TESLA_CONSUMER) {
            if (this.getPowerSides().contains(facing)) {
                return true;
            }
        } else if (capability == Capabilities.GTCE_ENERGY) {
            if (this.getPowerSides().contains(facing)) {
                return true;
            }
        }

        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == Capabilities.FORGE_ENERGY) {
            if (this.getPowerSides().contains(facing)) {
                return (T) this.forgeEnergyAdapter;
            }
        } else if (capability == Capabilities.TESLA_CONSUMER) {
            if (this.getPowerSides().contains(facing)) {
                return (T) this.teslaEnergyAdapter;
            }
        } else if (capability == Capabilities.GTCE_ENERGY) {
            if (this.getPowerSides().contains(facing)) {
                return (T) this.gtceEnergyAdapter;
            }
        }

        return super.getCapability(capability, facing);
    }

}
