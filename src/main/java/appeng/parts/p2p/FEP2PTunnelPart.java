/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2017, AlgorithmX2, All rights reserved.
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

package appeng.parts.p2p;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;

public class FEP2PTunnelPart extends CapabilityP2PTunnelPart<FEP2PTunnelPart, IEnergyStorage> {
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fe");
    private static final IEnergyStorage NULL_ENERGY_STORAGE = new NullEnergyStorage();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public FEP2PTunnelPart(ItemStack is) {
        super(is, CapabilityEnergy.ENERGY);
        inputHandler = new InputEnergyStorage();
        outputHandler = new OutputEnergyStorage();
        emptyHandler = NULL_ENERGY_STORAGE;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputEnergyStorage implements IEnergyStorage {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int total = 0;

            final int outputTunnels = FEP2PTunnelPart.this.getOutputs().size();

            if (outputTunnels == 0 | maxReceive == 0) {
                return 0;
            }

            final int amountPerOutput = maxReceive / outputTunnels;
            int overflow = amountPerOutput == 0 ? maxReceive : maxReceive % amountPerOutput;

            for (FEP2PTunnelPart target : FEP2PTunnelPart.this.getOutputs()) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    final IEnergyStorage output = capabilityGuard.get();
                    final int toSend = amountPerOutput + overflow;
                    final int received = output.receiveEnergy(toSend, simulate);

                    overflow = toSend - received;
                    total += received;
                }
            }

            if (!simulate) {
                FEP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, total);
            }

            return total;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public int getMaxEnergyStored() {
            int total = 0;

            for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                try (CapabilityGuard capabilityGuard = t.getAdjacentCapability()) {
                    total += capabilityGuard.get().getMaxEnergyStored();
                }
            }

            return total;
        }

        @Override
        public int getEnergyStored() {
            int total = 0;

            for (FEP2PTunnelPart t : FEP2PTunnelPart.this.getOutputs()) {
                try (CapabilityGuard capabilityGuard = t.getAdjacentCapability()) {
                    total += capabilityGuard.get().getEnergyStored();
                }
            }

            return total;
        }
    }

    private class OutputEnergyStorage implements IEnergyStorage {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            try (CapabilityGuard input = getInputCapability()) {
                final int total = input.get().extractEnergy(maxExtract, simulate);

                if (!simulate) {
                    FEP2PTunnelPart.this.queueTunnelDrain(PowerUnits.RF, total);
                }

                return total;
            }
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canExtract() {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().canExtract();
            }
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        @Override
        public int getMaxEnergyStored() {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().getMaxEnergyStored();
            }
        }

        @Override
        public int getEnergyStored() {
            try (CapabilityGuard input = getInputCapability()) {
                return input.get().getEnergyStored();
            }
        }
    }

    private static class NullEnergyStorage implements IEnergyStorage {

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
