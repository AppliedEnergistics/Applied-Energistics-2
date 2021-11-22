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

import net.minecraft.world.level.Level;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.storage.GenericStack;
import appeng.api.storage.data.AEKey;
import appeng.api.storage.data.KeyCounter;
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
    private final KeyCounter missing = new KeyCounter();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private final CraftingTreeNode tree;
    private final GenericStack output;
    private boolean simulate = false;
    final ICraftingSimulationRequester simRequester;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;

    public CraftingCalculation(Level level, IGrid grid, ICraftingSimulationRequester simRequester,
            GenericStack output) {
        this.level = level;
        this.output = output;
        this.simRequester = simRequester;

        var storageService = grid.getStorageService();
        var craftingService = grid.getCraftingService();
        this.networkInv = new NetworkCraftingSimulationState(storageService, simRequester.getActionSource());

        this.tree = new CraftingTreeNode(craftingService, this, output.what(), 1, null, -1);
    }

    void addMissing(AEKey what, long amount) {
        missing.add(what, amount);
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
        craftingInventory.ignore(this.output.what());

        // Do the crafting. Throws in case of failure.
        this.tree.request(craftingInventory, output.amount(), null);
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

    public GenericStack getOutput() {
        return this.output;
    }

    public KeyCounter getMissingItems() {
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
            String itemToOutput = this.output.toString();
            long elapsedTime = timer.elapsed(TimeUnit.MILLISECONDS);
            var actionSource = this.simRequester.getActionSource();
            String actionSourceName;

            if (actionSource != null && actionSource.player().isPresent()) {
                var player = actionSource.player().get();
                actionSourceName = player.toString();
            } else if (actionSource != null && actionSource.machine().isPresent()) {
                var machineSource = actionSource.machine().get();
                var actionableNode = machineSource.getActionableNode();
                actionSourceName = actionableNode != null ? actionableNode.toString() : machineSource.toString();
            } else {
                actionSourceName = "[unknown source]";
            }

            String type = plan.simulation() ? "simulation" : "real";
            AELog.crafting(LOG_CRAFTING_JOB, type, actionSourceName, itemToOutput, plan.bytes(), elapsedTime);
        }
    }

    public boolean hasMultiplePaths() {
        return this.tree.hasMultiplePaths();
    }
}
