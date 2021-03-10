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

package appeng.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;

public class InWorldToolOperationResult {

    private final BlockState blockState;
    private final Fluid fluid;
    private final List<ItemStack> drops;

    public InWorldToolOperationResult() {
        this.blockState = null;
        this.drops = null;
        this.fluid = null;
    }

    public InWorldToolOperationResult(final BlockState block, final List<ItemStack> drops) {
        this.blockState = block;
        this.fluid = null;
        this.drops = drops;
    }

    public InWorldToolOperationResult(final BlockState block) {
        this.blockState = block;
        this.drops = null;
        this.fluid = null;
    }

    public InWorldToolOperationResult(final BlockState block, Fluid fluid) {
        this.blockState = block;
        this.drops = null;
        this.fluid = fluid;
    }

    public static InWorldToolOperationResult getBlockOperationResult(final ItemStack[] items) {
        final List<ItemStack> temp = new ArrayList<>();
        BlockState b = null;

        for (final ItemStack l : items) {
            if (b == null) {
                final Block bl = Block.byItem(l.getItem());

                if (bl != null && !(bl instanceof AirBlock)) {
                    b = bl.defaultBlockState();
                    continue;
                }
            }

            temp.add(l);
        }

        return new InWorldToolOperationResult(b, temp);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    public List<ItemStack> getDrops() {
        return this.drops;
    }
}
