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

import appeng.api.util.AEColor;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.packets.PaintedEntityPacket;
import appeng.crafting.CraftingJob;
import appeng.me.Grid;
import appeng.tile.AEBaseBlockEntity;
import appeng.util.IWorldCallable;
import appeng.util.Platform;
import com.google.common.base.Stopwatch;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TickHandler {

    public static final TickHandler INSTANCE = new TickHandler();
    private final Queue<IWorldCallable<?>> serverQueue = new ArrayDeque<>();
    private final Multimap<World, CraftingJob> craftingJobs = LinkedListMultimap.create();
    private final WeakHashMap<WorldAccess, Queue<IWorldCallable<?>>> callQueue = new WeakHashMap<>();
    private final HandlerRep server = new HandlerRep();
    private final HandlerRep client = new HandlerRep();
    private final HashMap<Integer, PlayerColor> srvPlayerColors = new HashMap<>();

    public TickHandler() {
        // Register for all the tick events we care about
        ServerTickEvents.END_SERVER_TICK.register(this::onAfterServerTick);
        ServerTickEvents.START_WORLD_TICK.register(this::onBeforeWorldTick);
        ServerTickEvents.END_WORLD_TICK.register(this::onAfterWorldTick);

    }

    public HashMap<Integer, PlayerColor> getPlayerColors() {
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
        if (Platform.isServer()) // for no there is no reason to care about this on the client...
        {
            this.getRepo().tiles.add(tile);
        }
    }

    private HandlerRep getRepo() {
        if (Platform.isServer()) {
            return this.server;
        }
        return this.client;
    }

    public void addNetwork(final Grid grid) {
        if (Platform.isServer()) // for no there is no reason to care about this on the client...
        {
            this.getRepo().addNetwork(grid);
        }
    }

    public void removeNetwork(final Grid grid) {
        if (Platform.isServer()) // for no there is no reason to care about this on the client...
        {
            this.getRepo().removeNetwork(grid);
        }
    }

    public Iterable<Grid> getGridList() {
        return this.getRepo().networks;
    }

    public void shutdown() {
        this.getRepo().clear();
    }

// FIXME FABRIC It does not look like worlds can ever unload in Fabric.
// FIXME FABRIC    public void unloadWorld(final WorldEvent.Unload ev) {
// FIXME FABRIC        if (Platform.isServer()) // for no there is no reason to care about this on the client...
// FIXME FABRIC        {
// FIXME FABRIC            final List<IGridNode> toDestroy = new ArrayList<>();
// FIXME FABRIC
// FIXME FABRIC            this.getRepo().updateNetworks();
// FIXME FABRIC            for (final Grid g : this.getRepo().networks) {
// FIXME FABRIC                for (final IGridNode n : g.getNodes()) {
// FIXME FABRIC                    if (n.getWorld() == ev.getWorld()) {
// FIXME FABRIC                        toDestroy.add(n);
// FIXME FABRIC                    }
// FIXME FABRIC                }
// FIXME FABRIC            }
// FIXME FABRIC
// FIXME FABRIC            for (final IGridNode n : toDestroy) {
// FIXME FABRIC                n.destroy();
// FIXME FABRIC            }
// FIXME FABRIC        }
// FIXME FABRIC    }

    private void onBeforeWorldTick(ServerWorld world) {
        final Queue<IWorldCallable<?>> queue = this.callQueue.get(world);
        this.processQueue(queue, world);
    }

    private void onAfterWorldTick(ServerWorld world) {
        synchronized (this.craftingJobs) {
            final Collection<CraftingJob> jobSet = this.craftingJobs.get(world);
            if (!jobSet.isEmpty()) {
                final int simTime = Math.max(1,
                        AEConfig.instance().getCraftingCalculationTimePerTick() / jobSet.size());
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

    private void onAfterServerTick(MinecraftServer server) {
        this.tickColors(this.srvPlayerColors);
        // ready tiles.
        final HandlerRep repo = this.getRepo();
        while (!repo.tiles.isEmpty()) {
            final AEBaseBlockEntity bt = repo.tiles.poll();
            if (!bt.isRemoved()) {
                bt.onReady();
            }
        }

        // tick networks.
        this.getRepo().updateNetworks();
        for (final Grid g : this.getRepo().networks) {
            g.update();
        }

        // cross world queue.
        this.processQueue(this.serverQueue, null);
    }

    protected void tickColors(final HashMap<Integer, PlayerColor> playerSet) {
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

        // long time = sw.elapsed( TimeUnit.MILLISECONDS );
        // if ( time > 0 )
        // AELog.info( "processQueue Time: " + time + "ms" );
    }

    public void registerCraftingSimulation(final World world, final CraftingJob craftingJob) {
        synchronized (this.craftingJobs) {
            this.craftingJobs.put(world, craftingJob);
        }
    }

    private static class HandlerRep {

        private Queue<AEBaseBlockEntity> tiles = new ArrayDeque<>();
        private Set<Grid> networks = new HashSet<>();
        private Set<Grid> toAdd = new HashSet<>();
        private Set<Grid> toRemove = new HashSet<>();

        private void clear() {
            this.tiles = new ArrayDeque<>();
            this.networks = new HashSet<>();
            this.toAdd = new HashSet<>();
            this.toRemove = new HashSet<>();
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
