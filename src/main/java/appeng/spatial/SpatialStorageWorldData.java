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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.List;

/**
 * Extra data attached to the spatial storage level.
 */
public class SpatialStorageWorldData extends SavedData {

    /**
     * ID of this data when it is attached to a level.
     */
    public static final String ID = "ae2_spatial_storage";

    public static final Codec<SpatialStorageWorldData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SpatialStoragePlot.CODEC.listOf().fieldOf("plots").forGetter(SpatialStorageWorldData::getPlots)
    ).apply(builder, SpatialStorageWorldData::new));

    private final Int2ObjectOpenHashMap<SpatialStoragePlot> plots = new Int2ObjectOpenHashMap<>();

    public SpatialStorageWorldData() {
    }

    public SpatialStorageWorldData(List<SpatialStoragePlot> plots) {
        for (var plot : plots) {
            this.plots.put(plot.getId(), plot);
        }
    }

    public SpatialStoragePlot getPlotById(int id) {
        return plots.get(id);
    }

    public List<SpatialStoragePlot> getPlots() {
        return List.copyOf(plots.values());
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
}
