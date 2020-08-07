package appeng.spatial;

import net.minecraft.util.math.BlockPos;

/**
 * A lot inside the storage cell world that is assigned to a specific storage cell.
 */
public class StorageCellLot {

    private static final int MAX_SIZE = 512;

    /**
     * Id of the lot.
     */
    private final int id;

    /**
     * The storage size of this dimension. This is dicateted by the pylon structure
     * size used to perform the first transfer into this dimension. Once it's set,
     * it cannot be changed anymore.
     */
    private final BlockPos size;

    /**
     * AE2 player id of who primarily owned the network when the storage lot was
     * allocated.
     */
    private final int owner;

    public StorageCellLot(int id, BlockPos size, int owner) {
        this.id = id;
        this.size = size;
        this.owner = owner;
        if (size.getX() < 1 || size.getY() < 1 || size.getZ() < 1) {
            throw new IllegalArgumentException("Lot size " + size + " is smaller than minimum size.");
        }
        if (size.getX() > MAX_SIZE || size.getY() >= MAX_SIZE || size.getZ() >= MAX_SIZE) {
            throw new IllegalArgumentException("Lot size " + size + " exceeds maximum size of " + MAX_SIZE);
        }
    }

    public int getId() {
        return id;
    }

    public BlockPos getSize() {
        return size;
    }

    public int getOwner() {
        return owner;
    }

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
