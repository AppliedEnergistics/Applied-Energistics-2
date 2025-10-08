/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.helpers;

import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.blockentity.powersink.IExternalPowerSink;
import appeng.me.energy.StoredEnergyAmount;

/**
 * Adapts an {@link IExternalPowerSink} to {@link EnergyHandler}, for accepting FE.
 */
public class ForgeEnergyAdapter extends SnapshotJournal<Double> implements EnergyHandler {

    private final IExternalPowerSink sink;
    private double buffer = 0;

    public ForgeEnergyAdapter(IExternalPowerSink sink) {
        this.sink = sink;
    }

    @Override
    protected Double createSnapshot() {
        return buffer;
    }

    @Override
    protected void revertToSnapshot(Double snapshot) {
        buffer = snapshot;
    }

    @Override
    protected void onRootCommit(Double originalState) {
        buffer = sink.injectExternalPower(PowerUnit.FE, buffer, Actionable.MODULATE);
        if (buffer < StoredEnergyAmount.MIN_AMOUNT) {
            // Prevent a small leftover amount from blocking further energy insertions.
            buffer = 0;
        }
    }

    @Override
    public int insert(int maxAmount, TransactionContext tx) {
        TransferPreconditions.checkNonNegative(maxAmount);
        // Always schedule a push into the network at outer commit.
        updateSnapshots(tx);

        if (buffer == 0) {
            // Cap at the remaining capacity...
            maxAmount = (int) Math
                    .floor(Math.min(maxAmount, this.sink.getExternalPowerDemand(PowerUnit.FE, maxAmount)));
            buffer = maxAmount;
            return maxAmount;
        }

        return 0;
    }

    @Override
    public final long getAmountAsLong() {
        return (long) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.sink.getAECurrentPower()));
    }

    @Override
    public final long getCapacityAsLong() {
        return (long) Math.floor(PowerUnit.AE.convertTo(PowerUnit.FE, this.sink.getAEMaxPower()));
    }

    @Override
    public int extract(int maxAmount, TransactionContext transaction) {
        return 0;
    }
}
