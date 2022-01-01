/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

import java.util.List;
import java.util.function.DoubleSupplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;

public abstract class AEBasePoweredItem extends AEBaseItem implements IAEItemPowerStorage {
    private static final String CURRENT_POWER_NBT_KEY = "internalCurrentPower";
    private final DoubleSupplier powerCapacity;

    public AEBasePoweredItem(DoubleSupplier powerCapacity, Item.Properties props) {
        super(props);
        this.powerCapacity = powerCapacity;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        final CompoundTag tag = stack.getTag();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getAEMaxPower(stack);

        if (tag != null) {
            internalCurrentPower = tag.getDouble(CURRENT_POWER_NBT_KEY);
        }

        lines.add(
                Tooltips.energyStorageComponent(internalCurrentPower, internalMaxPower));

    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        super.fillItemCategory(group, items);

        if (this.allowdedIn(group)) {
            final ItemStack charged = new ItemStack(this, 1);
            injectAEPower(charged, getAEMaxPower(charged), Actionable.MODULATE);
            items.add(charged);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        double filled = getAECurrentPower(itemStack) / getAEMaxPower(itemStack);
        return Mth.clamp((int) (filled * 13), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        // This is the standard green color of full durability bars
        return Mth.hsvToRgb(1 / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public double injectAEPower(ItemStack is, double amount, Actionable mode) {
        final double maxStorage = this.getAEMaxPower(is);
        final double currentStorage = this.getAECurrentPower(is);
        final double required = maxStorage - currentStorage;
        final double overflow = amount - required;

        if (mode == Actionable.MODULATE) {
            final CompoundTag data = is.getOrCreateTag();
            final double toAdd = Math.min(amount, required);

            data.putDouble(CURRENT_POWER_NBT_KEY, currentStorage + toAdd);
        }

        return Math.max(0, overflow);
    }

    @Override
    public double extractAEPower(ItemStack is, double amount, Actionable mode) {
        final double currentStorage = this.getAECurrentPower(is);
        final double fulfillable = Math.min(amount, currentStorage);

        if (mode == Actionable.MODULATE) {
            final CompoundTag data = is.getOrCreateTag();

            data.putDouble(CURRENT_POWER_NBT_KEY, currentStorage - fulfillable);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack is) {
        return this.powerCapacity.getAsDouble();
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        final CompoundTag data = is.getOrCreateTag();

        return data.getDouble(CURRENT_POWER_NBT_KEY);
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

}
