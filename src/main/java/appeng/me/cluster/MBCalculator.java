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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.util.AEPartLocation;
import appeng.api.util.WorldCoord;
import appeng.core.AELog;
import appeng.util.Platform;

public abstract class MBCalculator {

    private static WeakReference<IAECluster> modificationInProgress = new WeakReference<>(null);

    private final IAEMultiBlock target;

    public MBCalculator(final IAEMultiBlock t) {
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

    public void updateMultiblockAfterNeighborUpdate(final World world, final WorldCoord loc, BlockPos changedPos) {
        boolean recheck;

        IAECluster cluster = target.getCluster();
        if (cluster != null) {
            if (isWithinBounds(changedPos, cluster.getBoundsMin(), cluster.getBoundsMax())) {
                // If the location is part of the current multiblock, always re-check
                recheck = true;
            } else {
                // If the location is outside, only re-check if it would now be considered part
                // of it
                recheck = isValidTileAt(world, changedPos.getX(), changedPos.getY(), changedPos.getZ());
            }
        } else {
            // Always recheck if the tile is not part of a cluster, because the adjacent
            // block could have
            // previously been a valid tile, but in a wrong placement, or the other way
            // around.
            recheck = true;
        }

        if (recheck) {
            calculateMultiblock(world, loc);
        }
    }

    public void calculateMultiblock(final World world, final WorldCoord loc) {
        if (Platform.isClient() || isModificationInProgress()) {
            return;
        }

        IAECluster currentCluster = target.getCluster();
        if (currentCluster != null && currentCluster.isDestroyed()) {
            return; // If we're still part of a cluster that is in the process of being destroyed,
                    // don't recalc.
        }

        try {
            final WorldCoord min = loc.copy();
            final WorldCoord max = loc.copy();

            // find size of MB structure...
            while (this.isValidTileAt(world, min.x - 1, min.y, min.z)) {
                min.x--;
            }
            while (this.isValidTileAt(world, min.x, min.y - 1, min.z)) {
                min.y--;
            }
            while (this.isValidTileAt(world, min.x, min.y, min.z - 1)) {
                min.z--;
            }
            while (this.isValidTileAt(world, max.x + 1, max.y, max.z)) {
                max.x++;
            }
            while (this.isValidTileAt(world, max.x, max.y + 1, max.z)) {
                max.y++;
            }
            while (this.isValidTileAt(world, max.x, max.y, max.z + 1)) {
                max.z++;
            }

            if (this.checkMultiblockScale(min, max)) {
                if (this.verifyUnownedRegion(world, min, max)) {
                    IAECluster c = this.createCluster(world, min, max);
                    setModificationInProgress(c);

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
                    final IAECluster cluster = this.target.getCluster();
                    if (cluster == null) {
                        this.updateTiles(c, world, min, max);

                        updateGrid = true;
                    } else {
                        c = cluster;
                    }

                    c.updateStatus(updateGrid);
                    return;
                }
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
        return (x >= boundsMin.getX() && y >= boundsMin.getY() && z >= boundsMin.getZ() && x <= boundsMax.getX()
                && y <= boundsMax.getY() && z <= boundsMax.getZ());
    }

    private boolean isValidTileAt(final World w, final int x, final int y, final int z) {
        return this.isValidTile(w.getBlockEntity(new BlockPos(x, y, z)));
    }

    /**
     * verify if the structure is the correct dimensions, or size
     *
     * @param min min world coord
     * @param max max world coord
     *
     * @return true if structure has correct dimensions or size
     */
    public abstract boolean checkMultiblockScale(WorldCoord min, WorldCoord max);

    private boolean verifyUnownedRegion(final World w, final WorldCoord min, final WorldCoord max) {
        for (final AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
            if (this.verifyUnownedRegionInner(w, min.x, min.y, min.z, max.x, max.y, max.z, side)) {
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
     *
     * @return created cluster
     */
    public abstract IAECluster createCluster(World w, WorldCoord min, WorldCoord max);

    public abstract boolean verifyInternalStructure(World world, WorldCoord min, WorldCoord max);

    /**
     * disassembles the multi-block.
     */
    public abstract void disconnect();

    /**
     * configure the multi-block tiles, most of the important stuff is in here.
     *
     * @param c   updated cluster
     * @param w   in world
     * @param min min world coord
     * @param max max world coord
     */
    public abstract void updateTiles(IAECluster c, World w, WorldCoord min, WorldCoord max);

    /**
     * check if the block entities are correct for the structure.
     *
     * @param te to be checked block entity
     *
     * @return true if block entity is valid for structure
     */
    public abstract boolean isValidTile(BlockEntity te);

    private boolean verifyUnownedRegionInner(final World w, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
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

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    final BlockEntity te = w.getBlockEntity(new BlockPos(x, y, z));
                    if (this.isValidTile(te)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
