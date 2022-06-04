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

package appeng.block;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.Tooltips;

public class AEBaseBlockItemChargeable extends AEBaseBlockItem implements IAEItemPowerStorage {

    public AEBaseBlockItemChargeable(Block block, Item.Properties props) {
        super(block, props);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addCheckedInformation(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getMaxEnergyCapacity();

        if (internalMaxPower > 0) {
            final CompoundTag tag = stack.getTag();
            if (tag != null) {
                internalCurrentPower = tag.getDouble("internalCurrentPower");
            }

            lines.add(
                    Tooltips.energyStorageComponent(internalCurrentPower, internalMaxPower));
        }
    }

    @Override
    public double injectAEPower(ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = this.getInternal(is);
        final double internalMaxPower = this.getAEMaxPower(is);
        final double required = internalMaxPower - internalCurrentPower;
        final double overflow = Math.max(0, Math.min(amount - required, amount));

        if (mode == Actionable.MODULATE) {
            final double toAdd = Math.min(required, amount);
            final double newPowerStored = internalCurrentPower + toAdd;

            this.setInternal(is, newPowerStored);
        }

        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = this.getInternal(is);
        final double fulfillable = Math.min(amount, internalCurrentPower);

        if (mode == Actionable.MODULATE) {
            final double newPowerStored = internalCurrentPower - fulfillable;

            this.setInternal(is, newPowerStored);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack is) {
        return this.getMaxEnergyCapacity();
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        return this.getInternal(is);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        if (getBlock() == AEBlocks.ENERGY_CELL.block()) {
            return 800d;
        } else {
            return 1600d;
        }
    }

    private double getMaxEnergyCapacity() {
        if (getBlock() == AEBlocks.ENERGY_CELL.block()) {
            return 200000;
        } else {
            return 8 * 200000;
        }
    }

    private double getInternal(ItemStack is) {
        final CompoundTag nbt = is.getOrCreateTag();
        return nbt.getDouble("internalCurrentPower");
    }

    private void setInternal(ItemStack is, double amt) {
        final CompoundTag nbt = is.getOrCreateTag();
        nbt.putDouble("internalCurrentPower", amt);
    }

}
