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

package appeng.me.service;

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterators;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.me.GridNode;
import appeng.me.service.helpers.TickTracker;

public class TickManagerService implements ITickManager, IGridServiceProvider {

    public static boolean MONITORING_ENABLED = false;

    private static final int TICK_RATE_SPEED_UP_FACTOR = 2;
    private static final int TICK_RATE_SLOW_DOWN_FACTOR = 1;

    private final Map<IGridNode, TickTracker> alertable = new HashMap<>();
    private final Map<IGridNode, TickTracker> sleeping = new HashMap<>();
    private final Map<IGridNode, TickTracker> awake = new HashMap<>();
    private final Map<Level, PriorityQueue<TickTracker>> upcomingTicks = new HashMap<>();

    private PriorityQueue<TickTracker> currentlyTickingQueue = null;

    private long currentTick = 0;
    private Stopwatch stopWatch = Stopwatch.createUnstarted();

    public TickManagerService(@SuppressWarnings("unused") final IGrid g) {
    }

    @Override
    public void onServerStartTick() {
        this.currentTick++;
    }

    @Override
    public void onLevelStartTick(Level level) {
    }

    @Override
    public void onLevelEndTick(Level level) {
        this.tickLevelQueue(level);
    }

    @Override
    public void onServerEndTick() {
        this.tickLevelQueue(null);
    }

    private void tickLevelQueue(@Nullable Level level) {
        var queue = this.upcomingTicks.get(level);

        if (queue != null) {
            currentlyTickingQueue = queue;

            try {
                tickQueue(queue);
            } finally {
                currentlyTickingQueue = null;
            }

            if (queue.isEmpty()) {
                this.upcomingTicks.remove(level);
            }
        }
    }

    private void tickQueue(PriorityQueue<TickTracker> queue) {
        TickTracker tt;

        while (!queue.isEmpty()) {
            // Peek and stop once it reaches a TickTracker running at a later tick
            tt = queue.peek();
            if (tt.getNextTick() > this.currentTick) {
                break;
            }

            queue.poll();
            final int diff = (int) (this.currentTick - tt.getLastTick());
            final TickRateModulation mod = this.unsafeTickingRequest(tt, diff);

            switch (mod) {
                case URGENT -> tt.setCurrentRate(0);
                case FASTER -> tt.setCurrentRate(tt.getCurrentRate() - TICK_RATE_SPEED_UP_FACTOR);
                case IDLE -> tt.setCurrentRate(tt.getRequest().maxTickRate);
                case SLOWER -> tt.setCurrentRate(tt.getCurrentRate() + TICK_RATE_SLOW_DOWN_FACTOR);
                case SLEEP -> this.sleepDevice(tt.getNode());
                default -> {
                    return;
                }
            }

            // Update the last time this node was ticked
            tt.setLastTick(this.currentTick);

            if (this.awake.containsKey(tt.getNode())) {
                // Queue already known, no need to use addToQueue() to resolve it again.
                queue.add(tt);
            }
        }
    }

    @Override
    public void removeNode(final IGridNode gridNode) {
        var tickable = gridNode.getService(IGridTickable.class);
        if (tickable != null) {
            this.alertable.remove(gridNode);
            this.sleeping.remove(gridNode);

            // Also remove the tracker from the queue to not tick it again.
            var tt = this.awake.remove(gridNode);
            this.removeFromQueue(gridNode, tt);
        }
    }

    @Override
    public void addNode(final IGridNode gridNode) {
        var tickable = gridNode.getService(IGridTickable.class);
        if (tickable != null) {
            var tr = tickable.getTickingRequest(gridNode);

            Preconditions.checkNotNull(tr);

            final TickTracker tt = new TickTracker(tr, gridNode, tickable, this.currentTick);

            if (tr.canBeAlerted) {
                this.alertable.put(gridNode, tt);
            }

            if (tr.isSleeping) {
                this.sleeping.put(gridNode, tt);
            } else {
                this.awake.put(gridNode, tt);
                this.addToQueue(gridNode, tt);
            }
        }
    }

    @Override
    public boolean alertDevice(final IGridNode node) {
        Preconditions.checkNotNull(node);

        final TickTracker tt = this.alertable.get(node);
        if (tt == null) {
            return false;
        }

        // set to awake, this is for sanity.
        this.sleeping.remove(node);
        this.awake.put(node, tt);

        // configure sort.
        tt.setLastTick(tt.getLastTick() - tt.getRequest().maxTickRate);
        tt.setCurrentRate(tt.getRequest().minTickRate);

        // prevent dupes and tick build up.
        this.updateQueuePosition(node, tt);

        return true;
    }

    @Override
    public boolean sleepDevice(final IGridNode node) {
        Preconditions.checkNotNull(node);

        if (this.awake.containsKey(node)) {
            final TickTracker gt = this.awake.get(node);
            this.awake.remove(node);
            this.sleeping.put(node, gt);
            return true;
        }

        return false;
    }

    @Override
    public boolean wakeDevice(final IGridNode node) {
        Preconditions.checkNotNull(node);

        if (this.sleeping.containsKey(node)) {
            final TickTracker tt = this.sleeping.get(node);
            this.sleeping.remove(node);
            this.awake.put(node, tt);
            this.updateQueuePosition(node, tt);

            return true;
        }

        return false;
    }

    /**
     * Reports the average time for a gridnode
     * <p>
     * There is no overflow handling for the internal counter.
     *
     * @return average time spent ticking this node in nanoseconds, or 0 for an unknown node
     */
    public long getAverageTime(final IGridNode node) {
        var stats = this.getStatistics(node);
        if (stats == null) {
            return 0;
        }

        return (long) stats.getAverage();
    }

    /**
     * Gets the overall time spent ticking this grid node in nanoseconds.
     *
     * @return 0 if the node isn't ticking or doesn't belong to this grid.
     */
    public long getOverallTime(final IGridNode node) {
        var stats = this.getStatistics(node);
        if (stats == null) {
            return 0;
        }

        return stats.getSum();
    }

    /**
     * The maximum time a {@link GridNode} across its existence.
     *
     * @param node
     * @return maximum time or 0 for an unknown node
     */
    public long getMaximumTime(final IGridNode node) {
        var stats = this.getStatistics(node);
        if (stats == null) {
            return 0;
        }

        return stats.getMax();
    }

    private LongSummaryStatistics getStatistics(final IGridNode node) {
        TickTracker tt = this.awake.get(node);

        if (tt == null) {
            tt = this.sleeping.get(node);
        }

        if (tt == null) {
            return null;
        }

        return tt.getStatistics();
    }

    /**
     * null as level could be used for virtual nodes.
     */
    private PriorityQueue<TickTracker> getQueue(@Nullable Level level) {
        return this.upcomingTicks.computeIfAbsent(level, (key) -> new PriorityQueue<>());
    }

    private void addToQueue(IGridNode node, TickTracker tt) {
        var queue = getQueue(node.getLevel());
        queue.add(tt);
    }

    private void removeFromQueue(IGridNode node, TickTracker tt) {
        var level = node.getLevel();
        var queue = getQueue(level);
        queue.remove(tt);

        // Make sure we don't cleanup a queue we are iterating over,
        // as something might be added to it later even if it's empty now.
        if (currentlyTickingQueue != queue && queue.isEmpty()) {
            this.upcomingTicks.remove(level);
        }
    }

    private void updateQueuePosition(IGridNode node, TickTracker tt) {
        this.removeFromQueue(node, tt);
        this.addToQueue(node, tt);
    }

    /**
     * Helper method to handle exceptions and report them without polluting the queue loop.
     * <p>
     * Also tracks time statistics.
     */
    private TickRateModulation unsafeTickingRequest(TickTracker tt, int diff) {
        try {
            // Shortcut to immediately return when monitoring is disabled.
            if (!MONITORING_ENABLED) {
                return tt.getGridTickable().tickingRequest(tt.getNode(), diff);
            }

            stopWatch.reset().start();

            var mod = tt.getGridTickable().tickingRequest(tt.getNode(), diff);

            stopWatch.stop();
            var elapsedTime = stopWatch.elapsed(TimeUnit.NANOSECONDS);
            tt.getStatistics().accept(elapsedTime);

            return mod;
        } catch (final Throwable t) {
            final CrashReport crashreport = CrashReport.forThrowable(t, "Ticking GridNode");
            final CrashReportCategory crashreportcategory = crashreport
                    .addCategory(tt.getGridTickable().getClass().getSimpleName() + " being ticked.");
            tt.addEntityCrashInfo(crashreportcategory);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * This method is slow and only for debugging purposes.
     */
    public NodeStatus getStatus(IGridNode node) {
        var sleepingTracker = sleeping.get(node);
        var awakeTracker = awake.get(node);
        var alertableTracker = alertable.get(node);

        // Also check if the node is _really_ queued for ticking. If it's awake
        // and not queued, this indicates a bug.
        boolean isQueued = false;
        var tickQueue = upcomingTicks.get(node.getLevel());
        if (awakeTracker != null && tickQueue != null) {
            isQueued = Iterators.contains(tickQueue.iterator(), awakeTracker);
        }

        // Get the tick-request stats
        var tracker = awakeTracker;
        if (tracker == null) {
            tracker = alertableTracker;
        }
        if (tracker == null) {
            tracker = sleepingTracker;
        }
        var currentRate = tracker != null ? tracker.getCurrentRate() : 0;
        var lastTick = tracker != null ? tracker.getLastTick() : 0;
        return new NodeStatus(
                alertableTracker != null,
                sleepingTracker != null,
                awakeTracker != null,
                isQueued,
                currentRate,
                currentTick - lastTick);
    }

    public record NodeStatus(
            boolean alertable,
            boolean sleeping,
            boolean awake,
            boolean queued,
            int currentRate,
            long lastTick) {
    }

}
