/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.hooks;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;

import appeng.api.networking.IGridNode;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.packets.PaintedEntityPacket;
import appeng.crafting.CraftingJob;
import appeng.me.Grid;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.IWorldCallable;

public class TickHandler {

    private static TickHandler INSTANCE;
    private final Queue<IWorldCallable<?>> serverQueue = new ArrayDeque<>();
    private final Multimap<World, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final Map<WorldAccess, Queue<IWorldCallable<?>>> callQueue = new WeakHashMap<>();
    private final HandlerRep serverRepo = new HandlerRep();
    private final HashMap<Integer, PlayerColor> srvPlayerColors = new HashMap<>();

    public TickHandler() {
        if (INSTANCE != null) {
            throw new IllegalStateException("There can only be a single tick handler.");
        }
        INSTANCE = this;

        // Register for all the tick events we care about
        ServerTickEvents.END_SERVER_TICK.register(this::onAfterServerTick);
        ServerTickEvents.START_WORLD_TICK.register(this::onBeforeWorldTick);
        ServerTickEvents.END_WORLD_TICK.register(this::onAfterWorldTick);
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onUnloadChunk);
        ServerWorldEvents.UNLOAD.register(this::onUnloadWorld);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> serverRepo.clear());
    }

    public static TickHandler instance() {
        return INSTANCE;
    }

    public Map<Integer, PlayerColor> getPlayerColors() {
        return this.srvPlayerColors;
    }

    public void addCallable(final WorldAccess w, final IWorldCallable<?> c) {
        if (w == null) {
            this.serverQueue.add(c);
        } else {
            Queue<IWorldCallable<?>> queue = this.callQueue.get(w);

            if (queue == null) {
                queue = new ArrayDeque<>();
                this.callQueue.put(w, queue);
            }

            queue.add(c);
        }
    }

    public void addInit(final AEBaseBlockEntity tile) {
        // this is called client-side too during block entity initialization
        if (!tile.isClient()) {
            this.serverRepo.addTileToReady(tile);
        }
    }

    public void addNetwork(final Grid grid) {
        validateLogicalServerSide(grid);
        serverRepo.addNetwork(grid);
    }

    public void removeNetwork(final Grid grid) {
        validateLogicalServerSide(grid);
        serverRepo.removeNetwork(grid);
    }

    private void validateLogicalServerSide(final Grid grid) {
        IGridNode pivot = grid.getPivot();
        // yes it's @Nonnull but it may be null during removeNetwork
        if (pivot != null) {
            Preconditions.checkArgument(!pivot.getWorld().isClient());
        }
    }

    public Iterable<Grid> getGridList() {
        return serverRepo.networks;
    }

    public void onUnloadChunk(ServerWorld world, WorldChunk chunk) {
        final Map<ChunkPos, Queue<AEBaseBlockEntity>> worldQueue = serverRepo.tilesToReady.get(world);
        if (worldQueue != null) {
            worldQueue.remove(chunk.getPos());
            if (worldQueue.size() == 0) {
                serverRepo.tilesToReady.remove(world);
            }
        }
    }

    public void onUnloadWorld(MinecraftServer server, ServerWorld world) {
        final List<IGridNode> toDestroy = new ArrayList<>();

        this.serverRepo.updateNetworks();
        for (final Grid g : this.serverRepo.networks) {
            for (final IGridNode n : g.getNodes()) {
                if (n.getWorld() == world) {
                    toDestroy.add(n);
                }
            }
        }

        for (final IGridNode n : toDestroy) {
            n.destroy();
        }
    }

    private void onBeforeWorldTick(ServerWorld world) {
        final Queue<IWorldCallable<?>> queue = this.callQueue.get(world);
        this.processQueue(queue, world);
    }

    private void onAfterWorldTick(ServerWorld world) {
        simulateCraftingJobs(world);
        readyTiles(world);
    }

    private void onAfterServerTick(MinecraftServer server) {
        this.tickColors(this.srvPlayerColors);

        // tick networks.
        this.serverRepo.updateNetworks();
        for (final Grid g : this.serverRepo.networks) {
            g.update();
        }

        // cross world queue.
        this.processQueue(this.serverQueue, null);
    }

    public void registerCraftingSimulation(final World world, final CraftingJob craftingJob) {
        synchronized (this.craftingJobs) {
            this.craftingJobs.put(world, craftingJob);
        }
    }

    /**
     * Simulates the current crafting requests before they user can submit them to be processed.
     */
    private void simulateCraftingJobs(World world) {
        synchronized (this.craftingJobs) {
            final Collection<CraftingJob> jobSet = this.craftingJobs.get(world);
            if (!jobSet.isEmpty()) {
                final int jobSize = jobSet.size();
                final int microSecondsPerTick = AEConfig.instance().getCraftingCalculationTimePerTick() * 1000;
                final int simTime = Math.max(1, microSecondsPerTick / jobSize);
                final Iterator<CraftingJob> i = jobSet.iterator();
                while (i.hasNext()) {
                    final CraftingJob cj = i.next();
                    if (!cj.simulateFor(simTime)) {
                        i.remove();
                    }
                }
            }
        }
    }

    /**
     * Ready the tiles in this world.
     */
    private void readyTiles(World world) {
        final Map<ChunkPos, Queue<AEBaseBlockEntity>> worldQueue = serverRepo.tilesToReady.getOrDefault(world,
                Collections.emptyMap());

        Iterator<Map.Entry<ChunkPos, Queue<AEBaseBlockEntity>>> it = worldQueue.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkPos, Queue<AEBaseBlockEntity>> entry = it.next();

            ChunkPos pos = entry.getKey();
            ChunkManager chunkManager = world.getChunkManager();

            // Using the blockpos of the chunk start to test if it can tick.
            // Relies on the world to test the chunkpos and not the explicit blockpos.
            BlockPos testBlockPos = new BlockPos(pos.getStartX(), 0, pos.getStartZ());

            // Readies this chunk, if it can tick and does exist.
            // Chunks which are considered a border chunk will not "exist", but are loaded. Once this state changes they
            // will be readied.
            if (world.isChunkLoaded(pos.x, pos.z) && chunkManager.shouldTickBlock(testBlockPos)) {
                Queue<AEBaseBlockEntity> queue = entry.getValue();

                while (!queue.isEmpty()) {
                    final AEBaseBlockEntity bt = queue.poll();

                    // Only ready tile entities which weren't destroyed in the meantime.
                    if (!bt.isRemoved()) {
                        bt.onReady();
                    }
                }

                // cleanup empty chunk queue
                it.remove();
            }
        }
    }

    protected void tickColors(final Map<Integer, PlayerColor> playerSet) {
        final Iterator<PlayerColor> i = playerSet.values().iterator();

        while (i.hasNext()) {
            final PlayerColor pc = i.next();
            if (pc.ticksLeft <= 0) {
                i.remove();
            }
            pc.ticksLeft--;
        }
    }

    private void processQueue(final Queue<IWorldCallable<?>> queue, final World world) {
        if (queue == null) {
            return;
        }

        final Stopwatch sw = Stopwatch.createStarted();

        IWorldCallable<?> c = null;
        while ((c = queue.poll()) != null) {
            try {
                c.call(world);

                if (sw.elapsed(TimeUnit.MILLISECONDS) > 50) {
                    break;
                }
            } catch (final Exception e) {
                AELog.debug(e);
            }
        }
    }

    private static class HandlerRep {

        private final Map<World, Map<ChunkPos, Queue<AEBaseBlockEntity>>> tilesToReady = new HashMap<>();
        private final Set<Grid> networks = new HashSet<>();
        private final Set<Grid> toAdd = new HashSet<>();
        private final Set<Grid> toRemove = new HashSet<>();

        private synchronized void addTileToReady(AEBaseBlockEntity tile) {
            World world = tile.getWorld();
            ChunkPos chunkPos = new ChunkPos(tile.getPos());

            Map<ChunkPos, Queue<AEBaseBlockEntity>> worldQueue = tilesToReady.computeIfAbsent(world,
                    w -> new HashMap<>());

            Queue<AEBaseBlockEntity> chunkQueue = worldQueue.computeIfAbsent(chunkPos, cp -> new ArrayDeque<>());

            chunkQueue.add(tile);
        }

        private synchronized void addNetwork(Grid g) {
            this.toAdd.add(g);
            this.toRemove.remove(g);
        }

        private synchronized void removeNetwork(Grid g) {
            this.toRemove.add(g);
            this.toAdd.remove(g);
        }

        private synchronized void updateNetworks() {
            this.networks.removeAll(this.toRemove);
            this.toRemove.clear();

            this.networks.addAll(this.toAdd);
            this.toAdd.clear();
        }

        private synchronized void clear() {
            if (!tilesToReady.isEmpty()) {
                tilesToReady.clear();
                AELog.warn("tilesToReady should be empty at server shutdown.");
            }
            if (!networks.isEmpty()) {
                networks.clear();
                AELog.warn("networks should be empty at server shutdown.");
            }
            if (!toAdd.isEmpty()) {
                toAdd.clear();
                AELog.warn("toAdd should be empty at server shutdown.");
            }
            if (!toRemove.isEmpty()) {
                toRemove.clear();
                AELog.warn("toRemove should be empty at server shutdown.");
            }
        }
    }

    public static class PlayerColor {

        public final AEColor myColor;
        private final int myEntity;
        private int ticksLeft;

        public PlayerColor(final int id, final AEColor col, final int ticks) {
            this.myEntity = id;
            this.myColor = col;
            this.ticksLeft = ticks;
        }

        public PaintedEntityPacket getPacket() {
            return new PaintedEntityPacket(this.myEntity, this.myColor, this.ticksLeft);
        }
    }
}
