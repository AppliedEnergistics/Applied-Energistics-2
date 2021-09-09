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
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.NetworkCraftingSimulationState;
import appeng.hooks.ticking.TickHandler;

public class CraftingCalculation {
    private static final String LOG_CRAFTING_JOB = "CraftingCalculation (%s) issued by %s requesting [%s] using %s bytes took %s ms";
    private static final String LOG_MACHINE_SOURCE_DETAILS = "Machine[object=%s, %s, %s]";

    private final NetworkCraftingSimulationState<IAEItemStack> networkInv;
    private final Level level;
    private final IItemList<IAEItemStack> missing = StorageChannels.items().createList();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private final CraftingTreeNode tree;
    private final IAEItemStack output;
    private boolean simulate = false;
    final IActionSource actionSrc;
    private final ICraftingCallback callback;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;

    public CraftingCalculation(final Level level, final IGrid grid, final IActionSource actionSrc,
            final IAEItemStack what,
            final ICraftingCallback callback) {
        this.level = level;
        this.output = what.copy();
        this.actionSrc = actionSrc;

        this.callback = callback;
        final ICraftingService cc = grid.getService(ICraftingService.class);
        final IStorageService sg = grid.getService(IStorageService.class);
        this.networkInv = new NetworkCraftingSimulationState<>(sg.getInventory(StorageChannels.items()), actionSrc);

        this.tree = new CraftingTreeNode(cc, this, what, null, -1);
    }

    void addMissing(IAEItemStack stack) {
        missing.add(stack);
    }

    public CraftingPlan run() throws Exception {
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
            throw ex;
        } finally {
            this.finish();
        }
    }

    private CraftingPlan computeCraft(boolean simulate) throws CraftBranchFailure, InterruptedException {
        final Stopwatch timer = Stopwatch.createStarted();

        ChildCraftingSimulationState<IAEItemStack> craftingInventory = new ChildCraftingSimulationState<>(
                StorageChannels.items(), networkInv);
        craftingInventory.ignore(this.output);

        // final MECraftingInventory craftingInventory = new MECraftingInventory(this.original, true, false);
        // craftingInventory.ignore(this.output);

        // this.availableCheck = new MECraftingInventory(this.original, false, false);
        // Do the crafting. Throws in case of failure.
        this.tree.request(craftingInventory, this.output.getStackSize());
        // Add bytes for the tree size.
        craftingInventory.addBytes(this.tree.getTreeSize() * 8);

        // TODO: log tree?
        // for (final String s : this.opsAndMultiplier.keySet()) {
        // final TwoIntegers ti = this.opsAndMultiplier.get(s);
        // AELog.crafting(s + " * " + ti.times + " = " + ti.perOp * ti.times);
        // }

        this.logCraftingJob(simulate ? "simulate" : "real", timer);
        return CraftingSimulationState.buildCraftingPlan(craftingInventory, this);
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
        if (this.callback != null) {
            // TODO: figure out whether the callback should only be called in case of success, and actually call it.
            // this.callback.calculationComplete(this);
        }

        synchronized (this.monitor) {
            this.running = false;
            this.done = true;
            this.monitor.notify();
        }
    }

    public boolean isSimulation() {
        return this.simulate;
    }

    public IAEItemStack getOutput() {
        return this.output;
    }

    public IItemList<IAEItemStack> getMissingItems() {
        return missing;
    }

    Level getLevel() {
        return this.level;
    }

    /**
     * returns true if this needs more simulation.
     *
     * @param milli milliseconds of simulation
     * @return true if this needs more simulation
     */
    public boolean simulateFor(final int milli) {
        this.time = milli;

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

    private void logCraftingJob(String type, Stopwatch timer) {
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

            // TODO: bytes
            AELog.crafting(LOG_CRAFTING_JOB, type, actionSource, itemToOutput, 0, elapsedTime);
        }
    }
}
