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
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.LogicalSide;

import appeng.api.parts.CableRenderMode;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEngClient;
import appeng.crafting.CraftingJob;
import appeng.items.misc.PaintBallItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.IWorldRunnable;
import appeng.util.Platform;

public class TickHandler {

    /**
     * Time limit for process queues with respect to the 50ms of a minecraft tick.
     */
    private static final int TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS = 25;

    private static final TickHandler INSTANCE = new TickHandler();
    private final Queue<IWorldRunnable> serverQueue = new ArrayDeque<>();
    private final Multimap<LevelAccessor, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final Map<LevelAccessor, Queue<IWorldRunnable>> callQueue = new HashMap<>();
    private final ServerBlockEntityRepo tiles = new ServerBlockEntityRepo();
    private final ServerGridRepo grids = new ServerGridRepo();
    private final Map<Integer, PlayerColor> cliPlayerColors = new HashMap<>();
    private final Map<Integer, PlayerColor> srvPlayerColors = new HashMap<>();
    private CableRenderMode crm = CableRenderMode.STANDARD;

    /**
     * A stop watch to limit processing the additional queues to honor
     * {@link TickHandler#TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS}.
     * <p>
     * This cumulative for all queues of one server tick.
     */
    private final Stopwatch sw = Stopwatch.createUnstarted();
    private int processQueueElementsProcessed = 0;
    private int processQueueElementsRemaining = 0;

    public static TickHandler instance() {
        return INSTANCE;
    }

    private TickHandler() {
    }

    public Map<Integer, PlayerColor> getPlayerColors() {
        if (Platform.isServer()) {
            return this.srvPlayerColors;
        }
        return this.cliPlayerColors;
    }

    public void addCallable(final LevelAccessor w, Runnable c) {
        addCallable(w, ignored -> c.run());
    }

    /**
     * Add a server or world callback which gets called the next time the queue is ticked.
     * <p>
     * Callbacks on the client are not support.
     * <p>
     * Using null as world will queue it into the global {@link ServerTickEvent}, otherwise it will be ticked with the
     * corresponding {@link WorldTickEvent}.
     *
     * @param w null or the specific {@link Level}
     * @param c the callback
     */
    public void addCallable(final LevelAccessor w, IWorldRunnable c) {
        Preconditions.checkArgument(w == null || !w.isClientSide(), "Can only register serverside callbacks");

        if (w == null) {
            this.serverQueue.add(c);
        } else {
            Queue<IWorldRunnable> queue = this.callQueue.get(w);

            if (queue == null) {
                queue = new ArrayDeque<>();
                this.callQueue.put(w, queue);
            }

            queue.add(c);
        }
    }

    /**
     * Add a {@link AEBaseBlockEntity} to be initializes with the next update.
     * <p>
     * Must be called on the server.
     *
     * @param tile to be added, must be not null
     */
    public void addInit(final AEBaseBlockEntity tile) {
        // for no there is no reason to care about this on the client...
        if (!tile.getLevel().isClientSide()) {
            Objects.requireNonNull(tile);
            this.tiles.addTile(tile);
        }
    }

    /**
     * Add a new grid for ticking on the next update.
     * <p>
     * Must only be called on the server.
     *
     * @param grid the {@link Grid} to add, must be not null
     */
    public void addNetwork(final Grid grid) {
        Platform.assertServerThread();

        this.grids.addNetwork(grid);
    }

    /**
     * Mark a {@link Grid} to be removed with the next update.
     * <p>
     * Must only be called on the server.
     *
     * @param grid the {@link Grid} to remove, must be not null
     */
    public void removeNetwork(final Grid grid) {
        Platform.assertServerThread();

        this.grids.removeNetwork(grid);
    }

    public Iterable<Grid> getGridList() {
        Platform.assertServerThread();
        return this.grids.getNetworks();
    }

    public void shutdown() {
        Platform.assertServerThread();
        this.tiles.clear();
        this.grids.clear();
    }

    /**
     * Handles a chunk being unloaded (on the server)
     * <p>
     * Removes any pending initialization callbacks for tile-entities in that chunk.
     */
    public void onUnloadChunk(final ChunkEvent.Unload ev) {
        if (!ev.getWorld().isClientSide()) {
            this.tiles.removeWorldChunk(ev.getWorld(), ev.getChunk().getPos().toLong());
        }
    }

    /**
     * Handle a newly loaded world and setup defaults when necessary.
     */
    public void onLoadWorld(final WorldEvent.Load ev) {
        if (!ev.getWorld().isClientSide()) {
            this.tiles.addWorld(ev.getWorld());
        }
    }

    /**
     * Handle a world unload and tear down related data structures.
     */
    public void onUnloadWorld(final WorldEvent.Unload ev) {
        // for no there is no reason to care about this on the client...
        if (!ev.getWorld().isClientSide()) {
            var toDestroy = new ArrayList<GridNode>();

            this.grids.updateNetworks();
            for (final Grid g : this.grids.getNetworks()) {
                for (var n : g.getNodes()) {
                    if (n.getWorld() == ev.getWorld()) {
                        toDestroy.add((GridNode) n);
                    }
                }
            }

            for (var n : toDestroy) {
                n.destroy();
            }

            this.tiles.removeWorld(ev.getWorld());
            this.callQueue.remove(ev.getWorld());
        }
    }

    /**
     * Client side ticking similar to the global server tick.
     */
    @OnlyIn(Dist.CLIENT)
    public void onClientTick(final ClientTickEvent ev) {
        if (ev.phase == Phase.START) {
            this.tickColors(this.cliPlayerColors);
            final CableRenderMode currentMode = Api.instance().partHelper().getCableRenderMode();

            // Handle changes to the cable-rendering mode
            if (currentMode != this.crm) {
                this.crm = currentMode;
                AppEngClient.instance().triggerUpdates();
            }
        }
    }

    /**
     * Tick a single {@link Level}
     * <p>
     * This can happen multiple times per world, but each world should only be ticked once per minecraft tick.
     */
    public void onWorldTick(final WorldTickEvent ev) {
        var level = ev.world;

        if (!(level instanceof ServerLevel serverLevel) || ev.side != LogicalSide.SERVER) {
            // While forge doesn't generate this event for client worlds,
            // the event is generic enough that some other mod might be insane enough to do so.
            return;
        }

        if (ev.phase == Phase.START) {
            final Queue<IWorldRunnable> queue = this.callQueue.get(level);
            processQueueElementsRemaining += this.processQueue(queue, level);
        } else if (ev.phase == Phase.END) {
            this.simulateCraftingJobs(level);
            this.readyTiles(serverLevel);
        }
    }

    /**
     * Tick everything related to a the global server tick once per minecraft tick.
     */
    public void onServerTick(final ServerTickEvent ev) {
        if (ev.phase == Phase.START) {
            // Reset the stop watch on the start of each server tick.
            this.processQueueElementsProcessed = 0;
            this.processQueueElementsRemaining = 0;
            this.sw.reset();
        }

        if (ev.phase == Phase.END) {
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
    }

    public void registerCraftingSimulation(final Level world, final CraftingJob craftingJob) {
        Preconditions.checkArgument(!world.isClientSide, "Trying to register a crafting job for a client-world");

        synchronized (this.craftingJobs) {
            this.craftingJobs.put(world, craftingJob);
        }
    }

    /**
     * Simulates the current crafting requests before they user can submit them to be processed.
     */
    private void simulateCraftingJobs(LevelAccessor world) {
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
     * Ready the tiles in this world. server-side only.
     */
    private void readyTiles(ServerLevel world) {
        var chunkProvider = world.getChunkSource();

        var worldQueue = tiles.getTiles(world);

        // Make a copy because this set may be modified
        // when new chunks are loaded by an onReady call below
        long[] workSet = worldQueue.keySet().toLongArray();

        for (long packedChunkPos : workSet) {
            // Readies all of our block entities in this chunk as soon as it can tick BEs
            // The following test is equivalent to ServerLevel#isPositionTickingWithEntitiesLoaded
            if (world.areEntitiesLoaded(packedChunkPos) && chunkProvider.isPositionTicking(packedChunkPos)) {
                // Take the currently waiting tiles for this chunk and ready them all. Should more tiles be added to
                // this chunk while we're working on it, a new list will be added automatically and we'll work on this
                // chunk again next tick.
                var chunkQueue = worldQueue.remove(packedChunkPos);
                if (chunkQueue == null) {
                    AELog.warn("Chunk %s was unloaded while we were readying tiles", new ChunkPos(packedChunkPos));
                    continue; // This should never happen, chunk unloaded under our noses
                }

                for (var bt : chunkQueue) {
                    // Only ready block entities which weren't destroyed in the meantime.
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
     * Process the {@link IWorldRunnable} queue in this {@link Level}
     * <p>
     * This has a hard limit of about 50 ms before deferring further processing into the next tick.
     *
     * @param queue the queue to process
     * @param world the world in which the queue is processed or null for the server queue
     * @return the amount of remaining callbacks
     */
    private int processQueue(final Queue<IWorldRunnable> queue, final Level world) {
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
