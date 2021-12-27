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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.config.CraftingSchedulingMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.events.GridCraftingCpuChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.util.IConfigManager;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.blockentity.crafting.CraftingMonitorBlockEntity;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.helpers.MachineSource;
import appeng.util.ConfigManager;

public final class CraftingCPUCluster implements IAECluster, ICraftingCPU {

    private static final String LOG_MARK_AS_COMPLETE = "Completed job for %s.";

    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    // INSTANCE sate
    private final List<CraftingBlockEntity> blockEntities = new ArrayList<>();
    private final List<CraftingBlockEntity> storage = new ArrayList<>();
    private final List<CraftingMonitorBlockEntity> status = new ArrayList<>();
    private final ConfigManager configManager = new ConfigManager();
    private Component myName = null;
    private boolean isDestroyed = false;
    private long availableStorage = 0;
    private MachineSource machineSrc = null;
    private int accelerator = 0;
    /**
     * crafting job info
     */
    public final CraftingCpuLogic craftingLogic = new CraftingCpuLogic(this);

    public CraftingCPUCluster(BlockPos boundsMin, BlockPos boundsMax) {
        this.boundsMin = boundsMin.immutable();
        this.boundsMax = boundsMax.immutable();

        this.configManager.registerSetting(Settings.CRAFTING_SCHEDULING_MODE, CraftingSchedulingMode.ALL);
    }

    @Override
    public boolean isDestroyed() {
        return this.isDestroyed;
    }

    @Override
    public BlockPos getBoundsMin() {
        return boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return boundsMax;
    }

    @Override
    public void updateStatus(boolean updateGrid) {
        for (CraftingBlockEntity r : this.blockEntities) {
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

            for (CraftingBlockEntity r : this.blockEntities) {
                final IGridNode n = r.getActionableNode();
                if (n != null && !posted) {
                    n.getGrid().postEvent(new GridCraftingCpuChange(n));
                    posted = true;
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

    void addBlockEntity(CraftingBlockEntity te) {
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

    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        return craftingLogic.insert(what, amount, mode);
    }

    public void markDirty() {
        this.getCore().saveChanges();
    }

    public void updateOutput(GenericStack finalOutput) {
        var send = finalOutput;

        if (finalOutput != null && finalOutput.amount() <= 0) {
            send = null;
        }

        for (CraftingMonitorBlockEntity t : this.status) {
            t.setJob(send);
        }
    }

    public IActionSource getSrc() {
        return Objects.requireNonNull(this.machineSrc);
    }

    private CraftingBlockEntity getCore() {
        if (this.machineSrc == null) {
            return null;
        }
        return (CraftingBlockEntity) this.machineSrc.machine().get();
    }

    @Nullable
    public IGrid getGrid() {
        IGridNode node = getNode();
        return node != null ? node.getGrid() : null;
    }

    public void cancel() {
        craftingLogic.cancel();
    }

    public ICraftingLink submitJob(IGrid g, ICraftingPlan plan, IActionSource src,
            ICraftingRequester requestingMachine) {
        return craftingLogic.trySubmitJob(g, plan, src, requestingMachine);
    }

    @Override
    public boolean isBusy() {
        return craftingLogic.hasJob();
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

    @Nullable
    public IGridNode getNode() {
        CraftingBlockEntity core = getCore();
        return core != null ? core.getActionableNode() : null;
    }

    public boolean isActive() {
        IGridNode node = getNode();
        return node != null && node.isActive();
    }

    public void writeToNBT(CompoundTag data) {
        this.craftingLogic.writeToNBT(data);
        this.configManager.writeToNBT(data);
    }

    void done() {
        final CraftingBlockEntity core = this.getCore();

        core.setCoreBlock(true);

        if (core.getPreviousState() != null) {
            this.readFromNBT(core.getPreviousState());
            core.setPreviousState(null);
        }

        this.updateName();
    }

    public void readFromNBT(CompoundTag data) {
        this.craftingLogic.readFromNBT(data);
        this.configManager.readFromNBT(data);
    }

    public void updateName() {
        this.myName = null;
        for (CraftingBlockEntity te : this.blockEntities) {

            if (te.hasCustomInventoryName()) {
                if (this.myName != null) {
                    this.myName.copy().append(" ").append(te.getCustomInventoryName());
                } else {
                    this.myName = te.getCustomInventoryName().copy();
                }
            }
        }
    }

    public Level getLevel() {
        return this.getCore().getLevel();
    }

    public void breakCluster() {
        final CraftingBlockEntity t = this.getCore();

        if (t != null) {
            t.breakCluster();
        }
    }

    public CraftingSchedulingMode getSchedulingMode() {
        return this.configManager.getSetting(Settings.CRAFTING_SCHEDULING_MODE);
    }

    public boolean canAccept(boolean playerQueued) {
        var mode = this.getSchedulingMode();

        if (mode == CraftingSchedulingMode.ALL) {
            return true;
        } else {
            return (mode == CraftingSchedulingMode.PLAYER_ONLY) == playerQueued;
        }
    }

    public IConfigManager getConfigManager() {
        return configManager;
    }
}
