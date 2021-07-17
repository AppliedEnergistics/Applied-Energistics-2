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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCallback;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.hooks.ticking.TickHandler;

public class CraftingJob implements Runnable, ICraftingJob {
    private static final String LOG_CRAFTING_JOB = "CraftingJob (%s) issued by %s requesting [%s] using %s bytes took %s ms";
    private static final String LOG_MACHINE_SOURCE_DETAILS = "Machine[object=%s, %s, %s]";

    private final MECraftingInventory original;
    private final World world;
    private final IItemList<IAEItemStack> crafting = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final IItemList<IAEItemStack> missing = Api.instance().storage()
            .getStorageChannel(IItemStorageChannel.class).createList();
    private final HashMap<String, TwoIntegers> opsAndMultiplier = new HashMap<>();
    private final Object monitor = new Object();
    private final Stopwatch watch = Stopwatch.createUnstarted();
    private CraftingTreeNode tree;
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

    private World wrapWorld(final World w) {
        return w;
    }

    public CraftingJob(final World w, final IGrid grid, final IActionSource actionSrc, final IAEItemStack what,
            final ICraftingCallback callback) {
        this.world = this.wrapWorld(w);
        this.output = what.copy();
        this.actionSrc = actionSrc;

        this.callback = callback;
        final ICraftingService cc = grid.getService(ICraftingService.class);
        final IStorageService sg = grid.getService(IStorageService.class);
        this.original = new MECraftingInventory(
                sg.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class)), actionSrc,
                false, false, false);

        this.setTree(this.getCraftingTree(cc, what));
        this.availableCheck = null;
    }

    private CraftingTreeNode getCraftingTree(final ICraftingService cc, final IAEItemStack what) {
        return new CraftingTreeNode(cc, this, what, null, -1, 0);
    }

    void refund(final IAEItemStack o) {
        this.availableCheck.injectItems(o, Actionable.MODULATE, this.actionSrc);
    }

    IAEItemStack checkUse(final IAEItemStack available) {
        return this.availableCheck.extractItems(available, Actionable.MODULATE, this.actionSrc);
    }

    public void writeToNBT(final CompoundNBT out) {

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
                TickHandler.instance().registerCraftingSimulation(this.world, this);
                this.handlePausing();

                final Stopwatch timer = Stopwatch.createStarted();

                final MECraftingInventory craftingInventory = new MECraftingInventory(this.original, true, false, true);
                craftingInventory.ignore(this.output);

                this.availableCheck = new MECraftingInventory(this.original, false, false, false);
                this.getTree().request(craftingInventory, this.output.getStackSize(), this.actionSrc);
                this.getTree().dive(this);

                for (final String s : this.opsAndMultiplier.keySet()) {
                    final TwoIntegers ti = this.opsAndMultiplier.get(s);
                    AELog.crafting(s + " * " + ti.times + " = " + ti.perOp * ti.times);
                }

                this.logCraftingJob("real", timer);
                // if ( mode == Actionable.MODULATE )
                // craftingInventory.moveItemsToStorage( storage );
            } catch (final CraftBranchFailure e) {
                this.simulate = true;

                try {
                    final Stopwatch timer = Stopwatch.createStarted();
                    final MECraftingInventory craftingInventory = new MECraftingInventory(this.original, true, false,
                            true);
                    craftingInventory.ignore(this.output);

                    this.availableCheck = new MECraftingInventory(this.original, false, false, false);

                    this.getTree().setSimulate();
                    this.getTree().request(craftingInventory, this.output.getStackSize(), this.actionSrc);
                    this.getTree().dive(this);

                    for (final String s : this.opsAndMultiplier.keySet()) {
                        final TwoIntegers ti = this.opsAndMultiplier.get(s);
                        AELog.crafting(s + " * " + ti.times + " = " + ti.perOp * ti.times);
                    }

                    this.logCraftingJob("simulate", timer);
                } catch (final CraftBranchFailure e1) {
                    AELog.debug(e1);
                } catch (final CraftingCalculationFailure f) {
                    AELog.debug(f);
                } catch (final InterruptedException e1) {
                    AELog.crafting("Crafting calculation canceled.");
                    this.finish();
                    return;
                }
            } catch (final CraftingCalculationFailure f) {
                AELog.debug(f);
            } catch (final InterruptedException e1) {
                AELog.crafting("Crafting calculation canceled.");
                this.finish();
                return;
            }

            AELog.craftingDebug("crafting job now done");
        } catch (final Throwable t) {
            this.finish();
            throw new IllegalStateException(t);
        }

        this.finish();
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

    private void setTree(final CraftingTreeNode tree) {
        this.tree = tree;
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
                actionSource = actionableNode != null ? actionableNode.toString() : machineSource.toString();
            } else {
                actionSource = "[unknown source]";
            }

            AELog.crafting(LOG_CRAFTING_JOB, type, actionSource, itemToOutput, this.bytes, elapsedTime);
        }
    }

    private static class TwoIntegers {
        private final long perOp = 0;
        private final long times = 0;
    }
}
