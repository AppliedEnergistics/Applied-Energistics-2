package appeng.spatial;

import appeng.core.AELog;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

/**
 * Extra data attached to the spatial storage world.
 */
public class SpatialStorageWorldData extends WorldSavedData {

    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "ae2_spatial_storage";

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 2;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_PLOTS = "plots";

    private static final String TAG_PLOT_ID = "id";

    private static final String TAG_PLOT_SIZE = "size";

    private static final String TAG_PLOT_OWNER = "owner";

    private final Int2ObjectOpenHashMap<SpatialStoragePlot> plots = new Int2ObjectOpenHashMap<>();

    public SpatialStorageWorldData() {
        super(ID);
    }

    public SpatialStoragePlot getPlotById(int id) {
        return plots.get(id);
    }

    public SpatialStoragePlot allocatePlot(BlockPos size, int owner) {

        int nextId = 1;
        for (int id : plots.keySet()) {
            if (id >= nextId) {
                nextId = id + 1;
            }
        }

        SpatialStoragePlot plot = new SpatialStoragePlot(nextId, size, owner);
        plots.put(nextId, plot);
        markDirty();
        return plot;
    }

    public void removePlot(int plotId) {
        plots.remove(plotId);
        markDirty();
    }

    public void setLastTransition(int plotId, TransitionInfo info) {
        SpatialStoragePlot plot = plots.get(plotId);
        if (plot != null) {
            plot.setLastTransition(info);
        }
        markDirty();
    }

    @Override
    public void read(CompoundNBT tag) {
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        ListNBT plotsTag = tag.getList(TAG_PLOTS, Constants.NBT.TAG_COMPOUND);
        for (INBT plotTag : plotsTag) {
            CompoundNBT plotCompound = (CompoundNBT) plotTag;

            int plotId = plotCompound.getInt(TAG_PLOT_ID);
            if (plots.containsKey(plotId)) {
                AELog.warn("Overwriting duplicate plot id %s", plotId);
            }

            BlockPos plotSize = NBTUtil.readBlockPos(plotCompound.getCompound(TAG_PLOT_SIZE));
            int plotOwner = plotCompound.getInt(TAG_PLOT_OWNER);
            plots.put(plotId, new SpatialStoragePlot(plotId, plotSize, plotOwner));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);

        ListNBT plotTags = new ListNBT();
        for (SpatialStoragePlot plot : plots.values()) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt(TAG_PLOT_ID, plot.getId());
            nbt.putInt(TAG_PLOT_OWNER, plot.getOwner());
            nbt.put(TAG_PLOT_SIZE, NBTUtil.writeBlockPos(plot.getSize()));
            plotTags.add(nbt);
        }
        tag.put(TAG_PLOTS, plotTags);

        return tag;
    }

}