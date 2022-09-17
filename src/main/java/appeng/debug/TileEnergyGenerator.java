/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.debug;


import appeng.tile.AEBaseTile;
import com.google.common.math.IntMath;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;
import java.util.EnumSet;


public class TileEnergyGenerator extends AEBaseTile implements ITickable, IEnergyStorage {
    /**
     * The base energy injected each tick.
     * Adjacent TileEnergyGenerators will increase it to pow(base, #generators).
     */
    private static final int BASE_ENERGY = 8;

    @Override
    public void update() {
        int tier = 1;
        final EnumSet<EnumFacing> validEnergyReceivers = EnumSet.noneOf(EnumFacing.class);

        for (EnumFacing facing : EnumFacing.values()) {
            final TileEntity te = this.getWorld().getTileEntity(this.getPos().offset(facing));

            if (te instanceof TileEnergyGenerator) {
                tier++;
            }

            if (te != null && te.hasCapability(CapabilityEnergy.ENERGY, facing.getOpposite())) {
                validEnergyReceivers.add(facing);
            }

        }

        final int energyToInsert = IntMath.pow(BASE_ENERGY, tier);

        for (EnumFacing facing : validEnergyReceivers) {
            final TileEntity te = this.getWorld().getTileEntity(this.getPos().offset(facing));
            final IEnergyStorage cap = te.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite());

            if (cap.canReceive()) {

                cap.receiveEnergy(energyToInsert, false);
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return maxExtract;
    }

    @Override
    public int getEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
