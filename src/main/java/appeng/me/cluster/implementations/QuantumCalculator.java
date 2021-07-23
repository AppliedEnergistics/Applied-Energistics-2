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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.server.level.ServerLevel;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.BlockDefinition;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.qnb.QuantumBridgeTileEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

public class QuantumCalculator extends MBCalculator<QuantumBridgeTileEntity, QuantumCluster> {

    public QuantumCalculator(final QuantumBridgeTileEntity t) {
        super(t);
    }

    @Override
    public boolean checkMultiblockScale(final BlockPos min, final BlockPos max) {
        if ((max.getX() - min.getX() + 1) * (max.getY() - min.getY() + 1) * (max.getZ() - min.getZ() + 1) == 9) {
            final int ones = (max.getX() - min.getX() == 0 ? 1 : 0) + (max.getY() - min.getY() == 0 ? 1 : 0)
                    + (max.getZ() - min.getZ() == 0 ? 1 : 0);

            final int threes = (max.getX() - min.getX() == 2 ? 1 : 0) + (max.getY() - min.getY() == 2 ? 1 : 0)
                    + (max.getZ() - min.getZ() == 2 ? 1 : 0);

            return ones == 1 && threes == 2;
        }
        return false;
    }

    @Override
    public QuantumCluster createCluster(final ServerLevel w, final BlockPos min, final BlockPos max) {
        return new QuantumCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(final ServerLevel w, final BlockPos min, final BlockPos max) {

        byte num = 0;

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final IAEMultiBlock<?> te = (IAEMultiBlock<?>) w.getBlockEntity(p);

            if (te == null || !te.isValid()) {
                return false;
            }

            num++;
            if (num == 5) {
                if (!this.isBlockAtLocation(w, p, AEBlocks.QUANTUM_LINK)) {
                    return false;
                }
            } else if (!this.isBlockAtLocation(w, p, AEBlocks.QUANTUM_RING)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateTiles(final QuantumCluster c, final ServerLevel w, final BlockPos min, final BlockPos max) {
        byte num = 0;
        byte ringNum = 0;

        for (BlockPos p : BlockPos.betweenClosed(min, max)) {
            final QuantumBridgeTileEntity te = (QuantumBridgeTileEntity) w.getBlockEntity(p);

            num++;
            final byte flags;
            if (num == 5) {
                flags = num;
                c.setCenter(te);
            } else {
                if (num == 1 || num == 3 || num == 7 || num == 9) {
                    flags = (byte) (this.target.getCorner() | num);
                } else {
                    flags = num;
                }
                c.getRing()[ringNum] = te;
                ringNum++;
            }

            te.updateStatus(c, flags, true);
        }
    }

    @Override
    public boolean isValidTile(final BlockEntity te) {
        return te instanceof QuantumBridgeTileEntity;
    }

    private boolean isBlockAtLocation(final BlockGetter w, final BlockPos pos, final BlockDefinition def) {
        return def.block() == w.getBlockState(pos).getBlock();
    }
}
