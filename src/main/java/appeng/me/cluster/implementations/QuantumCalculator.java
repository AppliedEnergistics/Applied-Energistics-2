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


import appeng.api.AEApi;
import appeng.api.definitions.IBlockDefinition;
import appeng.api.definitions.IBlocks;
import appeng.api.util.WorldCoord;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import appeng.tile.qnb.TileQuantumBridge;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class QuantumCalculator extends MBCalculator {

    private final TileQuantumBridge tqb;

    public QuantumCalculator(final IAEMultiBlock t) {
        super(t);
        this.tqb = (TileQuantumBridge) t;
    }

    @Override
    public boolean checkMultiblockScale(final WorldCoord min, final WorldCoord max) {

        if ((max.x - min.x + 1) * (max.y - min.y + 1) * (max.z - min.z + 1) == 9) {
            final int ones = ((max.x - min.x) == 0 ? 1 : 0) + ((max.y - min.y) == 0 ? 1 : 0) + ((max.z - min.z) == 0 ? 1 : 0);

            final int threes = ((max.x - min.x) == 2 ? 1 : 0) + ((max.y - min.y) == 2 ? 1 : 0) + ((max.z - min.z) == 2 ? 1 : 0);

            return ones == 1 && threes == 2;
        }
        return false;
    }

    @Override
    public IAECluster createCluster(final World w, final WorldCoord min, final WorldCoord max) {
        return new QuantumCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(final World w, final WorldCoord min, final WorldCoord max) {

        byte num = 0;

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    final BlockPos p = new BlockPos(x, y, z);
                    final IAEMultiBlock te = (IAEMultiBlock) w.getTileEntity(p);

                    if (!te.isValid()) {
                        return false;
                    }

                    num++;
                    final IBlocks blocks = AEApi.instance().definitions().blocks();
                    if (num == 5) {
                        if (!this.isBlockAtLocation(w, p, blocks.quantumLink())) {
                            return false;
                        }
                    } else {
                        if (!this.isBlockAtLocation(w, p, blocks.quantumRing())) {
                            return false;
                        }
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
        byte num = 0;
        byte ringNum = 0;
        final QuantumCluster c = (QuantumCluster) cl;

        for (int x = min.x; x <= max.x; x++) {
            for (int y = min.y; y <= max.y; y++) {
                for (int z = min.z; z <= max.z; z++) {
                    final TileQuantumBridge te = (TileQuantumBridge) w.getTileEntity(new BlockPos(x, y, z));

                    num++;
                    final byte flags;
                    if (num == 5) {
                        flags = num;
                        c.setCenter(te);
                    } else {
                        if (num == 1 || num == 3 || num == 7 || num == 9) {
                            flags = (byte) (this.tqb.getCorner() | num);
                        } else {
                            flags = num;
                        }
                        c.getRing()[ringNum] = te;
                        ringNum++;
                    }

                    te.updateStatus(c, flags, true);
                }
            }
        }
    }

    @Override
    public boolean isValidTile(final TileEntity te) {
        return te instanceof TileQuantumBridge;
    }

    private boolean isBlockAtLocation(final IBlockAccess w, final BlockPos pos, final IBlockDefinition def) {
        return def.maybeBlock().map(block -> block == w.getBlockState(pos).getBlock()).orElse(false);
    }
}
