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

package appeng.me.cluster;

import java.lang.ref.WeakReference;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.util.AEPartLocation;
import appeng.core.AELog;

public abstract class MBCalculator<TBlockEntity extends IAEMultiBlock<TCluster>, TCluster extends IAECluster> {

    /**
     * To avoid recursive cluster rebuilds, we use a global field to prevent this from happening. This is set to the
     * cluster that is currently causing a Multiblock modification.
     */
    private static WeakReference<IAECluster> modificationInProgress = new WeakReference<>(null);

    protected final TBlockEntity target;

    public MBCalculator(final TBlockEntity t) {
        this.target = t;
    }

    public static void setModificationInProgress(IAECluster cluster) {
        IAECluster inProgress = modificationInProgress.get();
        if (inProgress == cluster) {
            return;
        }
        if (inProgress != null && cluster != null) {
            throw new IllegalStateException("A modification is already in-progress for: " + inProgress);
        }
        modificationInProgress = new WeakReference<>(cluster);
    }

    public static boolean isModificationInProgress() {
        return modificationInProgress.get() != null;
    }

    public void updateMultiblockAfterNeighborUpdate(final ServerLevel world, final BlockPos loc, BlockPos changedPos) {
        boolean recheck;

        TCluster cluster = target.getCluster();
        if (cluster != null) {
            if (isWithinBounds(changedPos, cluster.getBoundsMin(), cluster.getBoundsMax())) {
                // If the location is part of the current multiblock, always re-check
                recheck = true;
            } else {
                // If the location is outside, only re-check if it would now be considered part
                // of it
                recheck = isValidBlockEntityAt(world, changedPos.getX(), changedPos.getY(), changedPos.getZ());
            }
        } else {
            // Always recheck if the block entity is not part of a cluster, because the adjacent
            // block could have previously been a valid block entity, but in a wrong placement,
            // or the other way around.
            recheck = true;
        }

        if (recheck) {
            calculateMultiblock(world, loc);
        }
    }

    public void calculateMultiblock(final ServerLevel world, final BlockPos loc) {
        if (isModificationInProgress()) {
            return;
        }

        IAECluster currentCluster = target.getCluster();
        if (currentCluster != null && currentCluster.isDestroyed()) {
            return; // If we're still part of a cluster that is in the process of being destroyed,
            // don't recalc.
        }

        try {
            final MutableBlockPos min = loc.mutable();
            final MutableBlockPos max = loc.mutable();

            // find size of MB structure...
            while (this.isValidBlockEntityAt(world, min.getX() - 1, min.getY(), min.getZ())) {
                min.setX(min.getX() - 1);
            }
            while (this.isValidBlockEntityAt(world, min.getX(), min.getY() - 1, min.getZ())) {
                min.setY(min.getY() - 1);
            }
            while (this.isValidBlockEntityAt(world, min.getX(), min.getY(), min.getZ() - 1)) {
                min.setZ(min.getZ() - 1);
            }
            while (this.isValidBlockEntityAt(world, max.getX() + 1, max.getY(), max.getZ())) {
                max.setX(max.getX() + 1);
            }
            while (this.isValidBlockEntityAt(world, max.getX(), max.getY() + 1, max.getZ())) {
                max.setY(max.getY() + 1);
            }
            while (this.isValidBlockEntityAt(world, max.getX(), max.getY(), max.getZ() + 1)) {
                max.setZ(max.getZ() + 1);
            }

            if (this.checkMultiblockScale(min, max) && this.verifyUnownedRegion(world, min, max)) {
                try {
                    if (!this.verifyInternalStructure(world, min, max)) {
                        this.disconnect();
                        return;
                    }
                } catch (final Exception err) {
                    this.disconnect();
                    return;
                }

                boolean updateGrid = false;
                TCluster cluster = this.target.getCluster();
                if (cluster == null || !cluster.getBoundsMin().equals(min) || !cluster.getBoundsMax().equals(max)) {
                    cluster = this.createCluster(world, min, max);
                    setModificationInProgress(cluster);
                    // NOTE: The following will break existing clusters within the bounds
                    this.updateBlockEntities(cluster, world, min, max);

                    updateGrid = true;
                } else {
                    setModificationInProgress(cluster);
                }

                cluster.updateStatus(updateGrid);
                return;
            }
        } catch (final Throwable err) {
            AELog.debug(err);
        } finally {
            setModificationInProgress(null);
        }

        this.disconnect();
    }

    private static boolean isWithinBounds(BlockPos pos, BlockPos boundsMin, BlockPos boundsMax) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return x >= boundsMin.getX() && y >= boundsMin.getY() && z >= boundsMin.getZ() && x <= boundsMax.getX()
                && y <= boundsMax.getY() && z <= boundsMax.getZ();
    }

    private boolean isValidBlockEntityAt(final Level w, final int x, final int y, final int z) {
        return this.isValidBlockEntity(w.getBlockEntity(new BlockPos(x, y, z)));
    }

    /**
     * verify if the structure is the correct dimensions, or size
     *
     * @param min min world coord
     * @param max max world coord
     * @return true if structure has correct dimensions or size
     */
    public abstract boolean checkMultiblockScale(BlockPos min, BlockPos max);

    private boolean verifyUnownedRegion(final ServerLevel w, final BlockPos min, final BlockPos max) {
        for (final AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
            if (this.verifyUnownedRegionInner(w, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(),
                    side)) {
                return false;
            }
        }

        return true;
    }

    /**
     * construct the correct cluster, usually very simple.
     *
     * @param w   world
     * @param min min world coord
     * @param max max world coord
     * @return created cluster
     */
    public abstract TCluster createCluster(ServerLevel w, BlockPos min, BlockPos max);

    public abstract boolean verifyInternalStructure(ServerLevel world, BlockPos min, BlockPos max);

    /**
     * disassembles the multi-block.
     */
    public void disconnect() {
        this.target.disconnect(true);
    }

    /**
     * configure the multi-block block entities, most of the important stuff is in here.
     *
     * @param c   updated cluster
     * @param w   in world
     * @param min min world coord
     * @param max max world coord
     */
    public abstract void updateBlockEntities(TCluster c, ServerLevel w, BlockPos min, BlockPos max);

    /**
     * check if the block entities are correct for the structure.
     *
     * @param te to be checked block entity
     * @return true if block entity is valid for structure
     */
    public abstract boolean isValidBlockEntity(BlockEntity te);

    private boolean verifyUnownedRegionInner(final ServerLevel w, int minX, int minY, int minZ, int maxX, int maxY,
            int maxZ,
            final AEPartLocation side) {
        switch (side) {
            case WEST:
                minX -= 1;
                maxX = minX;
                break;
            case EAST:
                maxX += 1;
                minX = maxX;
                break;
            case DOWN:
                minY -= 1;
                maxY = minY;
                break;
            case NORTH:
                maxZ += 1;
                minZ = maxZ;
                break;
            case SOUTH:
                minZ -= 1;
                maxZ = minZ;
                break;
            case UP:
                maxY += 1;
                minY = maxY;
                break;
            case INTERNAL:
                return false;
        }

        for (BlockPos p : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            final BlockEntity te = w.getBlockEntity(p);
            if (this.isValidBlockEntity(te)) {
                return true;
            }
        }

        return false;
    }
}
