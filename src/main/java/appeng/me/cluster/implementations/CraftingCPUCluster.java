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

package appeng.me.cluster.implementations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.ICraftingHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingEvent;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingWatcher;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.helpers.MachineSource;
import appeng.me.service.CraftingService;
import appeng.menu.NullMenu;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public final class CraftingCPUCluster implements IAECluster, ICraftingCPU {

    private static final String LOG_MARK_AS_COMPLETE = "Completed job for %s.";

    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private final int[] usedOps = new int[3];
    private final Map<ICraftingPatternDetails, TaskProgress> tasks = new HashMap<>();
    // INSTANCE sate
    private final List<CraftingBlockEntity> blockEntities = new ArrayList<>();
    private final List<CraftingBlockEntity> storage = new ArrayList<>();
    private final List<CraftingMonitorBlockEntity> status = new ArrayList<>();
    private final HashMap<IMEMonitorHandlerReceiver<IAEItemStack>, Object> listeners = new HashMap<>();
    private ICraftingLink myLastLink;
    private Component myName = null;
    private boolean isDestroyed = false;
    /**
     * crafting job info
     */
    private MECraftingInventory inventory = new MECraftingInventory();
    private IAEItemStack finalOutput;
    private boolean waiting = false;
    private IItemList<IAEItemStack> waitingFor = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
            .createList();
    private long availableStorage = 0;
    private MachineSource machineSrc = null;
    private int accelerator = 0;
    private boolean isComplete = true;
    private int remainingOperations;
    private boolean somethingChanged;

    private long lastTime;
    private long elapsedTime;
    private long startItemCount;
    private long remainingItemCount;

    public CraftingCPUCluster(final BlockPos boundsMin, final BlockPos boundsMax) {
        this.boundsMin = boundsMin.immutable();
        this.boundsMax = boundsMax.immutable();
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    public ICraftingLink getLastCraftingLink() {
        return this.myLastLink;
    }

    @Override
    public BlockPos getBoundsMin() {
        return boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return boundsMax;
    }

    /**
     * add a new Listener to the monitor, be sure to properly remove yourself when your done.
     */
    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEItemStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    /**
     * remove a Listener to the monitor.
     */
    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEItemStack> l) {
        this.listeners.remove(l);
    }

    public IMEInventory<IAEItemStack> getInventory() {
        return this.inventory;
    }

    @Override
    public void updateStatus(final boolean updateGrid) {
        for (final CraftingBlockEntity r : this.blockEntities) {
            r.updateSubType(true);
        }
    }

    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        boolean ownsModification = !MBCalculator.isModificationInProgress();
        if (ownsModification) {
            MBCalculator.setModificationInProgress(this);
        }
        try {
            boolean posted = false;

            for (final CraftingBlockEntity r : this.blockEntities) {
                final IGridNode n = r.getActionableNode();
                if (n != null && !posted) {
                    final IGrid g = n.getGrid();
                    if (g != null) {
                        g.postEvent(new GridCraftingCpuChange(n));
                        posted = true;
                    }
                }

                r.updateStatus(null);
            }
        } finally {
            if (ownsModification) {
                MBCalculator.setModificationInProgress(null);
            }
        }
    }

    @Override
    public Iterator<CraftingBlockEntity> getBlockEntities() {
        return this.blockEntities.iterator();
    }

    void addBlockEntity(final CraftingBlockEntity te) {
        if (this.machineSrc == null || te.isCoreBlock()) {
            this.machineSrc = new MachineSource(te);
        }

        te.setCoreBlock(false);
        te.saveChanges();
        this.blockEntities.add(0, te);

        if (te.isStorage()) {
            this.availableStorage += te.getStorageBytes();
            this.storage.add(te);
        } else if (te.isStatus()) {
            this.status.add((CraftingMonitorBlockEntity) te);
        } else if (te.isAccelerator()) {
            this.accelerator++;
        }
    }

    public boolean canAccept(final IAEItemStack input) {
        if (input instanceof IAEItemStack) {
            final IAEItemStack is = this.waitingFor.findPrecise(input);
            if (is != null && is.getStackSize() > 0) {
                return true;
            }
        }
        return false;
    }

    public IAEItemStack injectItems(final IAEItemStack input, final Actionable type, final IActionSource src) {
        // also stop accepting items when the job is complete, i.e. to prevent re-insertion when pushing out
        // items during storeItems
        if (input == null || isComplete) {
            return input;
        }

        final IAEItemStack what = input.copy();
        final IAEItemStack is = this.waitingFor.findPrecise(what);

        if (type == Actionable.SIMULATE)// causes crafting to lock up?
        {
            if (is != null && is.getStackSize() > 0) {
                if (is.getStackSize() >= what.getStackSize()) {
                    if (this.finalOutput.equals(what)) {
                        if (this.myLastLink != null) {
                            return ((CraftingLink) this.myLastLink).injectItems(what.copy(), type);
                        }

                        return what; // ignore it.
                    }

                    return null;
                }

                final IAEItemStack leftOver = what.copy();
                leftOver.decStackSize(is.getStackSize());

                final IAEItemStack used = what.copy();
                used.setStackSize(is.getStackSize());

                if (this.finalOutput.equals(what)) {
                    if (this.myLastLink != null) {
                        leftOver.add(((CraftingLink) this.myLastLink).injectItems(used.copy(), type));
                        return leftOver;
                    }

                    return what; // ignore it.
                }

                return leftOver;
            }
        } else if (type == Actionable.MODULATE && is != null && is.getStackSize() > 0) {
            this.waiting = false;

            this.postChange(what, src);

            if (is.getStackSize() >= what.getStackSize()) {
                is.decStackSize(what.getStackSize());

                this.updateElapsedTime(what);
                this.markDirty();
                this.postCraftingStatusChange(what.copy().setStackSize(-what.getStackSize()));

                if (this.finalOutput.equals(what)) {
                    IAEItemStack leftover = what;

                    this.finalOutput.decStackSize(what.getStackSize());

                    if (this.myLastLink != null) {
                        leftover = ((CraftingLink) this.myLastLink).injectItems(what, type);
                    }

                    if (this.finalOutput.getStackSize() <= 0) {
                        this.completeJob();
                    }

                    this.updateCPU();

                    return leftover; // ignore it.
                }

                // 2000
                return this.inventory.injectItems(what, type, src);
            }

            final IAEItemStack insert = what.copy();
            insert.setStackSize(is.getStackSize());
            what.decStackSize(is.getStackSize());

            is.setStackSize(0);
            this.postCraftingStatusChange(insert.copy().setStackSize(-insert.getStackSize()));

            if (this.finalOutput.equals(insert)) {
                IAEItemStack leftover = input;

                this.finalOutput.decStackSize(insert.getStackSize());

                if (this.myLastLink != null) {
                    what.add(((CraftingLink) this.myLastLink).injectItems(insert.copy(), type));
                    leftover = what;
                }

                if (this.finalOutput.getStackSize() <= 0) {
                    this.completeJob();
                }

                this.updateCPU();
                this.markDirty();

                return leftover; // ignore it.
            }

            this.inventory.injectItems(insert, type, src);
            this.markDirty();

            return what;
        }

        return input;
    }

    private void postChange(final IAEItemStack diff, final IActionSource src) {
        final Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> i = this.getListeners();

        // protect integrity
        if (i.hasNext()) {
            final ImmutableList<IAEItemStack> single = ImmutableList.of(diff.copy());

            while (i.hasNext()) {
                final Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object> o = i.next();
                final IMEMonitorHandlerReceiver<IAEItemStack> receiver = o.getKey();

                if (receiver.isValid(o.getValue())) {
                    receiver.postChange(null, single, src);
                } else {
                    i.remove();
                }
            }
        }
    }

    private void markDirty() {
        this.getCore().saveChanges();
    }

    private void postCraftingStatusChange(final IAEItemStack diff) {
        if (this.getGrid() == null) {
            return;
        }

        var sg = (CraftingService) this.getGrid().getService(ICraftingService.class);

        if (sg.getInterestManager().containsKey(diff)) {
            final Collection<CraftingWatcher> list = sg.getInterestManager().get(diff);

            if (!list.isEmpty()) {
                for (final CraftingWatcher iw : list) {
                    iw.getHost().onRequestChange(sg, diff);
                }
            }
        }
    }

    private void completeJob() {
        if (this.myLastLink != null) {
            ((CraftingLink) this.myLastLink).markDone();
        }

        if (AELog.isCraftingLogEnabled()) {
            final IAEItemStack logStack = this.finalOutput.copy();
            logStack.setStackSize(this.startItemCount);
            AELog.crafting(LOG_MARK_AS_COMPLETE, logStack);
        }

        // Waiting for can potentially contain items at this point, if the user has a 64xplank->64xbutton processing
        // recipe for example, but only requested 1xbutton. We just ignore the rest since it will be dumped
        // back into the network inventory regardless. For this to work it's important that injectItems in this CPU
        // does not accept any further items if isComplete is true.
        this.waitingFor.resetStatus();
        this.remainingItemCount = 0;
        this.startItemCount = 0;
        this.lastTime = 0;
        this.elapsedTime = 0;
        this.isComplete = true;
    }

    private void updateCPU() {
        IAEItemStack send = this.finalOutput;

        if (this.finalOutput != null && this.finalOutput.getStackSize() <= 0) {
            send = null;
        }

        for (final CraftingMonitorBlockEntity t : this.status) {
            t.setJob(send);
        }
    }

    private Iterator<Entry<IMEMonitorHandlerReceiver<IAEItemStack>, Object>> getListeners() {
        return this.listeners.entrySet().iterator();
    }

    private CraftingBlockEntity getCore() {
        if (this.machineSrc == null) {
            return null;
        }
        return (CraftingBlockEntity) this.machineSrc.machine().get();
    }

    private IGrid getGrid() {
        for (final CraftingBlockEntity r : this.blockEntities) {
            final IGridNode gn = r.getActionableNode();
            if (gn != null) {
                return gn.getGrid();
            }
        }

        return null;
    }

    private boolean canCraft(final ICraftingPatternDetails details) {

        if (!details.isCraftable()) {
            // Processing patterns are relatively easy
            for (IAEItemStack input : details.getInputs()) {
                final IAEItemStack ais = this.inventory.extractItems(input.copy(), Actionable.SIMULATE,
                        this.machineSrc);

                if (ais == null || ais.getStackSize() < input.getStackSize()) {
                    return false;
                }
            }
        } else if (details.canSubstitute()) {

            // When substitutions are allowed, we have to keep track of which items we've reserved
            IAEItemStack[] sparseInputs = details.getSparseInputs();
            Map<IAEItemStack, Integer> consumedCount = new HashMap<>();
            for (int i = 0; i < sparseInputs.length; i++) {
                List<IAEItemStack> substitutes = details.getSubstituteInputs(i);
                if (substitutes.isEmpty()) {
                    continue;
                }

                boolean found = false;
                for (IAEItemStack substitute : substitutes) {
                    for (IAEItemStack fuzz : this.inventory.getItemList().findFuzzy(substitute, FuzzyMode.IGNORE_ALL)) {
                        int alreadyConsumed = consumedCount.getOrDefault(fuzz, 0);
                        if (fuzz.getStackSize() - alreadyConsumed <= 0) {
                            continue; // Already fully consumed by a previous slot of this recipe
                        }

                        fuzz = fuzz.copy();
                        fuzz.setStackSize(1); // We're iterating over SPARSE inputs which means there's 1 of each needed
                        final IAEItemStack ais = this.inventory.extractItems(fuzz, Actionable.SIMULATE,
                                this.machineSrc);

                        if (ais != null && ais.getStackSize() > 0) {
                            // Mark 1 of the stack as consumed
                            consumedCount.merge(fuzz, 1, Integer::sum);
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }

        } else {

            // When no substitutions can occur, we can simply check that all items are accounted since
            // each type of item should only occur once
            for (IAEItemStack g : details.getInputs()) {
                boolean found = false;

                for (IAEItemStack fuzz : this.inventory.getItemList().findFuzzy(g, FuzzyMode.IGNORE_ALL)) {
                    fuzz = fuzz.copy();
                    fuzz.setStackSize(g.getStackSize());
                    final IAEItemStack ais = this.inventory.extractItems(fuzz, Actionable.SIMULATE, this.machineSrc);

                    if (ais != null && ais.getStackSize() >= g.getStackSize()) {
                        found = true;
                        break;
                    } else if (ais != null) {
                        g = g.copy();
                        g.decStackSize(ais.getStackSize());
                    }
                }

                if (!found) {
                    return false;
                }
            }

        }

        return true;
    }

    public void cancel() {
        if (this.myLastLink != null) {
            this.myLastLink.cancel();
        }

        final IItemList<IAEItemStack> list;
        this.getListOfItem(list = Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createList(),
                CraftingItemList.ALL);
        for (final IAEItemStack is : list) {
            this.postChange(is, this.machineSrc);
        }

        this.isComplete = true;
        this.myLastLink = null;
        this.tasks.clear();

        final List<IAEItemStack> items = new ArrayList<>(this.waitingFor.size());
        this.waitingFor.forEach(stack -> items.add(stack.copy().setStackSize(-stack.getStackSize())));
        this.waitingFor.resetStatus();

        for (final IAEItemStack is : items) {
            this.postCraftingStatusChange(is);
        }

        this.finalOutput = null;
        this.updateCPU();

        this.storeItems(); // marks dirty
    }

    public void updateCraftingLogic(final IGrid grid, final IEnergyService eg, final CraftingService cc) {
        if (!this.getCore().isActive()) {
            return;
        }

        if (this.myLastLink != null && this.myLastLink.isCanceled()) {
            this.myLastLink = null;
            this.cancel();
        }

        if (this.isComplete) {
            if (this.inventory.getItemList().isEmpty()) {
                return;
            }

            this.storeItems();
            return;
        }

        this.waiting = false;
        if (this.waiting || this.tasks.isEmpty()) // nothing to do here...
        {
            return;
        }

        this.remainingOperations = this.accelerator + 1 - (this.usedOps[0] + this.usedOps[1] + this.usedOps[2]);
        final int started = this.remainingOperations;

        if (this.remainingOperations > 0) {
            do {
                this.somethingChanged = false;
                this.executeCrafting(eg, cc);
            } while (this.somethingChanged && this.remainingOperations > 0);
        }
        this.usedOps[2] = this.usedOps[1];
        this.usedOps[1] = this.usedOps[0];
        this.usedOps[0] = started - this.remainingOperations;

        if (this.remainingOperations > 0 && !this.somethingChanged) {
            this.waiting = true;
        }
    }

    private void executeCrafting(final IEnergyService eg, final CraftingService cc) {
        final Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = this.tasks.entrySet().iterator();

        while (i.hasNext()) {
            final Entry<ICraftingPatternDetails, TaskProgress> e = i.next();

            if (e.getValue().value <= 0) {
                i.remove();
                continue;
            }

            final ICraftingPatternDetails details = e.getKey();

            if (this.canCraft(details)) {
                CraftingContainer ic = null;

                for (final ICraftingMedium m : cc.getMediums(e.getKey())) {
                    if (e.getValue().value <= 0) {
                        continue;
                    }

                    if (!m.isBusy()) {
                        if (ic == null) {
                            final IAEItemStack[] input = details.getSparseInputs();
                            double sum = 0;

                            for (final IAEItemStack anInput : input) {
                                if (anInput != null) {
                                    sum += anInput.getStackSize();
                                }
                            }

                            // power...
                            if (eg.extractAEPower(sum, Actionable.MODULATE, PowerMultiplier.CONFIG) < sum - 0.01) {
                                continue;
                            }

                            ic = new CraftingContainer(new NullMenu(), 3, 3);
                            boolean found = false;

                            for (int x = 0; x < input.length; x++) {
                                if (input[x] != null) {
                                    found = false;

                                    if (details.isCraftable()) {
                                        final Collection<IAEItemStack> itemList;

                                        if (details.canSubstitute()) {
                                            final List<IAEItemStack> substitutes = details.getSubstituteInputs(x);
                                            itemList = new ArrayList<>(substitutes.size());

                                            for (IAEItemStack stack : substitutes) {
                                                itemList.addAll(this.inventory.getItemList().findFuzzy(stack,
                                                        FuzzyMode.IGNORE_ALL));
                                            }
                                        } else {
                                            itemList = new ArrayList<>(1);

                                            final IAEItemStack item = this.inventory.getItemList()
                                                    .findPrecise(input[x]);

                                            if (item != null) {
                                                itemList.add(item);
                                            }
                                        }

                                        for (IAEItemStack fuzz : itemList) {
                                            fuzz = fuzz.copy();
                                            fuzz.setStackSize(input[x].getStackSize());

                                            if (details.isValidItemForSlot(x, fuzz.createItemStack(),
                                                    this.getLevel())) {
                                                final IAEItemStack ais = this.inventory.extractItems(fuzz,
                                                        Actionable.MODULATE, this.machineSrc);
                                                final ItemStack is = ais == null ? ItemStack.EMPTY
                                                        : ais.createItemStack();

                                                if (!is.isEmpty()) {
                                                    this.postChange(AEItemStack.fromItemStack(is), this.machineSrc);
                                                    ic.setItem(x, is);
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        final IAEItemStack ais = this.inventory.extractItems(input[x].copy(),
                                                Actionable.MODULATE, this.machineSrc);
                                        final ItemStack is = ais == null ? ItemStack.EMPTY : ais.createItemStack();

                                        if (!is.isEmpty()) {
                                            this.postChange(input[x], this.machineSrc);
                                            ic.setItem(x, is);
                                            if (is.getCount() == input[x].getStackSize()) {
                                                found = true;
                                                continue;
                                            }
                                        }
                                    }

                                    if (!found) {
                                        break;
                                    }
                                }
                            }

                            if (!found) {
                                // put stuff back..
                                for (int x = 0; x < ic.getContainerSize(); x++) {
                                    final ItemStack is = ic.getItem(x);
                                    if (!is.isEmpty()) {
                                        this.inventory.injectItems(AEItemStack.fromItemStack(is), Actionable.MODULATE,
                                                this.machineSrc);
                                    }
                                }
                                ic = null;
                                break;
                            }
                        }

                        if (m.pushPattern(details, ic)) {
                            this.somethingChanged = true;
                            this.remainingOperations--;

                            for (final IAEItemStack out : details.getOutputs()) {
                                this.postChange(out, this.machineSrc);
                                this.waitingFor.add(out.copy());
                                this.postCraftingStatusChange(out.copy());
                            }

                            if (details.isCraftable()) {
                                CraftingEvent.fireAutoCraftingEvent(getLevel(), details, ic);

                                for (int x = 0; x < ic.getContainerSize(); x++) {
                                    final ItemStack output = Platform.getContainerItem(ic.getItem(x));
                                    if (!output.isEmpty()) {
                                        final IAEItemStack cItem = AEItemStack.fromItemStack(output);
                                        this.postChange(cItem, this.machineSrc);
                                        this.waitingFor.add(cItem);
                                        this.postCraftingStatusChange(cItem);
                                    }
                                }
                            }

                            ic = null; // hand off complete!
                            this.markDirty();

                            e.getValue().value--;
                            if (e.getValue().value <= 0) {
                                continue;
                            }

                            if (this.remainingOperations == 0) {
                                return;
                            }
                        }
                    }
                }

                if (ic != null) {
                    // put stuff back..
                    for (int x = 0; x < ic.getContainerSize(); x++) {
                        final ItemStack is = ic.getItem(x);
                        if (!is.isEmpty()) {
                            this.inventory.injectItems(AEItemStack.fromItemStack(is), Actionable.MODULATE,
                                    this.machineSrc);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tries to dump all locally stored items back into the storage network.
     */
    private void storeItems() {
        Preconditions.checkState(isComplete, "CPU should be complete to prevent re-insertion when dumping items");
        final IGrid g = this.getGrid();

        if (g == null) {
            return;
        }

        final IStorageService sg = g.getService(IStorageService.class);
        final IMEInventory<IAEItemStack> ii = sg
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

        IItemList<IAEItemStack> itemList = this.inventory.getItemList();
        for (IAEItemStack is : itemList) {
            this.postChange(is, this.machineSrc);
            IAEItemStack remainder = ii.injectItems(is.copy(), Actionable.MODULATE, this.machineSrc);

            // The network was unable to receive all of the items, i.e. no or not enough storage space left
            if (remainder != null) {
                is.setStackSize(remainder.getStackSize());
            } else {
                is.reset();
            }
        }

        // If everything got moved back to the network. Reset.
        if (itemList.isEmpty()) {
            this.inventory = new MECraftingInventory();
        }

        this.markDirty();
    }

    public ICraftingLink submitJob(final IGrid g, final ICraftingJob job, final IActionSource src,
            final ICraftingRequester requestingMachine) {
        if (!this.tasks.isEmpty() || !this.waitingFor.isEmpty()) {
            return null;
        }

        if (!(job instanceof CraftingJob)) {
            return null;
        }

        if (this.isBusy() || !this.isActive() || this.availableStorage < job.getByteTotal()) {
            return null;
        }

        final IStorageService sg = g.getService(IStorageService.class);
        final IMEInventory<IAEItemStack> storage = sg
                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final MECraftingInventory ci = new MECraftingInventory(storage, true, false, false);

        try {
            this.waitingFor.resetStatus();
            ((CraftingJob) job).getTree().setJob(ci, this, src);
            if (ci.commit(src)) {
                this.finalOutput = job.getOutput();
                this.waiting = false;
                this.isComplete = false;
                this.markDirty();

                this.updateCPU();
                final String craftID = this.generateCraftingID();

                this.myLastLink = new CraftingLink(this.generateLinkData(craftID, requestingMachine == null, false),
                        this);

                this.prepareElapsedTime();

                if (requestingMachine == null) {
                    return this.myLastLink;
                }

                final ICraftingLink whatLink = new CraftingLink(this.generateLinkData(craftID, false, true),
                        requestingMachine);

                this.submitLink(this.myLastLink);
                this.submitLink(whatLink);

                final IItemList<IAEItemStack> list = Api.instance().storage()
                        .getStorageChannel(IItemStorageChannel.class).createList();
                this.getListOfItem(list, CraftingItemList.ALL);
                for (final IAEItemStack ge : list) {
                    this.postChange(ge, this.machineSrc);
                }

                return whatLink;
            } else {
                this.tasks.clear();
                this.inventory.getItemList().resetStatus();
            }
        } catch (final CraftBranchFailure e) {
            this.tasks.clear();
            this.inventory.getItemList().resetStatus();
            // AELog.error( e );
        }

        return null;
    }

    @Override
    public boolean isBusy() {
        final Iterator<Entry<ICraftingPatternDetails, TaskProgress>> i = this.tasks.entrySet().iterator();

        while (i.hasNext()) {
            if (i.next().getValue().value <= 0) {
                i.remove();
            }
        }

        return !this.tasks.isEmpty() || !this.waitingFor.isEmpty();
    }

    @Override
    public IActionSource getActionSource() {
        return this.machineSrc;
    }

    @Override
    public long getAvailableStorage() {
        return this.availableStorage;
    }

    @Override
    public int getCoProcessors() {
        return this.accelerator;
    }

    @Override
    public Component getName() {
        return this.myName;
    }

    public boolean isActive() {
        final CraftingBlockEntity core = this.getCore();

        if (core == null) {
            return false;
        }

        final IGridNode node = core.getActionableNode();
        if (node == null) {
            return false;
        }

        return node.isActive();
    }

    private String generateCraftingID() {
        final long now = System.currentTimeMillis();
        final int hash = System.identityHashCode(this);
        final int hmm = this.finalOutput == null ? 0 : this.finalOutput.hashCode();

        return Long.toString(now, Character.MAX_RADIX) + '-' + Integer.toString(hash, Character.MAX_RADIX) + '-'
                + Integer.toString(hmm, Character.MAX_RADIX);
    }

    private CompoundTag generateLinkData(final String craftingID, final boolean standalone, final boolean req) {
        final CompoundTag tag = new CompoundTag();

        tag.putString("CraftID", craftingID);
        tag.putBoolean("canceled", false);
        tag.putBoolean("done", false);
        tag.putBoolean("standalone", standalone);
        tag.putBoolean("req", req);

        return tag;
    }

    private void submitLink(final ICraftingLink myLastLink2) {
        if (this.getGrid() != null) {
            var cc = (CraftingService) this.getGrid().getService(ICraftingService.class);
            cc.addLink((CraftingLink) myLastLink2);
        }
    }

    public void getListOfItem(final IItemList<IAEItemStack> list, final CraftingItemList whichList) {
        switch (whichList) {
            case ACTIVE:
                for (final IAEItemStack ais : this.waitingFor) {
                    list.add(ais);
                }
                break;
            case PENDING:
                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (IAEItemStack ais : t.getKey().getOutputs()) {
                        ais = ais.copy();
                        ais.setStackSize(ais.getStackSize() * t.getValue().value);
                        list.add(ais);
                    }
                }
                break;
            case STORAGE:
                this.inventory.getAvailableItems(list);
                break;
            default:
            case ALL:
                this.inventory.getAvailableItems(list);

                for (final IAEItemStack ais : this.waitingFor) {
                    list.add(ais);
                }

                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (IAEItemStack ais : t.getKey().getOutputs()) {
                        ais = ais.copy();
                        ais.setStackSize(ais.getStackSize() * t.getValue().value);
                        list.add(ais);
                    }
                }
                break;
        }
    }

    public void addStorage(final IAEItemStack extractItems) {
        this.inventory.injectItems(extractItems, Actionable.MODULATE, null);
    }

    public void addEmitable(final IAEItemStack i) {
        this.waitingFor.add(i);
        this.postCraftingStatusChange(i);
    }

    public void addCrafting(final ICraftingPatternDetails details, final long crafts) {
        TaskProgress i = this.tasks.get(details);

        if (i == null) {
            this.tasks.put(details, i = new TaskProgress());
        }

        i.value += crafts;
    }

    public IAEItemStack getItemStack(final IAEItemStack what, final CraftingItemList storage2) {
        IAEItemStack is;

        switch (storage2) {
            case STORAGE:
                is = this.inventory.getItemList().findPrecise(what);
                break;
            case ACTIVE:
                is = this.waitingFor.findPrecise(what);
                break;
            case PENDING:

                is = what.copy();
                is.setStackSize(0);

                for (final Entry<ICraftingPatternDetails, TaskProgress> t : this.tasks.entrySet()) {
                    for (final IAEItemStack ais : t.getKey().getOutputs()) {
                        if (ais.equals(is)) {
                            is.setStackSize(is.getStackSize() + ais.getStackSize() * t.getValue().value);
                        }
                    }
                }

                break;
            default:
            case ALL:
                throw new IllegalStateException("Invalid Operation");
        }

        if (is != null) {
            return is.copy();
        }

        is = what.copy();
        is.setStackSize(0);
        return is;
    }

    public void writeToNBT(final CompoundTag data) {
        data.put("finalOutput", this.writeItem(this.finalOutput));
        data.put("inventory", this.writeList(this.inventory.getItemList()));
        data.putBoolean("waiting", this.waiting);
        data.putBoolean("isComplete", this.isComplete);

        if (this.myLastLink != null) {
            final CompoundTag link = new CompoundTag();
            this.myLastLink.writeToNBT(link);
            data.put("link", link);
        }

        final ListTag list = new ListTag();
        for (final Entry<ICraftingPatternDetails, TaskProgress> e : this.tasks.entrySet()) {
            final CompoundTag item = this.writeItem(AEItemStack.fromItemStack(e.getKey().getPattern()));
            item.putLong("craftingProgress", e.getValue().value);
            list.add(item);
        }
        data.put("tasks", list);

        data.put("waitingFor", this.writeList(this.waitingFor));

        data.putLong("elapsedTime", this.getElapsedTime());
        data.putLong("startItemCount", this.getStartItemCount());
        data.putLong("remainingItemCount", this.getRemainingItemCount());
    }

    private CompoundTag writeItem(final IAEItemStack finalOutput2) {
        final CompoundTag out = new CompoundTag();

        if (finalOutput2 != null) {
            finalOutput2.writeToNBT(out);
        }

        return out;
    }

    private ListTag writeList(final IItemList<IAEItemStack> myList) {
        final ListTag out = new ListTag();

        for (final IAEItemStack ais : myList) {
            out.add(this.writeItem(ais));
        }

        return out;
    }

    void done() {
        final CraftingBlockEntity core = this.getCore();

        core.setCoreBlock(true);

        if (core.getPreviousState() != null) {
            this.readFromNBT(core.getPreviousState());
            core.setPreviousState(null);
        }

        this.updateCPU();
        this.updateName();
    }

    public void readFromNBT(final CompoundTag data) {
        this.finalOutput = AEItemStack.fromNBT(data.getCompound("finalOutput"));
        for (final IAEItemStack ais : this.readList(data.getList("inventory", 10))) {
            this.inventory.injectItems(ais, Actionable.MODULATE, this.machineSrc);
        }

        this.waiting = data.getBoolean("waiting");
        this.isComplete = data.getBoolean("isComplete");

        if (data.contains("link")) {
            final CompoundTag link = data.getCompound("link");
            this.myLastLink = new CraftingLink(link, this);
            this.submitLink(this.myLastLink);
        }

        final ListTag list = data.getList("tasks", 10);
        for (int x = 0; x < list.size(); x++) {
            final CompoundTag item = list.getCompound(x);
            final IAEItemStack pattern = AEItemStack.fromNBT(item);
            ICraftingHelper craftingHelper = Api.instance().crafting();
            if (craftingHelper.isEncodedPattern(pattern)) {
                final ICraftingPatternDetails details = craftingHelper.decodePattern(pattern.createItemStack(),
                        this.getLevel());
                if (details != null) {
                    final TaskProgress tp = new TaskProgress();
                    tp.value = item.getLong("craftingProgress");
                    this.tasks.put(details, tp);
                }
            }
        }

        this.waitingFor = this.readList(data.getList("waitingFor", 10));
        for (final IAEItemStack is : this.waitingFor) {
            this.postCraftingStatusChange(is.copy());
        }

        this.lastTime = System.nanoTime();
        this.elapsedTime = data.getLong("elapsedTime");
        this.startItemCount = data.getLong("startItemCount");
        this.remainingItemCount = data.getLong("remainingItemCount");
    }

    public void updateName() {
        this.myName = null;
        for (final CraftingBlockEntity te : this.blockEntities) {

            if (te.hasCustomInventoryName()) {
                if (this.myName != null) {
                    this.myName.copy().append(" ").append(te.getCustomInventoryName());
                } else {
                    this.myName = te.getCustomInventoryName().copy();
                }
            }
        }
    }

    private IItemList<IAEItemStack> readList(final ListTag tag) {
        final IItemList<IAEItemStack> out = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                .createList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.size(); x++) {
            final IAEItemStack ais = AEItemStack.fromNBT(tag.getCompound(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }

    private Level getLevel() {
        return this.getCore().getLevel();
    }

    public IAEItemStack making(final IAEItemStack what) {
        return this.waitingFor.findPrecise(what);
    }

    public void breakCluster() {
        final CraftingBlockEntity t = this.getCore();

        if (t != null) {
            t.breakCluster();
        }
    }

    private void prepareElapsedTime() {
        this.lastTime = System.nanoTime();
        this.elapsedTime = 0;

        final IItemList<IAEItemStack> list = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                .createList();

        this.getListOfItem(list, CraftingItemList.ACTIVE);
        this.getListOfItem(list, CraftingItemList.PENDING);

        int itemCount = 0;
        for (final IAEItemStack ge : list) {
            itemCount += ge.getStackSize();
        }

        this.startItemCount = itemCount;
        this.remainingItemCount = itemCount;
    }

    private void updateElapsedTime(final IAEItemStack is) {
        final long nextStartTime = System.nanoTime();
        this.elapsedTime = this.getElapsedTime() + nextStartTime - this.lastTime;
        this.lastTime = nextStartTime;
        this.remainingItemCount = this.getRemainingItemCount() - is.getStackSize();
    }

    public long getElapsedTime() {
        return this.elapsedTime;
    }

    public long getRemainingItemCount() {
        return this.remainingItemCount;
    }

    public long getStartItemCount() {
        return this.startItemCount;
    }

    private static class TaskProgress {
        private long value;
    }
}
