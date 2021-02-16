package appeng.spatial;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import appeng.core.AELog;

/**
 * Extra data attached to the spatial storage world.
 */
public class SpatialStorageWorldData extends PersistentState {

    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "ae2_spatial_storage";

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 2;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_PLOTS = "plots";

    private final Int2ObjectOpenHashMap<SpatialStoragePlot> plots = new Int2ObjectOpenHashMap<>();

    public SpatialStorageWorldData() {
        super(ID);
    }

    public SpatialStoragePlot getPlotById(int id) {
        return plots.get(id);
    }

    public List<SpatialStoragePlot> getPlots() {
        return ImmutableList.copyOf(plots.values());
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
    public void fromTag(CompoundTag tag) {
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        ListTag plotsTag = tag.getList(TAG_PLOTS, NbtType.COMPOUND);
        for (Tag plotTag : plotsTag) {
            SpatialStoragePlot plot = SpatialStoragePlot.fromTag((CompoundTag) plotTag);

            if (plots.containsKey(plot.getId())) {
                AELog.warn("Overwriting duplicate plot id %s", plot.getId());
            }
            plots.put(plot.getId(), plot);
        }
    }

    @Override
    public CompoundTag toNbt(CompoundTag tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);

        ListTag plotTags = new ListTag();
        for (SpatialStoragePlot plot : plots.values()) {
            plotTags.add(plot.toTag());
        }
        tag.put(TAG_PLOTS, plotTags);

        return tag;
    }

}