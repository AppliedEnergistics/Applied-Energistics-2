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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.spatial.SpatialPylonBlockEntity;

public class SpatialPylonCalculator extends MBCalculator {

    private final SpatialPylonBlockEntity tqb;

    public SpatialPylonCalculator(final IAEMultiBlock t) {
        super(t);
        this.tqb = (SpatialPylonBlockEntity) t;
    }

    @Override
    public boolean checkMultiblockScale(final WorldCoord min, final WorldCoord max) {
        return (min.x == max.x && min.y == max.y && min.z != max.z)
                || (min.x == max.x && min.y != max.y && min.z == max.z)
                || (min.x != max.x && min.y == max.y && min.z == max.z);
    }

    @Override
    public IAECluster createCluster(final World w, final WorldCoord min, final WorldCoord max) {
        return new SpatialPylonCluster(w, min.getBlockPos(), max.getBlockPos());
    }

    @Override
    public boolean verifyInternalStructure(final World w, final WorldCoord min, final WorldCoord max) {

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    final IAEMultiBlock te = (IAEMultiBlock) w.getBlockEntity(new BlockPos(x, y, z));

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
                    final SpatialPylonBlockEntity te = (SpatialPylonBlockEntity) w.getBlockEntity(new BlockPos(x, y, z));
                    te.updateStatus(c);
                    c.getLine().add((te));
                }
            }
        }
    }

    @Override
    public boolean isValidTile(final BlockEntity te) {
        return te instanceof SpatialPylonBlockEntity;
    }
}
