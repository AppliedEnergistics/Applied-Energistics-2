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


import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.text.MessageFormat;
import java.util.List;


public class AEBaseItemBlockChargeable extends AEBaseItemBlock implements IAEItemPowerStorage {

    public AEBaseItemBlockChargeable(final Block id) {
        super(id);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        final NBTTagCompound tag = stack.getTagCompound();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getMaxEnergyCapacity();

        if (internalMaxPower > 0) {
            if (tag != null) {
                internalCurrentPower = tag.getDouble("internalCurrentPower");
            }

            final double percent = internalCurrentPower / internalMaxPower;

            lines.add(GuiText.StoredEnergy.getLocal() + ':' + MessageFormat.format(" {0,number,#} ", internalCurrentPower) + Platform
                    .gui_localize(PowerUnits.AE.unlocalizedName) + " - " + MessageFormat.format(" {0,number,#.##%} ", percent));
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
    public double extractAEPower(final ItemStack is, double amount, Actionable mode) {
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
    public AccessRestriction getPowerFlow(final ItemStack is) {
        return AccessRestriction.WRITE;
    }

    private double getMaxEnergyCapacity() {
        final Block blockID = Block.getBlockFromItem(this);
        final IBlockDefinition energyCell = Api.INSTANCE.definitions().blocks().energyCell();

        return energyCell.maybeBlock().map(block ->
        {
            if (blockID == block) {
                return 200000;
            } else {
                return 8 * 200000;
            }
        }).orElse(0);
    }

    private double getInternal(final ItemStack is) {
        final NBTTagCompound nbt = Platform.openNbtData(is);
        return nbt.getDouble("internalCurrentPower");
    }

    private void setInternal(final ItemStack is, final double amt) {
        final NBTTagCompound nbt = Platform.openNbtData(is);
        nbt.setDouble("internalCurrentPower", amt);
    }
}
