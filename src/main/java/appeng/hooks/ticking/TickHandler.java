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
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEngClient;
import appeng.crafting.CraftingJob;
import appeng.items.misc.PaintBallItem;
import appeng.me.Grid;
import appeng.me.GridNode;
import appeng.util.ILevelRunnable;
import appeng.util.Platform;

public class TickHandler {

    /**
     * Time limit for process queues with respect to the 50ms of a minecraft tick.
     */
    private static final int TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS = 25;

    private static final TickHandler INSTANCE = new TickHandler();
    private final Queue<ILevelRunnable> serverQueue = new ArrayDeque<>();
    private final Multimap<LevelAccessor, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final Map<LevelAccessor, Queue<ILevelRunnable>> callQueue = new HashMap<>();
    private final ServerBlockEntityRepo blockEntities = new ServerBlockEntityRepo();
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

    public void addCallable(final LevelAccessor level, Runnable c) {
        addCallable(level, ignored -> c.run());
    }

    /**
     * Add a server or level callback which gets called the next time the queue is ticked.
     * <p>
     * Callbacks on the client are not support.
     * <p>
     * Using null as level will queue it into the global {@link ServerTickEvent}, otherwise it will be ticked with the
     * corresponding {@link WorldTickEvent}.
     *
     * @param level null or the specific {@link Level}
     * @param c     the callback
     */
    public void addCallable(LevelAccessor level, ILevelRunnable c) {
        Preconditions.checkArgument(level == null || !level.isClientSide(), "Can only register serverside callbacks");

        if (level == null) {
            this.serverQueue.add(c);
        } else {
            Queue<ILevelRunnable> queue = this.callQueue.get(level);

            if (queue == null) {
                queue = new ArrayDeque<>();
                this.callQueue.put(level, queue);
            }

            queue.add(c);
        }
    }

    /**
     * Add a {@link AEBaseBlockEntity} to be initializes with the next update.
     * <p>
     * Must be called on the server.
     *
     * @param blockEntity to be added, must be not null
     */
    public void addInit(final AEBaseBlockEntity blockEntity) {
        // for no there is no reason to care about this on the client...
        if (!blockEntity.getLevel().isClientSide()) {
            Objects.requireNonNull(blockEntity);
            this.blockEntities.addBlockEntity(blockEntity);
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
        this.blockEntities.clear();
        this.grids.clear();
    }

    /**
     * Handles a chunk being unloaded (on the server)
     * <p>
     * Removes any pending initialization callbacks for block entities in that chunk.
     */
    public void onUnloadChunk(final ChunkEvent.Unload ev) {
        if (!ev.getWorld().isClientSide()) {
            this.blockEntities.removeChunk(ev.getWorld(), ev.getChunk().getPos().toLong());
        }
    }

    /**
     * Handle a newly loaded level and setup defaults when necessary.
     */
    public void onLoadWorld(final WorldEvent.Load ev) {
        if (!ev.getWorld().isClientSide()) {
            this.blockEntities.addLevel(ev.getWorld());
        }
    }

    /**
     * Handle a level unload and tear down related data structures.
     */
    public void onUnloadWorld(final WorldEvent.Unload ev) {
        // for no there is no reason to care about this on the client...
        if (!ev.getWorld().isClientSide()) {
            var toDestroy = new ArrayList<GridNode>();

            this.grids.updateNetworks();
            for (final Grid g : this.grids.getNetworks()) {
                for (var n : g.getNodes()) {
                    if (n.getLevel() == ev.getWorld()) {
                        toDestroy.add((GridNode) n);
                    }
                }
            }

            for (var n : toDestroy) {
                n.destroy();
            }

            this.blockEntities.removeLevel(ev.getWorld());
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
     * This can happen multiple times per level, but each level should only be ticked once per minecraft tick.
     */
    public void onWorldTick(final WorldTickEvent ev) {
        var level = ev.world;

        if (!(level instanceof ServerLevel serverLevel) || ev.side != LogicalSide.SERVER) {
            // While forge doesn't generate this event for client worlds,
            // the event is generic enough that some other mod might be insane enough to do so.
            return;
        }

        if (ev.phase == Phase.START) {
            final Queue<ILevelRunnable> queue = this.callQueue.get(level);
            processQueueElementsRemaining += this.processQueue(queue, level);
        } else if (ev.phase == Phase.END) {
            this.simulateCraftingJobs(level);
            this.readyBlockEntities(serverLevel);
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

            // cross level queue.
            processQueueElementsRemaining += this.processQueue(this.serverQueue, null);

            if (this.sw.elapsed(TimeUnit.MILLISECONDS) > TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS) {
                AELog.warn("Exceeded time limit of %d ms after processing %d queued tick callbacks (%d remain)",
                        TIME_LIMIT_PROCESS_QUEUE_MILLISECONDS, processQueueElementsProcessed,
                        processQueueElementsRemaining);
            }
        }
    }

    public void registerCraftingSimulation(final Level level, final CraftingJob craftingJob) {
        Preconditions.checkArgument(!level.isClientSide, "Trying to register a crafting job for a client-level");

        synchronized (this.craftingJobs) {
            this.craftingJobs.put(level, craftingJob);
        }
    }

    /**
     * Simulates the current crafting requests before they user can submit them to be processed.
     */
    private void simulateCraftingJobs(LevelAccessor level) {
        synchronized (this.craftingJobs) {
            final Collection<CraftingJob> jobSet = this.craftingJobs.get(level);

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
     * Ready the block entities in this level. server-side only.
     */
    private void readyBlockEntities(ServerLevel level) {
        var chunkProvider = level.getChunkSource();

        var levelQueue = blockEntities.getBlockEntities(level);

        // Make a copy because this set may be modified
        // when new chunks are loaded by an onReady call below
        long[] workSet = levelQueue.keySet().toLongArray();

        for (long packedChunkPos : workSet) {
            // Readies all of our block entities in this chunk as soon as it can tick BEs
            // The following test is equivalent to ServerLevel#isPositionTickingWithEntitiesLoaded
            if (level.areEntitiesLoaded(packedChunkPos) && chunkProvider.isPositionTicking(packedChunkPos)) {
                // Take the currently waiting block entities for this chunk and ready them all. Should more block
                // entities be added to
                // this chunk while we're working on it, a new list will be added automatically and we'll work on this
                // chunk again next tick.
                var chunkQueue = levelQueue.remove(packedChunkPos);
                if (chunkQueue == null) {
                    AELog.warn("Chunk %s was unloaded while we were readying block entities",
                            new ChunkPos(packedChunkPos));
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
     * Process the {@link ILevelRunnable} queue in this {@link Level}
     * <p>
     * This has a hard limit of about 50 ms before deferring further processing into the next tick.
     *
     * @param queue the queue to process
     * @param level the level in which the queue is processed or null for the server queue
     * @return the amount of remaining callbacks
     */
    private int processQueue(final Queue<ILevelRunnable> queue, final Level level) {
        if (queue == null) {
            return 0;
        }

        // start the clock
        sw.start();

        while (!queue.isEmpty()) {
            try {
                // call the first queue element.
                queue.poll().call(level);
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
