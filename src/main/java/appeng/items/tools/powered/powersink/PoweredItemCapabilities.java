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

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * The capability provider to expose chargable items to other mods.
 */
public class PoweredItemCapabilities implements IEnergyStorage {

    private final ItemStack is;

    private final IAEItemPowerStorage item;

    public PoweredItemCapabilities(ItemStack is, IAEItemPowerStorage item) {
        this.is = is;
        this.item = item;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        final double convertedOffer = PowerUnits.FE.convertTo(PowerUnits.AE, maxReceive);
        final double overflow = this.item.injectAEPower(this.is, convertedOffer,
                simulate ? Actionable.SIMULATE : Actionable.MODULATE);

        return maxReceive - (int) PowerUnits.AE.convertTo(PowerUnits.FE, overflow);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return (int) PowerUnits.AE.convertTo(PowerUnits.FE, this.item.getAECurrentPower(this.is));
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) PowerUnits.AE.convertTo(PowerUnits.FE, this.item.getAEMaxPower(this.is));
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

}
