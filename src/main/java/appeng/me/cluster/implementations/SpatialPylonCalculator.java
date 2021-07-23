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

package appeng.me.cluster.implementations;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.SpatialPylonTileEntity;

public class SpatialPylonCalculator extends MBCalculator<SpatialPylonTileEntity, SpatialPylonCluster> {

    public SpatialPylonCalculator(final SpatialPylonTileEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(final BlockPos min, final BlockPos max) {
        return min.getX() == max.getX() && min.getY() == max.getY() && min.getZ() != max.getZ()
                || min.getX() == max.getX() && min.getY() != max.getY() && min.getZ() == max.getZ()
                || min.getX() != max.getX() && min.getY() == max.getY() && min.getZ() == max.getZ();
    }

    @Override
    public SpatialPylonCluster createCluster(ServerWorld w, final BlockPos min, final BlockPos max) {
        return new SpatialPylonCluster(w, min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerWorld w, final BlockPos min, final BlockPos max) {

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final IAEMultiBlock<?> te = (IAEMultiBlock<?>) w.getBlockEntity(p);

            if (te == null || !te.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateTiles(final SpatialPylonCluster c, final ServerWorld w, final BlockPos min, final BlockPos max) {
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final SpatialPylonTileEntity te = (SpatialPylonTileEntity) w.getBlockEntity(p);
            te.updateStatus(c);
            c.getLine().add(te);
        }
    }

    @Override
    public boolean isValidTile(final TileEntity te) {
        return te instanceof SpatialPylonTileEntity;
    }
}
