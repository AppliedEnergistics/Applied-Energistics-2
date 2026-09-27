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

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.config.PowerUnit;
import appeng.api.parts.IPartItem;

public class FEP2PTunnelPart extends CapabilityP2PTunnelPart<FEP2PTunnelPart, EnergyHandler> {
    public FEP2PTunnelPart(IPartItem<?> partItem) {
        super(partItem, Capabilities.Energy.BLOCK);
        inputHandler = new InputEnergyHandler();
        outputHandler = new OutputEnergyHandler();
        emptyHandler = EmptyEnergyHandler.INSTANCE;
    }

    private class InputEnergyHandler implements EnergyHandler {
        @Override
        public int insert(int maxAmount, TransactionContext tx) {
            TransferPreconditions.checkNonNegative(maxAmount);
            int total = 0;

            int outputTunnels = getOutputs().size();
            int amount = maxAmount;

            if (outputTunnels == 0 || amount == 0) {
                return 0;
            }

            int amountPerOutput = amount / outputTunnels;
            int overflow = amountPerOutput == 0 ? amount : amount % amountPerOutput;

            for (var target : getOutputs()) {
                try (CapabilityGuard capabilityGuard = target.getAdjacentCapability()) {
                    var output = capabilityGuard.get();
                    int toSend = amountPerOutput + overflow;

                    int received = output.insert(toSend, tx);

                    overflow = toSend - received;
                    total += received;
                }
            }

            deductEnergyCost(total, PowerUnit.FE, tx);

            return total;
        }

        @Override
        public int extract(int maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public long getAmountAsLong() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getAmountAsLong();
                }
            }
            return tot;
        }

        @Override
        public long getCapacityAsLong() {
            long tot = 0;
            for (var output : getOutputs()) {
                try (var capabilityGuard = output.getAdjacentCapability()) {
                    tot += capabilityGuard.get().getCapacityAsLong();
                }
            }
            return tot;
        }
    }

    private class OutputEnergyHandler implements EnergyHandler {
        @Override
        public int insert(int maxAmount, TransactionContext tx) {
            return 0;
        }

        @Override
        public int extract(int maxAmount, TransactionContext tx) {
            try (var input = getInputCapability()) {
                int extracted = input.get().extract(maxAmount, tx);
                deductEnergyCost(extracted, PowerUnit.FE, tx);
                return extracted;
            }
        }

        @Override
        public long getAmountAsLong() {
            try (var input = getInputCapability()) {
                return input.get().getAmountAsLong();
            }
        }

        @Override
        public long getCapacityAsLong() {
            try (var input = getInputCapability()) {
                return input.get().getCapacityAsLong();
            }
        }
    }
}
