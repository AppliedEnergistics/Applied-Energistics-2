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

package appeng.block.spatial;


import appeng.block.AEBaseTileBlock;
import appeng.client.render.spatial.SpatialPylonStateProperty;
import appeng.helpers.AEGlassMaterial;
import appeng.tile.spatial.TileSpatialPylon;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;


public class BlockSpatialPylon extends AEBaseTileBlock {

    public static final SpatialPylonStateProperty STATE = new SpatialPylonStateProperty();

    public BlockSpatialPylon() {
        super(AEGlassMaterial.INSTANCE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, this.getAEStates(), new IUnlistedProperty[]{STATE});
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extState = (IExtendedBlockState) state;

        return extState.withProperty(STATE, this.getDisplayState(world, pos));
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        final TileSpatialPylon tsp = this.getTileEntity(world, pos);
        if (tsp != null) {
            tsp.neighborChanged();
        }
    }

    @Override
    public int getLightValue(final IBlockState state, final IBlockAccess w, final BlockPos pos) {
        final TileSpatialPylon tsp = this.getTileEntity(w, pos);
        if (tsp != null) {
            return tsp.getLightValue();
        }
        return super.getLightValue(state, w, pos);
    }

    private int getDisplayState(IBlockAccess world, BlockPos pos) {
        TileSpatialPylon te = this.getTileEntity(world, pos);

        if (te == null) {
            return 0;
        }

        return te.getDisplayBits();
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

}
