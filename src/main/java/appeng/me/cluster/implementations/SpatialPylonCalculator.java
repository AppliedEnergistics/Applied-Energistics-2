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

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.blockentity.spatial.SpatialPylonBlockEntity;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;

public class SpatialPylonCalculator extends MBCalculator<SpatialPylonBlockEntity, SpatialPylonCluster> {

    public SpatialPylonCalculator(final SpatialPylonBlockEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(final BlockPos min, final BlockPos max) {
        return min.getX() == max.getX() && min.getY() == max.getY() && min.getZ() != max.getZ()
                || min.getX() == max.getX() && min.getY() != max.getY() && min.getZ() == max.getZ()
                || min.getX() != max.getX() && min.getY() == max.getY() && min.getZ() == max.getZ();
    }

    @Override
    public SpatialPylonCluster createCluster(ServerLevel level, final BlockPos min, final BlockPos max) {
        return new SpatialPylonCluster(level, min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, final BlockPos min, final BlockPos max) {

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final IAEMultiBlock<?> te = (IAEMultiBlock<?>) level.getBlockEntity(p);

            if (te == null || !te.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updateBlockEntities(final SpatialPylonCluster c, final ServerLevel level, final BlockPos min,
                                    final BlockPos max) {
        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final SpatialPylonBlockEntity te = (SpatialPylonBlockEntity) level.getBlockEntity(p);
            te.updateStatus(c);
            c.getLine().add(te);
        }
    }

    @Override
    public boolean isValidBlockEntity(final BlockEntity te) {
        return te instanceof SpatialPylonBlockEntity;
    }
}
