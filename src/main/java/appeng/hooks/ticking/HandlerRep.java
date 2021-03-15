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

package appeng.hooks.ticking;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import appeng.me.Grid;
import appeng.tile.AEBaseTileEntity;

/**
 * A class to hold data related to ticking networks and tiles.
 */
class HandlerRep {

    private Map<IWorld, Map<Long, Queue<AEBaseTileEntity>>> tiles = new Object2ObjectOpenHashMap<>();
    private Set<Grid> networks = new ObjectOpenHashSet<>();
    private Set<Grid> toAdd = new ObjectOpenHashSet<>();
    private Set<Grid> toRemove = new ObjectOpenHashSet<>();

    public HandlerRep() {
    }

    /**
     * Resets all internal data
     * 
     */
    void clear() {
        this.tiles = new Object2ObjectOpenHashMap<>();
        this.networks = new HashSet<>();
        this.toAdd = new HashSet<>();
        this.toRemove = new HashSet<>();
    }

    /**
     * Add a new tile to be initializes in a later tick.
     * 
     * @param tile
     */
    synchronized void addTile(AEBaseTileEntity tile) {
        final IWorld world = tile.getWorld();
        final int x = tile.getPos().getX() >> 4;
        final int z = tile.getPos().getZ() >> 4;
        final long chunkPos = ChunkPos.asLong(x, z);

        Map<Long, Queue<AEBaseTileEntity>> worldQueue = this.tiles.get(world);

        Queue<AEBaseTileEntity> queue = worldQueue.computeIfAbsent(chunkPos, (key) -> {
            return new ArrayDeque<>();
        });

        queue.add(tile);
    }

    /**
     * Queues adding a new network.
     * 
     * Is added once {@link HandlerRep#updateNetworks()} is called.
     * 
     * Also removes it from the removal list, in case the network is validated again.
     * 
     * @param g
     */
    synchronized void addNetwork(Grid g) {
        this.toAdd.add(g);
        this.toRemove.remove(g);
    }

    /**
     * Queues removal of a network.
     * 
     * Is fully removed once {@link HandlerRep#updateNetworks()} is called.
     * 
     * Also removes it from the list to add in case it got invalid.
     * 
     * @param g
     */
    synchronized void removeNetwork(Grid g) {
        this.toRemove.add(g);
        this.toAdd.remove(g);
    }

    /**
     * Processes all networks to add or remove.
     * 
     * First all removals are handled, then the ones queued to be added.
     * 
     */
    synchronized void updateNetworks() {
        this.networks.removeAll(this.toRemove);
        this.toRemove.clear();

        this.networks.addAll(this.toAdd);
        this.toAdd.clear();
    }

    /**
     * Sets up the necessary defaults when a new world is loaded
     * 
     * @param world
     */
    synchronized public void addWorld(IWorld world) {
        this.tiles.computeIfAbsent(world, (key) -> {
            return new Long2ObjectOpenHashMap<>();
        });
    }

    /**
     * Tears down data related to a now unloaded world
     * 
     * @param world
     */
    synchronized void removeWorld(IWorld world) {
        this.tiles.remove(world);
    }

    /**
     * Removes a unloaded chunk within a world.
     * 
     * There is no related addWorldChunk. The necessary queue will be created once the first tile is added to a chunk to
     * save memory.
     * 
     * @param world
     * @param chunkPos
     */
    synchronized void removeWorldChunk(IWorld world, Long chunkPos) {
        this.tiles.get(world).remove(chunkPos);
    }

    /**
     * Get all registered {@link Grid}s
     * 
     * @return
     */
    public Set<Grid> getNetworks() {
        return networks;
    }

    /**
     * Get the tiles needing to be initialized in this specific {@link IWorld}.
     * 
     * @param world
     * @return
     */
    public Map<Long, Queue<AEBaseTileEntity>> getTiles(IWorld world) {
        return tiles.get(world);
    }

}