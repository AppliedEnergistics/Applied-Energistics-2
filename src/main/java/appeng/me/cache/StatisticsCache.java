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

package appeng.me.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.statistics.MENetworkChunkEvent;
import appeng.api.util.DimensionalCoord;

public class StatisticsCache implements IGridCache {
    private IGrid grid;
    private Map<IWorld, Multiset<ChunkPos>> chunks;

    public StatisticsCache(final IGrid g) {
        this.grid = g;
        this.chunks = new HashMap<>();
    }

    @Override
    public void onUpdateTick() {
    }

    @Override
    public void removeNode(final IGridNode node, final IGridHost machine) {
        if (node.getGridBlock().isWorldAccessible()) {
            DimensionalCoord loc = node.getGridBlock().getLocation();

            this.removeChunk(loc.getWorld(), loc.getBlockPos());
        }
    }

    @Override
    public void addNode(final IGridNode node, final IGridHost machine) {
        if (node.getGridBlock().isWorldAccessible()) {
            DimensionalCoord loc = node.getGridBlock().getLocation();

            this.addChunk(loc.getWorld(), loc.getBlockPos());
        }
    }

    @Override
    public void onSplit(final IGridStorage storageB) {

    }

    @Override
    public void onJoin(final IGridStorage storageB) {

    }

    @Override
    public void populateGridStorage(final IGridStorage storage) {

    }

    public IGrid getGrid() {
        return grid;
    }

    /**
     * A set of all {@link IWorld} this grid spans.
     * 
     * @return
     */
    public Set<IWorld> worlds() {
        return this.chunks.keySet();
    }

    /**
     * A set of chunks this grid spans in a specific world.
     * 
     * @param world
     * @return
     */
    public Set<ChunkPos> chunks(IWorld world) {
        return this.chunks.get(world).elementSet();
    }

    public Map<IWorld, Multiset<ChunkPos>> getChunks() {
        return this.chunks;
    }

    /**
     * Mark the chunk of the {@link BlockPos} as location of the network.
     * 
     * @param world
     * @param pos
     * @return
     */
    private boolean addChunk(IWorld world, BlockPos pos) {
        final ChunkPos position = new ChunkPos(pos);

        if (!this.getChunks(world).contains(position)) {
            this.grid.postEvent(new MENetworkChunkEvent.MENetworkChunkAdded((ServerWorld) world, position));
        }

        return this.getChunks(world).add(position);
    }

    /**
     * Remove the chunk of this {@link BlockPos} from the network locations.
     * 
     * This uses a {@link Multiset} to ensure it will only marked as no longer containing a grid once all other
     * gridnodes are removed as well.
     * 
     * @param world
     * @param pos
     * @return
     */
    private boolean removeChunk(IWorld world, BlockPos pos) {
        final ChunkPos position = new ChunkPos(pos);
        boolean ret = this.getChunks(world).remove(position);

        if (ret && !this.getChunks(world).contains(position)) {
            this.grid.postEvent(new MENetworkChunkEvent.MENetworkChunkRemoved((ServerWorld) world, position));
        }

        this.clearWorld(world);

        return ret;
    }

    private Multiset<ChunkPos> getChunks(IWorld world) {
        return this.chunks.computeIfAbsent(world, w -> {
            return HashMultiset.create();
        });
    }

    /**
     * Cleanup the map in case a whole world is unloaded
     * 
     * @param world
     */
    private void clearWorld(IWorld world) {
        if (this.chunks.get(world).isEmpty()) {
            this.chunks.remove(world);
        }
    }
}
