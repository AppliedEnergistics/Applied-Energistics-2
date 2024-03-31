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

package appeng.me.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridService;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.events.statistics.GridChunkEvent;
import appeng.me.InWorldGridNode;
import appeng.util.JsonStreamUtil;

/**
 * A grid providing precomupted statistics about a network.
 * <p>
 * Currently this tracks the chunks a network is occupying.
 */
public class StatisticsService implements IGridService, IGridServiceProvider {

    private final IGrid grid;

    /**
     * This uses a {@link Multiset} so we can simply add or remove {@link IGridNode} without having to take into account
     * that others still might exist without explicitly counting these.
     */
    private final Map<ServerLevel, Multiset<ChunkPos>> chunks;

    public StatisticsService(IGrid g) {
        this.grid = g;
        this.chunks = new HashMap<>();
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node instanceof InWorldGridNode inWorldNode) {
            this.removeChunk(inWorldNode.getLevel(), inWorldNode.getLocation());
        }
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        if (node instanceof InWorldGridNode inWorldNode) {
            this.addChunk(inWorldNode.getLevel(), inWorldNode.getLocation());
        }
    }

    public IGrid getGrid() {
        return grid;
    }

    /**
     * A set of all {@link ServerLevel} this grid spans.
     */
    public Set<ServerLevel> getLevels() {
        return this.chunks.keySet();
    }

    /**
     * A set of chunks this grid spans in a specific level.
     */
    public Set<ChunkPos> chunks(ServerLevel level) {
        return this.chunks.get(level).elementSet();
    }

    public Map<ServerLevel, Multiset<ChunkPos>> getChunks() {
        return this.chunks;
    }

    /**
     * Mark the chunk of the {@link BlockPos} as location of the network.
     */
    private boolean addChunk(ServerLevel level, BlockPos pos) {
        final ChunkPos position = new ChunkPos(pos);

        if (!this.getChunks(level).contains(position)) {
            this.grid.postEvent(new GridChunkEvent.GridChunkAdded(level, position));
        }

        return this.getChunks(level).add(position);
    }

    /**
     * Remove the chunk of this {@link BlockPos} from the network locations.
     * <p>
     * This uses a {@link Multiset} to ensure it will only marked as no longer containing a grid once all other
     * gridnodes are removed as well.
     */
    private boolean removeChunk(ServerLevel level, BlockPos pos) {
        final ChunkPos position = new ChunkPos(pos);
        boolean ret = this.getChunks(level).remove(position);

        if (ret && !this.getChunks(level).contains(position)) {
            this.grid.postEvent(new GridChunkEvent.GridChunkRemoved(level, position));
        }

        this.clearLevel(level);

        return ret;
    }

    private Multiset<ChunkPos> getChunks(ServerLevel level) {
        return this.chunks.computeIfAbsent(level, l -> HashMultiset.create());
    }

    /**
     * Cleanup the map in case a whole level is unloaded
     */
    private void clearLevel(ServerLevel level) {
        if (this.chunks.get(level).isEmpty()) {
            this.chunks.remove(level);
        }
    }

    @Override
    public void debugDump(JsonWriter writer) throws IOException {
        JsonStreamUtil.writeProperties(Map.<String, Object>of("chunks",
                chunks.keySet().stream().collect(
                        Collectors.toMap(
                                level -> level.dimension().location().toString(),
                                level -> chunks.get(level).elementSet().stream().map(JsonStreamUtil::toJson)
                                        .toList()))),
                writer);
    }
}
