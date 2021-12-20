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

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.config.PowerUnits;
import appeng.api.parts.IPartModel;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModels;

public class FEP2PTunnelPart extends CapabilityP2PTunnelPart<FEP2PTunnelPart, EnergyStorage> {
    private static final P2PModels MODELS = new P2PModels("part/p2p/p2p_tunnel_fe");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public FEP2PTunnelPart(PartItem<?> is) {
        super(is, EnergyStorage.SIDED);
        inputHandler = new InputEnergyStorage();
        outputHandler = new OutputEnergyStorage();
        emptyHandler = EnergyStorage.EMPTY;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    private class InputEnergyStorage implements EnergyStorage {
        @Override
        public boolean supportsInsertion() {
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    if (capabilityGuard.get().supportsInsertion()) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            StoragePreconditions.notNegative(maxAmount);
            long total = 0;

            final int outputTunnels = getOutputs().size();
            final long amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            final long amountPerOutput = amount / outputTunnels;
            long overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    final EnergyStorage output = capabilityGuard.get();
                    final long toSend = amountPerOutput + overflow;

                    final long received = output.insert(toSend, transaction);

                    overflow = toSend - received;
                    total += received;
                }
            }

            queueTunnelDrain(PowerUnits.TR, total, transaction);

            return total;
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmount() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getAmount();
                }
            }
            return tot;
        }

        @Override
        public long getCapacity() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getCapacity();
                }
            }
            return tot;
        }
    }

    private class OutputEnergyStorage implements EnergyStorage {
        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean supportsExtraction() {
            try (var input = getInputCapability()) {
                return input.get().supportsExtraction();
            }
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            try (var input = getInputCapability()) {
                long extracted = input.get().extract(maxAmount, transaction);
                queueTunnelDrain(PowerUnits.TR, extracted, transaction);
                return extracted;
            }
        }

        @Override
        public long getAmount() {
            try (var input = getInputCapability()) {
                return input.get().getAmount();
            }
        }

        @Override
        public long getCapacity() {
            try (var input = getInputCapability()) {
                return input.get().getCapacity();
            }
        }
    }
}
