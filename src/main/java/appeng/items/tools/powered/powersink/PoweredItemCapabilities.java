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

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import team.reborn.energy.api.EnergyStorage;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * The capability provider to expose chargable items to other mods.
 */
public class PoweredItemCapabilities implements EnergyStorage {

    private final ContainerItemContext context;

    public PoweredItemCapabilities(ContainerItemContext context) {
        this.context = context;
    }

    @Override
    public long insert(long maxReceive, TransactionContext transaction) {
        var current = context.getItemVariant();
        if (current.getItem() instanceof IAEItemPowerStorage powerStorage) {
            var is = current.toStack();

            var convertedOffer = PowerUnits.TR.convertTo(PowerUnits.AE, maxReceive);
            var overflow = powerStorage.injectAEPower(is, convertedOffer, Actionable.MODULATE);
            long inserted = maxReceive - (long) PowerUnits.AE.convertTo(PowerUnits.TR, overflow);

            if (context.exchange(ItemVariant.of(is), 1, transaction) == 1) {
                return inserted;
            }
        }

        return 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmount() {
        var current = context.getItemVariant();
        if (current.getItem() instanceof IAEItemPowerStorage powerStorage) {
            return (long) PowerUnits.AE.convertTo(PowerUnits.TR, powerStorage.getAECurrentPower(current.toStack()));
        }

        return 0;
    }

    @Override
    public long getCapacity() {
        var current = context.getItemVariant();
        if (current.getItem() instanceof IAEItemPowerStorage powerStorage) {
            return (int) PowerUnits.AE.convertTo(PowerUnits.TR, powerStorage.getAEMaxPower(current.toStack()));
        }
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

}
