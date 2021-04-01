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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;

public abstract class AEBasePoweredItem extends AEBaseItem implements IAEItemPowerStorage {
    private static final String CURRENT_POWER_NBT_KEY = "internalCurrentPower";
    private static final String MAX_POWER_NBT_KEY = "internalMaxPower";
    private final DoubleSupplier powerCapacity;

    public AEBasePoweredItem(final DoubleSupplier powerCapacity, Properties props) {
        super(props);
        // FIXME this.setFull3D();

        this.powerCapacity = powerCapacity;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void addInformation(final ItemStack stack, final World world, final List<ITextComponent> lines,
            final ITooltipFlag advancedTooltips) {
        final CompoundNBT tag = stack.getTag();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getAEMaxPower(stack);

        if (tag != null) {
            internalCurrentPower = tag.getDouble(CURRENT_POWER_NBT_KEY);
        }

        final double percent = internalCurrentPower / internalMaxPower;

        lines.add(GuiText.StoredEnergy.text().copyRaw()
                .appendString(':' + MessageFormat.format(" {0,number,#} ", internalCurrentPower))
                .append(new TranslationTextComponent(PowerUnits.AE.unlocalizedName))
                .appendString(" - " + MessageFormat.format(" {0,number,#.##%} ", percent)));
    }

    @Override
    public boolean isDamageable() {
        return true;
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);

        if (this.isInGroup(group)) {
            final ItemStack charged = new ItemStack(this, 1);
            final CompoundNBT tag = charged.getOrCreateTag();
            tag.putDouble(CURRENT_POWER_NBT_KEY, this.getAEMaxPower(charged));
            tag.putDouble(MAX_POWER_NBT_KEY, this.getAEMaxPower(charged));

            items.add(charged);
        }
    }

// FIXME FABRIC Currently no direct equivalent
// FIXME FABRIC    @Override
// FIXME FABRIC    public double getDurabilityForDisplay(final ItemStack is) {
// FIXME FABRIC        return 1 - this.getAECurrentPower(is) / this.getAEMaxPower(is);
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    @Override
// FIXME FABRIC    public boolean isDamaged(final ItemStack stack) {
// FIXME FABRIC        return true;
// FIXME FABRIC    }
// FIXME FABRIC
// FIXME FABRIC    @Override
// FIXME FABRIC    public void setDamage(final ItemStack stack, final int damage) {
// FIXME FABRIC
// FIXME FABRIC    }

    @Override
    public double injectAEPower(final ItemStack is, final double amount, Actionable mode) {
        final double maxStorage = this.getAEMaxPower(is);
        final double currentStorage = this.getAECurrentPower(is);
        final double required = maxStorage - currentStorage;
        final double overflow = amount - required;

        if (mode == Actionable.MODULATE) {
            final CompoundNBT data = is.getOrCreateTag();
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
            final CompoundNBT data = is.getOrCreateTag();

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
        final CompoundNBT data = is.getOrCreateTag();

        return data.getDouble(CURRENT_POWER_NBT_KEY);
    }

    @Override
    public AccessRestriction getPowerFlow(final ItemStack is) {
        return AccessRestriction.WRITE;
    }

// FIXME FABRIC    @Override
// FIXME FABRIC    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
// FIXME FABRIC        return new PoweredItemCapabilities(stack, this);
// FIXME FABRIC    }
}
