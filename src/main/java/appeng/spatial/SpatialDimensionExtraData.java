package appeng.spatial;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

/**
 * Helps with encoding and decoding the extra data we attach to each created
 * storage dimension world as persistent state.
 */
public final class SpatialDimensionExtraData extends PersistentState {

    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "ae2_spatial_info";

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 1;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_SIZE = "size";

    /**
     * The storage size of this dimension. This is dicateted by the pylon structure
     * size used to perform the first transfer into this dimension. Once it's set,
     * it cannot be changed anymore.
     */
    private BlockPos size = BlockPos.ORIGIN;

    public SpatialDimensionExtraData() {
        super(ID);
    }

    public SpatialDimensionExtraData(BlockPos size) {
        super(ID);
        this.size = size;
    }

    public BlockPos getSize() {
        return size;
    }

    public void setSize(BlockPos size) {
        this.size = size;
        setDirty(true);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        size = NbtHelper.toBlockPos(tag.getCompound(TAG_SIZE));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);
        tag.put(TAG_SIZE, NbtHelper.fromBlockPos(size));
        return tag;
    }

}
