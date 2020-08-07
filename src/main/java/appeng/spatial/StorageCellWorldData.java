package appeng.spatial;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

/**
 * Extra data attached to the storage cell world.
 */
public class StorageCellWorldData extends WorldSavedData {

    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "ae2_storage_cell_world";

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 2;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_LOTS = "lots";

    private static final String TAG_LOT_ID = "id";

    private static final String TAG_LOT_SIZE = "size";

    private static final String TAG_LOT_OWNER = "owner";

    private final Int2ObjectOpenHashMap<StorageCellLot> lots = new Int2ObjectOpenHashMap<>();

    public StorageCellWorldData() {
        super(ID);
    }

    public StorageCellLot getLotById(int id) {
        return lots.get(id);
    }

    public StorageCellLot allocateLot(BlockPos size, int owner) {

        int nextId = 1;
        for (int id : lots.keySet()) {
            if (id >= nextId) {
                nextId = id + 1;
            }
        }

        StorageCellLot lot = new StorageCellLot(nextId, size, owner);
        lots.put(nextId, lot);
        return lot;
    }

    public void removeLot(int lotId) {
        lots.remove(lotId);
    }

    @Override
    public void read(CompoundNBT tag) {
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        ListNBT lotsTags = tag.getList(TAG_LOTS, Constants.NBT.TAG_COMPOUND);
        for (INBT lotsTag : lotsTags) {
            CompoundNBT lotsCompound = (CompoundNBT) lotsTag;

            int lotId = lotsCompound.getInt(TAG_LOT_ID);
            BlockPos lotSize = NBTUtil.readBlockPos(lotsCompound.getCompound(TAG_LOT_SIZE));
            int lotOwner = lotsCompound.getInt(TAG_LOT_OWNER);
            StorageCellLot lot = new StorageCellLot(lotId, lotSize, lotOwner);
            lots.put(lotId, lot);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);

        ListNBT lotTags = new ListNBT();
        for (StorageCellLot lot : lots.values()) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt(TAG_LOT_ID, lot.getId());
            nbt.putInt(TAG_LOT_OWNER, lot.getOwner());
            nbt.put(TAG_LOT_SIZE, NBTUtil.writeBlockPos(lot.getSize()));
            lotTags.add(nbt);
        }
        tag.put(TAG_LOTS, lotTags);

        return tag;
    }

}