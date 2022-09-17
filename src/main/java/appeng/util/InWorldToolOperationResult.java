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


import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;


public class InWorldToolOperationResult {

    private final IBlockState blockState;
    private final List<ItemStack> drops;

    public InWorldToolOperationResult() {
        this.blockState = null;
        this.drops = null;
    }

    public InWorldToolOperationResult(final IBlockState block, final List<ItemStack> drops) {
        this.blockState = block;
        this.drops = drops;
    }

    public InWorldToolOperationResult(final IBlockState block) {
        this.blockState = block;
        this.drops = null;
    }

    public static InWorldToolOperationResult getBlockOperationResult(final ItemStack[] items) {
        final List<ItemStack> temp = new ArrayList<>();
        IBlockState b = null;

        for (final ItemStack l : items) {
            if (b == null) {
                final Block bl = Block.getBlockFromItem(l.getItem());

                if (bl != null && !(bl instanceof BlockAir)) {
                    b = bl.getDefaultState();
                    continue;
                }
            }

            temp.add(l);
        }

        return new InWorldToolOperationResult(b, temp);
    }

    public IBlockState getBlockState() {
        return this.blockState;
    }

    public List<ItemStack> getDrops() {
        return this.drops;
    }
}
