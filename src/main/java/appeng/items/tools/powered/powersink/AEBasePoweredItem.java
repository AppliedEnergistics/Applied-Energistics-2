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

import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.localization.Tooltips;
import appeng.items.AEBaseItem;

public abstract class AEBasePoweredItem extends AEBaseItem implements IAEItemPowerStorage {
    // Any energy capacity below this threshold will be clamped to zero
    private static final double MIN_POWER = 0.0001;
    private final DoubleSupplier powerCapacity;

    public AEBasePoweredItem(DoubleSupplier powerCapacity, Properties props) {
        super(props);
        this.powerCapacity = powerCapacity;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> lines,
            TooltipFlag advancedTooltips) {
        var storedEnergy = getAECurrentPower(stack);
        var energyCapacity = getAEMaxPower(stack);
        lines.accept(Tooltips.energyStorageComponent(storedEnergy, energyCapacity));
    }

    @Override
    public void addToMainCreativeTab(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        super.addToMainCreativeTab(parameters, output);

        var charged = new ItemStack(this, 1);
        injectAEPower(charged, getAEMaxPower(charged), Actionable.MODULATE);
        output.accept(charged);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        double filled = getAECurrentPower(stack) / getAEMaxPower(stack);
        return Mth.clamp((int) Math.round(filled * 13), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // This is the standard green color of full durability bars
        return Mth.hsvToRgb(1 / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        final double maxStorage = this.getAEMaxPower(stack);
        final double currentStorage = this.getAECurrentPower(stack);
        final double required = maxStorage - currentStorage;
        final double overflow = Math.max(0, Math.min(amount - required, amount));

        if (mode == Actionable.MODULATE) {
            var toAdd = Math.min(amount, required);
            setAECurrentPower(stack, currentStorage + toAdd);
        }

        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        final double currentStorage = this.getAECurrentPower(stack);
        final double fulfillable = Math.min(amount, currentStorage);

        if (mode == Actionable.MODULATE) {
            setAECurrentPower(stack, currentStorage - fulfillable);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        // Allow per-item-stack overrides of the maximum power storage
        return stack.getOrDefault(AEComponents.ENERGY_CAPACITY, powerCapacity.getAsDouble());
    }

    /**
     * Allows items to change the max power of their stacks without incurring heavy deserialization cost every time it's
     * accessed.
     */
    protected final void setAEMaxPower(ItemStack stack, double maxPower) {
        var defaultCapacity = powerCapacity.getAsDouble();
        if (Math.abs(maxPower - defaultCapacity) < MIN_POWER) {
            stack.remove(AEComponents.ENERGY_CAPACITY);
        } else {
            stack.set(AEComponents.ENERGY_CAPACITY, maxPower);
        }

        // Clamp current power to be within bounds
        var currentPower = getAECurrentPower(stack);
        if (currentPower > maxPower) {
            setAECurrentPower(stack, maxPower);
        }
    }

    /**
     * Changes the maximum power of the chargeable item based on a multiplier for the configured default power. The
     * multiplier is clamped to [1,100]
     */
    protected final void setAEMaxPowerMultiplier(ItemStack stack, int multiplier) {
        multiplier = Mth.clamp(multiplier, 1, 100);
        setAEMaxPower(stack, multiplier * powerCapacity.getAsDouble());
    }

    /**
     * Clears any custom maximum power from the given stack.
     */
    protected final void resetAEMaxPower(ItemStack stack) {
        setAEMaxPower(stack, powerCapacity.getAsDouble());
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        return is.getOrDefault(AEComponents.STORED_ENERGY, 0.0);
    }

    protected final void setAECurrentPower(ItemStack stack, double power) {
        if (power < MIN_POWER) {
            stack.remove(AEComponents.STORED_ENERGY);
        } else {
            stack.set(AEComponents.STORED_ENERGY, power);
        }
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

}
