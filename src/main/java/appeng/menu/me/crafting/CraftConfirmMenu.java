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

import java.util.List;
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
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ISubMenuHost;
import appeng.api.storage.ITerminalHost;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftConfirmPlanPacket;
import appeng.me.helpers.PlayerSource;
import appeng.menu.AEBaseMenu;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.guisync.GuiSync;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.locator.MenuLocator;

/**
 * @see appeng.client.gui.me.crafting.CraftConfirmScreen
 */
public class CraftConfirmMenu extends AEBaseMenu implements ISubMenu {

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

    private final ITerminalHost host;

    /**
     * List of stacks to craft, once this request is through. This is currently used when requesting multiple
     * ingredients of a recipe in REI at the same time via ctrl+click.
     * <p>
     * The list is empty if the stacks have been requested via REI and canceling should just return to the screen, null
     * if canceling should return to the craft amount menu.
     */
    @Nullable
    private List<GenericStack> autoCraftingQueue;

    public CraftConfirmMenu(int id, Inventory ip, ITerminalHost te) {
        super(TYPE, id, ip, te);
        this.host = te;
        this.cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        // A player can select no crafting CPU to use a suitable one automatically
        this.cpuCycler.setAllowNoSelection(true);

        registerClientAction(ACTION_BACK, this::goBack);
        registerClientAction(ACTION_CYCLE_CPU, Boolean.class, this::cycleSelectedCPU);
        registerClientAction(ACTION_START_JOB, this::startJob);
    }

    /**
     * Open with a list of items to craft, i.e. via REI ctrl+click.
     */
    public static void openWithCraftingList(@Nullable IActionHost terminal, ServerPlayer player,
            @Nullable MenuLocator locator, List<GenericStack> stacksToCraft) {
        if (terminal == null || locator == null || stacksToCraft.isEmpty()) {
            return;
        }

        var firstToCraft = stacksToCraft.get(0);
        var subsequentCrafts = stacksToCraft.subList(1, stacksToCraft.size());

        final IGridNode gn = terminal.getActionableNode();
        if (gn == null) {
            return;
        }

        final IGrid g = gn.getGrid();
        Future<ICraftingPlan> futureJob = null;
        try {
            var cg = g.getCraftingService();
            var actionSource = IActionSource.ofPlayer(player, terminal);
            // Use CRAFT_LESS to still try to partially craft the ingredients.
            futureJob = cg.beginCraftingCalculation(player.level, () -> actionSource, firstToCraft.what(),
                    firstToCraft.amount(), CalculationStrategy.CRAFT_LESS);

            MenuOpener.open(CraftConfirmMenu.TYPE, player, locator);

            if (player.containerMenu instanceof CraftConfirmMenu ccc) {
                ccc.setJob(futureJob);
                ccc.autoCraftingQueue = subsequentCrafts;
                ccc.broadcastChanges();
            }
        } catch (Throwable e) {
            if (futureJob != null) {
                futureJob.cancel(true);
            }
            AELog.info(e);
        }
    }

    public void cycleSelectedCPU(boolean next) {
        if (isClientSide()) {
            sendClientAction(ACTION_CYCLE_CPU, next);
        } else {
            this.cpuCycler.cycleCpu(next);
        }
    }

    @Override
    public void broadcastChanges() {
        if (isClientSide()) {
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
            } catch (Throwable e) {
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

    private boolean cpuMatches(ICraftingCPU c) {
        if (this.plan == null) {
            return true;
        }
        return c.getAvailableStorage() >= this.plan.getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        if (isClientSide()) {
            sendClientAction(ACTION_START_JOB);
            return;
        }

        if (this.result != null && !this.result.simulation()) {
            final ICraftingService cc = this.getGrid().getCraftingService();
            final ICraftingLink g = cc.submitJob(this.result, null, this.selectedCpu, true, this.getActionSrc());
            this.setAutoStart(false);
            if (g != null) {
                if (autoCraftingQueue != null && !autoCraftingQueue.isEmpty()) {
                    // Process next stack!
                    CraftConfirmMenu.openWithCraftingList(getActionHost(), (ServerPlayer) getPlayer(), getLocator(),
                            autoCraftingQueue);
                } else {
                    this.host.returnToMainMenu(getPlayer(), this);
                }
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInventory().player, (IActionHost) this.getTarget());
    }

    @Override
    public void removed(Player par1PlayerEntity) {
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

    public void setAutoStart(boolean autoStart) {
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

    public void setJob(Future<ICraftingPlan> job) {
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
            if (autoCraftingQueue != null && !autoCraftingQueue.isEmpty()) {
                // Process next stack!
                CraftConfirmMenu.openWithCraftingList(getActionHost(), (ServerPlayer) getPlayer(), getLocator(),
                        autoCraftingQueue);
            } else if (whatToCraft != null) {
                CraftAmountMenu.open(serverPlayer, getLocator(), whatToCraft, amount);
            } else {
                // Go back to host menu
                this.host.returnToMainMenu(getPlayer(), this);
            }
        } else {
            sendClientAction(ACTION_BACK);
        }
    }

    @Override
    public ISubMenuHost getHost() {
        return host;
    }
}
