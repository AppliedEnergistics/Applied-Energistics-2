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

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnit;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * The capability provider to expose chargable items to other mods.
 */
public class PoweredItemCapabilities implements EnergyHandler {
    private final ItemAccess itemAccess;
    private final Item validItem;
    private final IAEItemPowerStorage item;

    public PoweredItemCapabilities(ItemAccess itemAccess, Item validItem, IAEItemPowerStorage item) {
        this.itemAccess = itemAccess;
        this.validItem = validItem;
        this.item = item;
    }

    private int getAmountFrom(ItemResource currentItem) {
        if (!currentItem.is(validItem)) {
            return 0;
        }
        return (int) PowerUnit.AE.convertTo(PowerUnit.FE, item.getAECurrentPower(currentItem.toStack()));
    }

    @Override
    public long getAmountAsLong() {
        var currentItem = itemAccess.getResource();
        return getAmountFrom(currentItem);
    }

    @Override
    public long getCapacityAsLong() {
        var currentItem = itemAccess.getResource();
        if (!currentItem.is(validItem)) {
            return 0;
        }
        return (int) PowerUnit.AE.convertTo(PowerUnit.FE, item.getAEMaxPower(currentItem.toStack()));
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonNegative(amount);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0) {
            return 0;
        }
        int amountPerItem = amount / accessAmount;
        if (amountPerItem == 0) {
            return 0;
        }

        ItemResource accessResource = itemAccess.getResource();
        if (!accessResource.is(validItem)) {
            return 0;
        }

        // We'll essentially perform the insertion into a copy of the stack, then convert back to the resource
        var amountAE = PowerUnit.FE.convertTo(PowerUnit.AE, amount);
        var mutableStack = accessResource.toStack();
        double overflowAE = item.injectAEPower(mutableStack, amountAE, Actionable.MODULATE);
        var insertedPerItem = (int) PowerUnit.AE.convertTo(PowerUnit.FE, amountAE - overflowAE);

        insertedPerItem = Math.min(amountPerItem, insertedPerItem);
        if (insertedPerItem > 0) {
            var filledResource = ItemResource.of(mutableStack);

            if (!filledResource.isEmpty()) {
                return insertedPerItem * itemAccess.exchange(filledResource, accessAmount, transaction);
            }
        }

        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }
}
