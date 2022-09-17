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

package appeng.items;


import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;


public abstract class AEBaseItem extends Item {

    public AEBaseItem() {
        this.setNoRepair();
    }

    @Override
    public String toString() {
        String regName = this.getRegistryName() != null ? this.getRegistryName().getResourcePath() : "unregistered";
        return this.getClass().getSimpleName() + "[" + regName + "]";
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("unchecked")
    public final void addInformation(final ItemStack stack, final World world, final List lines, final ITooltipFlag advancedTooltips) {
        this.addCheckedInformation(stack, world, lines, advancedTooltips);
    }

    @Override
    public final void getSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        if (this.isInCreativeTab(creativeTab)) {
            this.getCheckedSubItems(creativeTab, itemStacks);
        }
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    protected void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addInformation(stack, world, lines, advancedTooltips);
    }

    protected void getCheckedSubItems(final CreativeTabs creativeTab, final NonNullList<ItemStack> itemStacks) {
        super.getSubItems(creativeTab, itemStacks);
    }

}
