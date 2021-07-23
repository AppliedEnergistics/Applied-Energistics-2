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

import java.text.MessageFormat;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.definitions.AEBlocks;
import appeng.core.localization.GuiText;

import net.minecraft.world.item.Item.Properties;

public class AEBaseBlockItemChargeable extends AEBaseBlockItem implements IAEItemPowerStorage {

    public AEBaseBlockItemChargeable(Block id, net.minecraft.world.item.Item.Properties props) {
        super(id, props);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final Level world, final List<net.minecraft.network.chat.Component> lines,
                                      final net.minecraft.world.item.TooltipFlag advancedTooltips) {
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getMaxEnergyCapacity();

        if (internalMaxPower > 0) {
            final CompoundTag tag = stack.getTag();
            if (tag != null) {
                internalCurrentPower = tag.getDouble("internalCurrentPower");
            }

            final double percent = internalCurrentPower / internalMaxPower;

            lines.add(GuiText.StoredEnergy.text().copy()
                    .append(':' + MessageFormat.format(" {0,number,#} ", internalCurrentPower))
                    .append(new TranslatableComponent(PowerUnits.AE.unlocalizedName))
                    .append(" - " + MessageFormat.format("{0,number,#.##%}", percent)));
        }
    }

    @Override
    public double injectAEPower(final ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = this.getInternal(is);
        final double internalMaxPower = this.getAEMaxPower(is);
        final double required = internalMaxPower - internalCurrentPower;
        final double overflow = Math.max(0, amount - required);

        if (mode == Actionable.MODULATE) {
            final double toAdd = Math.min(required, amount);
            final double newPowerStored = internalCurrentPower + toAdd;

            this.setInternal(is, newPowerStored);
        }

        return overflow;
    }

    @Override
    public double extractAEPower(final net.minecraft.world.item.ItemStack is, double amount, Actionable mode) {
        final double internalCurrentPower = this.getInternal(is);
        final double fulfillable = Math.min(amount, internalCurrentPower);

        if (mode == Actionable.MODULATE) {
            final double newPowerStored = internalCurrentPower - fulfillable;

            this.setInternal(is, newPowerStored);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(final ItemStack is) {
        return this.getMaxEnergyCapacity();
    }

    @Override
    public double getAECurrentPower(final ItemStack is) {
        return this.getInternal(is);
    }

    @Override
    public AccessRestriction getPowerFlow(final net.minecraft.world.item.ItemStack is) {
        return AccessRestriction.WRITE;
    }

    private double getMaxEnergyCapacity() {
        if (net.minecraft.world.level.block.Block.byItem(this) == AEBlocks.ENERGY_CELL.block()) {
            return 200000;
        } else {
            return 8 * 200000;
        }
    }

    private double getInternal(final ItemStack is) {
        final CompoundTag nbt = is.getOrCreateTag();
        return nbt.getDouble("internalCurrentPower");
    }

    private void setInternal(final net.minecraft.world.item.ItemStack is, final double amt) {
        final CompoundTag nbt = is.getOrCreateTag();
        nbt.putDouble("internalCurrentPower", amt);
    }

}
