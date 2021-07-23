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

import java.text.MessageFormat;
import java.util.List;
import java.util.function.DoubleSupplier;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;

import net.minecraft.world.item.Item.Properties;

public abstract class AEBasePoweredItem extends AEBaseItem implements IAEItemPowerStorage {
    private static final String CURRENT_POWER_NBT_KEY = "internalCurrentPower";
    private final DoubleSupplier powerCapacity;

    public AEBasePoweredItem(final DoubleSupplier powerCapacity, Item.Properties props) {
        super(props);
        // FIXME this.setFull3D();

        this.powerCapacity = powerCapacity;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, final Level world, final List<Component> lines,
                                final TooltipFlag advancedTooltips) {
        final CompoundTag tag = stack.getTag();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getAEMaxPower(stack);

        if (tag != null) {
            internalCurrentPower = tag.getDouble(CURRENT_POWER_NBT_KEY);
        }

        final double percent = internalCurrentPower / internalMaxPower;

        lines.add(GuiText.StoredEnergy.text().copy()
                .append(':' + MessageFormat.format(" {0,number,#} ", internalCurrentPower))
                .append(PowerUnits.AE.textComponent())
                .append(" - " + MessageFormat.format(" {0,number,#.##%} ", percent)));
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
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(final ItemStack is) {
        return 1 - this.getAECurrentPower(is) / this.getAEMaxPower(is);
    }

    @Override
    public double injectAEPower(final ItemStack is, final double amount, Actionable mode) {
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
    public double extractAEPower(final ItemStack is, final double amount, Actionable mode) {
        final double currentStorage = this.getAECurrentPower(is);
        final double fulfillable = Math.min(amount, currentStorage);

        if (mode == Actionable.MODULATE) {
            final CompoundTag data = is.getOrCreateTag();

            data.putDouble(CURRENT_POWER_NBT_KEY, currentStorage - fulfillable);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(final ItemStack is) {
        return this.powerCapacity.getAsDouble();
    }

    @Override
    public double getAECurrentPower(final ItemStack is) {
        final CompoundTag data = is.getOrCreateTag();

        return data.getDouble(CURRENT_POWER_NBT_KEY);
    }

    @Override
    public AccessRestriction getPowerFlow(final ItemStack is) {
        return AccessRestriction.WRITE;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new PoweredItemCapabilities(stack, this);
    }
}
