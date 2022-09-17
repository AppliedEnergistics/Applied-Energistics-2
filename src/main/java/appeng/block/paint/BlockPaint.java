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

package appeng.block.paint;


import appeng.block.AEBaseTileBlock;
import appeng.helpers.Splotch;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;


public class BlockPaint extends AEBaseTileBlock {

    static final PaintSplotchesProperty SPLOTCHES = new PaintSplotchesProperty();

    public BlockPaint() {
        super(new MaterialLiquid(MapColor.AIR));

        this.setLightOpacity(0);
        this.setFullSize(false);
        this.setOpaque(false);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{SPLOTCHES});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extState = (IExtendedBlockState) state;

        TilePaint te = this.getTileEntity(world, pos);

        Collection<Splotch> splotches = Collections.emptyList();
        if (te != null) {
            splotches = te.getDots();
        }

        return extState.withProperty(SPLOTCHES, new PaintSplotches(splotches));
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(final CreativeTabs tabs, final NonNullList<ItemStack> itemStacks) {
        // do nothing
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public boolean canCollideCheck(final IBlockState state, final boolean hitIfLiquid) {
        return false;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        final TilePaint tp = this.getTileEntity(world, pos);

        if (tp != null) {
            tp.neighborChanged();
        }
    }

    @Override
    public Item getItemDropped(final IBlockState state, final Random rand, final int fortune) {
        return null;
    }

    @Override
    public void dropBlockAsItemWithChance(final World worldIn, final BlockPos pos, final IBlockState state, final float chance, final int fortune) {

    }

    @Override
    public void fillWithRain(final World w, final BlockPos pos) {
        if (Platform.isServer()) {
            w.setBlockToAir(pos);
        }
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess w, final BlockPos pos) {
        final TilePaint tp = this.getTileEntity(w, pos);

        if (tp != null) {
            return tp.getLightLevel();
        }

        return 0;
    }

    @Override
    public boolean isAir(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        return true;
    }

    @Override
    public boolean isReplaceable(final IBlockAccess worldIn, final BlockPos pos) {
        return true;
    }

}
