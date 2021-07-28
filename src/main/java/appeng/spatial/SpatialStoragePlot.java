/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.spatial;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.util.Constants;

/**
 * A plot inside the storage cell world that is assigned to a specific storage cell.
 */
public class SpatialStoragePlot {

    private static final String TAG_ID = "id";

    private static final String TAG_SIZE = "size";

    private static final String TAG_OWNER = "owner";

    private static final String TAG_LAST_TRANSITION = "last_transition";

    private static final int REGION_SIZE = 512;

    public static final int MAX_SIZE = 128;

    /**
     * Id of the plot.
     */
    private final int id;

    /**
     * The storage size of this dimension. This is dictated by the pylon structure size used to perform the first
     * transfer into this dimension. Once it's set, it cannot be changed anymore.
     */
    private final BlockPos size;

    /**
     * AE2 player id of who primarily owned the network when the storage plot was allocated.
     */
    private final int owner;

    /**
     * Information about the last transition into this plot.
     */
    @Nullable
    private TransitionInfo lastTransition;

    public SpatialStoragePlot(int id, BlockPos size, int owner) {
        this.id = id;
        this.size = size;
        this.owner = owner;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
            throw new IllegalArgumentException("Plot size " + size + " is smaller than minimum size.");
        }
        if (size.getX() > MAX_SIZE || size.getY() > MAX_SIZE || size.getZ() > MAX_SIZE) {
            throw new IllegalArgumentException("Plot size " + size + " exceeds maximum size of " + MAX_SIZE);
        }
    }

    public int getId() {
        return id;
    }

    /**
     * The size in blocks of the plot in the storage dimension.
     *
     * @see #getOrigin()
     */
    public BlockPos getSize() {
        return size;
    }

    /**
     * Returns the AE player id of the owner of this plot, or -1 if unknown.
     *
     * @see appeng.core.worlddata.IWorldPlayerData
     */
    public int getOwner() {
        return owner;
    }

    /**
     * Returns information about the last transition into this plot (if any).
     */
    @Nullable
    public TransitionInfo getLastTransition() {
        return lastTransition;
    }

    void setLastTransition(TransitionInfo info) {
        this.lastTransition = info;
    }

    /**
     * The origin of this plot within the spatial storage dimension.
     * <p>
     * To map an integer to a specific position, it uses the following algorithm.
     * <p>
     * The 2 least significant bits determine the sign for the x and z axis. Every other pack of 2 bits locate the plot
     * within a quadrant of a increasing area by the bit position.
     * <p>
     * The first 2 bits after the sign address a quadrant within 1024x1024 blocks (or 4 region files)
     * <p>
     * Every further will continue to double both x and z values. E.g. 2048x2048 for the 3rd pack and 4096x4096 for the
     * 4th.
     *
     * @see #getSize()
     */
    public BlockPos getOrigin() {
        int signBits = id & 0b11;
        int offsetBits = id >> 2;
        int offsetScale = 1;
        int posx = REGION_SIZE / 2;
        int posz = REGION_SIZE / 2;

        // find quadrant
        while (offsetBits != 0) {
            posx += REGION_SIZE * offsetScale * (offsetBits & 0b01);
            posz += REGION_SIZE * offsetScale * (offsetBits >> 1 & 0b01);

            offsetBits >>= 2;
            offsetScale <<= 1;
        }

        // mirror in one of 4 directions
        // First flip the z axis on every increment and then every two the x axis
        if ((signBits & 0b01) != 0) {
            posz *= -1;
        }
        if ((signBits & 0b10) != 0) {
            posx *= -1;
        }

        // offset from cell center
        //
        // This is to ensure a 128x128x128 block plot (or 8x8 chunks) is centered within
        // the region.
        // This provides 12 chunks around it, so the default chunk loading radius on
        // servers of 10 will prevent adjacent regions to be generated. As well as a 24
        // chunks to the next plot.
        posx -= 64;
        posz -= 64;

        return new BlockPos(posx, 64, posz);
    }

    /**
     * Returns the filename of the region in the world save containing this plot.
     */
    public String getRegionFilename() {
        BlockPos origin = this.getOrigin();
        ChunkPos originChunk = new ChunkPos(origin);

        return String.format(Locale.ROOT, "r.%d.%d.mca", originChunk.getRegionX(), originChunk.getRegionZ());
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(TAG_ID, id);
        tag.put(TAG_SIZE, NbtUtils.writeBlockPos(size));
        tag.putInt(TAG_OWNER, owner);
        if (lastTransition != null) {
            tag.put(TAG_LAST_TRANSITION, lastTransition.toTag());
        }
        return tag;
    }

    public static SpatialStoragePlot fromTag(CompoundTag tag) {
        int id = tag.getInt(TAG_ID);
        BlockPos size = NbtUtils.readBlockPos(tag.getCompound(TAG_SIZE));
        int ownerId = tag.getInt(TAG_OWNER);
        SpatialStoragePlot plot = new SpatialStoragePlot(id, size, ownerId);

        if (tag.contains(TAG_LAST_TRANSITION, Constants.NBT.TAG_COMPOUND)) {
            plot.lastTransition = TransitionInfo.fromTag(tag.getCompound(TAG_LAST_TRANSITION));
        }
        return plot;
    }

}
