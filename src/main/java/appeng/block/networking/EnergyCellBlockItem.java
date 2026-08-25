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

package appeng.block.networking;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import appeng.api.config.AccessRestriction;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseBlockItem;
import appeng.core.localization.Tooltips;

public class EnergyCellBlockItem extends AEBaseBlockItem implements IAEItemPowerStorage {

    public EnergyCellBlockItem(Block block, Properties props) {
        super(block, props);
    }

    @Override
    public void addCheckedInformation(ItemStack stack, TooltipContext context, Consumer<Component> lines,
            TooltipFlag tooltipFlags) {
        var access = ItemAccess.forStack(stack);
        var storedEnergy = getAECurrentPower(access);
        var maxEnergy = getAEMaxPower(access);
        lines.accept(Tooltips.energyStorageComponent(storedEnergy, maxEnergy));
    }

    @Override
    public double injectAEPower(ItemAccess access, double amount, TransactionContext tr) {
        final double internalCurrentPower = getAECurrentPower(access);
        final double internalMaxPower = getAEMaxPower(access);
        final double required = internalMaxPower - internalCurrentPower;
        final double overflow = Mth.clamp(amount - required, 0, amount);

        final double toAdd = Math.min(required, amount);
        final double newPowerStored = internalCurrentPower + toAdd;
        setAECurrentPower(access, newPowerStored, tr);

        return overflow;
    }

    @Override
    public double extractAEPower(ItemAccess access, double amount, TransactionContext tr) {
        final double internalCurrentPower = getAECurrentPower(access);
        final double fulfillable = Math.min(amount, internalCurrentPower);

        final double newPowerStored = internalCurrentPower - fulfillable;
        setAECurrentPower(access, newPowerStored, tr);

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemAccess access) {
        return this.getMaxEnergyCapacity();
    }

    @Override
    public double getAECurrentPower(ItemAccess access) {
        return access.getResource().getOrDefault(AEComponents.STORED_ENERGY, 0.0);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemAccess access) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemAccess access) {
        return ((EnergyCellBlock) getBlock()).getChargeRate();
    }

    private double getMaxEnergyCapacity() {
        return ((EnergyCellBlock) getBlock()).getMaxPower();
    }

    private void setAECurrentPower(ItemAccess access, double amt, TransactionContext tr) {
        if (amt < 0.00001) {
            access.exchange(access.getResource().without(AEComponents.STORED_ENERGY), access.getAmount(), tr);
        } else {
            access.exchange(access.getResource().with(AEComponents.STORED_ENERGY, amt), access.getAmount(), tr);
        }
    }

}
