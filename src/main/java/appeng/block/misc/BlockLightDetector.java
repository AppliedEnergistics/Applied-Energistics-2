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

package appeng.block.misc;


import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseTileBlock;
import appeng.helpers.ICustomCollision;
import appeng.helpers.MetaRotation;
import appeng.tile.misc.TileLightDetector;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.Random;


public class BlockLightDetector extends AEBaseTileBlock implements IOrientableBlock, ICustomCollision {

    // Cannot use the vanilla FACING property here because it excludes facing DOWN
    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    // Used to alternate between two variants of the fixture on adjacent blocks
    public static final PropertyBool ODD = PropertyBool.create("odd");

    public BlockLightDetector() {
        super(Material.CIRCUITS);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP).withProperty(ODD, false));
        this.setLightOpacity(0);
        this.setFullSize(false);
        this.setOpaque(false);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        EnumFacing facing = EnumFacing.values()[meta];
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[]{FACING, ODD};
    }

    @Override
    public int getWeakPower(final IBlockState state, final IBlockAccess w, final BlockPos pos, final EnumFacing side) {
        if (w instanceof World && ((TileLightDetector) this.getTileEntity(w, pos)).isReady()) {
            return ((World) w).getLightFromNeighbors(pos) - 6;
        }

        return 0;
    }

    @Override
    public void onNeighborChange(final IBlockAccess world, final BlockPos pos, final BlockPos neighbor) {
        super.onNeighborChange(world, pos, neighbor);

        final TileLightDetector tld = this.getTileEntity(world, pos);
        if (tld != null) {
            tld.updateLight();
        }
    }

    @Override
    public void randomDisplayTick(final IBlockState state, final World worldIn, final BlockPos pos, final Random rand) {
        // cancel out lightning
    }

    @Override
    public boolean isValidOrientation(final World w, final BlockPos pos, final EnumFacing forward, final EnumFacing up) {
        return this.canPlaceAt(w, pos, up.getOpposite());
    }

    private boolean canPlaceAt(final World w, final BlockPos pos, final EnumFacing dir) {
        return w.isSideSolid(pos.offset(dir), dir.getOpposite(), false);
    }

    @Override
    public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool(final World w, final BlockPos pos, final Entity thePlayer, final boolean b) {
        final EnumFacing up = this.getOrientable(w, pos).getUp();
        final double xOff = -0.3 * up.getFrontOffsetX();
        final double yOff = -0.3 * up.getFrontOffsetY();
        final double zOff = -0.3 * up.getFrontOffsetZ();
        return Collections.singletonList(new AxisAlignedBB(xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7));
    }

    @Override
    public void addCollidingBlockToList(final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e) {/*
     * double xOff = -0.15 * getUp().offsetX; double yOff = -0.15 *
     * getUp().offsetY; double zOff = -0.15 * getUp().offsetZ; out.add(
     * AxisAlignedBB.getBoundingBox( xOff + (double) x + 0.15, yOff +
     * (double) y + 0.15, zOff + (double) z + 0.15,// ahh xOff + (double) x
     * + 0.85, yOff + (double) y + 0.85, zOff + (double) z + 0.85 ) );
     */
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        final EnumFacing up = this.getOrientable(world, pos).getUp();
        if (!this.canPlaceAt(world, pos, up.getOpposite())) {
            this.dropTorch(world, pos);
        }
    }

    private void dropTorch(final World w, final BlockPos pos) {
        final IBlockState prev = w.getBlockState(pos);
        w.destroyBlock(pos, true);
        w.notifyBlockUpdate(pos, prev, w.getBlockState(pos), 3);
    }

    @Override
    public boolean canPlaceBlockAt(final World w, final BlockPos pos) {
        for (final EnumFacing dir : EnumFacing.VALUES) {
            if (this.canPlaceAt(w, pos, dir)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean usesMetadata() {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public IOrientable getOrientable(final IBlockAccess w, final BlockPos pos) {
        return new MetaRotation(w, pos, FACING);
    }

}
