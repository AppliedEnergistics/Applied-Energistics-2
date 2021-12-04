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

package appeng.menu.me.crafting;

import java.util.concurrent.Future;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.stacks.AEKey;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftConfirmPlanPacket;
import appeng.helpers.WirelessTerminalMenuHost;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.me.common.MEMonitorableMenu;
import appeng.menu.me.items.CraftingTermMenu;
import appeng.menu.me.items.PatternTermMenu;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.ItemTerminalPart;
import appeng.parts.reporting.PatternEncodingTerminalPart;

/**
 * @see appeng.client.gui.me.crafting.CraftConfirmScreen
 */
public class CraftConfirmMenu extends AEBaseMenu {

    private static final String ACTION_BACK = "back";
    private static final String ACTION_CYCLE_CPU = "cycleCpu";
    private static final String ACTION_START_JOB = "startJob";

    public static final MenuType<CraftConfirmMenu> TYPE = MenuTypeBuilder
            .create(CraftConfirmMenu::new, ITerminalHost.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("craftconfirm");

    private final CraftingCPUCycler cpuCycler;

    private ICraftingCPU selectedCpu;

    private AEKey whatToCraft;
    private int amount;
    private Future<ICraftingPlan> job;
    private ICraftingPlan result;

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
    public Component cpuName;

    private CraftingPlanSummary plan;

    public CraftConfirmMenu(int id, Inventory ip, ITerminalHost te) {
        super(TYPE, id, ip, te);
        this.cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        // A player can select no crafting CPU to use a suitable one automatically
        this.cpuCycler.setAllowNoSelection(true);

        registerClientAction(ACTION_BACK, this::goBack);
        registerClientAction(ACTION_CYCLE_CPU, Boolean.class, this::cycleSelectedCPU);
        registerClientAction(ACTION_START_JOB, this::startJob);
    }

    public void cycleSelectedCPU(boolean next) {
        if (isClient()) {
            sendClientAction(ACTION_CYCLE_CPU, next);
        } else {
            this.cpuCycler.cycleCpu(next);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isClient()) {
            return;
        }

        var grid = this.getGrid();

        // Close the screen if the grid no longer exists
        if (grid == null) {
            this.setValidMenu(false);
            return;
        }

        this.cpuCycler.detectAndSendChanges(grid);

        super.broadcastChanges();

        if (this.job != null && this.job.isDone()) {
            try {
                this.result = this.job.get();

                if (!this.result.simulation() && this.isAutoStart()) {
                    this.startJob();
                    return;
                }

                this.plan = CraftingPlanSummary.fromJob(getGrid(), getActionSrc(), this.result);

                sendPacketToClient(new CraftConfirmPlanPacket(plan));
            } catch (final Throwable e) {
                this.getPlayerInventory().player.sendMessage(new TextComponent("Error: " + e),
                        Util.NIL_UUID);
                AELog.debug(e);
                this.setValidMenu(false);
                this.result = null;
            }

            this.setJob(null);
        }
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    private IGrid getGrid() {
        final IActionHost h = (IActionHost) this.getTarget();
        final IGridNode a = h.getActionableNode();
        return a != null ? a.getGrid() : null;
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        if (this.plan == null) {
            return true;
        }
        return c.getAvailableStorage() >= this.plan.getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        if (isClient()) {
            sendClientAction(ACTION_START_JOB);
            return;
        }

        MenuType<?> originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof WirelessTerminalMenuHost) {
            originalGui = MEMonitorableMenu.WIRELESS_TYPE;
        }

        if (ah instanceof ItemTerminalPart) {
            originalGui = MEMonitorableMenu.TYPE;
        }

        if (ah instanceof CraftingTerminalPart) {
            originalGui = CraftingTermMenu.TYPE;
        }

        if (ah instanceof PatternEncodingTerminalPart) {
            originalGui = PatternTermMenu.TYPE;
        }

        if (this.result != null && !this.result.simulation()) {
            final ICraftingService cc = this.getGrid().getCraftingService();
            final ICraftingLink g = cc.submitJob(this.result, null, this.selectedCpu, true, this.getActionSrc());
            this.setAutoStart(false);
            if (g != null && originalGui != null && this.getLocator() != null) {
                MenuOpener.open(originalGui, getPlayerInventory().player, getLocator());
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    @Override
    public void removed(final Player par1PlayerEntity) {
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

    public Level getLevel() {
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

    public Component getName() {
        return this.cpuName;
    }

    public boolean hasNoCPU() {
        return this.noCPU;
    }

    public void setWhatToCraft(AEKey whatToCraft, int amount) {
        this.whatToCraft = whatToCraft;
        this.amount = amount;
    }

    public void setJob(final Future<ICraftingPlan> job) {
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
        Player player = getPlayerInventory().player;
        if (player instanceof ServerPlayer serverPlayer) {
            if (whatToCraft != null) {
                CraftAmountMenu.open(serverPlayer, getLocator(), whatToCraft, amount);
            }
        } else {
            sendClientAction(ACTION_BACK);
        }
    }
}
