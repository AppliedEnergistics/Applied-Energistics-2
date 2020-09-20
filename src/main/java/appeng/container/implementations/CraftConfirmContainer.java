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

package appeng.container.implementations;

import java.io.IOException;
import java.util.concurrent.Future;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.MEInventoryUpdatePacket;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.PlayerSource;
import appeng.parts.reporting.CraftingTerminalPart;
import appeng.parts.reporting.PatternTerminalPart;
import appeng.parts.reporting.TerminalPart;

public class CraftConfirmContainer extends AEBaseContainer implements CraftingCPUCyclingContainer {

    public static ScreenHandlerType<CraftConfirmContainer> TYPE;

    private static final ContainerHelper<CraftConfirmContainer, ITerminalHost> helper = new ContainerHelper<>(
            CraftConfirmContainer::new, ITerminalHost.class, SecurityPermissions.CRAFT);

    public static CraftConfirmContainer fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    private final CraftingCPUCycler cpuCycler;

    private ICraftingCPU selectedCpu;

    private Future<ICraftingJob> job;
    private ICraftingJob result;
    @GuiSync(0)
    public long bytesUsed;
    @GuiSync(3)
    public boolean autoStart = false;
    @GuiSync(4)
    public boolean simulation = true;

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
    public Text cpuName;

    public CraftConfirmContainer(int id, PlayerInventory ip, ITerminalHost te) {
        super(TYPE, id, ip, te);
        this.cpuCycler = new CraftingCPUCycler(this::cpuMatches, this::onCPUSelectionChanged);
        // A player can select no crafting CPU to use a suitable one automatically
        this.cpuCycler.setAllowNoSelection(true);
    }

    @Override
    public void cycleSelectedCPU(final boolean next) {
        this.cpuCycler.cycleCpu(next);
    }

    @Override
    public void sendContentUpdates() {
        if (isClient()) {
            return;
        }

        this.cpuCycler.detectAndSendChanges(this.getGrid());

        super.sendContentUpdates();

        if (this.getJob() != null && this.getJob().isDone()) {
            try {
                this.result = this.getJob().get();

                if (!this.result.isSimulation()) {
                    this.setSimulation(false);
                    if (this.isAutoStart()) {
                        this.startJob();
                        return;
                    }
                } else {
                    this.setSimulation(true);
                }

                try {
                    final MEInventoryUpdatePacket a = new MEInventoryUpdatePacket((byte) 0);
                    final MEInventoryUpdatePacket b = new MEInventoryUpdatePacket((byte) 1);
                    final MEInventoryUpdatePacket c = this.result.isSimulation() ? new MEInventoryUpdatePacket((byte) 2)
                            : null;

                    final IItemList<IAEItemStack> plan = Api.instance().storage()
                            .getStorageChannel(IItemStorageChannel.class).createList();
                    this.result.populatePlan(plan);

                    this.setUsedBytes(this.result.getByteTotal());

                    for (final IAEItemStack out : plan) {

                        IAEItemStack o = out.copy();
                        o.reset();
                        o.setStackSize(out.getStackSize());

                        final IAEItemStack p = out.copy();
                        p.reset();
                        p.setStackSize(out.getCountRequestable());

                        final IStorageGrid sg = this.getGrid().getCache(IStorageGrid.class);
                        final IMEInventory<IAEItemStack> items = sg
                                .getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));

                        IAEItemStack m = null;
                        if (c != null && this.result.isSimulation()) {
                            m = o.copy();
                            o = items.extractItems(o, Actionable.SIMULATE, this.getActionSource());

                            if (o == null) {
                                o = m.copy();
                                o.setStackSize(0);
                            }

                            m.setStackSize(m.getStackSize() - o.getStackSize());
                        }

                        if (o.getStackSize() > 0) {
                            a.appendItem(o);
                        }

                        if (p.getStackSize() > 0) {
                            b.appendItem(p);
                        }

                        if (c != null && m != null && m.getStackSize() > 0) {
                            c.appendItem(m);
                        }
                    }

                    for (final Object g : this.getListeners()) {
                        if (g instanceof PlayerEntity) {
                            NetworkHandler.instance().sendTo(a, (ServerPlayerEntity) g);
                            NetworkHandler.instance().sendTo(b, (ServerPlayerEntity) g);
                            if (c != null) {
                                NetworkHandler.instance().sendTo(c, (ServerPlayerEntity) g);
                            }
                        }
                    }
                } catch (final IOException e) {
                    // :P
                }
            } catch (final Throwable e) {
                this.getPlayerInv().player.sendSystemMessage(new LiteralText("Error: " + e.toString()), Util.NIL_UUID);
                AELog.debug(e);
                this.setValidContainer(false);
                this.result = null;
            }

            this.setJob(null);
        }
        this.verifyPermissions(SecurityPermissions.CRAFT, false);
    }

    private IGrid getGrid() {
        final IActionHost h = ((IActionHost) this.getTarget());
        return h.getActionableNode().getGrid();
    }

    private boolean cpuMatches(final ICraftingCPU c) {
        return c.getAvailableStorage() >= this.getUsedBytes() && !c.isBusy();
    }

    public void startJob() {
        ScreenHandlerType<?> originalGui = null;

        final IActionHost ah = this.getActionHost();
        if (ah instanceof WirelessTerminalGuiObject) {
            originalGui = WirelessTermContainer.TYPE;
        }

        if (ah instanceof TerminalPart) {
            originalGui = MEMonitorableContainer.TYPE;
        }

        if (ah instanceof CraftingTerminalPart) {
            originalGui = CraftingTermContainer.TYPE;
        }

        if (ah instanceof PatternTerminalPart) {
            originalGui = PatternTermContainer.TYPE;
        }

        if (this.result != null && !this.isSimulation()) {
            final ICraftingGrid cc = this.getGrid().getCache(ICraftingGrid.class);
            final ICraftingLink g = cc.submitJob(this.result, null, this.selectedCpu, true, this.getActionSrc());
            this.setAutoStart(false);
            if (g != null && originalGui != null && this.getLocator() != null) {
                ContainerOpener.openContainer(originalGui, getPlayerInventory().player, getLocator());
            }
        }
    }

    private IActionSource getActionSrc() {
        return new PlayerSource(this.getPlayerInv().player, (IActionHost) this.getTarget());
    }

    @Override
    public void removeListener(final ScreenHandlerListener c) {
        super.removeListener(c);
        if (this.getJob() != null) {
            this.getJob().cancel(true);
            this.setJob(null);
        }
    }

    @Override
    public void close(final PlayerEntity par1PlayerEntity) {
        super.close(par1PlayerEntity);
        if (this.getJob() != null) {
            this.getJob().cancel(true);
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
        return this.getPlayerInv().player.world;
    }

    public boolean isAutoStart() {
        return this.autoStart;
    }

    public void setAutoStart(final boolean autoStart) {
        this.autoStart = autoStart;
    }

    public long getUsedBytes() {
        return this.bytesUsed;
    }

    private void setUsedBytes(final long bytesUsed) {
        this.bytesUsed = bytesUsed;
    }

    public long getCpuAvailableBytes() {
        return this.cpuBytesAvail;
    }

    public int getCpuCoProcessors() {
        return this.cpuCoProcessors;
    }

    public Text getName() {
        return this.cpuName;
    }

    public boolean hasNoCPU() {
        return this.noCPU;
    }

    public boolean isSimulation() {
        return this.simulation;
    }

    private void setSimulation(final boolean simulation) {
        this.simulation = simulation;
    }

    private Future<ICraftingJob> getJob() {
        return this.job;
    }

    public void setJob(final Future<ICraftingJob> job) {
        this.job = job;
    }
}
