package appeng.spatial;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;

import javax.annotation.Nullable;

/**
 * A plot inside the storage cell world that is assigned to a specific storage cell.
 */
public class SpatialStoragePlot {

    private static final int MAX_SIZE = 512;

    /**
     * Id of the plot.
     */
    private final int id;

    /**
     * The storage size of this dimension. This is dictated by the pylon structure
     * size used to perform the first transfer into this dimension. Once it's set,
     * it cannot be changed anymore.
     */
    private final BlockPos size;

    /**
     * AE2 player id of who primarily owned the network when the storage plot was
     * allocated.
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
        if (size.getX() > MAX_SIZE || size.getY() >= MAX_SIZE || size.getZ() >= MAX_SIZE) {
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
     *
     * @see #getSize()
     */
    public BlockPos getOrigin() {
        int signBits = id & 0b11;
        int offsetBits = id >> 2;
        int offsetScale = 1;
        int posx = MAX_SIZE / 2;
        int posz = MAX_SIZE / 2;

        // find quadrant
        while (offsetBits != 0) {
            posx += MAX_SIZE * offsetScale * (offsetBits & 0b01);
            posz += MAX_SIZE * offsetScale * (offsetBits >> 1 & 0b01);

            offsetBits >>= 2;
            offsetScale <<= 1;
        }

        // mirror in one of 4 directions
        if ((signBits & 0b01) == 0) {
            posx *= -1;
        }
        if ((signBits & 0b10) == 0) {
            posz *= -1;
        }

        // offset from cell center
        posx -= 64;
        posz -= 64;

        return new BlockPos(posx, 64, posz);
    }

}
