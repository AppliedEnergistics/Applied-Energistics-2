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

package appeng.items.tools.powered.powersink;


import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.capabilities.Capabilities;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;


/**
 * The capability provider to expose chargable items to other mods.
 */
class PoweredItemCapabilities implements ICapabilityProvider, IEnergyStorage {

    private final ItemStack is;

    private final IAEItemPowerStorage item;

    private final Object teslaAdapter;

    PoweredItemCapabilities(ItemStack is, IAEItemPowerStorage item) {
        this.is = is;
        this.item = item;
        if (Capabilities.TESLA_CONSUMER != null || Capabilities.TESLA_HOLDER != null) {
            this.teslaAdapter = new TeslaAdapter();
        } else {
            this.teslaAdapter = null;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == Capabilities.FORGE_ENERGY || capability == Capabilities.TESLA_CONSUMER || capability == Capabilities.TESLA_HOLDER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == Capabilities.FORGE_ENERGY) {
            return (T) this;
        } else if (capability == Capabilities.TESLA_CONSUMER || capability == Capabilities.TESLA_HOLDER) {
            return (T) this.teslaAdapter;
        }
        return null;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        final double convertedOffer = PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive);
        final double overflow = this.item.injectAEPower(this.is, convertedOffer, simulate ? Actionable.SIMULATE : Actionable.MODULATE);

        return maxReceive - (int) PowerUnits.AE.convertTo(PowerUnits.RF, overflow);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) PowerUnits.AE.convertTo(PowerUnits.RF, this.item.getAECurrentPower(this.is));
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) PowerUnits.AE.convertTo(PowerUnits.RF, this.item.getAEMaxPower(this.is));
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    private class TeslaAdapter implements ITeslaConsumer, ITeslaHolder {

        @Override
        public long givePower(long power, boolean simulated) {
            return PoweredItemCapabilities.this.receiveEnergy((int) power, simulated);
        }

        @Override
        public long getStoredPower() {
            return PoweredItemCapabilities.this.getEnergyStored();
        }

        @Override
        public long getCapacity() {
            return PoweredItemCapabilities.this.getMaxEnergyStored();
        }
    }
}
