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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.hooks.ticking.TickHandler;

/**
 * Implementation of a crafting job simulation.
 * <p>
 * A crafting job is implemented using a tree of {@linkplain CraftingTreeNode nodes} and {@linkplain CraftingTreeProcess processes}.
 * Each node corresponds to a potentially requested stack, and each process corresponds to a pattern that will be explored.
 * Each node has the patterns that can produce its content as children, and each process has the nodes for the ingredients of its pattern as children.
 * <p>
 * FIXME CRAFTING explain how threads are used to simulate for a given amount of time
 */
public class CraftingJob implements Runnable, ICraftingJob {
    private static final String LOG_CRAFTING_JOB = "CraftingJob (%s) issued by %s requesting [%s] using %s bytes took %s ms";
    private static final String LOG_MACHINE_SOURCE_DETAILS = "Machine[object=%s, %s]";

    private final MECraftingInventory original;
    private final World world;
    private final IItemList<IAEItemStack> crafting = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> missing = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private final CraftingTreeNode tree;
    private final IAEItemStack output;
    private boolean simulate = false;
    private MECraftingInventory availableCheck;
    private long bytes = 0;
    private final IActionSource actionSrc;
    private final ICraftingCallback callback;
    private boolean running = false;
    private boolean done = false;
    private int time = 5;
    private int incTime = Integer.MAX_VALUE;

    /**
     * Build this crafting job, gathering all patterns that can produce the target stack,
     * the patterns that can produce their ingredients, etc...
     */
    public CraftingJob(final World w, final IGrid grid, final IActionSource actionSrc, final IAEItemStack what,
            final ICraftingCallback callback) {
        this.world = w;
        this.output = what.copy();
        this.actionSrc = actionSrc;

        this.callback = callback;
        final ICraftingGrid cc = grid.getCache(ICraftingGrid.class);
        final IStorageGrid sg = grid.getCache(IStorageGrid.class);
        this.original = new MECraftingInventory(
                sg.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class)), actionSrc,
                false, false);

        // This call is recursive and will build the entire tree.
        this.tree = new CraftingTreeNode(cc, this, what, null, -1, 0);
        this.availableCheck = null;
    }

    void refund(final IAEItemStack o) {
        this.availableCheck.injectItems(o, Actionable.MODULATE, this.actionSrc);
    }

    IAEItemStack checkUse(final IAEItemStack available) {
        return this.availableCheck.extractItems(available, Actionable.MODULATE, this.actionSrc);
    }

    void addTask(IAEItemStack what, final long crafts, final ICraftingPatternDetails details, final int depth) {
        if (crafts > 0) {
            what = what.copy();
            what.setStackSize(what.getStackSize() * crafts);
            this.crafting.add(what);
        }
    }

    void addMissing(IAEItemStack what) {
        what = what.copy();
        this.missing.add(what);
    }

    @Override
    public void run() {
        try {
            try {
                // Register this job to be scheduled.
                TickHandler.instance().registerCraftingSimulation(this.world, this);
                // Immediately pause until simulateFor() is called.
                this.handlePausing();

                // Calculate this job (will pause by calling handlePausing() when necessary)
                calculateCrafting();
            } catch (final CraftBranchFailure e) {
                // If the job failed, we mark this as a simulation
                this.simulate = true;
                // Reset the tree state to prepare for the simulation
                this.getTree().resetForSimulation();

                try {
                    // Perform the simulation (will pause by calling handlePausing() when necessary)
                    calculateCrafting();
                } catch (final CraftBranchFailure e1) {
                    AELog.debug(e1);
                }
            }

            AELog.craftingDebug("crafting job now done");
        } catch (final InterruptedException e1) {
            AELog.crafting("Crafting calculation canceled.");
        } catch (final Throwable t) {
            throw new IllegalStateException(t);
        } finally {
            this.finish();
        }
    }

    private void calculateCrafting() throws CraftBranchFailure, InterruptedException {
        final Stopwatch timer = Stopwatch.createStarted();
        final MECraftingInventory craftingInventory = new MECraftingInventory(this.original, true, false);
        craftingInventory.resetStackSize(this.output);

        this.availableCheck = new MECraftingInventory(this.original, false, false);

        this.getTree().request(craftingInventory, this.output.getStackSize(), this.actionSrc);
        this.getTree().dive(this);

        this.logCraftingJob(simulate ? "simulate" : "real", timer);
    }

    /**
     * If simulation time was exceeded, hand back control to the server thread still waiting in {@link #simulateFor}.
     * The check is only performed every 100th call for performance reasons.
     * @throws InterruptedException If the job was interrupted.
     */
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
            this.callback.calculationComplete(this);
        }

        this.availableCheck = null;

        synchronized (this.monitor) {
            this.running = false;
            this.done = true;
            this.monitor.notify();
        }
    }

    @Override
    public boolean isSimulation() {
        return this.simulate;
    }

    @Override
    public long getByteTotal() {
        return this.bytes;
    }

    @Override
    public void populatePlan(final IItemList<IAEItemStack> plan) {
        if (this.getTree() != null) {
            this.getTree().getPlan(plan);
        }
    }

    @Override
    public IAEItemStack getOutput() {
        return this.output;
    }

    public boolean isDone() {
        return this.done;
    }

    World getWorld() {
        return this.world;
    }

    /**
     * returns true if this needs more simulation.
     *
     * @param milli milliseconds of simulation
     *
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

    void addBytes(final long crafts) {
        this.bytes += crafts;
    }

    public CraftingTreeNode getTree() {
        return this.tree;
    }

    private void logCraftingJob(String type, Stopwatch timer) {
        if (AELog.isCraftingLogEnabled()) {
            final String itemToOutput = this.output.toString();
            final long elapsedTime = timer.elapsed(TimeUnit.MILLISECONDS);
            final String actionSource;

            if (this.actionSrc.player().isPresent()) {
                final PlayerEntity player = this.actionSrc.player().get();

                actionSource = player.toString();
            } else if (this.actionSrc.machine().isPresent()) {
                final IActionHost machineSource = this.actionSrc.machine().get();
                final IGridNode actionableNode = machineSource.getActionableNode();
                final IGridHost machine = actionableNode.getMachine();
                final DimensionalCoord location = actionableNode.getGridBlock().getLocation();

                actionSource = String.format(LOG_MACHINE_SOURCE_DETAILS, machine, location);
            } else {
                actionSource = "[unknown source]";
            }

            AELog.crafting(LOG_CRAFTING_JOB, type, actionSource, itemToOutput, this.bytes, elapsedTime);
        }
    }
}
