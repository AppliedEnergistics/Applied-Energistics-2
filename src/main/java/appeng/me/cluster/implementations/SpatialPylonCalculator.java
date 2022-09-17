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


import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.TileSpatialPylon;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class SpatialPylonCalculator extends MBCalculator {

    private final TileSpatialPylon tqb;

    public SpatialPylonCalculator(final IAEMultiBlock t) {
        super(t);
        this.tqb = (TileSpatialPylon) t;
    }

    @Override
    public boolean checkMultiblockScale(final WorldCoord min, final WorldCoord max) {
        return (min.x == max.x && min.y == max.y && min.z != max.z) || (min.x == max.x && min.y != max.y && min.z == max.z) || (min.x != max.x && min.y == max.y && min.z == max.z);
    }

    @Override
    public IAECluster createCluster(final World w, final WorldCoord min, final WorldCoord max) {
        return new SpatialPylonCluster(new DimensionalCoord(w, min.x, min.y, min.z), new DimensionalCoord(w, max.x, max.y, max.z));
    }

    @Override
    public boolean verifyInternalStructure(final World w, final WorldCoord min, final WorldCoord max) {

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    final IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity(new BlockPos(x, y, z));

                    if (!te.isValid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void disconnect() {
        this.tqb.disconnect(true);
    }

    @Override
    public void updateTiles(final IAECluster cl, final World w, final WorldCoord min, final WorldCoord max) {
        final SpatialPylonCluster c = (SpatialPylonCluster) cl;

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    final TileSpatialPylon te = (TileSpatialPylon) w.getTileEntity(new BlockPos(x, y, z));
                    te.updateStatus(c);
                    c.getLine().add((te));
                }
            }
        }
    }

    @Override
    public boolean isValidTile(final TileEntity te) {
        return te instanceof TileSpatialPylon;
    }
}
