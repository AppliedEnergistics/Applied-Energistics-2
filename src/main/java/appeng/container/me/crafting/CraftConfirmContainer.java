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

package appeng.container.me.crafting;

import java.util.concurrent.Future;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpener;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.me.items.CraftingTermContainer;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.me.items.PatternTermContainer;
import appeng.container.me.items.WirelessTermContainer;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftConfirmPlanPacket;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.PlayerSource;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.TerminalPart;

/**
 * @see appeng.client.gui.me.crafting.CraftConfirmScreen
 */
public class CraftConfirmContainer extends AEBaseContainer implements CraftingCPUCyclingContainer {

    private static final String ACTION_BACK = "back";

    public static final ContainerType<CraftConfirmContainer> TYPE = ContainerTypeBuilder
            .create(CraftConfirmContainer::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftconfirm");

    private final CraftingCPUCycler cpuCycler;

    private ICraftingCPU selectedCpu;

    private IAEItemStack itemToCreate;
    private Future<ICraftingJob> job;
    private ICraftingJob result;

    @GuiSync(3)
    public boolean autoStart = false;

    // Indicates whether any CPUs are available
    @GuiSync(6)
    public boolean noCPU = true;

    // Properties of the currently selected crafting CPU, this can be null
    // if no CPUs are available, or if an automatic one is selected
    @GuiSync(1)
    public long cpuBytesAvail;
    @GuiSync(2)
    public int cpuCoProcessors;
    @GuiSync(7)
    public ITextComponent cpuName;

    private CraftingPlanSummary plan;

    public CraftConfirmContainer(int id, PlayerInventory ip, ITerminalHost te) {
        super(TYPE, id, ip, te);
        this.cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        // A player can select no crafting CPU to use a suitable one automatically
        this.cpuCycler.setAllowNoSelection(true);

        registerClientAction(ACTION_BACK, this::goBack);
    }

    @Override
    public void cycleSelectedCPU(final boolean next) {
        this.cpuCycler.cycleCpu(next);
    }

    @Override
    public void broadcastChanges() {
        if (isClient()) {
            return;
        }

        this.cpuCycler.detectAndSendChanges(this.getGrid());

        super.broadcastChanges();

        if (this.job != null && this.job.isDone()) {
            try {
                this.result = this.job.get();

                if (!this.result.isSimulation() && this.isAutoStart()) {
                    this.startJob();
                    return;
                }

                this.plan = CraftingPlanSummary.fromJob(getGrid(), getActionSrc(), this.result);

                sendPacketToClient(new CraftConfirmPlanPacket(plan));
            } catch (final Throwable e) {
                this.getPlayerInventory().player.sendMessage(new StringTextComponent("Error: " + e.toString()),
                        Util.NIL_UUID);
                AELog.debug(e);
                this.setValidContainer(false);
                this.result = null;
            }

            this.setJob(null);
        }
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    private IGrid getGrid() {
        final IActionHost h = (IActionHost) this.getTarget();
        return h.getActionableNode().getGrid();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        if (this.plan == null) {
            return true;
        }
        return c.getAvailableStorage() >= this.plan.getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        ContainerType<?> originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof WirelessTerminalGuiObject) {
            originalGui = WirelessTermContainer.TYPE;
        }

        if (ah instanceof TerminalPart) {
            originalGui = ItemTerminalContainer.TYPE;
        }

        if (ah instanceof CraftingTerminalPart) {
            originalGui = CraftingTermContainer.TYPE;
        }

        if (ah instanceof PatternTerminalPart) {
            originalGui = PatternTermContainer.TYPE;
        }

        if (this.result != null && !this.result.isSimulation()) {
            final ICraftingService cc = this.getGrid().getService(ICraftingService.class);
            final ICraftingLink g = cc.submitJob(this.result, null, this.selectedCpu, true, this.getActionSrc());
            this.setAutoStart(false);
            if (g != null && originalGui != null && this.getLocator() != null) {
                ContainerOpener.openContainer(originalGui, getPlayerInventory().player, getLocator());
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    @Override
    public void removeSlotListener(final IContainerListener c) {
        super.removeSlotListener(c);
        if (this.job != null) {
            this.job.cancel(true);
            this.setJob(null);
        }
    }

    @Override
    public void removed(final PlayerEntity par1PlayerEntity) {
        super.removed(par1PlayerEntity);
        if (this.job != null) {
            this.job.cancel(true);
            this.setJob(null);
        }
    }

    private void onCPUSelectionChanged(CraftingCPURecord cpuRecord, boolean cpusAvailable) {
        noCPU = !cpusAvailable;

        if (cpuRecord == null) {
            cpuBytesAvail = 0;
            cpuCoProcessors = 0;
            cpuName = null;
            selectedCpu = null;
        } else {
            cpuBytesAvail = cpuRecord.getSize();
            cpuCoProcessors = cpuRecord.getProcessors();
            cpuName = cpuRecord.getName();
            selectedCpu = cpuRecord.getCpu();
        }
    }

    public World getWorld() {
        return this.getPlayerInventory().player.level;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

    public long getCpuAvailableBytes() {
        return this.cpuBytesAvail;
    }

    public int getCpuCoProcessors() {
        return this.cpuCoProcessors;
    }

    public ITextComponent getName() {
        return this.cpuName;
    }

    public boolean hasNoCPU() {
        return this.noCPU;
    }

    public void setItemToCreate(IAEItemStack itemToCreate) {
        this.itemToCreate = itemToCreate;
    }

    public void setJob(final Future<ICraftingJob> job) {
        this.job = job;
    }

    /**
     * @return The summary of the crafting plan. This is null as long as the plan has not yet finished computing, or it
     *         wasn't synced to the client yet.
     */
    @Nullable
    public CraftingPlanSummary getPlan() {
        return this.plan;
    }

    public void setPlan(CraftingPlanSummary plan) {
        this.plan = plan;
    }

    public void goBack() {
        PlayerEntity player = getPlayerInventory().player;
        if (player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            if (itemToCreate != null) {
                CraftAmountContainer.open(serverPlayer, getLocator(), itemToCreate, (int) itemToCreate.getStackSize());
            }
        } else {
            sendClientAction(ACTION_BACK);
        }
    }
}
