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

package appeng.hooks.ticking;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import appeng.api.networking.IGridNode;
import appeng.api.parts.CableRenderMode;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.crafting.CraftingJob;
import appeng.items.misc.PaintBallItem;
import appeng.me.Grid;
import appeng.tile.AEBaseTileEntity;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;

public class TickHandler {

    /**
     * Time limit for process queues with respect to the 50ms of a minecraft tick.
     */
    private static final int TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS = 25;

    private static final TickHandler INSTANCE = new TickHandler();
    private final Queue<IWorldCallable<?>> serverQueue = new ArrayDeque<>();
    private final Multimap<IWorld, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final Map<IWorld, Queue<IWorldCallable<?>>> callQueue = new HashMap<>();
    private final ServerTileRepo tiles = new ServerTileRepo();
    private final ServerGridRepo grids = new ServerGridRepo();
    private final Map<Integer, PlayerColor> srvPlayerColors = new HashMap<>();

    /**
     * A stop watch to limit processing the additional queues to honor
     * {@link TickHandler#TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS}.
     *
     * This cumulative for all queues of one server tick.
     */
    private final Stopwatch sw = Stopwatch.createUnstarted();
    private int processQueueElementsProcessed = 0;
    private int processQueueElementsRemaining = 0;

    public TickHandler() {
        // Register for all the tick events we care about
        ServerTickEvents.START_SERVER_TICK.register(this::onBeforeServerTick);
        ServerTickEvents.END_SERVER_TICK.register(this::onAfterServerTick);
        ServerTickEvents.START_WORLD_TICK.register(this::onBeforeWorldTick);
        ServerTickEvents.END_WORLD_TICK.register(this::onAfterWorldTick);
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onUnloadChunk);
        ServerWorldEvents.LOAD.register(this::onLoadWorld);
        ServerWorldEvents.UNLOAD.register(this::onUnloadWorld);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
    }

    public static TickHandler instance() {
        return INSTANCE;
    }

    public Map<Integer, PlayerColor> getPlayerColors() {
        return this.srvPlayerColors;
    }

    /**
     * Add a server or world callback which gets called the next time the queue is ticked.
     *
     * Callbacks on the client are not support.
     * <p>
     * Using null as world will queue it into the global {@link ServerTickEvent}, otherwise it will be ticked with the
     * corresponding {@link WorldTickEvent}.
     *
     * @param w null or the specific {@link World}
     * @param c the callback
     */
    public void addCallable(final IWorld w, final IWorldCallable<?> c) {
        Preconditions.checkArgument(w == null || !w.isRemote(), "Can only register serverside callbacks");

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

    /**
     * Add a {@link AEBaseTileEntity} to be initializes with the next update.
     *
     * Must be called on the server.
     *
     * @param tile to be added, must be not null
     */
    public void addInit(final AEBaseTileEntity tile) {
        // for no there is no reason to care about this on the client...
        if (!tile.getWorld().isRemote()) {
            Objects.requireNonNull(tile);
            this.tiles.addTile(tile);
        }
    }

    /**
     * Add a new grid for ticking on the next update.
     *
     * Must only be called on the server.
     *
     * @param grid the {@link Grid} to add, must be not null
     */
    public void addNetwork(final Grid grid) {
        validateLogicalServerSide(grid);

        this.grids.addNetwork(grid);
    }

    /**
     * Mark a {@link Grid} to be removed with the next update.
     *
     * Must only be called on the server.
     *
     * @param grid the {@link Grid} to remove, must be not null
     */
    public void removeNetwork(final Grid grid) {
        validateLogicalServerSide(grid);

        this.grids.removeNetwork(grid);
    }

    private void validateLogicalServerSide(final Grid grid) {
        IGridNode pivot = grid.getPivot();
        // yes it's @Nonnull but it may be null during removeNetwork
        if (pivot != null) {
            Preconditions.checkArgument(!pivot.getWorld().isRemote());
        }
    }

    public Iterable<Grid> getGridList() {
        Platform.assertServerThread();
        return this.grids.getNetworks();
    }

    public void onServerStopped(MinecraftServer server) {
        Platform.assertServerThread();
        this.tiles.clear();
        this.grids.clear();
    }

    /**
     * Handles a chunk being unloaded (on the server)
     *
     * Removes any pending initialization callbacks for tile-entities in that chunk.
     */
    public void onUnloadChunk(ServerWorld world, Chunk chunk) {
        this.tiles.removeWorldChunk(world, chunk.getPos().asLong());
    }

    /**
     * Handle a newly loaded world and setup defaults when necessary.
     */
    public void onLoadWorld(MinecraftServer server, ServerWorld world) {
        this.tiles.addWorld(world);
    }

    /**
     * Handle a world unload and tear down related data structures.
     */
    public void onUnloadWorld(MinecraftServer server, ServerWorld world) {
        // for no there is no reason to care about this on the client...
        if (!world.isRemote()) {
            final List<IGridNode> toDestroy = new ArrayList<>();

            this.grids.updateNetworks();
            for (final Grid g : this.grids.getNetworks()) {
                for (final IGridNode n : g.getNodes()) {
                    if (n.getWorld() == world) {
                        toDestroy.add(n);
                    }
                }
            }

            for (final IGridNode n : toDestroy) {
                n.destroy();
            }

            this.tiles.removeWorld(world);
            this.callQueue.remove(world);
        }
    }

    private void onBeforeWorldTick(ServerWorld world) {
        final Queue<IWorldCallable<?>> queue = this.callQueue.get(world);
        processQueueElementsRemaining += this.processQueue(queue, world);
    }

    private void onAfterWorldTick(ServerWorld world) {
        this.simulateCraftingJobs(world);
        this.readyTiles(world);
    }

    private void onBeforeServerTick(MinecraftServer server) {
        // Reset the stop watch on the start of each server tick.
        this.processQueueElementsProcessed = 0;
        this.processQueueElementsRemaining = 0;
        this.sw.reset();
    }

    private void onAfterServerTick(MinecraftServer server) {
        this.tickColors(this.srvPlayerColors);

        // tick networks.
        this.grids.updateNetworks();
        for (final Grid g : this.grids.getNetworks()) {
            g.update();
        }

        // cross world queue.
        processQueueElementsRemaining += this.processQueue(this.serverQueue, null);

        if (this.sw.elapsed(TimeUnit.MILLISECONDS) > TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS) {
            AELog.warn("Exceeded time limit of %d ms after processing %d queued tick callbacks (%d remain)",
                    TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS, processQueueElementsProcessed,
                    processQueueElementsRemaining);
        }
    }

    public void registerCraftingSimulation(final World world, final CraftingJob craftingJob) {
        Preconditions.checkArgument(!world.isRemote, "Trying to register a crafting job for a client-world");

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
     * Ready the tiles in this world
     */
    private void readyTiles(ServerWorld world) {
        AbstractChunkProvider chunkProvider = world.getChunkProvider();

        final Long2ObjectMap<List<AEBaseTileEntity>> worldQueue = tiles.getTiles(world);

        // Make a copy because this set may be modified
        // when new chunks are loaded by an onReady call below
        long[] workSet = worldQueue.keySet().toLongArray();

        for (long packedChunkPos : workSet) {
            ChunkPos chunkPos = new ChunkPos(packedChunkPos);

            // Using the blockpos of the chunk start to test if it can tick.
            // Relies on the world to test the chunkpos and not the explicit blockpos.
            BlockPos testBlockPos = new BlockPos(chunkPos.getXStart(), 0, chunkPos.getZStart());

            // Readies this chunk, if it can tick and does exist.
            // Chunks which are considered a border chunk will not "exist", but are loaded. Once this state changes they
            // will be readied.
            if (world.chunkExists(chunkPos.x, chunkPos.z) && chunkProvider.canTick(testBlockPos)) {
                // Take the currently waiting tiles for this chunk and ready them all. Should more tiles be added to
                // this chunk while we're working on it, a new list will be added automatically and we'll work on this
                // chunk again next tick.
                List<AEBaseTileEntity> chunkQueue = worldQueue.remove(packedChunkPos);
                if (chunkQueue == null) {
                    AELog.warn("Chunk %s was unloaded while we were readying tiles", chunkPos);
                    continue; // This should never happen, chunk unloaded under our noses
                }

                for (AEBaseTileEntity bt : chunkQueue) {
                    // Only ready tile entites which weren't destroyed in the meantime.
                    if (!bt.isRemoved()) {
                        // Note that this can load more chunks, but they'll at the earliest
                        // be initialized on the next tick
                        bt.onReady();
                    }
                }
            }
        }
    }

    /**
     * Tick all currently players having a color applied by a {@link PaintBallItem}.
     */
    private void tickColors(final Map<Integer, PlayerColor> playerSet) {
        final Iterator<PlayerColor> i = playerSet.values().iterator();

        while (i.hasNext()) {
            final PlayerColor pc = i.next();
            if (pc.isDone()) {
                i.remove();
            }
            pc.tick();
        }
    }

    /**
     * Process the {@link IWorldCallable} queue in this {@link World}
     *
     * This has a hard limit of about 50 ms before deferring further processing into the next tick.
     *
     * @param queue the queue to process
     * @param world the world in which the queue is processed or null for the server queue
     *
     * @return the amount of remaining callbacks
     */
    private int processQueue(final Queue<IWorldCallable<?>> queue, final World world) {
        if (queue == null) {
            return 0;
        }

        // start the clock
        sw.start();

        while (!queue.isEmpty()) {
            try {
                // call the first queue element.
                queue.poll().call(world);
                this.processQueueElementsProcessed++;

                if (sw.elapsed(TimeUnit.MILLISECONDS) > TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS) {
                    break;
                }
            } catch (final Exception e) {
                AELog.warn(e);
            }
        }

        // stop watch for the next call
        sw.stop();

        return queue.size();
    }
}
