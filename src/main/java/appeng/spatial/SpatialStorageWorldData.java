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

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.util.Constants;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import appeng.core.AELog;

/**
 * Extra data attached to the spatial storage world.
 */
public class SpatialStorageWorldData extends SavedData {

    /**
     * ID of this data when it is attached to a world.
     */
    public static final String ID = "ae2_spatial_storage";

    // Used to allow forward compatibility
    private static final int CURRENT_FORMAT = 2;

    private static final String TAG_FORMAT = "format";

    private static final String TAG_PLOTS = "plots";

    private final Int2ObjectOpenHashMap<SpatialStoragePlot> plots = new Int2ObjectOpenHashMap<>();

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
        setDirty();
        return plot;
    }

    public void removePlot(int plotId) {
        plots.remove(plotId);
        setDirty();
    }

    public void setLastTransition(int plotId, TransitionInfo info) {
        SpatialStoragePlot plot = plots.get(plotId);
        if (plot != null) {
            plot.setLastTransition(info);
        }
        setDirty();
    }

    public static SpatialStorageWorldData load(CompoundTag tag) {
        SpatialStorageWorldData result = new SpatialStorageWorldData();
        int version = tag.getInt(TAG_FORMAT);
        if (version != CURRENT_FORMAT) {
            // Currently no new format has been defined, as such anything but the current
            // version is invalid
            throw new IllegalStateException("Invalid AE2 spatial info version: " + version);
        }

        ListTag plotsTag = tag.getList(TAG_PLOTS, Constants.NBT.TAG_COMPOUND);
        for (Tag plotTag : plotsTag) {
            SpatialStoragePlot plot = SpatialStoragePlot.fromTag((CompoundTag) plotTag);

            if (result.plots.containsKey(plot.getId())) {
                AELog.warn("Overwriting duplicate plot id %s", plot.getId());
            }
            result.plots.put(plot.getId(), plot);
        }
        return result;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(TAG_FORMAT, CURRENT_FORMAT);

        ListTag plotTags = new ListTag();
        for (SpatialStoragePlot plot : plots.values()) {
            plotTags.add(plot.toTag());
        }
        tag.put(TAG_PLOTS, plotTags);

        return tag;
    }

}
