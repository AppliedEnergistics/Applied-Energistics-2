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

package appeng.decorative.solid;


import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinitionException;
import appeng.block.AEBaseBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;


public class BlockQuartzOre extends AEBaseBlock {
    public BlockQuartzOre() {
        super(Material.ROCK);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random rand) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(null, rand, fortune)) {
            int j = rand.nextInt(fortune + 2) - 1;

            if (j < 0) {
                j = 0;
            }

            return this.quantityDropped(rand) * (j + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    @Override
    public int quantityDropped(final Random rand) {
        return 1 + rand.nextInt(2);
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        Random rand = world instanceof World ? ((World) world).rand : new Random();

        if (this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            return MathHelper.getInt(rand, 2, 5);
        }
        return super.getExpDrop(state, world, pos, fortune);
    }

    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return AEApi.instance()
                .definitions()
                .materials()
                .certusQuartzCrystal()
                .maybeItem()
                .orElseThrow(() -> new MissingDefinitionException("Tried to access certus quartz crystal, even though they are disabled"));
    }

    @Override
    public int damageDropped(final IBlockState state) {
        return AEApi.instance()
                .definitions()
                .materials()
                .certusQuartzCrystal()
                .maybeStack(1)
                .orElseThrow(() -> new MissingDefinitionException("Tried to access certus quartz crystal, even though they are disabled"))
                .getItemDamage();
    }
}
