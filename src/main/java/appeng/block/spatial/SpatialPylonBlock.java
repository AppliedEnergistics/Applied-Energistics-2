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

import appeng.tile.spatial.SpatialPylonTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.helpers.AEMaterials;

public class SpatialPylonBlock extends AEBaseTileBlock<SpatialPylonTileEntity> {

    public SpatialPylonBlock() {
        super(defaultProps(AEMaterials.GLASS));
    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos,
            boolean isMoving) {
        final SpatialPylonTileEntity tsp = this.getTileEntity(world, pos);
        if (tsp != null) {
            tsp.neighborUpdate(fromPos);
        }
    }

// FIXME FABRIC  Must use block states for dynamic lighting
// FIXME FABRIC    @Override
// FIXME FABRIC    public int getLightValue(final BlockState state, final BlockView w, final BlockPos pos) {
// FIXME FABRIC        final SpatialPylonTileEntity tsp = this.getBlockEntity(w, pos);
// FIXME FABRIC        if (tsp != null) {
// FIXME FABRIC            return tsp.getLightValue();
// FIXME FABRIC        }
// FIXME FABRIC        return super.getLightValue(state, w, pos);
// FIXME FABRIC    }

}
