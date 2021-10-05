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

package appeng.crafting;

import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.MixedStackList;
import appeng.core.AELog;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.hooks.ticking.TickHandler;

public class CraftingCalculation {
    private static final String LOG_CRAFTING_JOB = "CraftingCalculation (%s) issued by %s requesting [%s] using %s bytes took %s ms";
    private static final String LOG_MACHINE_SOURCE_DETAILS = "Machine[object=%s, %s, %s]";

    private final NetworkCraftingSimulationState networkInv;
    private final Level level;
    private final MixedStackList missing = new MixedStackList();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private final CraftingTreeNode tree;
    private final IAEStack output;
    private boolean simulate = false;
    final IActionSource actionSrc;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;

    public CraftingCalculation(Level level, IGrid grid, IActionSource actionSrc, IAEStack what) {
        this.level = level;
        this.output = IAEStack.copy(what);
        this.actionSrc = actionSrc;

        final ICraftingService cc = grid.getCraftingService();
        final IStorageService sg = grid.getStorageService();
        this.networkInv = new NetworkCraftingSimulationState(sg, actionSrc);

        this.tree = new CraftingTreeNode(cc, this, IAEStack.copy(what, (long) 1), null, -1);
    }

    void addMissing(IAEStack stack) {
        missing.add(stack);
    }

    public CraftingPlan run() {
        try {
            TickHandler.instance().registerCraftingSimulation(this.level, this);
            this.handlePausing();

            try {
                return computeCraft(false);
            } catch (final CraftBranchFailure e) {
                this.simulate = true;
                return computeCraft(true);
            }
        } catch (Exception ex) {
            AELog.info(ex, "Exception during crafting calculation.");
            throw new RuntimeException(ex);
        } finally {
            this.finish();
        }
    }

    private CraftingPlan computeCraft(boolean simulate) throws CraftBranchFailure, InterruptedException {
        final Stopwatch timer = Stopwatch.createStarted();

        ChildCraftingSimulationState craftingInventory = new ChildCraftingSimulationState(networkInv);
        craftingInventory.ignore(this.output);

        // Do the crafting. Throws in case of failure.
        this.tree.request(craftingInventory, this.output.getStackSize(), null);
        // Add bytes for the tree size.
        craftingInventory.addBytes(this.tree.getNodeCount() * 8);

        // TODO: log tree?
        // for (final String s : this.opsAndMultiplier.keySet()) {
        // final TwoIntegers ti = this.opsAndMultiplier.get(s);
        // AELog.crafting(s + " * " + ti.times + " = " + ti.perOp * ti.times);
        // }

        var plan = CraftingSimulationState.buildCraftingPlan(craftingInventory, this);
        this.logCraftingJob(plan, timer);
        return plan;
    }

    void handlePausing() throws InterruptedException {
        if (this.incTime > 100) {
            this.incTime = 0;

            synchronized (this.monitor) {
                if (this.watch.elapsed(TimeUnit.MICROSECONDS) > this.time) {
                    this.running = false;
                    this.watch.stop();
                    this.monitor.notify();
                }

                if (!this.running) {
                    AELog.craftingDebug("crafting job will now sleep");

                    while (!this.running) {
                        this.monitor.wait();
                    }

                    AELog.craftingDebug("crafting job now active");
                }
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        this.incTime++;
    }

    private void finish() {
        synchronized (this.monitor) {
            this.running = false;
            this.done = true;
            this.monitor.notify();
        }
    }

    public boolean isSimulation() {
        return this.simulate;
    }

    public IAEStack getOutput() {
        return this.output;
    }

    public MixedStackList getMissingItems() {
        return missing;
    }

    Level getLevel() {
        return this.level;
    }

    /**
     * returns true if this needs more simulation.
     *
     * @param micros microseconds of simulation
     * @return true if this needs more simulation
     */
    public boolean simulateFor(final int micros) {
        this.time = micros;

        synchronized (this.monitor) {
            if (this.done) {
                return false;
            }

            this.watch.reset();
            this.watch.start();
            this.running = true;

            AELog.craftingDebug("main thread is now going to sleep");

            this.monitor.notify();

            while (this.running) {
                try {
                    this.monitor.wait();
                } catch (final InterruptedException ignored) {
                }
            }

            AELog.craftingDebug("main thread is now active");
        }

        return true;
    }

    private void logCraftingJob(CraftingPlan plan, Stopwatch timer) {
        if (AELog.isCraftingLogEnabled()) {
            final String itemToOutput = this.output.toString();
            final long elapsedTime = timer.elapsed(TimeUnit.MILLISECONDS);
            final String actionSource;

            if (this.actionSrc.player().isPresent()) {
                final Player player = this.actionSrc.player().get();

                actionSource = player.toString();
            } else if (this.actionSrc.machine().isPresent()) {
                final IActionHost machineSource = this.actionSrc.machine().get();
                final IGridNode actionableNode = machineSource.getActionableNode();
                actionSource = actionableNode != null ? actionableNode.toString() : machineSource.toString();
            } else {
                actionSource = "[unknown source]";
            }

            String type = plan.simulation() ? "simulation" : "real";
            AELog.crafting(LOG_CRAFTING_JOB, type, actionSource, itemToOutput, plan.bytes(), elapsedTime);
        }
    }

    public boolean hasMultiplePaths() {
        return this.tree.hasMultiplePaths();
    }
}
