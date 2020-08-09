package appeng.spatial;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

/**
 * A plot inside the storage cell world that is assigned to a specific storage cell.
 */
public class SpatialStoragePlot {

    private static final String TAG_ID = "id";

    private static final String TAG_SIZE = "size";

    private static final String TAG_OWNER = "owner";

    private static final String TAG_LAST_TRANSITION = "last_transition";

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

    public CompoundNBT toTag() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt(TAG_ID, id);
        tag.put(TAG_SIZE, NBTUtil.writeBlockPos(size));
        tag.putInt(TAG_OWNER, owner);
        if (lastTransition != null) {
            tag.put(TAG_LAST_TRANSITION, lastTransition.toTag());
        }
        return tag;
    }

    public static SpatialStoragePlot fromTag(CompoundNBT tag) {
        int id = tag.getInt(TAG_ID);
        BlockPos size = NBTUtil.readBlockPos(tag.getCompound(TAG_SIZE));
        int ownerId = tag.getInt(TAG_OWNER);
        SpatialStoragePlot plot = new SpatialStoragePlot(id, size, ownerId);

        if (tag.contains(TAG_LAST_TRANSITION, Constants.NBT.TAG_COMPOUND)) {
            plot.lastTransition = TransitionInfo.fromTag(tag.getCompound(TAG_LAST_TRANSITION));
        }
        return plot;
    }

}
