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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;

import appeng.api.networking.IGridNode;
import appeng.api.parts.CableRenderMode;
import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.packets.PaintedEntityPacket;
import appeng.crafting.CraftingJob;
import appeng.me.Grid;
import appeng.tile.AEBaseTileEntity;
import appeng.util.IWorldCallable;
import appeng.util.Platform;

public class TickHandler {

    private static final TickHandler INSTANCE = new TickHandler();
    private final Queue<IWorldCallable<?>> serverQueue = new ArrayDeque<>();
    private final Multimap<IWorld, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final Map<IWorld, Queue<IWorldCallable<?>>> callQueue = new HashMap<>();
    private final HandlerRep server = new HandlerRep();
    private final Map<Integer, PlayerColor> cliPlayerColors = new HashMap<>();
    private final Map<Integer, PlayerColor> srvPlayerColors = new HashMap<>();
    private CableRenderMode crm = CableRenderMode.STANDARD;

    public static TickHandler instance() {
        return INSTANCE;
    }

    public static void setup(IEventBus eventBus) {
        eventBus.addListener(INSTANCE::onServerTick);
        eventBus.addListener(INSTANCE::onWorldTick);
        eventBus.addListener(INSTANCE::onUnloadChunk);
        eventBus.addListener(INSTANCE::onUnloadWorld);

        // DistExecutor does not like functional interfaces
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new SafeRunnable() {

            private static final long serialVersionUID = 5221919736953944125L;

            @Override
            public void run() {
                eventBus.addListener(INSTANCE::onClientTick);

            }
        });
    }

    public Map<Integer, PlayerColor> getPlayerColors() {
        if (Platform.isServer()) {
            return this.srvPlayerColors;
        }
        return this.cliPlayerColors;
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

    public void addInit(final AEBaseTileEntity tile) {
        // for no there is no reason to care about this on the client...
        if (!tile.getWorld().isRemote()) {
            this.getRepo().addTile(tile);
        }
    }

    private HandlerRep getRepo() {
        if (Platform.isClient()) {
            throw new IllegalAccessError("Only supported on the server");
        }

        return this.server;
    }

    public void addNetwork(final Grid grid) {
        // for no there is no reason to care about this on the client...
        if (Platform.isServer()) {
            this.getRepo().addNetwork(grid);
        }
    }

    public void removeNetwork(final Grid grid) {
        // for no there is no reason to care about this on the client...
        if (Platform.isServer()) {
            this.getRepo().removeNetwork(grid);
        }
    }

    public Iterable<Grid> getGridList() {
        return this.getRepo().networks;
    }

    public void shutdown() {
        this.getRepo().clear();
    }

    public void onUnloadChunk(final ChunkEvent.Unload ev) {
        // for no there is no reason to care about this on the client...
        if (!ev.getWorld().isRemote()) {
            this.getRepo().tiles.get(ev.getWorld()).remove(ev.getChunk().getPos());
        }
    }

    public void onUnloadWorld(final WorldEvent.Unload ev) {
        // for no there is no reason to care about this on the client...
        if (!ev.getWorld().isRemote()) {
            final List<IGridNode> toDestroy = new ArrayList<>();

            this.getRepo().updateNetworks();
            for (final Grid g : this.getRepo().networks) {
                for (final IGridNode n : g.getNodes()) {
                    if (n.getWorld() == ev.getWorld()) {
                        toDestroy.add(n);
                    }
                }
            }

            for (final IGridNode n : toDestroy) {
                n.destroy();
            }

            this.getRepo().tiles.remove(ev.getWorld());
            this.callQueue.remove(ev.getWorld());
        }
    }

    public void onClientTick(final ClientTickEvent ev) {
        if (ev.phase == Phase.START) {
            this.tickColors(this.cliPlayerColors);
            final CableRenderMode currentMode = Api.instance().partHelper().getCableRenderMode();

            if (currentMode != this.crm) {
                this.crm = currentMode;
                AppEng.proxy.triggerUpdates();
            }
        }
    }

    public void onWorldTick(final WorldTickEvent ev) {
        if (ev.phase == Phase.START) {
            final World world = ev.world;
            final Queue<IWorldCallable<?>> queue = this.callQueue.get(world);
            this.processQueue(queue, world);
        }

        if (ev.phase == Phase.END) {
            this.simulateCraftingJobs(ev.world);
            this.readyTiles(ev.world);
        }
    }

    public void onServerTick(final ServerTickEvent ev) {
        if (ev.phase == Phase.END) {
            this.tickColors(this.srvPlayerColors);

            // tick networks.
            this.getRepo().updateNetworks();
            for (final Grid g : this.getRepo().networks) {
                g.update();
            }

            // cross world queue.
            this.processQueue(this.serverQueue, null);
        }
    }

    public void registerCraftingSimulation(final World world, final CraftingJob craftingJob) {
        synchronized (this.craftingJobs) {
            this.craftingJobs.put(world, craftingJob);
        }
    }

    /**
     * Simulates the current crafting requests before they user can submit them to be processed.
     * 
     * @param world
     */
    private void simulateCraftingJobs(IWorld world) {
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
     * 
     * @param world
     */
    private void readyTiles(IWorld world) {
        final HandlerRep repo = this.getRepo();
        final Map<ChunkPos, Queue<AEBaseTileEntity>> worldQueue = repo.tiles.getOrDefault(world,
                Collections.emptyMap());

        for (Iterator<Entry<ChunkPos, Queue<AEBaseTileEntity>>> it = worldQueue.entrySet().iterator(); it
                .hasNext();) {
            Entry<ChunkPos, Queue<AEBaseTileEntity>> entry = it.next();
            ChunkPos pos = entry.getKey();
            AbstractChunkProvider chunkProvider = world.getChunkProvider();

            // Using the blockpos of the chunk start to test if it can tick.
            // Relies on the world to test the chunkpos and not the explicit blockpos.
            BlockPos testBlockPos = new BlockPos(pos.getXStart(), 0, pos.getZStart());

            // Readies this chunk, if it can tick and does exist.
            // Chunks which are considered a border chunk will not "exist", but are loaded. Once this state changes they
            // will be readied.
            if (world.chunkExists(pos.x, pos.z) && chunkProvider.canTick(testBlockPos)) {
                Queue<AEBaseTileEntity> queue = entry.getValue();

                while (!queue.isEmpty()) {
                    final AEBaseTileEntity bt = queue.poll();

                    // Only ready tile entites which were destroyed in the meantime.
                    if (!bt.isRemoved()) {
                        bt.onReady();
                    }
                }

                // cleanup empty chunk queue
                it.remove();
            }
        }
    }

    private void tickColors(final Map<Integer, PlayerColor> playerSet) {
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

        private Map<IWorld, Map<ChunkPos, Queue<AEBaseTileEntity>>> tiles = new HashMap<>();
        private Set<Grid> networks = new HashSet<>();
        private Set<Grid> toAdd = new HashSet<>();
        private Set<Grid> toRemove = new HashSet<>();

        public HandlerRep() {
        }

        private void clear() {
            this.tiles = new WeakHashMap<>();
            this.networks = new HashSet<>();
            this.toAdd = new HashSet<>();
            this.toRemove = new HashSet<>();
        }

        private synchronized void addTile(AEBaseTileEntity tile) {
            IWorld world = tile.getWorld();
            ChunkPos chunkPos = new ChunkPos(tile.getPos());

            Map<ChunkPos, Queue<AEBaseTileEntity>> worldQueue = this.tiles.computeIfAbsent(world, (key) -> {
                return new HashMap<>();
            });

            Queue<AEBaseTileEntity> queue = worldQueue.computeIfAbsent(chunkPos, (key) -> {
                return new ArrayDeque<>();
            });

            queue.add(tile);

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
