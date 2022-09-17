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

package appeng.core.features;


import appeng.api.util.AEColor;
import appeng.api.util.AEColoredItemDefinition;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;


public final class ColoredItemDefinition implements AEColoredItemDefinition {

    private final ItemStackSrc[] colors = new ItemStackSrc[17];

    public void add(final AEColor v, final ItemStackSrc is) {
        this.colors[v.ordinal()] = is;
    }

    @Override
    public Block block(final AEColor color) {
        return null;
    }

    @Override
    public Item item(final AEColor color) {
        final ItemStackSrc is = this.colors[color.ordinal()];

        if (is == null) {
            return null;
        }

        return is.getItem();
    }

    @Override
    public Class<? extends TileEntity> entity(final AEColor color) {
        return null;
    }

    @Override
    public ItemStack stack(final AEColor color, final int stackSize) {
        final ItemStackSrc is = this.colors[color.ordinal()];

        if (is == null) {
            return ItemStack.EMPTY;
        }

        return is.stack(stackSize);
    }

    @Override
    public ItemStack[] allStacks(final int stackSize) {
        final ItemStack[] is = new ItemStack[this.colors.length];
        for (int x = 0; x < is.length; x++) {
            is[x] = this.colors[x].stack(1);
        }
        return is;
    }

    @Override
    public boolean sameAs(final AEColor color, final ItemStack comparableItem) {
        final ItemStackSrc is = this.colors[color.ordinal()];

        if (comparableItem.isEmpty() || is == null) {
            return false;
        }

        return comparableItem.getItem() == is.getItem() && comparableItem.getItemDamage() == is.getDamage();
    }
}
