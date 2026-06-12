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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
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
        var storedEnergy = getAECurrentPower(stack);
        var maxEnergy = getAEMaxPower(stack);
        lines.accept(Tooltips.energyStorageComponent(storedEnergy, maxEnergy));
    }

    @Override
    public double injectAEPower(ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = getAECurrentPower(is);
        final double internalMaxPower = this.getAEMaxPower(is);
        final double required = internalMaxPower - internalCurrentPower;
        final double overflow = Math.max(0, Math.min(amount - required, amount));

        if (mode == Actionable.MODULATE) {
            final double toAdd = Math.min(required, amount);
            final double newPowerStored = internalCurrentPower + toAdd;

            setAECurrentPower(is, newPowerStored);
        }

        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = getAECurrentPower(is);
        final double fulfillable = Math.min(amount, internalCurrentPower);

        if (mode == Actionable.MODULATE) {
            final double newPowerStored = internalCurrentPower - fulfillable;

            setAECurrentPower(is, newPowerStored);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack is) {
        return this.getMaxEnergyCapacity();
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        return is.getOrDefault(AEComponents.STORED_ENERGY, 0.0);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return ((EnergyCellBlock) getBlock()).getChargeRate();
    }

    private double getMaxEnergyCapacity() {
        return ((EnergyCellBlock) getBlock()).getMaxPower();
    }

    private void setAECurrentPower(ItemStack is, double amt) {
        if (amt < 0.00001) {
            is.remove(AEComponents.STORED_ENERGY);
        } else {
            is.set(AEComponents.STORED_ENERGY, amt);
        }
    }

}
